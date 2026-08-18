package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    public boolean inserisciRistorante(RistoranteDTO r, int idGestore) {
        String sql = "INSERT INTO Ristoranti (id_gestore, nome, indirizzo, nazione, latitudine, longitudine, tipo_cucina, prezzo_medio, delivery, booking_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);
            stmt.setString(2, r.nome());
            stmt.setString(3, r.indirizzo());
            stmt.setString(4, r.nazione());
            stmt.setDouble(5, r.latitudine() != null ? r.latitudine() : 0.0);
            stmt.setDouble(6, r.longitudine() != null ? r.longitudine() : 0.0);
            stmt.setString(7, r.tipoCucina());
            stmt.setDouble(8, r.prezzoMedio() != null ? r.prezzoMedio() : 0.0);
            stmt.setBoolean(9, r.delivery() != null ? r.delivery() : false);
            stmt.setBoolean(10, r.bookingOnline() != null ? r.bookingOnline() : false);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento ristorante: " + e.getMessage());
            return false;
        }
    }

    public RistoranteDTO trovaPerId(int idRistorante) {
        String sql = "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle " +
                "FROM Ristoranti r " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                "WHERE r.id_ristorante = ? " +
                "GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRistorante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mappaResultSetRistorante(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore ricerca ristorante per ID: " + e.getMessage());
        }
        return null;
    }

    public List<RistoranteDTO> trovaPerGestore(int idGestore) {
        List<RistoranteDTO> lista = new ArrayList<>();
        String sql = "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle " +
                "FROM Ristoranti r " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                "WHERE r.id_gestore = ? " +
                "GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mappaResultSetRistorante(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore ricerca ristoranti del gestore: " + e.getMessage());
        }
        return lista;
    }

    public List<RistoranteDTO> cercaConFiltri(FiltriRicercaDTO filtri) {
        List<RistoranteDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle " +
                        "FROM Ristoranti r " +
                        "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                        "WHERE 1=1"
        );
        List<Object> parametri = new ArrayList<>();

        if (filtri != null) {
            // 1. Locazione (Obbligatoria nel record, mappata su nazione o indirizzo)
            if (filtri.locazione() != null && !filtri.locazione().trim().isEmpty()) {
                sql.append(" AND (LOWER(r.nazione) LIKE LOWER(?) OR LOWER(r.indirizzo) LIKE LOWER(?))");
                String locPattern = "%" + filtri.locazione().trim() + "%";
                parametri.add(locPattern);
                parametri.add(locPattern);
            }

            // 2. Tipo Cucina
            if (filtri.tipoCucina() != null && !filtri.tipoCucina().trim().isEmpty()) {
                sql.append(" AND LOWER(r.tipo_cucina) = LOWER(?)");
                parametri.add(filtri.tipoCucina().trim());
            }

            // 3. Prezzo Massimo
            if (filtri.prezzoMax() != null) {
                sql.append(" AND r.prezzo_medio <= ?");
                parametri.add(filtri.prezzoMax());
            }

            // 4. Delivery
            if (filtri.delivery() != null && filtri.delivery()) {
                sql.append(" AND r.delivery = TRUE");
            }

            // 5. Booking Online
            if (filtri.bookingOnline() != null && filtri.bookingOnline()) {
                sql.append(" AND r.booking_online = TRUE");
            }
        }

        // Raggruppamento obbligatorio per la funzione di aggregazione AVG
        sql.append(" GROUP BY r.id_ristorante");

        // 6. Media Stelle Minima (Filtro post-aggregazione tramite HAVING)
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
                while (rs.next()) {
                    lista.add(mappaResultSetRistorante(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante la ricerca filtrata ristoranti: " + e.getMessage());
        }
        return lista;
    }

    private RistoranteDTO mappaResultSetRistorante(ResultSet rs) throws SQLException {
        return new RistoranteDTO(
                rs.getInt("id_ristorante"),
                rs.getString("nome"),
                rs.getString("indirizzo"),
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