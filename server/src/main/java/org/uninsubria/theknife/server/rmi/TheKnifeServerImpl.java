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
import java.util.List;
import java.util.UUID;

public class TheKnifeServerImpl implements ITheKnifeServer, AutoCloseable {

    private final RemoteSubscriptionManager subscriptions = new RemoteSubscriptionManager();
    private final UtenteService utenteService = new UtenteService();
    private final RistoranteService ristoranteService = new RistoranteService(subscriptions);
    private final RecensioneService recensioneService = new RecensioneService(subscriptions);
    private final PreferitiService preferitiService = new PreferitiService(subscriptions);

    @Override
    public UUID subscribe(SubscriptionTopic topic, Integer targetId, RemoteObserver observer)
            throws RemoteException, DatiMancantiException {
        if (topic == null || observer == null
                || (topic == SubscriptionTopic.CATALOG && targetId != null)
                || (topic != SubscriptionTopic.CATALOG && (targetId == null || targetId <= 0))) {
            throw new DatiMancantiException("Argomenti di sottoscrizione non validi.");
        }
        return subscriptions.subscribe(topic, targetId, observer);
    }

    @Override
    public void unsubscribe(UUID subscriptionId) {
        subscriptions.unsubscribe(subscriptionId);
    }

    @Override
    public void close() {
        subscriptions.close();
    }

    @Override
    public UtenteDTO eseguiLogin(String email, String password)
            throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException {
        return utenteService.autentica(email, password);
    }

    @Override
    public UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException {
        return utenteService.registra(nuovoUtente, passwordInChiaro);
    }

    @Override
    public List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.cercaRistoranti(filtri);
    }

    @Override
    public PaginaRistorantiDTO cercaRistorantiPagina(RicercaRistorantiPaginaDTO richiesta)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.cercaRistorantiPagina(richiesta);
    }

    @Override
    public List<String> ottieniTutteTipologieCucina()
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.ottieniTutteTipologieCucina();
    }

    @Override
    public RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException {
        return ristoranteService.ottieniDettaglio(idRistorante);
    }

    @Override
    public List<RistoranteDTO> getRistorantiGestore(Integer idGestore)
            throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.ottieniRistorantiGestore(idGestore);
    }

    @Override
    public void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        ristoranteService.aggiungiRistorante(ristorante, idGestore);
    }

    @Override
    public void modificaRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        ristoranteService.modificaRistorante(ristorante, idGestore);
    }

    @Override
    public List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerRistorante(idRistorante);
    }

    @Override
    public List<RecensioneDTO> getRecensioniRistoranteAnonimo(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerRistoranteAnonimo(idRistorante);
    }

    @Override
    public List<RecensioneDTO> getRecensioniUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerUtente(idUtente);
    }

    @Override
    public void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
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
    public void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        // Corretta la chiamata aggiungendo l'idGestore come parametro
        recensioneService.aggiungiRisposta(idRecensione, idGestore, testoRisposta);
    }

    @Override
    public List<RistoranteDTO> getPreferitiUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException {
        return preferitiService.getPreferiti(idUtente);
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
}