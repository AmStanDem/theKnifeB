package org.uninsubria.serverTK.service;

import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.PaginaRistorantiDTO;
import org.uninsubria.common.dto.RicercaRistorantiPaginaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.serverTK.dao.RistoranteDAO;

import java.sql.SQLException;
import java.util.List;

public class RistoranteService {

    private final RistoranteDAO ristoranteDAO = new RistoranteDAO();

    public void aggiungiRistorante(RistoranteDTO ristorante, Integer idGestore)
            throws DatiMancantiException, OperazioneNonConsentitaException, SistemaIndisponibileException {

        if (ristorante == null || idGestore == null) {
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

        try {
            boolean successo = ristoranteDAO.inserisciRistorante(ristorante, idGestore);
            if (!successo) {
                throw new SistemaIndisponibileException();
            }
        } catch (SQLException e) {
            throw databaseError("inserimento ristorante", e);
        }
    }

    public RistoranteDTO ottieniDettaglio(Integer idRistorante)
            throws RistoranteNonTrovatoException, SistemaIndisponibileException {
        if (idRistorante == null) {
            throw new RistoranteNonTrovatoException(idRistorante);
        }

        try {
            RistoranteDTO ristorante = ristoranteDAO.trovaPerId(idRistorante);
            if (ristorante == null) {
                throw new RistoranteNonTrovatoException(idRistorante);
            }
            return ristorante;
        } catch (SQLException e) {
            throw databaseError("ricerca ristorante per ID", e);
        }
    }

    public List<String> ottieniTutteTipologieCucina() throws SistemaIndisponibileException {
        try {
            return ristoranteDAO.ottieniTutteTipologieCucina();
        } catch (SQLException e) {
            throw databaseError("caricamento tipologie cucina", e);
        }
    }

    public List<RistoranteDTO> ottieniRistorantiGestore(Integer idGestore) throws SistemaIndisponibileException {
        if (idGestore == null) {
            return List.of();
        }
        try {
            return ristoranteDAO.trovaPerGestore(idGestore);
        } catch (SQLException e) {
            throw databaseError("ricerca ristoranti del gestore", e);
        }
    }

    public List<RistoranteDTO> cercaRistoranti(FiltriRicercaDTO filtri) throws SistemaIndisponibileException {
        try {
            return ristoranteDAO.cercaConFiltri(filtri);
        } catch (SQLException e) {
            throw databaseError("ricerca ristoranti", e);
        }
    }

    public PaginaRistorantiDTO cercaRistorantiPagina(RicercaRistorantiPaginaDTO richiesta)
            throws SistemaIndisponibileException {
        if (richiesta == null) {
            throw new IllegalArgumentException("La richiesta di ricerca è obbligatoria.");
        }
        try {
            return ristoranteDAO.cercaPagina(richiesta);
        } catch (SQLException e) {
            throw databaseError("ricerca paginata ristoranti", e);
        }
    }

    private SistemaIndisponibileException databaseError(String operation, SQLException cause) {
        System.err.println("[DAO_ERROR] Errore durante " + operation + ": " + cause.getMessage());
        return new SistemaIndisponibileException(cause);
    }
}