package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.PaginaRistorantiDTO;
import org.uninsubria.common.dto.RicercaRistorantiPaginaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RistoranteDAO {

    private static final String POSIZIONE_RISTORANTE =
            "ST_SetSRID(ST_MakePoint(r.longitudine::double precision, " +
                    "r.latitudine::double precision), 4326)::geography";
    private static final String POSIZIONE_RIFERIMENTO =
            "ST_SetSRID(ST_MakePoint(?::double precision, ?::double precision), 4326)::geography";

    public boolean inserisciRistorante(RistoranteDTO r, int idGestore) throws SQLException {
        String sqlRisto = "INSERT INTO RistorantiTheKnife (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio, delivery, booking_online) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlTipologiaCheck = "INSERT INTO Tipologia_Cucina (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        String sqlTipologiaId = "SELECT id_tipologia FROM Tipologia_Cucina WHERE nome = ?";
        String sqlRistoTipo = "INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection()) {
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

                int idRistorante = -1;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idRistorante = rs.getInt(1);
                    }
                }

                if (idRistorante == -1) {
                    conn.rollback();
                    return false;
                }

                // Inserimento cucine e associazioni
                if (r.tipologieCucina() != null) {
                    for (String tipo : r.tipologieCucina()) {
                        if (tipo == null || tipo.trim().isEmpty()) continue;
                        String tipoClean = tipo.trim();

                        try (PreparedStatement sCheck = conn.prepareStatement(sqlTipologiaCheck)) {
                            sCheck.setString(1, tipoClean);
                            sCheck.executeUpdate();
                        }

                        int idTipo = -1;
                        try (PreparedStatement sId = conn.prepareStatement(sqlTipologiaId)) {
                            sId.setString(1, tipoClean);
                            try (ResultSet rsId = sId.executeQuery()) {
                                if (rsId.next()) {
                                    idTipo = rsId.getInt(1);
                                }
                            }
                        }

                        if (idTipo != -1) {
                            try (PreparedStatement sLink = conn.prepareStatement(sqlRistoTipo)) {
                                sLink.setInt(1, idRistorante);
                                sLink.setInt(2, idTipo);
                                sLink.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    public boolean modificaRistorante(RistoranteDTO r, int idGestore) throws SQLException {
        String sqlRistorante = "UPDATE RistorantiTheKnife SET nome = ?, indirizzo = ?, citta = ?, " +
                "nazione = ?, latitudine = ?, longitudine = ?, prezzo_medio = ?, delivery = ?, " +
                "booking_online = ? WHERE id_ristorante = ? AND id_gestore = ?";
        String sqlEliminaTipologie = "DELETE FROM Ristoranti_Tipologie WHERE id_ristorante = ?";
        String sqlAggiungiTipologia = "INSERT INTO Ristoranti_Tipologie (id_ristorante, id_tipologia) " +
                "SELECT ?, id_tipologia FROM Tipologia_Cucina WHERE LOWER(nome) = LOWER(?) " +
                "ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int aggiornati;
                try (PreparedStatement stmt = conn.prepareStatement(sqlRistorante)) {
                    stmt.setString(1, r.nome().trim());
                    stmt.setString(2, r.indirizzo().trim());
                    stmt.setString(3, r.citta().trim());
                    stmt.setString(4, r.nazione().trim());
                    stmt.setDouble(5, r.latitudine());
                    stmt.setDouble(6, r.longitudine());
                    stmt.setDouble(7, r.prezzoMedio());
                    stmt.setBoolean(8, Boolean.TRUE.equals(r.delivery()));
                    stmt.setBoolean(9, Boolean.TRUE.equals(r.bookingOnline()));
                    stmt.setInt(10, r.idRistorante());
                    stmt.setInt(11, idGestore);
                    aggiornati = stmt.executeUpdate();
                }
                if (aggiornati == 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlEliminaTipologie)) {
                    stmt.setInt(1, r.idRistorante());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(sqlAggiungiTipologia)) {
                    for (String tipo : r.tipologieCucina()) {
                        stmt.setInt(1, r.idRistorante());
                        stmt.setString(2, tipo.trim());
                        if (stmt.executeUpdate() == 0) {
                            throw new SQLException("Tipologia cucina non presente nel catalogo: " + tipo);
                        }
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    public RistoranteDTO trovaPerId(int idRistorante) throws SQLException {
        String sql = costruisciQueryBase() + " WHERE r.id_ristorante = ? GROUP BY r.id_ristorante";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRistorante);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mappaResultSetRistorante(rs);
            }
        }
        return null;
    }

    public List<RistoranteDTO> trovaPerGestore(int idGestore) throws SQLException {
        List<RistoranteDTO> lista = new ArrayList<>();
        String sql = costruisciQueryBase() + " WHERE r.id_gestore = ? GROUP BY r.id_ristorante ORDER BY r.nome ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idGestore);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mappaResultSetRistorante(rs));
            }
        }
        return lista;
    }

    public List<RistoranteDTO> cercaConFiltri(FiltriRicercaDTO filtri) throws SQLException {
        List<RistoranteDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(costruisciQueryBase() + " WHERE 1=1");
        List<Object> parametri = new ArrayList<>();

        if (filtri != null) {
            // Ricerca per città o nazione
            if (filtri.locazione() != null && !filtri.locazione().trim().isEmpty()) {
                sql.append(" AND (LOWER(r.nazione) LIKE LOWER(?) OR LOWER(r.citta) LIKE LOWER(?))");
                String locPattern = "%" + filtri.locazione().trim() + "%";
                parametri.add(locPattern);
                parametri.add(locPattern);
            }

            // Ricerca geografica su geography per sfruttare l'indice GiST PostGIS.
            boolean usaCoordinate = filtri.latitudineRiferimento() != null &&
                    filtri.longitudineRiferimento() != null &&
                    filtri.raggioKm() != null;

            if (usaCoordinate) {
                sql.append(" AND ST_DWithin(").append(POSIZIONE_RISTORANTE)
                        .append(", ").append(POSIZIONE_RIFERIMENTO).append(", ?)");
                parametri.add(filtri.longitudineRiferimento());
                parametri.add(filtri.latitudineRiferimento());
                parametri.add(filtri.raggioKm() * 1000.0);
            }

            // Filtro tipologia cucina
            if (filtri.tipoCucina() != null && !filtri.tipoCucina().trim().isEmpty()) {
                sql.append(" AND r.id_ristorante IN (" +
                        "SELECT rt_sub.id_ristorante FROM Ristoranti_Tipologie rt_sub " +
                        "JOIN Tipologia_Cucina tc_sub ON rt_sub.id_tipologia = tc_sub.id_tipologia " +
                        "WHERE LOWER(tc_sub.nome) LIKE LOWER(?))");
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

            sql.append(" GROUP BY r.id_ristorante");

            if (filtri.mediaStelleMinima() != null) {
                sql.append(" HAVING COALESCE(AVG(rec_stat.valutazione), 0.0) >= ?");
                parametri.add(filtri.mediaStelleMinima());
            }

            // Ordinamento
            if (usaCoordinate) {
                sql.append(" ORDER BY ST_Distance(")
                        .append(POSIZIONE_RISTORANTE).append(", ")
                        .append(POSIZIONE_RIFERIMENTO).append(") ASC");
                parametri.add(filtri.longitudineRiferimento());
                parametri.add(filtri.latitudineRiferimento());
            } else {
                sql.append(" ORDER BY r.nome ASC");
            }
        } else {
            sql.append(" GROUP BY r.id_ristorante ORDER BY r.nome ASC");
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
        }
        return lista;
    }

    public PaginaRistorantiDTO cercaPagina(RicercaRistorantiPaginaDTO richiesta) throws SQLException {
        List<Object> parametri = new ArrayList<>();
        StringBuilder sql = new StringBuilder(costruisciQueryBase()).append(" WHERE 1=1");

        // Filtro per località
        if (richiesta.localita() != null && !richiesta.localita().isBlank()) {
            sql.append(" AND LOWER(r.citta) LIKE ?");
            parametri.add("%" + richiesta.localita().trim().toLowerCase() + "%");
        }

        if (richiesta.testo() != null && !richiesta.testo().isBlank()) {
            String testo = "%" + richiesta.testo().trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(r.nome) LIKE ? OR LOWER(r.indirizzo) LIKE ? OR LOWER(r.citta) LIKE ?")
                    .append(" OR EXISTS (SELECT 1 FROM Ristoranti_Tipologie rt_q ")
                    .append("JOIN Tipologia_Cucina tc_q ON tc_q.id_tipologia = rt_q.id_tipologia ")
                    .append("WHERE rt_q.id_ristorante = r.id_ristorante AND LOWER(tc_q.nome) LIKE ?))");
            parametri.add(testo);
            parametri.add(testo);
            parametri.add(testo);
            parametri.add(testo);
        }

        if (richiesta.latitudineRiferimento() != null) {
            sql.append(" AND ST_DWithin(").append(POSIZIONE_RISTORANTE)
                    .append(", ").append(POSIZIONE_RIFERIMENTO).append(", ?)");
            parametri.add(richiesta.longitudineRiferimento());
            parametri.add(richiesta.latitudineRiferimento());
            parametri.add(richiesta.raggioKm() * 1000.0);
        }

        if (!richiesta.tipologieCucina().isEmpty()) {
            String placeholders = String.join(", ", java.util.Collections.nCopies(
                    richiesta.tipologieCucina().size(), "LOWER(?)"));
            sql.append(" AND EXISTS (SELECT 1 FROM Ristoranti_Tipologie rt_f ")
                    .append("JOIN Tipologia_Cucina tc_f ON tc_f.id_tipologia = rt_f.id_tipologia ")
                    .append("WHERE rt_f.id_ristorante = r.id_ristorante ")
                    .append("AND LOWER(tc_f.nome) IN (").append(placeholders).append("))");
            richiesta.tipologieCucina().stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(parametri::add);
        }

        if (richiesta.prezzoMin() != null) {
            sql.append(" AND r.prezzo_medio >= ?");
            parametri.add(richiesta.prezzoMin());
        }
        if (richiesta.prezzoMax() != null) {
            sql.append(" AND r.prezzo_medio <= ?");
            parametri.add(richiesta.prezzoMax());
        }
        if (richiesta.delivery()) {
            sql.append(" AND r.delivery = TRUE");
        }
        if (richiesta.bookingOnline()) {
            sql.append(" AND r.booking_online = TRUE");
        }

        sql.append(" GROUP BY r.id_ristorante");
        if (richiesta.mediaStelleMinima() != null) {
            sql.append(" HAVING COALESCE(AVG(rec_stat.valutazione), 0.0) >= ?");
            parametri.add(richiesta.mediaStelleMinima());
        }
        if (richiesta.latitudineRiferimento() != null) {
            sql.append(" ORDER BY ST_Distance(")
                    .append(POSIZIONE_RISTORANTE).append(", ")
                    .append(POSIZIONE_RIFERIMENTO).append(") ASC, r.nome ASC");
            parametri.add(richiesta.longitudineRiferimento());
            parametri.add(richiesta.latitudineRiferimento());
        } else {
            sql.append(" ORDER BY r.nome ASC");
        }
        sql.append(" LIMIT ? OFFSET ?");
        parametri.add(richiesta.limite() + 1);
        parametri.add(richiesta.offset());

        List<RistoranteDTO> risultati = new ArrayList<>(richiesta.limite() + 1);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametri.size(); i++) {
                stmt.setObject(i + 1, parametri.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risultati.add(mappaResultSetRistorante(rs));
                }
            }
        }

        boolean hasMore = risultati.size() > richiesta.limite();
        if (hasMore) {
            risultati.remove(risultati.size() - 1);
        }
        return new PaginaRistorantiDTO(risultati, hasMore);
    }

    private String costruisciQueryBase() {
        // Separiamo le recensioni per evitare righe duplicate provocate dalle molteplici tipologie cucina
        return "SELECT r.id_ristorante, r.nome, r.indirizzo, r.citta, r.nazione, r.latitudine, r.longitudine, " +
                "r.prezzo_medio, r.delivery, r.booking_online, " +
                "COALESCE(AVG(rec_stat.valutazione), 0.0) as media_stelle, " +
                "STRING_AGG(DISTINCT tc.nome, ',') as tipologie_cucina " +
                "FROM RistorantiTheKnife r " +
                "LEFT JOIN Recensioni rec_stat ON r.id_ristorante = rec_stat.id_ristorante " +
                "LEFT JOIN Ristoranti_Tipologie rt ON r.id_ristorante = rt.id_ristorante " +
                "LEFT JOIN Tipologia_Cucina tc ON rt.id_tipologia = tc.id_tipologia";
    }

    public List<String> ottieniTutteTipologieCucina() throws SQLException {
        List<String> tipologie = new ArrayList<>();
        String sql = "SELECT DISTINCT nome FROM Tipologia_Cucina ORDER BY nome";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tipologie.add(rs.getString("nome"));
            }
        }
        return tipologie;
    }

    private RistoranteDTO mappaResultSetRistorante(ResultSet rs) throws SQLException {
        String stringaCucine = rs.getString("tipologie_cucina");
        List<String> tipologie = (stringaCucine != null && !stringaCucine.isBlank())
                ? Arrays.asList(stringaCucine.split(","))
                : new ArrayList<>();
        BigDecimal prezzoMedio = rs.getBigDecimal("prezzo_medio");

        return new RistoranteDTO(
                rs.getInt("id_ristorante"),
                rs.getString("nome"),
                rs.getString("indirizzo"),
                rs.getString("citta"),
                rs.getString("nazione"),
                rs.getDouble("latitudine"),
                rs.getDouble("longitudine"),
                tipologie,
                prezzoMedio == null ? null : prezzoMedio.doubleValue(),
                rs.getBoolean("delivery"),
                rs.getBoolean("booking_online"),
                rs.getDouble("media_stelle")
        );
    }
}