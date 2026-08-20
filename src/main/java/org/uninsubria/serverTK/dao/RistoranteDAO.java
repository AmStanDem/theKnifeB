package org.uninsubria.serverTK.dao;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.serverTK.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RistoranteDAO {

    /**
     * Inserisce un nuovo ristorante nel database.
     */
    public boolean inserisciRistorante(RistoranteDTO r, int idGestore) {

        String sql = """
                INSERT INTO RistorantiTheKnife
                (id_gestore, nome, indirizzo, citta, nazione,
                 latitudine, longitudine, tipo_cucina, prezzo_medio,
                 delivery, booking_online)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);
            stmt.setString(2, r.nome());
            stmt.setString(3, r.indirizzo());
            stmt.setString(4, r.citta());
            stmt.setString(5, r.nazione());

            if (r.latitudine() != null) {
                stmt.setDouble(6, r.latitudine());
            } else {
                stmt.setNull(6, Types.DOUBLE);
            }

            if (r.longitudine() != null) {
                stmt.setDouble(7, r.longitudine());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }

            stmt.setString(8, r.tipoCucina());

            if (r.prezzoMedio() != null) {
                stmt.setDouble(9, r.prezzoMedio());
            } else {
                stmt.setNull(9, Types.DOUBLE);
            }

            if (r.delivery() != null) {
                stmt.setBoolean(10, r.delivery());
            } else {
                stmt.setNull(10, Types.BOOLEAN);
            }

            if (r.bookingOnline() != null) {
                stmt.setBoolean(11, r.bookingOnline());
            } else {
                stmt.setNull(11, Types.BOOLEAN);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(
                    "[DAO_ERROR] Errore inserimento ristorante: "
                            + e.getMessage()
            );
            return false;
        }
    }


    /**
     * Cerca un ristorante tramite il suo ID.
     */
    public RistoranteDTO trovaPerId(int idRistorante) {

        String sql = costruisciQueryBase()
                + " WHERE r.id_ristorante = ? "
                + "GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRistorante);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return mappaResultSetRistorante(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "[DAO_ERROR] Errore ricerca ristorante per ID: "
                            + e.getMessage()
            );
        }

        return null;
    }


    /**
     * Restituisce tutti i ristoranti appartenenti a un determinato gestore.
     */
    public List<RistoranteDTO> trovaPerGestore(int idGestore) {

        List<RistoranteDTO> lista = new ArrayList<>();

        String sql = costruisciQueryBase()
                + " WHERE r.id_gestore = ? "
                + "GROUP BY r.id_ristorante";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idGestore);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    lista.add(mappaResultSetRistorante(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "[DAO_ERROR] Errore ricerca ristoranti del gestore: "
                            + e.getMessage()
            );
        }

        return lista;
    }


    /**
     * Cerca i ristoranti applicando i filtri specificati.
     */
    public List<RistoranteDTO> cercaConFiltri(FiltriRicercaDTO filtri) {

        List<RistoranteDTO> lista = new ArrayList<>();

        StringBuilder sql =
                new StringBuilder(costruisciQueryBase() + " WHERE 1=1");

        List<Object> parametri = new ArrayList<>();


        if (filtri != null) {

            // FILTRO LOCALITÀ
            if (filtri.locazione() != null
                    && !filtri.locazione().trim().isEmpty()) {

                sql.append(
                        " AND (LOWER(r.nazione) LIKE LOWER(?) "
                                + "OR LOWER(r.citta) LIKE LOWER(?))"
                );

                String locPattern =
                        "%" + filtri.locazione().trim() + "%";

                parametri.add(locPattern);
                parametri.add(locPattern);
            }


            // FILTRO TIPO CUCINA
            if (filtri.tipoCucina() != null
                    && !filtri.tipoCucina().trim().isEmpty()) {

                sql.append(
                        " AND LOWER(r.tipo_cucina) LIKE LOWER(?)"
                );

                parametri.add(
                        "%" + filtri.tipoCucina().trim() + "%"
                );
            }


            // FILTRO PREZZO MINIMO
            if (filtri.prezzoMin() != null) {

                sql.append(
                        " AND r.prezzo_medio >= ?"
                );

                parametri.add(filtri.prezzoMin());
            }


            // FILTRO PREZZO MASSIMO
            if (filtri.prezzoMax() != null) {

                sql.append(
                        " AND r.prezzo_medio <= ?"
                );

                parametri.add(filtri.prezzoMax());
            }


            // FILTRO DELIVERY
            if (filtri.delivery() != null
                    && filtri.delivery()) {

                sql.append(
                        " AND r.delivery = TRUE"
                );
            }


            // FILTRO PRENOTAZIONE ONLINE
            if (filtri.bookingOnline() != null
                    && filtri.bookingOnline()) {

                sql.append(
                        " AND r.booking_online = TRUE"
                );
            }
        }


        // Raggruppamento necessario per AVG(rec.valutazione)
        sql.append(
                " GROUP BY r.id_ristorante"
        );


        // FILTRO MEDIA STELLE
        if (filtri != null
                && filtri.mediaStelleMinima() != null) {

            sql.append(
                    " HAVING COALESCE(AVG(rec.valutazione), 0.0) >= ?"
            );

            parametri.add(
                    filtri.mediaStelleMinima().doubleValue()
            );
        }


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql.toString()
             )) {

            // Impostazione parametri della query
            for (int i = 0; i < parametri.size(); i++) {

                stmt.setObject(
                        i + 1,
                        parametri.get(i)
                );
            }


            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    lista.add(
                            mappaResultSetRistorante(rs)
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "[DAO_ERROR] Errore durante la ricerca "
                            + "filtrata ristoranti: "
                            + e.getMessage()
            );
        }

        return lista;
    }


    /**
     * Query base utilizzata dalle varie operazioni di ricerca.
     *
     * La media delle recensioni viene calcolata dinamicamente
     * e restituita come "media_stelle".
     */
    private String costruisciQueryBase() {

        return """
                SELECT r.*,
                       COALESCE(AVG(rec.valutazione), 0.0) AS media_stelle
                FROM RistorantiTheKnife r
                LEFT JOIN Recensioni rec
                    ON r.id_ristorante = rec.id_ristorante
                """;
    }


    /**
     * Converte una riga del ResultSet in un RistoranteDTO.
     *
     * ATTENZIONE:
     * l'ordine dei parametri deve essere esattamente quello
     * definito nel record RistoranteDTO.
     */
    private RistoranteDTO mappaResultSetRistorante(ResultSet rs)
            throws SQLException {

        return new RistoranteDTO(

                // 1 - idRistorante
                rs.getInt("id_ristorante"),

                // 2 - nome
                rs.getString("nome"),

                // 3 - indirizzo
                rs.getString("indirizzo"),

                // 4 - nazione
                rs.getString("nazione"),

                // 5 - citta
                rs.getString("citta"),

                // 6 - latitudine
                rs.getDouble("latitudine"),

                // 7 - longitudine
                rs.getDouble("longitudine"),

                // 8 - tipoCucina
                rs.getString("tipo_cucina"),

                // 9 - prezzoMedio
                rs.getDouble("prezzo_medio"),

                // 10 - delivery
                rs.getBoolean("delivery"),

                // 11 - bookingOnline
                rs.getBoolean("booking_online"),

                // 12 - mediaStelle
                rs.getDouble("media_stelle")
        );
    }
}