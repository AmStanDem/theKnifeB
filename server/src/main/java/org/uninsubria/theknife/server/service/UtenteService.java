/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.mindrot.jbcrypt.BCrypt;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.CredenzialiErrateException;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.UtenteGiaEsistenteException;
import org.uninsubria.theknife.server.dao.UtenteDAO;
import org.uninsubria.theknife.server.dao.UtenteDAO.UtenteConHash;
import org.uninsubria.theknife.server.rmi.RemoteSubscriptionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gestisce autenticazione, registrazione e aggiornamento degli account utente,
 * applicando le validazioni e la protezione delle password.
 */
public class UtenteService {

    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final RemoteSubscriptionManager subscriptions;

    /**
     * Crea il servizio usando il gestore delle sottoscrizioni per notificare
     * le modifiche agli account.
     *
     * @param subscriptions gestore delle sottoscrizioni remote
     */
    public UtenteService(RemoteSubscriptionManager subscriptions) {
        this.subscriptions = subscriptions;
    }

    // Work Factor di BCrypt: bilanciamento ottimale tra sicurezza e performance
    private static final int BCRYPT_WORK_FACTOR = 12;
    private static final LocalDate DATA_NASCITA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /**
     * Autentica un utente verificando la password con l'hash memorizzato.
     *
     * @param email indirizzo email dell'utente
     * @param passwordInChiaro password fornita dall'utente
     * @return dati dell'utente autenticato, senza hash della password
     * @throws CredenzialiErrateException se email o password non sono valide
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public UtenteDTO autentica(String email, String passwordInChiaro)
            throws CredenzialiErrateException, SistemaIndisponibileException {

        if (email == null || passwordInChiaro == null) {
            throw new CredenzialiErrateException();
        }

        UtenteConHash authData;
        try {
            authData = utenteDAO.trovaPerEmail(email);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante la ricerca dell'utente per email: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }

        if (authData == null) {
            throw new CredenzialiErrateException();
        }

        // Verifica crittografica sicura tramite BCrypt (confronta password in chiaro e hash salvato)
        boolean passwordCorretta = BCrypt.checkpw(passwordInChiaro, authData.passwordHash());

        if (!passwordCorretta) {
            throw new CredenzialiErrateException();
        }

        // Restituisce il DTO pulito, senza mai esporre l'hash o la password
        return authData.utente();
    }

    /**
     * Recupera la data corrente dal database.
     *
     * @return data corrente
     * @throws SistemaIndisponibileException se non è possibile recuperare la data
     */
    public LocalDate ottieniDataCorrente() throws SistemaIndisponibileException {
        try {
            return utenteDAO.dataCorrente();
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante il recupero della data corrente: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Registra un nuovo utente dopo aver validato i dati e memorizza la password
     * sotto forma di hash BCrypt.
     *
     * @param nuovoUtente dati dell'utente da registrare
     * @param passwordInChiaro password scelta dall'utente
     * @return dati dell'utente registrato, senza hash della password
     * @throws UtenteGiaEsistenteException se l'indirizzo email è già registrato
     * @throws DatiMancantiException se i dati obbligatori o la data di nascita non sono validi
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public UtenteDTO registra(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException {

        if (nuovoUtente.email() == null || nuovoUtente.email().trim().isEmpty() ||
                passwordInChiaro == null || passwordInChiaro.trim().isEmpty() ||
                nuovoUtente.nome() == null || nuovoUtente.nome().trim().isEmpty() ||
                nuovoUtente.cognome() == null || nuovoUtente.cognome().trim().isEmpty() ||
                nuovoUtente.domicilio() == null || nuovoUtente.domicilio().trim().isEmpty() ||
                nuovoUtente.ruolo() == null) {
            throw new DatiMancantiException("Tutti i campi (incluso il domicilio) sono obbligatori per la registrazione.");
        }

        LocalDate dataCorrente = ottieniDataCorrente();

        if (nuovoUtente.dataNascita() != null) {
            if (nuovoUtente.dataNascita().isAfter(dataCorrente)) {
                throw new DatiMancantiException("La data di nascita non può essere nel futuro.");
            }
            if (nuovoUtente.dataNascita().isBefore(DATA_NASCITA_MINIMA)) {
                throw new DatiMancantiException(
                        "La data di nascita non può essere precedente al "
                                + DATA_NASCITA_MINIMA.format(FORMATO_DATA)
                                + ".");
            }
        }

        // Generazione dell'hash sicuro con BCrypt
        String hashSicuro = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt(BCRYPT_WORK_FACTOR));

        boolean successo;
        try {
            successo = utenteDAO.inserisciUtente(nuovoUtente, hashSicuro);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante l'inserimento dell'utente: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }

        if (!successo) {
            throw new UtenteGiaEsistenteException("Esiste già un account associato a questa email.");
        }

        try {
            UtenteConHash registrato = utenteDAO.trovaPerEmail(nuovoUtente.email());
            if (registrato == null) {
                throw new SistemaIndisponibileException();
            }
            return registrato.utente();
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante il recupero dell'utente registrato: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    /**
     * Aggiorna i dati dell'account dopo aver verificato identità e password corrente.
     * La password viene modificata solo se ne viene fornita una nuova valida.
     *
     * @param aggiornato dati aggiornati dell'utente
     * @param passwordCorrente password attualmente associata all'account
     * @param nuovaPassword nuova password, oppure {@code null} o vuota per mantenerla invariata
     * @return dati aggiornati dell'utente
     * @throws CredenzialiErrateException se l'account, il ruolo o la password corrente non sono validi
     * @throws UtenteGiaEsistenteException se l'indirizzo email è già associato a un altro account
     * @throws DatiMancantiException se mancano dati obbligatori o la nuova password non rispetta i requisiti
     * @throws SistemaIndisponibileException se si verifica un errore durante l'accesso al database
     */
    public UtenteDTO aggiornaAccount(UtenteDTO aggiornato, String passwordCorrente, String nuovaPassword)
            throws CredenzialiErrateException, UtenteGiaEsistenteException,
            DatiMancantiException, SistemaIndisponibileException {
        if (aggiornato == null || aggiornato.idUtente() == null
                || aggiornato.nome() == null || aggiornato.nome().isBlank()
                || aggiornato.cognome() == null || aggiornato.cognome().isBlank()
                || aggiornato.email() == null || aggiornato.email().isBlank()
                || aggiornato.domicilio() == null || aggiornato.domicilio().isBlank()
                || aggiornato.ruolo() == null || aggiornato.ruolo() == UtenteDTO.RuoloUtente.GUEST
                || passwordCorrente == null || passwordCorrente.isBlank()) {
            throw new DatiMancantiException("Compila tutti i campi obbligatori e inserisci la password attuale.");
        }
        LocalDate dataCorrente = ottieniDataCorrente();
        if (aggiornato.dataNascita() != null
                && (aggiornato.dataNascita().isAfter(dataCorrente)
                || aggiornato.dataNascita().isBefore(DATA_NASCITA_MINIMA))) {
            throw new DatiMancantiException("La data di nascita inserita non è valida.");
        }

        String email = aggiornato.email().trim();
        String nuovaPasswordNormalizzata = nuovaPassword == null ? "" : nuovaPassword;
        if (!nuovaPasswordNormalizzata.isEmpty()
                && (nuovaPasswordNormalizzata.length() < 8 || nuovaPasswordNormalizzata.length() > 64
                || nuovaPasswordNormalizzata.contains(" ")
                || !nuovaPasswordNormalizzata.matches(".*[a-z].*")
                || !nuovaPasswordNormalizzata.matches(".*[A-Z].*")
                || !nuovaPasswordNormalizzata.matches(".*\\d.*")
                || !nuovaPasswordNormalizzata.matches(".*[^A-Za-z0-9].*"))) {
            throw new DatiMancantiException("La password non soddisfa i requisiti di sicurezza.");
        }

        UtenteConHash account;
        try {
            account = utenteDAO.trovaPerId(aggiornato.idUtente());
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore durante il recupero dell'account: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
        if (account == null || account.utente().ruolo() != aggiornato.ruolo()) {
            throw new CredenzialiErrateException();
        }
        if (!BCrypt.checkpw(passwordCorrente, account.passwordHash())) {
            throw new CredenzialiErrateException();
        }

        UtenteDTO datiAggiornati = new UtenteDTO(
                aggiornato.idUtente(),
                aggiornato.nome().trim(),
                aggiornato.cognome().trim(),
                email,
                aggiornato.dataNascita(),
                aggiornato.domicilio().trim(),
                account.utente().ruolo()
        );
        String hashNuovo = nuovaPasswordNormalizzata.isEmpty()
                ? account.passwordHash()
                : BCrypt.hashpw(nuovaPasswordNormalizzata, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
        try {
            if (!utenteDAO.aggiornaUtente(datiAggiornati, account.passwordHash(), hashNuovo)) {
                throw new CredenzialiErrateException();
            }
            subscriptions.pubblica(null, datiAggiornati.idUtente(), DataChangedEvent.ChangeType.UTENTE_MODIFICATO);
            return datiAggiornati;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new UtenteGiaEsistenteException(email);
            }
            System.err.println("[DAO_ERROR] Errore durante l'aggiornamento dell'account: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }
}