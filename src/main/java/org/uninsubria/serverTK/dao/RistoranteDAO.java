package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    public boolean inserisciRistorante(RistoranteDTO r, int idGestore) {
        String sql = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, tipo_cucina, prezzo_medio, delivery, booking_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);
            stmt.setString(2, r.nome());
            stmt.setString(3, r.indirizzo());
            stmt.setString(4, r.citta());
            stmt.setString(5, r.nazione());
            stmt.setDouble(6, r.latitudine() != null ? r.latitudine() : 0.0);
            stmt.setDouble(7, r.longitudine() != null ? r.longitudine() : 0.0);
            stmt.setString(8, r.tipoCucina());
            stmt.setDouble(9, r.prezzoMedio() != null ? r.prezzoMedio() : 0.0);
            stmt.setBoolean(10, r.delivery() != null ? r.delivery() : false);
            stmt.setBoolean(11, r.bookingOnline() != null && r.bookingOnline());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento ristorante: " + e.getMessage());
            return false;
        }
    }

    public RistoranteDTO trovaPerId(int idRistorante) {
        String sql = costruisciQueryBase() + " WHERE r.id_ristorante = ? GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRistorante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mappaResultSetRistorante(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore ricerca ristorante per ID: " + e.getMessage());
        }
        return null;
    }

    public List<RistoranteDTO> trovaPerGestore(int idGestore) {
        List<RistoranteDTO> lista = new ArrayList<>();
        String sql = costruisciQueryBase() + " WHERE r.id_gestore = ? GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mappaResultSetRistorante(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore ricerca ristoranti del gestore: " + e.getMessage());
        }
        return lista;
    }

    public List<RistoranteDTO> cercaConFiltri(FiltriRicercaDTO filtri) {
        List<RistoranteDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(costruisciQueryBase() + " WHERE 1=1");
        List<Object> parametri = new ArrayList<>();

        if (filtri != null) {
            if (filtri.locazione() != null && !filtri.locazione().trim().isEmpty()) {
                sql.append(" AND (LOWER(r.nazione) LIKE LOWER(?) OR LOWER(r.citta) LIKE LOWER(?))");
                String locPattern = "%" + filtri.locazione().trim() + "%";
                parametri.add(locPattern);
                parametri.add(locPattern);
            }
            if (filtri.tipoCucina() != null && !filtri.tipoCucina().trim().isEmpty()) {
                sql.append(" AND LOWER(r.tipo_cucina) LIKE LOWER(?)");
                parametri.add("%" + filtri.tipoCucina().trim() + "%");
            }
            if (filtri.prezzoMin() != null) {
                sql.append(" AND r.prezzo_medio >= ?");
                parametri.add(filtri.prezzoMin());
            }
            if (filtri.prezzoMax() != null) {
                sql.append(" AND r.prezzo_medio <= ?");
                parametri.add(filtri.prezzoMax());
            }
            if (filtri.delivery() != null && filtri.delivery()) {
                sql.append(" AND r.delivery = TRUE");
            }
            if (filtri.bookingOnline() != null && filtri.bookingOnline()) {
                sql.append(" AND r.booking_online = TRUE");
            }
        }

        sql.append(" GROUP BY r.id_ristorante");

        if (filtri != null && filtri.mediaStelleMinima() != null) {
            sql.append(" HAVING COALESCE(AVG(rec.valutazione), 0.0) >= ?");
            parametri.add(filtri.mediaStelleMinima().doubleValue());
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametri.size(); i++) {
                stmt.setObject(i + 1, parametri.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mappaResultSetRistorante(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante la ricerca filtrata ristoranti: " + e.getMessage());
        }
        return lista;
    }

    private String costruisciQueryBase() {
        return "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante";
    }

    private RistoranteDTO mappaResultSetRistorante(ResultSet rs) throws SQLException {
        return new RistoranteDTO(
                rs.getInt("id_ristorante"),
                rs.getString("nome"),
                rs.getString("indirizzo"),
                rs.getString("citta"),
                rs.getString("nazione"),
                rs.getDouble("latitudine"),
                rs.getDouble("longitudine"),
                rs.getString("tipo_cucina"),
                rs.getDouble("prezzo_medio"),
                rs.getBoolean("delivery"),
                rs.getBoolean("booking_online"),
                rs.getDouble("media_stelle")
        );
    }
}