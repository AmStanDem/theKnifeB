/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.rmi;

import org.uninsubria.theknife.common.rmi.ITheKnifeServer;
import org.uninsubria.theknife.common.dto.*;
import org.uninsubria.theknife.common.exceptions.*;
import org.uninsubria.theknife.common.rmi.RemoteObserver;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;
import org.uninsubria.theknife.server.service.*;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Implementazione RMI del contratto {@link ITheKnifeServer}.
 * Delega le operazioni ai servizi applicativi e gestisce il ciclo di vita
 * delle sottoscrizioni agli eventi remoti.
 */
public class TheKnifeServerImpl implements ITheKnifeServer, AutoCloseable {

    private final RemoteSubscriptionManager subscriptions = new RemoteSubscriptionManager();
    private final UtenteService utenteService = new UtenteService(subscriptions);
    private final RistoranteService ristoranteService = new RistoranteService(subscriptions);
    private final RecensioneService recensioneService = new RecensioneService(subscriptions);
    private final PreferitiService preferitiService = new PreferitiService(subscriptions);

    @Override
    public UUID sottoscrivi(SubscriptionTopic ambito, Integer idDestinazione, RemoteObserver osservatore)
            throws RemoteException, DatiMancantiException {
        if (ambito == null || osservatore == null
                || (ambito == SubscriptionTopic.CATALOGO && idDestinazione != null)
                || (ambito != SubscriptionTopic.CATALOGO && (idDestinazione == null || idDestinazione <= 0))) {
            throw new DatiMancantiException("Argomenti di sottoscrizione non validi.");
        }
        return subscriptions.sottoscrivi(ambito, idDestinazione, osservatore);
    }

    @Override
    public void annullaSottoscrizione(UUID idSottoscrizione) {
        subscriptions.annullaSottoscrizione(idSottoscrizione);
    }

    /**
     * Arresta il gestore delle sottoscrizioni e le notifiche asincrone.
     */
    @Override
    public void close() {
        subscriptions.close();
    }

    @Override
    public UtenteDTO accedi(String email, String password)
            throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException {
        return utenteService.autentica(email, password);
    }

    @Override
    public UtenteDTO registrazione(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException {
        return utenteService.registra(nuovoUtente, passwordInChiaro);
    }

    @Override
    public UtenteDTO aggiornaAccount(UtenteDTO utenteAggiornato, String passwordCorrente, String nuovaPassword)
            throws RemoteException, CredenzialiErrateException, UtenteGiaEsistenteException,
            DatiMancantiException, SistemaIndisponibileException {
        return utenteService.aggiornaAccount(utenteAggiornato, passwordCorrente, nuovaPassword);
    }

    @Override
    public LocalDate ottieniDataCorrente() throws RemoteException, SistemaIndisponibileException {
        return utenteService.ottieniDataCorrente();
    }

    @Override
    public List<RistoranteDTO> cercaRistorante(FiltriRicercaDTO filtri)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.cercaRistorante(filtri);
    }

    @Override
    public PaginaRistorantiDTO cercaRistorantePaginato(RicercaRistorantiPaginaDTO richiesta)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.cercaRistorantePaginato(richiesta);
    }

    @Override
    public List<String> ottieniTutteTipologieCucina()
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.ottieniTutteTipologieCucina();
    }

    @Override
    public RistoranteDTO visualizzaRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException {
        return ristoranteService.ottieniDettaglio(idRistorante);
    }

    @Override
    public List<RistoranteDTO> ottieniRistorantiGestore(Integer idGestore)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.ottieniRistorantiGestore(idGestore);
    }

    @Override
    public void aggiungiRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        ristoranteService.aggiungiRistorante(ristorante, idGestore);
    }

    @Override
    public void modificaRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        ristoranteService.modificaRistorante(ristorante, idGestore);
    }

    @Override
    public List<RecensioneDTO> visualizzaRecensioni(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerRistorante(idRistorante);
    }

    @Override
    public List<RecensioneDTO> visualizzaRecensioniAnonimo(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerRistoranteAnonimo(idRistorante);
    }

    @Override
    public List<RecensioneDTO> visualizzaRecensioniUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerUtente(idUtente);
    }

    @Override
    public RiepilogoRecensioniDTO visualizzaRiepilogo(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.visualizzaRiepilogo(idRistorante);
    }

    @Override
    public void aggiungiRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, AzioneDuplicataException, SistemaIndisponibileException {
        recensioneService.aggiungiRecensione(recensione, idRistorante, idUtente);
    }

    @Override
    public void modificaRecensione(Integer idRecensione, Integer idUtente, String nuovoTesto,
                                   Double nuoveStelle, Integer versioneAttesa)
            throws RemoteException, RecensioneNonTrovataException, ModificaObsoletaException,
            OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        recensioneService.modifica(idRecensione, idUtente, nuovoTesto, nuoveStelle, versioneAttesa);
    }

    @Override
    public void eliminaRecensione(Integer idRecensione, Integer idUtente)
            throws RemoteException, RecensioneNonTrovataException, SistemaIndisponibileException {
        recensioneService.elimina(idRecensione, idUtente);
    }

    @Override
    public void rispostaRecensioni(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        // Corretta la chiamata aggiungendo l'idGestore come parametro
        recensioneService.aggiungiRisposta(idRecensione, idGestore, testoRisposta);
    }

    @Override
    public List<RistoranteDTO> visualizzaPreferiti(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException {
        return preferitiService.ottieniPreferiti(idUtente);
    }

    @Override
    public void aggiungiPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, AzioneDuplicataException, SistemaIndisponibileException {
        preferitiService.aggiungi(idUtente, idRistorante);
    }

    @Override
    public void rimuoviPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException {
        preferitiService.rimuovi(idUtente, idRistorante);
    }

    /**
     * Elimina un ristorante se il gestore è autorizzato a rimuoverlo.
     *
     * @param idRistorante identificativo del ristorante da eliminare
     * @param idGestore identificativo del gestore che richiede l'eliminazione
     * @throws RemoteException se si verifica un errore di comunicazione RMI
     * @throws OperazioneNonConsentitaException se il gestore non è autorizzato
     * @throws RistoranteNonTrovatoException se il ristorante non esiste
     * @throws SistemaIndisponibileException se il servizio non è disponibile
     */
    @Override
    public void eliminaRistorante(Integer idRistorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException,
            RistoranteNonTrovatoException, SistemaIndisponibileException {
        ristoranteService.eliminaRistorante(idRistorante, idGestore);
    }
}