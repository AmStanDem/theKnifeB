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

public class UtenteDAO {

    public record UtenteConHash(UtenteDTO utente, String passwordHash) {}

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
}