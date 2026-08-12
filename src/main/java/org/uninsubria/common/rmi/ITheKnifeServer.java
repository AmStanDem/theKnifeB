package org.uninsubria.common.rmi;

import org.uninsubria.common.dto.*;
import org.uninsubria.common.exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contratto RMI principale (Facade) che espone tutti i casi d'uso del sistema The Knife.
 * Ogni metodo dichiara la RemoteException per gestire i guasti di rete e
 * specifiche eccezioni di dominio per comunicare le violazioni della logica di business.
 */
public interface ITheKnifeServer extends Remote {

    UtenteDTO eseguiLogin(String email, String password)
            throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException;

    UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException;

    List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri)
            throws RemoteException, SistemaIndisponibileException;

    RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException;

    List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException;

    void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, AzioneDuplicataException, SistemaIndisponibileException;

    void aggiungiPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, AzioneDuplicataException, SistemaIndisponibileException;

    void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;
}