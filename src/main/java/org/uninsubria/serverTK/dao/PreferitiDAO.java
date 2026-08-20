package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferitiDAO {

    public boolean aggiungiPreferito(int idUtente, int idRistorante) {
        String sql = "INSERT INTO Preferiti (id_utente, id_ristorante) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore aggiunta preferito: " + e.getMessage());
            return false;
        }
    }

    public boolean rimuoviPreferito(int idUtente, int idRistorante) {
        String sql = "DELETE FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore rimozione preferito: " + e.getMessage());
            return false;
        }
    }

    public List<RistoranteDTO> ottieniPreferitiUtente(int idUtente) {
        List<RistoranteDTO> lista = new ArrayList<>();

        String sql = "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle, STRING_AGG(DISTINCT tc.nome, ',') as tipi_cucina " +
                "FROM RistorantiTheKnife r " +
                "JOIN Preferiti p ON r.id_ristorante = p.id_ristorante " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                "LEFT JOIN Ristoranti_Tipologie rt ON r.id_ristorante = rt.id_ristorante " +
                "LEFT JOIN Tipologia_Cucina tc ON rt.id_tipologia = tc.id_tipologia " +
                "WHERE p.id_utente = ? " +
                "GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipiCucinaRaw = rs.getString("tipi_cucina");
                    List<String> tipologie = tipiCucinaRaw != null && !tipiCucinaRaw.isEmpty()
                            ? Arrays.asList(tipiCucinaRaw.split(",")) : new ArrayList<>();

                    lista.add(new RistoranteDTO(
                            rs.getInt("id_ristorante"),
                            rs.getString("nome"),
                            rs.getString("indirizzo"),
                            rs.getString("citta"),
                            rs.getString("nazione"),
                            rs.getDouble("latitudine"),
                            rs.getDouble("longitudine"),
                            tipologie,
                            rs.getDouble("prezzo_medio"),
                            rs.getBoolean("delivery"),
                            rs.getBoolean("booking_online"),
                            rs.getDouble("media_stelle")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero lista preferiti: " + e.getMessage());
        }
        return lista;
    }
}