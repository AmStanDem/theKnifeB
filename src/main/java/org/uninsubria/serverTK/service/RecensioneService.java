package org.uninsubria.serverTK.service;

import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.exceptions.*;
import org.uninsubria.serverTK.dao.RecensioneDAO;

import java.sql.SQLException;
import java.util.List;

public class RecensioneService {

    private final RecensioneDAO recensioneDAO = new RecensioneDAO();

    public List<RecensioneDTO> ottieniPerRistorante(Integer idRistorante) {
        if (idRistorante == null) {
            return List.of();
        }
        return recensioneDAO.trovaPerRistorante(idRistorante);
    }

    /**
     * Recupera le recensioni di un ristorante oscurando l'identità dell'autore.
     * Utilizzato per la visualizzazione da parte degli utenti non loggati (Guest).
     */
    public List<RecensioneDTO> ottieniPerRistoranteAnonimo(Integer idRistorante) {
        if (idRistorante == null) {
            return List.of();
        }

        List<RecensioneDTO> recensioniReali = recensioneDAO.trovaPerRistorante(idRistorante);

        // Trasforma la lista oscurando il campo del nome autore
        return recensioniReali.stream()
                .map(r -> new RecensioneDTO(
                        r.idRecensione(),
                        r.valutazione(),
                        r.testo(),
                        "",
                        r.dataCreazione(),
                        r.rispostaGestore(),
                        r.idRistorante(),
                        r.nomeRistorante()
                ))
                .toList();
    }

    public List<RecensioneDTO> ottieniPerUtente(Integer idUtente) throws SistemaIndisponibileException {
        if (idUtente == null) {
            throw new SistemaIndisponibileException();
        }
        try {
            return recensioneDAO.trovaPerUtente(idUtente);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero recensioni utente: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    public void aggiungiRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws DatiMancantiException, OperazioneNonConsentitaException,
            AzioneDuplicataException, SistemaIndisponibileException {

        if (recensione == null ||
                recensione.testo() == null || recensione.testo().trim().isEmpty() ||
                recensione.valutazione() == null || recensione.valutazione() < 1 || recensione.valutazione() > 5 ||
                idRistorante == null || idUtente == null) {
            throw new DatiMancantiException("Ristorante, utente, testo e valutazione da 1 a 5 sono obbligatori.");
        }

        try {
            if (!recensioneDAO.utenteCliente(idUtente)) {
                throw new OperazioneNonConsentitaException("Solo un cliente registrato può inserire una recensione.");
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore verifica ruolo per inserimento recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }

        boolean successo = recensioneDAO.inserisciRecensione(idUtente, idRistorante, recensione);
        if (!successo) {
            throw new AzioneDuplicataException("Hai già recensito questo ristorante o si è verificato un errore.");
        }
    }

    public void modifica(Integer idRecensione, Integer idUtente, String nuovoTesto, Integer nuoveStelle)
            throws DatiMancantiException, RecensioneNonTrovataException, OperazioneNonConsentitaException {

        if (nuovoTesto == null || nuovoTesto.trim().isEmpty()) {
            throw new DatiMancantiException("Il testo della recensione non può essere vuoto.");
        }
        if (nuoveStelle == null || nuoveStelle < 1 || nuoveStelle > 5) {
            throw new OperazioneNonConsentitaException("La valutazione deve essere compresa tra 1 e 5.");
        }

        boolean successo = recensioneDAO.modificaRecensione(idRecensione, idUtente, nuovoTesto, nuoveStelle);
        if (!successo) {
            throw new RecensioneNonTrovataException("Impossibile modificare: recensione inesistente o non di tua proprietà.");
        }
    }

    public void elimina(Integer idRecensione, Integer idUtente) throws RecensioneNonTrovataException {
        boolean successo = recensioneDAO.eliminaRecensione(idRecensione, idUtente);
        if (!successo) {
            throw new RecensioneNonTrovataException("Impossibile eliminare: recensione inesistente o non di tua proprietà.");
        }
    }

    public void aggiungiRisposta(Integer idRecensione, Integer idGestore, String testoRisposta)
            throws DatiMancantiException, OperazioneNonConsentitaException, SistemaIndisponibileException {

        if (idRecensione == null || idGestore == null
                || testoRisposta == null || testoRisposta.trim().isEmpty()) {
            throw new DatiMancantiException("Recensione, gestore e testo della risposta sono obbligatori.");
        }

        try {
            boolean successo = recensioneDAO.aggiungiRispostaGestore(
                    idRecensione, idGestore, testoRisposta.trim());
            if (!successo) {
                throw new OperazioneNonConsentitaException(
                        "solo il gestore del ristorante può rispondere e ogni recensione può avere una sola risposta.");
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore inserimento risposta recensione: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }
}