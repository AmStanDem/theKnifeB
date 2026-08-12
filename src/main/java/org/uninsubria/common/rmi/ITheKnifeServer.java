package org.uninsubria.common.rmi;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contratto RMI principale (Facade) che espone tutti i casi d'uso del sistema The Knife.
 * Ogni metodo deve dichiarare la RemoteException per gestire i guasti di rete.
 */
public interface ITheKnifeServer extends Remote {

    // ==========================================
    // AREA AUTENTICAZIONE
    // ==========================================

    UtenteDTO eseguiLogin(String email, String password)
            throws RemoteException, CredenzialiErrateException;

    UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException;

    // ==========================================
    // AREA RISTORANTI (Consultazione)
    // ==========================================

    List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri)
            throws RemoteException;

    RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException;

    // ==========================================
    // AREA RECENSIONI E INTERAZIONI
    // ==========================================

    List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException;

    void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException;

    void aggiungiPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException;

    // ==========================================
    // AREA GESTIONE (Solo per i Gestori)
    // ==========================================

    void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException;

    void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException;
}

