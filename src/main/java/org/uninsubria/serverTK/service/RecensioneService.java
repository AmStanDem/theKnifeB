package org.uninsubria.serverTK.service;

import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.exceptions.*;
import org.uninsubria.serverTK.dao.RecensioneDAO;

import java.util.List;

public class RecensioneService {

    private final RecensioneDAO recensioneDAO = new RecensioneDAO();

    public List<RecensioneDTO> ottieniPerRistorante(Integer idRistorante) {
        if (idRistorante == null) {
            return List.of();
        }
        return recensioneDAO.trovaPerRistorante(idRistorante);
    }

    public void aggiungiRecensione(RecensioneDTO recensione, Integer idRistorante, Integer idUtente)
            throws DatiMancantiException, AzioneDuplicataException {

        if (recensione == null ||
                recensione.testo() == null || recensione.testo().trim().isEmpty() ||
                recensione.valutazione() == null || recensione.valutazione() < 1 || recensione.valutazione() > 5) {
            //throw new DatiMancantiException("Il testo non può essere vuoto e la valutazione deve essere compresa tra 1 e 5.");
        }

        boolean successo = recensioneDAO.inserisciRecensione(idUtente, idRistorante, recensione);
        if (!successo) {
            //throw new AzioneDuplicataException("Hai già recensito questo ristorante o si è verificato un errore.");
        }
    }

    public void modifica(Integer idRecensione, Integer idUtente, String nuovoTesto, Integer nuoveStelle)
            throws DatiMancantiException, RecensioneNonTrovataException, OperazioneNonConsentitaException {

        if (nuovoTesto == null || nuovoTesto.trim().isEmpty()) {
            //throw new DatiMancantiException("Il testo della recensione non può essere vuoto.");
        }
        if (nuoveStelle == null || nuoveStelle < 1 || nuoveStelle > 5) {
            //throw new OperazioneNonConsentitaException("La valutazione deve essere compresa tra 1 e 5.");
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

    public void aggiungiRisposta(Integer idRecensione, String testoRisposta)
            throws DatiMancantiException, OperazioneNonConsentitaException {

        if (testoRisposta == null || testoRisposta.trim().isEmpty()) {
            //throw new DatiMancantiException("Il testo della risposta non può essere vuoto.");
        }

        boolean successo = recensioneDAO.aggiungiRispostaGestore(idRecensione, testoRisposta);
        if (!successo) {
            throw new OperazioneNonConsentitaException("Impossibile rispondere: potresti non essere il gestore del ristorante o esiste già una risposta.");
        }
    }
}