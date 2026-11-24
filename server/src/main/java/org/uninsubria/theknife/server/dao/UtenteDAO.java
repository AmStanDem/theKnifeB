/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.dao;

import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;
import org.uninsubria.theknife.server.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;

/**
 * DAO per la lettura, la registrazione e l'aggiornamento degli account utente.
 */
public class UtenteDAO {

    /**
     * Dati utente associati all'hash della password memorizzato.
     *
     * @param utente dati pubblici dell'utente
     * @param passwordHash hash della password
     */
    public record UtenteConHash(UtenteDTO utente, String passwordHash) {}

    /**
     * Recupera la data corrente secondo il database.
     *
     * @return data corrente del database
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public LocalDate dataCorrente() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT CURRENT_DATE");
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Impossibile recuperare la data corrente dal database.");
            }
            return rs.getDate(1).toLocalDate();
        }
    }

    /**
     * Inserisce un utente e il relativo hash della password.
     *
     * @param utente dati dell'utente da inserire
     * @param passwordHash hash della password
     * @return {@code true} se l'utente è stato inserito, {@code false} in caso di email duplicata
     * @throws SQLException se si verifica un errore diverso da un vincolo univoco duplicato
     */
    public boolean inserisciUtente(UtenteDTO utente, String passwordHash) throws SQLException {
        String sql = "INSERT INTO Utenti (email, password, nome, cognome, data_nascita, domicilio, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.email());
            stmt.setString(2, passwordHash);
            stmt.setString(3, utente.nome());
            stmt.setString(4, utente.cognome());

            if (utente.dataNascita() != null) {
                stmt.setDate(5, Date.valueOf(utente.dataNascita()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, utente.domicilio());
            stmt.setString(7, utente.ruolo().name().toLowerCase());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Cerca un utente tramite indirizzo email.
     *
     * @param email indirizzo email da cercare
     * @return dati dell'utente e hash della password, oppure {@code null} se non trovato
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public UtenteConHash trovaPerEmail(String email) throws SQLException {
        String sql = "SELECT id_utente, email, password, nome, cognome, data_nascita, domicilio, ruolo FROM Utenti WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dateNascita = rs.getDate("data_nascita");
                    LocalDate localDateNascita = dateNascita != null ? dateNascita.toLocalDate() : null;

                    UtenteDTO dto = new UtenteDTO(
                            rs.getInt("id_utente"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("email"),
                            localDateNascita,
                            rs.getString("domicilio"),
                            RuoloUtente.valueOf(rs.getString("ruolo").toUpperCase())
                    );

                    // ATTENZIONE: La colonna nel DB si chiama 'password', non 'password_hash'
                    return new UtenteConHash(dto, rs.getString("password"));
                }
            }
        }
        return null;
    }

    /**
     * Cerca un utente tramite identificativo.
     *
     * @param idUtente identificativo dell'utente
     * @return dati dell'utente e hash della password, oppure {@code null} se non trovato
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public UtenteConHash trovaPerId(Integer idUtente) throws SQLException {
        String sql = "SELECT id_utente, email, password, nome, cognome, data_nascita, domicilio, ruolo "
                + "FROM Utenti WHERE id_utente = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Date dataNascita = rs.getDate("data_nascita");
                UtenteDTO dto = new UtenteDTO(
                        rs.getInt("id_utente"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("email"),
                        dataNascita == null ? null : dataNascita.toLocalDate(),
                        rs.getString("domicilio"),
                        RuoloUtente.valueOf(rs.getString("ruolo").toUpperCase())
                );
                return new UtenteConHash(dto, rs.getString("password"));
            }
        }
    }

    /**
     * Aggiorna i dati dell'utente e il suo hash password solo se l'hash attuale
     * corrisponde a quello fornito.
     *
     * @param utente dati aggiornati dell'utente
     * @param hashPrecedente hash password atteso, usato per verificare la versione corrente
     * @param hashNuovo nuovo hash password
     * @return {@code true} se è stato aggiornato un utente, {@code false} se non è stato aggiornato
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public boolean aggiornaUtente(UtenteDTO utente, String hashPrecedente, String hashNuovo)
            throws SQLException {
        String sql = "UPDATE Utenti SET email = ?, password = ?, nome = ?, cognome = ?, "
                + "data_nascita = ?, domicilio = ? WHERE id_utente = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utente.email());
            stmt.setString(2, hashNuovo);
            stmt.setString(3, utente.nome());
            stmt.setString(4, utente.cognome());
            if (utente.dataNascita() == null) {
                stmt.setNull(5, Types.DATE);
            } else {
                stmt.setDate(5, Date.valueOf(utente.dataNascita()));
            }
            stmt.setString(6, utente.domicilio());
            stmt.setInt(7, utente.idUtente());
            stmt.setString(8, hashPrecedente);
            return stmt.executeUpdate() == 1;
        }
    }
}