/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.dao;

import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.server.config.DatabaseConfig;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DAO per la gestione delle associazioni tra utenti e ristoranti preferiti.
 */
public class PreferitiDAO {

    /**
     * Aggiunge un ristorante ai preferiti dell'utente.
     * Se l'associazione esiste già, non la duplica.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @return {@code true} se è stata inserita una nuova associazione, {@code false} se esisteva già
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public boolean aggiungiPreferito(int idUtente, int idRistorante) throws SQLException {
        String sql = "INSERT INTO Preferiti (id_utente, id_ristorante) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Rimuove un ristorante dai preferiti dell'utente.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @return {@code true} se è stata rimossa un'associazione, {@code false} se non esisteva
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public boolean rimuoviPreferito(int idUtente, int idRistorante) throws SQLException {
        String sql = "DELETE FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            stmt.setInt(2, idRistorante);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Recupera i ristoranti preferiti dell'utente, ordinati per nome.
     * I dati restituiti includono prezzo, servizi, tipologie di cucina e valutazione media.
     *
     * @param idUtente identificativo dell'utente
     * @return lista dei ristoranti preferiti, vuota se non ne risultano associati
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public List<RistoranteDTO> ottieniPreferitiUtente(int idUtente) throws SQLException {
        List<RistoranteDTO> lista = new ArrayList<>();

        String sql = "SELECT r.id_ristorante, r.nome, r.indirizzo, r.citta, r.nazione, " +
                "r.latitudine, r.longitudine, r.prezzo_medio, r.prezzo_stimato, r.delivery, r.booking_online, " +
                "r.telefono, r.sito_web, r.descrizione, " +
                "COALESCE(rec.media_stelle, 0.0) AS media_stelle, cucine.tipi_cucina " +
                "FROM RistorantiTheKnife r " +
                "JOIN Preferiti p ON r.id_ristorante = p.id_ristorante " +
                "LEFT JOIN (SELECT id_ristorante, AVG(valutazione) AS media_stelle " +
                "FROM Recensioni GROUP BY id_ristorante) rec ON r.id_ristorante = rec.id_ristorante " +
                "LEFT JOIN (SELECT rt.id_ristorante, STRING_AGG(tc.nome, ',') AS tipi_cucina " +
                "FROM Ristoranti_Tipologie rt JOIN Tipologia_Cucina tc " +
                "ON rt.id_tipologia = tc.id_tipologia GROUP BY rt.id_ristorante) cucine " +
                "ON r.id_ristorante = cucine.id_ristorante " +
                "WHERE p.id_utente = ? ORDER BY r.nome";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipiCucinaRaw = rs.getString("tipi_cucina");
                    List<String> tipologie = tipiCucinaRaw != null && !tipiCucinaRaw.isEmpty()
                            ? Arrays.asList(tipiCucinaRaw.split(",")) : new ArrayList<>();
                    BigDecimal prezzoMedio = rs.getBigDecimal("prezzo_medio");

                    lista.add(new RistoranteDTO(
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
                            rs.getDouble("media_stelle"),
                            rs.getBoolean("prezzo_stimato"),
                            rs.getString("telefono"),
                            rs.getString("sito_web"),
                            rs.getString("descrizione")
                    ));
                }
            }
        }
        return lista;
    }
}