package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecensioneDAO {

    public boolean utenteCliente(int idUtente) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM Utenti WHERE id_utente = ? AND ruolo = 'cliente')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

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
        String sql = "SELECT rec.*, u.nome as nome_autore, resp.testo as risposta_gestore, r.nome AS nome_ristorante " +
                "FROM Recensioni rec " +
                "JOIN Utenti u ON rec.id_utente = u.id_utente " +
                "JOIN RistorantiTheKnife r ON rec.id_ristorante = r.id_ristorante " +
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
                            rs.getString("risposta_gestore"),
                            rs.getInt("id_ristorante"),
                            rs.getString("nome_ristorante")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero recensioni: " + e.getMessage());
        }
        return lista;
    }

    public List<RecensioneDTO> trovaPerUtente(int idUtente) throws SQLException {
        List<RecensioneDTO> lista = new ArrayList<>();
        String sql = "SELECT rec.*, u.nome AS nome_autore, r.nome AS nome_ristorante, " +
                "resp.testo AS risposta_gestore " +
                "FROM Recensioni rec " +
                "JOIN Utenti u ON rec.id_utente = u.id_utente " +
                "JOIN RistorantiTheKnife r ON rec.id_ristorante = r.id_ristorante " +
                "LEFT JOIN Risposte_Recensioni resp ON rec.id_recensione = resp.id_recensione " +
                "WHERE rec.id_utente = ? AND u.ruolo = 'cliente' " +
                "ORDER BY rec.data_creazione DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
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
                            rs.getString("risposta_gestore"),
                            rs.getInt("id_ristorante"),
                            rs.getString("nome_ristorante")
                    ));
                }
            }
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

    public boolean aggiungiRispostaGestore(int idRecensione, int idGestore, String testoRisposta)
            throws SQLException {
        String sql = "INSERT INTO Risposte_Recensioni (id_recensione, id_gestore, testo) " +
                "SELECT rec.id_recensione, r.id_gestore, ? " +
                "FROM Recensioni rec " +
                "JOIN RistorantiTheKnife r ON r.id_ristorante = rec.id_ristorante " +
                "JOIN Utenti u ON u.id_utente = r.id_gestore " +
                "WHERE rec.id_recensione = ? AND r.id_gestore = ? AND u.ruolo = 'gestore' " +
                "AND NOT EXISTS (SELECT 1 FROM Risposte_Recensioni resp " +
                "WHERE resp.id_recensione = rec.id_recensione)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, testoRisposta);
            stmt.setInt(2, idRecensione);
            stmt.setInt(3, idGestore);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw e;
        }
    }
}