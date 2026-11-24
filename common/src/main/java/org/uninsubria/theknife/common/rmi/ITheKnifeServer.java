/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.rmi;

import org.uninsubria.theknife.common.dto.*;
import org.uninsubria.theknife.common.exceptions.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contratto RMI principale (Facade) che espone tutti i casi d'uso del sistema The Knife.
 */
public interface ITheKnifeServer extends Remote {

    /**
     * Sottoscrive un osservatore remoto a un tipo di evento.
     *
     * @param topic tipo di evento a cui sottoscriversi
     * @param targetId identificativo dell'entità interessata, oppure {@code null}
     * @param observer osservatore remoto da notificare
     * @return identificativo univoco della sottoscrizione
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws DatiMancantiException se uno dei dati necessari manca
     */
    UUID subscribe(SubscriptionTopic topic, Integer targetId, RemoteObserver observer)
            throws RemoteException, DatiMancantiException;

    /**
     * Rimuove una sottoscrizione esistente.
     *
     * @param subscriptionId identificativo della sottoscrizione
     * @throws RemoteException se la comunicazione RMI non è disponibile
     */
    void unsubscribe(UUID subscriptionId) throws RemoteException;

    // --- IDENTITÀ ---
    /**
     * Autentica un utente.
     *
     * @param email indirizzo email dell'utente
     * @param password password dell'utente
     * @return dati dell'utente autenticato
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws CredenzialiErrateException se email o password non sono valide
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    UtenteDTO eseguiLogin(String email, String password)
            throws RemoteException, CredenzialiErrateException, SistemaIndisponibileException;

    /**
     * Registra un nuovo utente cliente.
     *
     * @param nuovoUtente dati del nuovo utente
     * @param passwordInChiaro password scelta dall'utente
     * @return dati dell'utente registrato
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws UtenteGiaEsistenteException se l'email è già registrata
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    UtenteDTO registraCliente(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws RemoteException, UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException;

    /**
     * Aggiorna i dati e le credenziali di un account.
     *
     * @param utenteAggiornato dati aggiornati dell'utente
     * @param passwordCorrente password attualmente associata all'account
     * @param nuovaPassword nuova password, oppure {@code null} per mantenerla invariata
     * @return dati dell'utente aggiornato
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws CredenzialiErrateException se la password corrente non è valida
     * @throws UtenteGiaEsistenteException se la nuova email è già registrata
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    UtenteDTO aggiornaAccount(UtenteDTO utenteAggiornato, String passwordCorrente, String nuovaPassword)
            throws RemoteException, CredenzialiErrateException, UtenteGiaEsistenteException,
            DatiMancantiException, SistemaIndisponibileException;

    /**
     * Ottiene la data corrente del server.
     *
     * @return data corrente del sistema
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    LocalDate ottieniDataCorrente()
            throws RemoteException, SistemaIndisponibileException;

    // --- ESPLORAZIONE E CATALOGO ---
    /**
     * Cerca i ristoranti in base ai filtri specificati.
     *
     * @param filtri criteri della ricerca
     * @return ristoranti corrispondenti ai filtri
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Esegue una ricerca paginata dei ristoranti.
     *
     * @param richiesta criteri di ricerca e parametri di paginazione
     * @return pagina dei ristoranti trovati
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    PaginaRistorantiDTO cercaRistorantiPagina(RicercaRistorantiPaginaDTO richiesta)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Restituisce tutte le tipologie di cucina disponibili.
     *
     * @return elenco delle tipologie di cucina
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<String> ottieniTutteTipologieCucina()
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Restituisce il dettaglio di un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return dati completi del ristorante
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws RistoranteNonTrovatoException se il ristorante non esiste
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    RistoranteDTO getDettaglioRistorante(Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException;

    /**
     * Restituisce i ristoranti amministrati da un gestore.
     *
     * @param idGestore identificativo del gestore
     * @return ristoranti associati al gestore
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RistoranteDTO> getRistorantiGestore(Integer idGestore)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Registra un nuovo ristorante.
     *
     * @param ristorante dati del ristorante da registrare
     * @param idGestore identificativo del gestore che effettua la registrazione
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws OperazioneNonConsentitaException se il gestore non può registrare il ristorante
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void registraNuovoRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    /**
     * Modifica i dati di un ristorante esistente.
     *
     * @param ristorante dati aggiornati del ristorante
     * @param idGestore identificativo del gestore che modifica il ristorante
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws OperazioneNonConsentitaException se il gestore non può modificare il ristorante
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void modificaRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    // --- RECENSIONI E RISPOSTE ---
    /**
     * Restituisce tutte le recensioni associate a un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return elenco delle recensioni
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RecensioneDTO> getRecensioniRistorante(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Restituisce le recensioni di un ristorante accessibili senza autenticazione.
     *
     * @param idRistorante identificativo del ristorante
     * @return elenco delle recensioni anonime
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RecensioneDTO> getRecensioniRistoranteAnonimo(Integer idRistorante)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Restituisce tutte le recensioni pubblicate da un utente.
     *
     * @param idUtente identificativo dell'utente
     * @return elenco delle recensioni dell'utente
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RecensioneDTO> getRecensioniUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Inserisce una nuova recensione per un ristorante.
     *
     * @param recensione dati della recensione
     * @param idRistorante identificativo del ristorante recensito
     * @param idUtente identificativo dell'utente autore
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws OperazioneNonConsentitaException se l'utente non può inserire la recensione
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws AzioneDuplicataException se l'utente ha già recensito il ristorante
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void inserisciRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, AzioneDuplicataException, SistemaIndisponibileException;

    /**
     * Modifica una recensione esistente.
     *
     * @param idRecensione identificativo della recensione
     * @param idUtente identificativo dell'utente autore
     * @param nuovoTesto nuovo testo della recensione
     * @param nuoveStelle nuova valutazione della recensione
     * @param versioneAttesa versione attesa per rilevare modifiche obsolete
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws RecensioneNonTrovataException se la recensione non esiste
     * @throws ModificaObsoletaException se la versione attesa non è più valida
     * @throws OperazioneNonConsentitaException se l'utente non può modificare la recensione
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void modificaRecensione(Integer idRecensione, Integer idUtente, String nuovoTesto,
                            Double nuoveStelle, Integer versioneAttesa)
            throws RemoteException, RecensioneNonTrovataException, ModificaObsoletaException,
            OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    /**
     * Elimina una recensione esistente.
     *
     * @param idRecensione identificativo della recensione
     * @param idUtente identificativo dell'utente autore
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws RecensioneNonTrovataException se la recensione non esiste
     * @throws OperazioneNonConsentitaException se l'utente non può eliminare la recensione
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void eliminaRecensione(Integer idRecensione, Integer idUtente)
            throws RemoteException, RecensioneNonTrovataException, OperazioneNonConsentitaException, SistemaIndisponibileException;

    /**
     * Pubblica la risposta del gestore a una recensione.
     *
     * @param idRecensione identificativo della recensione
     * @param testoRisposta testo della risposta del gestore
     * @param idGestore identificativo del gestore
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws OperazioneNonConsentitaException se il gestore non può rispondere alla recensione
     * @throws DatiMancantiException se uno dei dati obbligatori manca
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void rispondiARecensione(Integer idRecensione, String testoRisposta, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException, DatiMancantiException, SistemaIndisponibileException;

    // --- PREFERITI ---
    /**
     * Restituisce i ristoranti preferiti di un utente.
     *
     * @param idUtente identificativo dell'utente
     * @return elenco dei ristoranti preferiti
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    List<RistoranteDTO> getPreferitiUtente(Integer idUtente)
            throws RemoteException, SistemaIndisponibileException;

    /**
     * Aggiunge un ristorante ai preferiti di un utente.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws AzioneDuplicataException se il ristorante è già tra i preferiti
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void aggiungiPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, AzioneDuplicataException, SistemaIndisponibileException;

    /**
     * Rimuove un ristorante dai preferiti di un utente.
     *
     * @param idUtente identificativo dell'utente
     * @param idRistorante identificativo del ristorante
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws RistoranteNonTrovatoException se il ristorante non esiste
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void rimuoviPreferito(Integer idUtente, Integer idRistorante)
            throws RemoteException, RistoranteNonTrovatoException, SistemaIndisponibileException;


    /**
     * Elimina un ristorante esistente.
     *
     * @param idRistorante identificativo del ristorante da eliminare
     * @param idGestore identificativo del gestore che richiede l'eliminazione
     * @throws RemoteException se la comunicazione RMI non è disponibile
     * @throws OperazioneNonConsentitaException se il gestore non può eliminare il ristorante
     * @throws RistoranteNonTrovatoException se il ristorante non esiste
     * @throws SistemaIndisponibileException se il sistema non è disponibile
     */
    void eliminaRistorante(Integer idRistorante, Integer idGestore)
            throws RemoteException, OperazioneNonConsentitaException,
            RistoranteNonTrovatoException, SistemaIndisponibileException;

}