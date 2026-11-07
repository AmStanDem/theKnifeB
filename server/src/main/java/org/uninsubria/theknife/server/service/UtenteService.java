/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.mindrot.jbcrypt.BCrypt;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.CredenzialiErrateException;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.UtenteGiaEsistenteException;
import org.uninsubria.theknife.server.dao.UtenteDAO;
import org.uninsubria.theknife.server.dao.UtenteDAO.UtenteConHash;

import java.sql.SQLException;

public class UtenteService {

    private final UtenteDAO utenteDAO = new UtenteDAO();

    // Work Factor di BCrypt: bilanciamento ottimale tra sicurezza e performance
    private static final int BCRYPT_WORK_FACTOR = 12;

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
}