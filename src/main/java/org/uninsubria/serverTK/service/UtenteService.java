package org.uninsubria.serverTK.service;

import org.mindrot.jbcrypt.BCrypt;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.CredenzialiErrateException;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.common.exceptions.UtenteGiaEsistenteException;
import org.uninsubria.serverTK.dao.UtenteDAO;
import org.uninsubria.serverTK.dao.UtenteDAO.UtenteConHash;

public class UtenteService {

    private final UtenteDAO utenteDAO = new UtenteDAO();

    // Work Factor di BCrypt: bilanciamento ottimale tra sicurezza e performance
    private static final int BCRYPT_WORK_FACTOR = 12;

    public UtenteDTO autentica(String email, String passwordInChiaro)
            throws CredenzialiErrateException, SistemaIndisponibileException {

        if (email == null || passwordInChiaro == null) {
            throw new CredenzialiErrateException("Email e password sono obbligatori.");
        }

        UtenteConHash authData = utenteDAO.trovaPerEmail(email);

        if (authData == null) {
            throw new CredenzialiErrateException("Credenziali non valide. Riprova.");
        }

        // Verifica crittografica sicura tramite BCrypt (confronta password in chiaro e hash salvato)
        boolean passwordCorretta = BCrypt.checkpw(passwordInChiaro, authData.passwordHash());

        if (!passwordCorretta) {
            throw new CredenzialiErrateException("Credenziali non valide. Riprova.");
        }

        // Restituisce il DTO pulito, senza mai esporre l'hash o la password
        return authData.utente();
    }

    public UtenteDTO registra(UtenteDTO nuovoUtente, String passwordInChiaro)
            throws UtenteGiaEsistenteException, DatiMancantiException, SistemaIndisponibileException {

        if (nuovoUtente.email() == null || nuovoUtente.email().trim().isEmpty() ||
                passwordInChiaro == null || passwordInChiaro.trim().isEmpty() ||
                nuovoUtente.nome() == null || nuovoUtente.cognome() == null ||
                nuovoUtente.ruolo() == null) {
            throw new DatiMancantiException("Tutti i campi sono obbligatori per la registrazione.");
        }

        if (utenteDAO.trovaPerEmail(nuovoUtente.email()) != null) {
            throw new UtenteGiaEsistenteException("Esiste già un account associato a questa email.");
        }

        // Generazione dell'hash sicuro con BCrypt
        String hashSicuro = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt(BCRYPT_WORK_FACTOR));

        boolean successo = utenteDAO.inserisciUtente(nuovoUtente, hashSicuro);

        if (!successo) {
            throw new SistemaIndisponibileException("Errore interno durante il salvataggio. Riprova più tardi.");
        }

        return utenteDAO.trovaPerEmail(nuovoUtente.email()).utente();
    }
}