package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    /**
     * Struttura dati interna utilizzata esclusivamente dal server per trasportare
     * il DTO pulito insieme all'hash BCrypt, isolando i dati sensibili.
     */
    public record UtenteConHash(UtenteDTO utente, String passwordHash) {}

    /**
     * Inserisce un nuovo utente nel database.
     *
     * @param utente       Il DTO contenente le informazioni anagrafiche e il ruolo.
     * @param passwordHash L'hash crittografico (BCrypt) della password.
     * @return true se l'inserimento ha avuto successo, false altrimenti.
     */
    public boolean inserisciUtente(UtenteDTO utente, String passwordHash) {
        // Il cast ?::ruolo_utente mappa correttamente la stringa Java nel tipo ENUM nativo di PostgreSQL
        String sql = "INSERT INTO Utenti (email, password_hash, nome, cognome, ruolo) VALUES (?, ?, ?, ?, ?::ruolo_utente)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Nei record Java si usa la sintassi dei metodi di accesso (es. utente.email())
            stmt.setString(1, utente.email());
            stmt.setString(2, passwordHash);
            stmt.setString(3, utente.nome());
            stmt.setString(4, utente.cognome());
            // Converte l'enum Java (CLIENTE / GESTORE) in minuscolo per rispettare l'ENUM del database
            stmt.setString(5, utente.ruolo().name().toLowerCase());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante l'inserimento dell'utente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cerca un utente all'interno del database tramite il suo indirizzo email.
     *
     * @ l'email dell'utente da cercare.
     * @return Un oggetto {@link UtenteConHash} contenente il DTO e l'hash della password, oppure null se non trovato.
     */
    public UtenteConHash trovaPerEmail(String email) {
        String sql = "SELECT id_utente, email, password_hash, nome, cognome, ruolo FROM Utenti WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Ricostruisce il DTO immutabile leggendo i campi relazionali
                    UtenteDTO dto = new UtenteDTO(
                            rs.getInt("id_utente"),
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("email"),
                            RuoloUtente.valueOf(rs.getString("ruolo").toUpperCase())
                    );

                    // Restituisce il pacchetto protetto con l'hash associato
                    return new UtenteConHash(dto, rs.getString("password_hash"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante la ricerca dell'utente per email: " + e.getMessage());
        }

        return null; // Utente non trovato
    }
}