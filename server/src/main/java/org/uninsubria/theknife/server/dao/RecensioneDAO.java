/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.dao;

import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.server.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la lettura e la gestione delle recensioni e delle relative risposte.
 */
public class RecensioneDAO {
    /**
     * Identificativi del ristorante e dell'utente associati a una recensione.
     *
     * @param idRistorante identificativo del ristorante recensito
     * @param idUtente identificativo dell'utente autore
     */
    public record RecensioneTarget(int idRistorante, int idUtente) {}

    /**
     * Recupera il ristorante e l'utente associati alla recensione.
     *
     * @param idRecensione identificativo della recensione
     * @return identificativi associati, oppure {@code null} se la recensione non esiste
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public RecensioneTarget trovaTargetRecensione(int idRecensione) throws SQLException {
        String sql = "SELECT id_ristorante, id_utente FROM Recensioni WHERE id_recensione = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecensione);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next()
                        ? new RecensioneTarget(rs.getInt("id_ristorante"), rs.getInt("id_utente"))
                        : null;
            }
        }
    }

    /**
     * Verifica se l'utente esiste ed è registrato con ruolo cliente.
     *
     * @param idUtente identificativo dell'utente
     * @return {@code true} se l'utente ha ruolo cliente
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
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

    /**
     * Inserisce una recensione per il cliente e il ristorante specificati.
     *
     * @param idCliente identificativo del cliente autore
     * @param idRistorante identificativo del ristorante recensito
     * @param r dati della recensione
     * @return {@code true} se la recensione è stata inserita, {@code false} in caso di vincolo univoco duplicato
     * @throws SQLException se si verifica un errore durante l'accesso al database diverso da un duplicato
     */
    public boolean aggiungiRecensione(int idCliente, int idRistorante, RecensioneDTO r) throws SQLException {
        String sql = "INSERT INTO Recensioni (id_utente, id_ristorante, valutazione, testo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idRistorante);
            stmt.setDouble(3, r.valutazione());
            stmt.setString(4, r.testo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Recupera le recensioni del ristorante, dalla più recente, includendo autore e risposta del gestore.
     *
     * @param idRistorante identificativo del ristorante
     * @return elenco delle recensioni del ristorante, eventualmente vuoto
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public List<RecensioneDTO> trovaPerRistorante(int idRistorante) throws SQLException {
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
                            rs.getDouble("valutazione"),
                            rs.getString("testo"),
                            rs.getString("nome_autore"),
                            dataCreazione,
                            rs.getString("risposta_gestore"),
                            rs.getInt("id_ristorante"),
                            rs.getString("nome_ristorante"),
                            rs.getInt("versione")
                    ));
                }
            }
        }
        return lista;
    }

    /**
     * Recupera le recensioni scritte dal cliente identificato, dalla più recente,
     * includendo nome del ristorante e risposta del gestore.
     *
     * @param idUtente identificativo del cliente
     * @return elenco delle recensioni dell'utente, eventualmente vuoto
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
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
                            rs.getDouble("valutazione"),
                            rs.getString("testo"),
                            rs.getString("nome_autore"),
                            dataCreazione,
                            rs.getString("risposta_gestore"),
                            rs.getInt("id_ristorante"),
                            rs.getString("nome_ristorante"),
                            rs.getInt("versione")
                    ));
                }
            }
        }
        return lista;
    }

    /**
     * Esito dell'aggiornamento ottimistico di una recensione.
     *
     * @param aggiornata indica se la recensione è stata aggiornata
     * @param esiste indica se la recensione dell'utente esisteva
     * @param idRistorante identificativo del ristorante in caso di aggiornamento, altrimenti {@code null}
     */
    public record EsitoModifica(boolean aggiornata, boolean esiste, Integer idRistorante) {}

    /**
     * Aggiorna testo e valutazione della recensione solo se appartiene al cliente
     * e la versione corrente corrisponde a quella attesa.
     *
     * @param idRecensione identificativo della recensione
     * @param idCliente identificativo del cliente autore
     * @param nuovoTesto nuovo testo della recensione
     * @param nuoveStelle nuova valutazione
     * @param versioneAttesa versione attesa del dato
     * @return esito dell'aggiornamento, con l'identificativo del ristorante se aggiornato
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public EsitoModifica modificaRecensione(
            int idRecensione, int idCliente, String nuovoTesto, double nuoveStelle, int versioneAttesa)
            throws SQLException {
        String sql = """
                UPDATE Recensioni
                SET valutazione = ?, testo = ?, data_modifica = CURRENT_TIMESTAMP,
                    versione = versione + 1
                WHERE id_recensione = ? AND id_utente = ? AND versione = ?
                RETURNING id_ristorante
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, nuoveStelle);
            stmt.setString(2, nuovoTesto);
            stmt.setInt(3, idRecensione);
            stmt.setInt(4, idCliente);
            stmt.setInt(5, versioneAttesa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EsitoModifica(true, true, rs.getInt("id_ristorante"));
                }
            }
        }

        String existsSql = "SELECT EXISTS (SELECT 1 FROM Recensioni WHERE id_recensione = ? AND id_utente = ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(existsSql)) {
            stmt.setInt(1, idRecensione);
            stmt.setInt(2, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                return new EsitoModifica(false, rs.next() && rs.getBoolean(1), null);
            }
        }
    }

    /**
     * Elimina la recensione indicata se appartiene al cliente specificato.
     *
     * @param idRecensione identificativo della recensione
     * @param idCliente identificativo del cliente autore
     * @return {@code true} se una recensione è stata eliminata
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public boolean eliminaRecensione(int idRecensione, int idCliente) throws SQLException {
        String sql = "DELETE FROM Recensioni WHERE id_recensione = ? AND id_utente = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecensione);
            stmt.setInt(2, idCliente);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Inserisce la risposta del gestore se il gestore è proprietario del ristorante
     * e la recensione non ha già una risposta.
     *
     * @param idRecensione identificativo della recensione
     * @param idGestore identificativo del gestore
     * @param testoRisposta testo della risposta
     * @return {@code true} se la risposta è stata inserita, {@code false} se non è stato possibile inserirla
     * @throws SQLException se si verifica un errore durante l'accesso al database diverso da un duplicato
     */
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