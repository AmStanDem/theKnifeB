package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecensioneDAO {

    public boolean inserisciRecensione(int idCliente, int idRistorante, RecensioneDTO r) {
        String sql = "INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idRistorante);
            stmt.setInt(3, r.valutazione());
            stmt.setString(4, r.testo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento recensione: " + e.getMessage());
            return false;
        }
    }

    public List<RecensioneDTO> trovaPerRistorante(int idRistorante) {
        List<RecensioneDTO> lista = new ArrayList<>();
        String sql = "SELECT rec.*, u.nome as nome_autore, resp.testo as risposta_gestore " +
                "FROM Recensioni rec " +
                "JOIN Utenti u ON rec.id_utente = u.id_utente " +
                "LEFT JOIN Risposte_Recensioni resp ON rec.id_recensione = resp.id_recensione " +
                "WHERE rec.id_ristorante = ? " +
                "ORDER BY rec.data_creazione DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRistorante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("data_creazione");
                    LocalDateTime dataCreazione = timestamp != null ? timestamp.toLocalDateTime() : null;

                    lista.add(new RecensioneDTO(
                            rs.getInt("id_recensione"),
                            rs.getInt("valutazione"),
                            rs.getString("testo"),
                            rs.getString("nome_autore"),
                            dataCreazione,
                            rs.getString("risposta_gestore")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero recensioni: " + e.getMessage());
        }
        return lista;
    }

    public boolean modificaRecensione(int idRecensione, int idCliente, String nuovoTesto, int nuoveStelle) {
        String sql = "UPDATE Recensioni SET valutazione = ?, testo = ?, data_modifica = CURRENT_TIMESTAMP " +
                "WHERE id_recensione = ? AND id_utente = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuoveStelle);
            stmt.setString(2, nuovoTesto);
            stmt.setInt(3, idRecensione);
            stmt.setInt(4, idCliente);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore modifica recensione: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminaRecensione(int idRecensione, int idCliente) {
        String sql = "DELETE FROM Recensioni WHERE id_recensione = ? AND id_utente = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecensione);
            stmt.setInt(2, idCliente);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore eliminazione recensione: " + e.getMessage());
            return false;
        }
    }

    public boolean aggiungiRispostaGestore(int idRecensione, int idGestore, String testoRisposta) {
        String sql = "INSERT INTO Risposte_Recensioni (id_recensione, id_gestore, testo) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecensione);
            stmt.setInt(2, idGestore); // Inserimento tracciato con l'ID del gestore per auditing
            stmt.setString(3, testoRisposta);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento risposta recensione: " + e.getMessage());
            return false;
        }
    }
}