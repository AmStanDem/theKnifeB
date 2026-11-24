/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.exceptions.*;
import org.uninsubria.theknife.server.dao.RecensioneDAO;
import org.uninsubria.theknife.server.rmi.RemoteSubscriptionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestisce i casi d'uso relativi a recensioni e risposte dei gestori,
 * applicando le regole di validazione e pubblicando gli eventi di modifica.
 */
public class RecensioneService {

    private final RecensioneDAO recensioneDAO = new RecensioneDAO();
    private final RemoteSubscriptionManager subscriptions;

    /**
     * Crea il servizio usando il gestore delle sottoscrizioni per notificare le modifiche.
     *
     * @param subscriptions gestore delle sottoscrizioni remote
     */
    public RecensioneService(RemoteSubscriptionManager subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     * Recupera le recensioni associate a un ristorante.
     *
     * @param idRistorante identificativo del ristorante; se nullo, non vengono restituiti risultati
     * @return recensioni del ristorante, eventualmente vuoto
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public List<RecensioneDTO> ottieniPerRistorante(Integer idRistorante)
            throws SistemaIndisponibileException {
        if (idRistorante == null) {
            return List.of();
        }
        try {
            return recensioneDAO.trovaPerRistorante(idRistorante);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero recensioni: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Recupera le recensioni di un ristorante oscurando l'identità dell'autore.
     * Utilizzato per la visualizzazione da parte degli utenti non loggati (Guest).
     *
     * @param idRistorante identificativo del ristorante; se nullo, non vengono restituiti risultati
     * @return recensioni del ristorante con il nome dell'autore oscurato, eventualmente vuoto
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public List<RecensioneDTO> ottieniPerRistoranteAnonimo(Integer idRistorante)
            throws SistemaIndisponibileException {
        if (idRistorante == null) {
            return List.of();
        }

        List<RecensioneDTO> recensioniReali = ottieniPerRistorante(idRistorante);

        // Trasforma la lista oscurando il campo del nome autore
        return recensioniReali.stream()
                .map(r -> new RecensioneDTO(
                        r.idRecensione(),
                        r.valutazione(),
                        r.testo(),
                        "",
                        r.dataCreazione(),
                        r.rispostaGestore(),
                        r.idRistorante(),
                        r.nomeRistorante(),
                        r.versione()
                ))
                .toList();
    }

    /**
     * Recupera le recensioni scritte da un utente.
     *
     * @param idUtente identificativo dell'utente
     * @return recensioni dell'utente, eventualmente vuoto
     * @throws SistemaIndisponibileException se l'identificativo è nullo o si verifica
     *         un errore durante l'accesso al database
     */
    public List<RecensioneDTO> ottieniPerUtente(Integer idUtente) throws SistemaIndisponibileException {
        if (idUtente == null) {
            throw new SistemaIndisponibileException();
        }
        try {
            return recensioneDAO.trovaPerUtente(idUtente);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero recensioni utente: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Inserisce una recensione dopo aver verificato i dati e il ruolo cliente dell'autore.
     *
     * @param recensione contenuto e valutazione da inserire
     * @param idRistorante identificativo del ristorante recensito
     * @param idUtente identificativo dell'autore
     * @throws DatiMancantiException se mancano dati obbligatori o la valutazione non è valida
     * @throws OperazioneNonConsentitaException se l'utente non ha ruolo cliente
     * @throws AzioneDuplicataException se l'utente ha già recensito il ristorante
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void aggiungiRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws DatiMancantiException, OperazioneNonConsentitaException,
            AzioneDuplicataException, SistemaIndisponibileException {

        if (recensione == null ||
                recensione.testo() == null || recensione.testo().trim().isEmpty() ||
                !valutazioneValida(recensione.valutazione()) ||
                idRistorante == null || idUtente == null) {
            throw new DatiMancantiException("Ristorante, utente, testo e valutazione da 1 a 5 sono obbligatori.");
        }

        try {
            if (!recensioneDAO.utenteCliente(idUtente)) {
                throw new OperazioneNonConsentitaException("Solo un cliente registrato può inserire una recensione.");
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore verifica ruolo per inserimento recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }

        try {
            boolean successo = recensioneDAO.inserisciRecensione(idUtente, idRistorante, recensione);
            if (!successo) {
                throw new AzioneDuplicataException("Hai già recensito questo ristorante.");
            }
            subscriptions.publish(idRistorante, idUtente, DataChangedEvent.ChangeType.REVIEW_CREATED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Modifica testo e valutazione di una recensione usando il controllo di versione ottimistico.
     *
     * @param idRecensione identificativo della recensione
     * @param idUtente identificativo dell'utente autore
     * @param nuovoTesto nuovo testo non vuoto
     * @param nuoveStelle nuova valutazione valida da 1 a 5, a passi di mezza stella
     * @param versioneAttesa versione della recensione attesa dal client
     * @throws DatiMancantiException se testo, identificativi o versione non sono validi
     * @throws RecensioneNonTrovataException se la recensione non esiste o non appartiene all'utente
     * @throws ModificaObsoletaException se la recensione è stata modificata dopo il caricamento
     * @throws OperazioneNonConsentitaException se la valutazione non è valida
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void modifica(Integer idRecensione, Integer idUtente, String nuovoTesto,
                         Double nuoveStelle, Integer versioneAttesa)
            throws DatiMancantiException, RecensioneNonTrovataException, ModificaObsoletaException,
            OperazioneNonConsentitaException, SistemaIndisponibileException {

        if (nuovoTesto == null || nuovoTesto.trim().isEmpty()) {
            throw new DatiMancantiException("Il testo della recensione non può essere vuoto.");
        }
        if (!valutazioneValida(nuoveStelle)) {
            throw new OperazioneNonConsentitaException(
                    "La valutazione deve essere compresa tra 1 e 5, anche con mezze stelle.");
        }

        try {
            if (idRecensione == null || idUtente == null || versioneAttesa == null || versioneAttesa < 0) {
                throw new DatiMancantiException("Identificativo e versione della recensione sono obbligatori.");
            }
            RecensioneDAO.EsitoModifica esito = recensioneDAO.modificaRecensione(
                    idRecensione, idUtente, nuovoTesto, nuoveStelle, versioneAttesa);
            if (!esito.aggiornata() && !esito.esiste()) {
                throw new RecensioneNonTrovataException(
                        "Impossibile modificare: recensione inesistente o non di tua proprietà.");
            }
            if (!esito.aggiornata()) {
                throw new ModificaObsoletaException(
                        "La recensione è stata modificata da un altro client. Ricarica i dati e riprova.");
            }
            subscriptions.publish(esito.restaurantId(), idUtente, DataChangedEvent.ChangeType.REVIEW_UPDATED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore modifica recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    private boolean valutazioneValida(Double valutazione) {
        return valutazione != null
                && Double.isFinite(valutazione)
                && valutazione >= 1
                && valutazione <= 5
                && Math.abs(valutazione * 2 - Math.rint(valutazione * 2)) < 1e-9;
    }

    /**
     * Elimina una recensione se appartiene all'utente indicato e pubblica l'evento di modifica.
     *
     * @param idRecensione identificativo della recensione
     * @param idUtente identificativo dell'utente autore
     * @throws RecensioneNonTrovataException se la recensione non esiste o non appartiene all'utente
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void elimina(Integer idRecensione, Integer idUtente)
            throws RecensioneNonTrovataException, SistemaIndisponibileException {
        try {
            RecensioneDAO.RecensioneTarget target = recensioneDAO.trovaTargetRecensione(idRecensione);
            boolean successo = recensioneDAO.eliminaRecensione(idRecensione, idUtente);
            if (!successo) {
                throw new RecensioneNonTrovataException(
                        "Impossibile eliminare: recensione inesistente o non di tua proprietà.");
            }
            subscriptions.publish(target.restaurantId(), idUtente, DataChangedEvent.ChangeType.REVIEW_DELETED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore eliminazione recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Aggiunge la risposta del gestore a una recensione e pubblica l'evento di modifica.
     *
     * @param idRecensione identificativo della recensione
     * @param idGestore identificativo del gestore
     * @param testoRisposta testo della risposta
     * @throws DatiMancantiException se recensione, gestore o testo della risposta mancano
     * @throws OperazioneNonConsentitaException se il gestore non può rispondere
     *         o la recensione ha già una risposta
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void aggiungiRisposta(Integer idRecensione, Integer idGestore, String testoRisposta)
            throws DatiMancantiException, OperazioneNonConsentitaException, SistemaIndisponibileException {

        if (idRecensione == null || idGestore == null
                || testoRisposta == null || testoRisposta.trim().isEmpty()) {
            throw new DatiMancantiException("Recensione, gestore e testo della risposta sono obbligatori.");
        }

        try {
            RecensioneDAO.RecensioneTarget target = recensioneDAO.trovaTargetRecensione(idRecensione);
            boolean successo = recensioneDAO.aggiungiRispostaGestore(
                    idRecensione, idGestore, testoRisposta.trim());
            if (!successo) {
                throw new OperazioneNonConsentitaException(
                        "solo il gestore del ristorante può rispondere e ogni recensione può avere una sola risposta.");
            }
            subscriptions.publish(
                    target.restaurantId(), target.userId(), DataChangedEvent.ChangeType.REVIEW_REPLY_CHANGED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento risposta recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }
}