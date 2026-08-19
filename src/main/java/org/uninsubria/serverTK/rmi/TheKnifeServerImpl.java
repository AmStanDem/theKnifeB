package org.uninsubria.serverTK.rmi;

import org.uninsubria.common.rmi.ITheKnifeServer;
import org.uninsubria.common.dto.*;
import org.uninsubria.common.exceptions.*;
import org.uninsubria.serverTK.service.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class TheKnifeServerImpl extends UnicastRemoteObject implements ITheKnifeServer {

    private final UtenteService utenteService = new UtenteService();
    private final RistoranteService ristoranteService = new RistoranteService();
    private final RecensioneService recensioneService = new RecensioneService();
    private final PreferitiService preferitiService = new PreferitiService();

    public TheKnifeServerImpl() throws RemoteException {
        super();
    }

    @Override
    public UtenteDTO eseguiLogin(String email, String password) throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException {
        return utenteService.autentica(email, password);
    }

    @Override
    public UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro) throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException {
        return utenteService.registra(nuovoUtente, passwordInChiaro);
    }

    @Override
    public List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri) throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.cercaRistoranti(filtri);
    }

    @Override
    public RistoranteDTO getDettaglioRistorante(Integer idRistorante) throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException {
        return ristoranteService.ottieniDettaglio(idRistorante);
    }

    @Override
    public List<RistoranteDTO> getRistorantiGestore(Integer idGestore) throws RemoteException, SistemaIndisponibileException {
        return ristoranteService.ottieniRistorantiGestore(idGestore);
    }

    @Override
    public void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore) throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        ristoranteService.aggiungiRistorante(ristorante, idGestore);
    }

    @Override
    public List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante) throws RemoteException, SistemaIndisponibileException {
        return recensioneService.ottieniPerRistorante(idRistorante);
    }

    @Override
    public void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente) throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, AzioneDuplicataException, SistemaIndisponibileException {
        recensioneService.aggiungiRecensione(recensione, idRistorante, idUtente);
    }

    @Override
    public void modificaRecensione(Integer idRecensione, Integer idUtente, String nuovoTesto, Integer nuoveStelle) throws RemoteException, RecensioneNonTrovataException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        recensioneService.modifica(idRecensione, idUtente, nuovoTesto, nuoveStelle);
    }

    @Override
    public void eliminaRecensione(Integer idRecensione, Integer idUtente) throws RemoteException, RecensioneNonTrovataException, OperazioneNonConsentitaException, SistemaIndisponibileException {
        recensioneService.elimina(idRecensione, idUtente);
    }

    @Override
    public void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore) throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException {
        recensioneService.aggiungiRisposta(idRecensione, testoRisposta);
    }

    @Override
    public List<RistoranteDTO> getPreferitiUtente(Integer idUtente) throws RemoteException, SistemaIndisponibileException {
        return preferitiService.getPreferiti(idUtente);
    }

    @Override
    public void aggiungiPreferito(Integer idUtente, Integer idRistorante) throws RemoteException, AzioneDuplicataException, SistemaIndisponibileException {
        preferitiService.aggiungi(idUtente, idRistorante);
    }

    @Override
    public void rimuoviPreferito(Integer idUtente, Integer idRistorante) throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException {
        preferitiService.rimuovi(idUtente, idRistorante);
    }
}