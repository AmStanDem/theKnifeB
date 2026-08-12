package org.uninsubria.common.rmi;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.exceptions.TheKnifeException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contratto RMI principale (Facade) che espone tutti i casi d'uso del sistema The Knife.
 * Ogni metodo deve dichiarare la RemoteException per gestire i guasti di rete.
 */
public interface ITheKnifeServer extends Remote {

    public UtenteDTO login(String email, String password) throws RemoteException;
    UtenteDTO registrazione(UtenteDTO utente, String passwordInChiaro) throws RemoteException;

    List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri) throws RemoteException;

    RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException;


    List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException;
    void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException;

}
