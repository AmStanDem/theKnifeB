package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RistoranteDAO {

    public boolean inserisciRistorante(RistoranteDTO r, int idGestore) {
        String insertRistorante = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertRistorante, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, idGestore);
                stmt.setString(2, r.nome());
                stmt.setString(3, r.indirizzo());
                stmt.setString(4, r.citta());
                stmt.setString(5, r.nazione());
                stmt.setDouble(6, r.latitudine() != null ? r.latitudine() : 0.0);
                stmt.setDouble(7, r.longitudine() != null ? r.longitudine() : 0.0);
                stmt.setDouble(8, r.prezzoMedio() != null ? r.prezzoMedio() : 0.0);
                stmt.setBoolean(9, r.delivery() != null ? r.delivery() : false);
                stmt.setBoolean(10, r.bookingOnline() != null && r.bookingOnline());

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                int idRistoranteGenerato;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idRistoranteGenerato = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

                if (r.tipologieCucina() != null && !r.tipologieCucina().isEmpty()) {
                    for (String tipologia : r.tipologieCucina()) {
                        associaTipologiaCucina(conn, idRistoranteGenerato, tipologia.trim());
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento ristorante: " + e.getMessage());
            return false;
        }
    }

    private void associaTipologiaCucina(Connection conn, int idRistorante, String nomeCucina) throws SQLException {
        if (nomeCucina.isEmpty()) return;
        String upsertCucina = "INSERT INTO Tipologia_Cucina (nome) VALUES (?) ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome RETURNING id_tipologia";
        int idTipologia = -1;
        try (PreparedStatement stmtCucina = conn.prepareStatement(upsertCucina)) {
            stmtCucina.setString(1, nomeCucina);
            try (ResultSet rs = stmtCucina.executeQuery()) {
                if (rs.next()) idTipologia = rs.getInt(1);
            }
        }
        if (idTipologia != -1) {
            String insertPonte = "INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmtPonte = conn.prepareStatement(insertPonte)) {
                stmtPonte.setInt(1, idRistorante);
                stmtPonte.setInt(2, idTipologia);
                stmtPonte.executeUpdate();
            }
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

    public List<RistoranteDTO> cercaConFiltri(FiltriRicercaDTO filtri) {
        List<RistoranteDTO> lista = new ArrayList<>();
        boolean usaCoordinate = (filtri != null && filtri.latitudineRiferimento() != null && filtri.longitudineRiferimento() != null);

        StringBuilder sql = new StringBuilder(
                "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle, STRING_AGG(DISTINCT tc.nome, ',') as tipi_cucina"
        );

        if (usaCoordinate) {
            sql.append(", (6371 * acos(cos(radians(?)) * cos(radians(r.latitudine)) * cos(radians(r.longitudine) - radians(?)) + sin(radians(?)) * sin(radians(r.latitudine)))) AS distanza_km");
        }

        sql.append(" FROM RistorantiTheKnife r ")
                .append("LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante ")
                .append("LEFT JOIN Ristoranti_Tipologie rt ON r.id_ristorante = rt.id_ristorante ")
                .append("LEFT JOIN Tipologia_Cucina tc ON rt.id_tipologia = tc.id_tipologia ")
                .append("WHERE 1=1");

        List<Object> parametri = new ArrayList<>();

        if (usaCoordinate) {
            parametri.add(filtri.latitudineRiferimento());
            parametri.add(filtri.longitudineRiferimento());
            parametri.add(filtri.latitudineRiferimento());
        }

        if (filtri != null) {
            if (filtri.locazione() != null && !filtri.locazione().trim().isEmpty() && !usaCoordinate) {
                sql.append(" AND (LOWER(r.nazione) LIKE LOWER(?) OR LOWER(r.citta) LIKE LOWER(?))");
                String locPattern = "%" + filtri.locazione().trim() + "%";
                parametri.add(locPattern);
                parametri.add(locPattern);
            }
            if (filtri.tipoCucina() != null && !filtri.tipoCucina().trim().isEmpty()) {
                sql.append(" AND EXISTS (SELECT 1 FROM Ristoranti_Tipologie rt2 JOIN Tipologia_Cucina tc2 ON rt2.id_tipologia = tc2.id_tipologia WHERE rt2.id_ristorante = r.id_ristorante AND LOWER(tc2.nome) LIKE LOWER(?))");
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

        if (usaCoordinate) {
            sql.append(" ORDER BY distanza_km ASC");
        } else {
            sql.append(" ORDER BY r.nome ASC");
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
        return "SELECT r.*, COALESCE(AVG(rec.valutazione), 0.0) as media_stelle, STRING_AGG(DISTINCT tc.nome, ',') as tipi_cucina " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                "LEFT JOIN Ristoranti_Tipologie rt ON r.id_ristorante = rt.id_ristorante " +
                "LEFT JOIN Tipologia_Cucina tc ON rt.id_tipologia = tc.id_tipologia";
    }

    private RistoranteDTO mappaResultSetRistorante(ResultSet rs) throws SQLException {
        String tipiCucinaRaw = rs.getString("tipi_cucina");
        List<String> tipologie = tipiCucinaRaw != null && !tipiCucinaRaw.isEmpty()
                ? Arrays.asList(tipiCucinaRaw.split(",")) : new ArrayList<>();

        return new RistoranteDTO(
                rs.getInt("id_ristorante"),
                rs.getString("nome"),
                rs.getString("indirizzo"),
                rs.getString("nazione"),
                rs.getString("citta"),
                rs.getDouble("latitudine"),
                rs.getDouble("longitudine"),
                tipologie,
                rs.getDouble("prezzo_medio"),
                rs.getBoolean("delivery"),
                rs.getBoolean("booking_online"),
                rs.getDouble("media_stelle")
        );
    }
}