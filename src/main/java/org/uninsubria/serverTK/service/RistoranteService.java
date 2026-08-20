package org.uninsubria.serverTK.service;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.serverTK.dao.RistoranteDAO;

import java.util.List;

public class RistoranteService {

    private final RistoranteDAO ristoranteDAO = new RistoranteDAO();

    public void aggiungiRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws DatiMancantiException, OperazioneNonConsentitaException {

        if (idGestore == null) {
            throw new OperazioneNonConsentitaException("Accesso negato: identificativo gestore mancante.");
        }

        if (ristorante.nome() == null || ristorante.nome().trim().isEmpty() ||
                ristorante.indirizzo() == null || ristorante.indirizzo().trim().isEmpty() ||
                ristorante.citta() == null || ristorante.citta().trim().isEmpty() ||
                ristorante.nazione() == null || ristorante.nazione().trim().isEmpty() ||
                ristorante.prezzoMedio() == null || ristorante.prezzoMedio() < 0.0) {
            throw new DatiMancantiException("Campi obbligatori mancanti o prezzo medio non valido.");
        }

        // Validazione della relazione Molti-a-Molti introdotta
        if (ristorante.tipologieCucina() == null || ristorante.tipologieCucina().isEmpty()) {
            throw new DatiMancantiException("È necessario specificare almeno una tipologia di cucina.");
        }

        boolean successo = ristoranteDAO.inserisciRistorante(ristorante, idGestore);
        if (!successo) {
            throw new RuntimeException("Impossibile registrare il ristorante nel database.");
        }
    }

    public RistoranteDTO ottieniDettaglio(Integer idRistorante) throws RistoranteNonTrovatoException {
        if (idRistorante == null) {
            throw new RistoranteNonTrovatoException(idRistorante);
        }

        RistoranteDTO ristorante = ristoranteDAO.trovaPerId(idRistorante);
        if (ristorante == null) {
            throw new RistoranteNonTrovatoException(idRistorante);
        }
        return ristorante;
    }

    public List<RistoranteDTO> ottieniRistorantiGestore(Integer idGestore) {
        return ristoranteDAO.trovaPerGestore(idGestore);
    }

    public List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri) {
        if (filtri == null) {
            return List.of();
        }
        return ristoranteDAO.cercaConFiltri(filtri);
    }
}