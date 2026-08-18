package org.uninsubria.common.rmi;

import org.uninsubria.common.dto.*;
import org.uninsubria.common.exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contratto RMI principale (Facade) che espone tutti i casi d'uso del sistema The Knife.
 */
public interface ITheKnifeServer extends Remote {

    // --- IDENTITÀ ---
    UtenteDTO eseguiLogin(String email, String password)
            throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException;

    UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException;

    // --- ESPLORAZIONE E CATALOGO ---
    List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri)
            throws RemoteException, SistemaIndisponibileException;

    RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException;

    // Metodo AGGIUNTO per la Dashboard Gestore
    List<RistoranteDTO> getRistorantiGestore(Integer idGestore)
            throws RemoteException, SistemaIndisponibileException;

    void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    // --- RECENSIONI E RISPOSTE ---
    List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException;

    void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, AzioneDuplicataException, SistemaIndisponibileException;

    // Metodo AGGIUNTO per Area Cliente
    void modificaRecensione(Integer idRecensione, Integer idUtente, String nuovoTesto, Integer nuoveStelle)
            throws RemoteException, RecensioneNonTrovataException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    // Metodo AGGIUNTO per Area Cliente
    void eliminaRecensione(Integer idRecensione, Integer idUtente)
            throws RemoteException, RecensioneNonTrovataException, OperazioneNonConsentitaException, SistemaIndisponibileException;

    void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    // --- PREFERITI ---
    // Metodo AGGIUNTO per Area Cliente
    List<RistoranteDTO> getPreferitiUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException;

    void aggiungiPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, AzioneDuplicataException, SistemaIndisponibileException;

    // Metodo AGGIUNTO per Area Cliente
    void rimuoviPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException;
}