/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.uninsubria.theknife.common.dto.FiltriRicercaDTO;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.dto.PaginaRistorantiDTO;
import org.uninsubria.theknife.common.dto.RicercaRistorantiPaginaDTO;
import org.uninsubria.theknife.common.dto.RiepilogoRecensioniDTO;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.server.dao.RistoranteDAO;
import org.uninsubria.theknife.server.rmi.RemoteSubscriptionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestisce i casi d'uso relativi al catalogo dei ristoranti, applicando le
 * validazioni di dominio e pubblicando gli eventi sulle modifiche.
 */
public class RistoranteService {

    private final RistoranteDAO ristoranteDAO = new RistoranteDAO();
    private final RemoteSubscriptionManager subscriptions;

    /**
     * Crea il servizio usando il gestore delle sottoscrizioni per notificare le modifiche.
     *
     * @param subscriptions gestore delle sottoscrizioni remote
     */
    public RistoranteService(RemoteSubscriptionManager subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     * Registra un ristorante dopo aver verificato i dati obbligatori, le coordinate
     * e la presenza di almeno una tipologia di cucina.
     *
     * @param ristorante dati del ristorante da registrare
     * @param idGestore identificativo del gestore proprietario
     * @throws DatiMancantiException se i dati richiesti sono assenti o non validi
     * @throws OperazioneNonConsentitaException se il ristorante o l'identificativo del gestore mancano
     * @throws SistemaIndisponibileException se l'inserimento fallisce o si verifica
     *         un errore durante l'accesso al database
     */
    public void aggiungiRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws DatiMancantiException, OperazioneNonConsentitaException, SistemaIndisponibileException {

        if (ristorante == null || idGestore == null) {
            throw new OperazioneNonConsentitaException("Accesso negato: identificativo gestore mancante.");
        }

        if (ristorante.nome() == null || ristorante.nome().trim().isEmpty() ||
                ristorante.indirizzo() == null || ristorante.indirizzo().trim().isEmpty() ||
                ristorante.citta() == null || ristorante.citta().trim().isEmpty() ||
                ristorante.nazione() == null || ristorante.nazione().trim().isEmpty() ||
                ristorante.prezzoMedio() == null || !Double.isFinite(ristorante.prezzoMedio())
                || ristorante.prezzoMedio() < 0.0) {
            throw new DatiMancantiException("Campi obbligatori mancanti o prezzo medio non valido.");
        }

        if (ristorante.latitudine() == null || !Double.isFinite(ristorante.latitudine())
                || ristorante.latitudine() < -90 || ristorante.latitudine() > 90
                || ristorante.longitudine() == null || !Double.isFinite(ristorante.longitudine())
                || ristorante.longitudine() < -180 || ristorante.longitudine() > 180) {
            throw new DatiMancantiException("Seleziona un luogo valido con coordinate geografiche corrette.");
        }

        // Validazione della relazione Molti-a-Molti introdotta
        if (ristorante.tipologieCucina() == null || ristorante.tipologieCucina().isEmpty()) {
            throw new DatiMancantiException("È necessario specificare almeno una tipologia di cucina.");
        }

        try {
            Integer restaurantId = ristoranteDAO.inserisciRistorante(ristorante, idGestore);
            if (restaurantId == null) {
                throw new SistemaIndisponibileException();
            }
            subscriptions.pubblica(restaurantId, idGestore, DataChangedEvent.ChangeType.RISTORANTE_MODIFICATO);
        } catch (SQLException e) {
            throw erroreDatabase("inserimento ristorante", e);
        }
    }

    /**
     * Aggiorna i dati di un ristorante e le tipologie di cucina associate,
     * verificando che il ristorante appartenga al gestore.
     *
     * @param ristorante dati aggiornati del ristorante
     * @param idGestore identificativo del gestore proprietario
     * @throws DatiMancantiException se i dati o le tipologie di cucina sono assenti o non validi
     * @throws OperazioneNonConsentitaException se il ristorante o il gestore non sono validi
     *         oppure il ristorante non appartiene al gestore
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void modificaRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws DatiMancantiException, OperazioneNonConsentitaException, SistemaIndisponibileException {
        if (ristorante == null || idGestore == null || ristorante.idRistorante() == null) {
            throw new OperazioneNonConsentitaException("Accesso negato: ristorante o gestore non valido.");
        }
        validaDatiModifica(ristorante);

        try {
            List<String> tipologieDisponibili = ristoranteDAO.ottieniTutteTipologieCucina();
            if (ristorante.tipologieCucina().stream()
                    .anyMatch(tipo -> tipo == null || tipologieDisponibili.stream()
                            .noneMatch(disponibile -> disponibile.equalsIgnoreCase(tipo.trim())))) {
                throw new DatiMancantiException("Seleziona tipologie di cucina presenti nel catalogo.");
            }
            if (!ristoranteDAO.modificaRistorante(ristorante, idGestore)) {
                throw new OperazioneNonConsentitaException(
                        "Il ristorante non esiste o non appartiene al gestore corrente.");
            }
            subscriptions.pubblica(
                    ristorante.idRistorante(), idGestore, DataChangedEvent.ChangeType.RISTORANTE_MODIFICATO);
        } catch (SQLException e) {
            throw erroreDatabase("modifica ristorante", e);
        }
    }

    private void validaDatiModifica(RistoranteDTO ristorante) throws DatiMancantiException {
        if (ristorante.nome() == null || ristorante.nome().isBlank()
                || ristorante.indirizzo() == null || ristorante.indirizzo().isBlank()
                || ristorante.citta() == null || ristorante.citta().isBlank()
                || ristorante.nazione() == null || ristorante.nazione().isBlank()
                || ristorante.prezzoMedio() == null || !Double.isFinite(ristorante.prezzoMedio())
                || ristorante.prezzoMedio() < 0
                || ristorante.latitudine() == null || !Double.isFinite(ristorante.latitudine())
                || ristorante.latitudine() < -90 || ristorante.latitudine() > 90
                || ristorante.longitudine() == null || !Double.isFinite(ristorante.longitudine())
                || ristorante.longitudine() < -180 || ristorante.longitudine() > 180) {
            throw new DatiMancantiException("Dati del ristorante mancanti o non validi.");
        }
        if (ristorante.tipologieCucina() == null || ristorante.tipologieCucina().isEmpty()) {
            throw new DatiMancantiException("È necessario specificare almeno una tipologia di cucina.");
        }
    }

    /**
     * Recupera i dati di un ristorante tramite identificativo.
     *
     * @param idRistorante identificativo del ristorante
     * @return dettaglio del ristorante
     * @throws RistoranteNonTrovatoException se l'identificativo è nullo o il ristorante non esiste
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public RistoranteDTO ottieniDettaglio(Integer idRistorante)
            throws RistoranteNonTrovatoException, SistemaIndisponibileException {
        if (idRistorante == null) {
            throw new RistoranteNonTrovatoException(idRistorante);
        }

        try {
            RistoranteDTO ristorante = ristoranteDAO.trovaPerId(idRistorante);
            if (ristorante == null) {
                throw new RistoranteNonTrovatoException(idRistorante);
            }
            return ristorante;
        } catch (SQLException e) {
            throw erroreDatabase("ricerca ristorante per ID", e);
        }
    }

    /**
     * Restituisce tutte le tipologie di cucina presenti nel catalogo.
     *
     * @return elenco delle tipologie di cucina
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public List<String> ottieniTutteTipologieCucina() throws SistemaIndisponibileException {
        try {
            return ristoranteDAO.ottieniTutteTipologieCucina();
        } catch (SQLException e) {
            throw erroreDatabase("caricamento tipologie cucina", e);
        }
    }

    /**
     * Recupera i ristoranti associati a un gestore.
     *
     * @param idGestore identificativo del gestore
     * @return ristoranti del gestore, oppure lista vuota se l'identificativo è nullo
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public List<RistoranteDTO> ottieniRistorantiGestore(Integer idGestore) throws SistemaIndisponibileException {
        if (idGestore == null) {
            return List.of();
        }
        try {
            return ristoranteDAO.trovaPerGestore(idGestore);
        } catch (SQLException e) {
            throw erroreDatabase("ricerca ristoranti del gestore", e);
        }
    }

    /**
     * Cerca ristoranti applicando i filtri indicati.
     *
     * @param filtri filtri di ricerca, oppure {@code null} per non applicare filtri
     * @return ristoranti corrispondenti ai criteri
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public List<RistoranteDTO> cercaRistorante(FiltriRicercaDTO filtri) throws SistemaIndisponibileException {
        try {
            return ristoranteDAO.cercaConFiltri(filtri);
        } catch (SQLException e) {
            throw erroreDatabase("ricerca ristoranti", e);
        }
    }

    /**
     * Esegue una ricerca paginata verificando la presenza di località e coordinate geografiche.
     *
     * @param richiesta criteri di ricerca e parametri di paginazione
     * @return pagina dei ristoranti corrispondenti
     * @throws IllegalArgumentException se la richiesta è nulla o mancano località, coordinate o raggio
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public PaginaRistorantiDTO cercaRistorantePaginato(RicercaRistorantiPaginaDTO richiesta)
            throws SistemaIndisponibileException {
        if (richiesta == null) {
            throw new IllegalArgumentException("La richiesta di ricerca è obbligatoria.");
        }
        if (richiesta.localita().isBlank()
                || richiesta.latitudineRiferimento() == null
                || richiesta.longitudineRiferimento() == null
                || richiesta.raggioKm() == null) {
            throw new IllegalArgumentException(
                    "Per la ricerca paginata sono obbligatorie località, coordinate e raggio.");
        }
        try {
            return ristoranteDAO.cercaPagina(richiesta);
        } catch (SQLException e) {
            throw erroreDatabase("ricerca paginata ristoranti", e);
        }
    }

    /**
     * Restituisce il riepilogo delle recensioni di un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return valutazione media e numero totale di recensioni
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public RiepilogoRecensioniDTO visualizzaRiepilogo(Integer idRistorante) throws SistemaIndisponibileException {
        if (idRistorante == null) {
            throw new IllegalArgumentException("L'identificativo del ristorante è obbligatorio.");
        }
        try {
            return ristoranteDAO.trovaRiepilogo(idRistorante);
        } catch (SQLException e) {
            throw erroreDatabase("riepilogo recensioni del ristorante", e);
        }
    }

    private SistemaIndisponibileException erroreDatabase(String operation, SQLException cause) {        System.err.println("[DAO_ERROR] Errore durante " + operation + ": " + cause.getMessage());
        return new SistemaIndisponibileException(cause);
    }


    /**
     * Elimina il ristorante se appartiene al gestore e pubblica l'evento di modifica.
     *
     * @param idRistorante identificativo del ristorante
     * @param idGestore identificativo del gestore proprietario
     * @throws RistoranteNonTrovatoException se il ristorante non esiste o non è associato al gestore
     * @throws OperazioneNonConsentitaException se manca l'identificativo del ristorante o del gestore
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public void eliminaRistorante(Integer idRistorante, Integer idGestore)
            throws RistoranteNonTrovatoException,
            OperazioneNonConsentitaException,
            SistemaIndisponibileException {

        if (idRistorante == null || idGestore == null) {
            throw new OperazioneNonConsentitaException(
                    "Accesso negato: identificativo ristorante o gestore mancante.");
        }

        try {
            if (!ristoranteDAO.eliminaRistorante(idRistorante, idGestore)) {
                throw new RistoranteNonTrovatoException(idRistorante);
            }

            subscriptions.pubblica(
                    idRistorante,
                    idGestore,
                    DataChangedEvent.ChangeType.RISTORANTE_MODIFICATO);

        } catch (SQLException e) {
            throw erroreDatabase("eliminazione ristorante", e);
        }
    }

}