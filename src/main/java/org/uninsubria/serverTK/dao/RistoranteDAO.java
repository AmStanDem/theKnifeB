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
        String sqlRisto = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlTipologiaCheck = "INSERT INTO Tipologia_Cucina (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        String sqlTipologiaId = "SELECT id_tipologia FROM Tipologia_Cucina WHERE nome = ?";
        String sqlRistoTipo = "INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Disabilito l'autocommit per gestire gli inserimenti multipli in una singola transazione
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlRisto, Statement.RETURN_GENERATED_KEYS)) {
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

                // Recupero l'ID autogenerato del nuovo ristorante
                int idRistorante = -1;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) idRistorante = rs.getInt(1);
                }

                // Inserimento relazione molti-a-molti (Cucine)
                if (r.tipologieCucina() != null && idRistorante != -1) {
                    for (String tipo : r.tipologieCucina()) {

                        // 1. Assicura che la tipologia esista (ignora se già presente)
                        try (PreparedStatement sCheck = conn.prepareStatement(sqlTipologiaCheck)) {
                            sCheck.setString(1, tipo.trim());
                            sCheck.executeUpdate();
                        }

                        // 2. Recupera l'ID della tipologia
                        int idTipo = -1;
                        try (PreparedStatement sId = conn.prepareStatement(sqlTipologiaId)) {
                            sId.setString(1, tipo.trim());
                            try (ResultSet rsId = sId.executeQuery()) {
                                if (rsId.next()) idTipo = rsId.getInt(1);
                            }
                        }

                        // 3. Collega ristorante e tipologia nella tabella ponte
                        if (idTipo != -1) {
                            try (PreparedStatement sLink = conn.prepareStatement(sqlRistoTipo)) {
                                sLink.setInt(1, idRistorante);
                                sLink.setInt(2, idTipo);
                                sLink.executeUpdate();
                            }
                        }
                    }
                }

                // Transazione completata con successo
                conn.commit();
                return true;

            } catch (SQLException ex) {
                conn.rollback(); // Annulla tutte le modifiche in caso di errore
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento ristorante e tipologie: " + e.getMessage());
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

            // Sub-query per verificare la corrispondenza con la tabella tipologie
            if (filtri.tipoCucina() != null && !filtri.tipoCucina().trim().isEmpty()) {
                sql.append(" AND r.id_ristorante IN (" +
                        "SELECT rt_sub.id_ristorante FROM Ristoranti_Tipologie rt_sub " +
                        "JOIN Tipologia_Cucina tc_sub ON rt_sub.id_tipologia = tc_sub.id_tipologia " +
                        "WHERE LOWER(tc_sub.nome) LIKE LOWER(?) " +
                        ")");
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
        // Uso STRING_AGG per riunire le n tipologie di cucina di un ristorante in un'unica stringa separata da virgola.
        return "SELECT r.id_ristorante, r.nome, r.indirizzo, r.citta, r.nazione, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.booking_online, " +
                "COALESCE(AVG(rec.valutazione), 0.0) as media_stelle, " +
                "STRING_AGG(DISTINCT tc.nome, ',') as tipologie_cucina " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec ON r.id_ristorante = rec.id_ristorante " +
                "LEFT JOIN Ristoranti_Tipologie rt ON r.id_ristorante = rt.id_ristorante " +
                "LEFT JOIN Tipologia_Cucina tc ON rt.id_tipologia = tc.id_tipologia";
    }

    private RistoranteDTO mappaResultSetRistorante(ResultSet rs) throws SQLException {
        // Separo la stringa aggregata fornita da PostgreSQL in una lista per il DTO
        String stringaCucine = rs.getString("tipologie_cucina");
        List<String> tipologie = stringaCucine != null && !stringaCucine.isEmpty()
                ? Arrays.asList(stringaCucine.split(","))
                : new ArrayList<>();

        return new RistoranteDTO(
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
        );
    }
}