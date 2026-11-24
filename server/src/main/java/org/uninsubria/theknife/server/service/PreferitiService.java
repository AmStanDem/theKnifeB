/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.exceptions.AzioneDuplicataException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.server.dao.PreferitiDAO;
import org.uninsubria.theknife.server.rmi.RemoteSubscriptionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestisce le operazioni applicative sui ristoranti preferiti degli utenti
 * e pubblica gli eventi di aggiornamento conseguenti.
 */
public class PreferitiService {

    private final PreferitiDAO preferitiDAO = new PreferitiDAO();
    private final RemoteSubscriptionManager subscriptions;

    /**
     * Crea il servizio usando il gestore delle sottoscrizioni per notificare le modifiche.
     *
     * @param subscriptions gestore delle sottoscrizioni remote
     */
    public PreferitiService(RemoteSubscriptionManager subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     * Recupera i ristoranti preferiti di un utente.
     *
     * @param idUtente identificativo dell'utente
     * @return ristoranti preferiti, eventualmente vuoto
     * @throws SistemaIndisponibileException se l'identificativo è nullo o si verifica
     *         un errore durante l'accesso al database
     */
    public List<RistoranteDTO> getPreferiti(Integer idUtente) throws SistemaIndisponibileException {
        if (idUtente == null) {
            throw new SistemaIndisponibileException();
        }
        try {
            return preferitiDAO.ottieniPreferitiUtente(idUtente);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero lista preferiti: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Aggiunge un ristorante ai preferiti e pubblica una notifica di modifica.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @throws AzioneDuplicataException se uno degli identificativi è nullo,
     *         se l'associazione esiste già o se non è possibile inserirla
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void aggiungi(Integer idUtente, Integer idRistorante)
            throws AzioneDuplicataException, SistemaIndisponibileException {
        if (idUtente == null || idRistorante == null) {
            throw new AzioneDuplicataException("Parametri non validi per l'aggiunta ai preferiti.");
        }

        try {
            boolean successo = preferitiDAO.aggiungiPreferito(idUtente, idRistorante);
            if (!successo) {
                throw new AzioneDuplicataException("Il ristorante è già presente nei tuoi preferiti o non esiste.");
            }
            subscriptions.publish(
                    idRistorante, idUtente, DataChangedEvent.ChangeType.FAVORITES_CHANGED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore aggiunta preferito: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Rimuove un ristorante dai preferiti e pubblica una notifica di modifica.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @throws RistoranteNonTrovatoException se uno degli identificativi è nullo
     *         o l'associazione non esiste
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void rimuovi(Integer idUtente, Integer idRistorante)
            throws RistoranteNonTrovatoException, SistemaIndisponibileException {
        if (idUtente == null || idRistorante == null) {
            throw new RistoranteNonTrovatoException();
        }

        try {
            boolean successo = preferitiDAO.rimuoviPreferito(idUtente, idRistorante);
            if (!successo) {
                throw new RistoranteNonTrovatoException(idRistorante);
            }
            subscriptions.publish(
                    idRistorante, idUtente, DataChangedEvent.ChangeType.FAVORITES_CHANGED);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore rimozione preferito: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }
}