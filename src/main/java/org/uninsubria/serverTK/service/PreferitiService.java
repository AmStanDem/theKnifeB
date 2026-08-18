package org.uninsubria.serverTK.service;

import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.exceptions.AzioneDuplicataException;
import org.uninsubria.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.serverTK.dao.PreferitiDAO;

import java.util.List;

public class PreferitiService {

    private final PreferitiDAO preferitiDAO = new PreferitiDAO();

    public List<RistoranteDTO> getPreferiti(Integer idUtente) {
        if (idUtente == null) {
            return List.of();
        }
        return preferitiDAO.ottieniPreferitiUtente(idUtente);
    }

    public void aggiungi(Integer idUtente, Integer idRistorante) throws AzioneDuplicataException {
        if (idUtente == null || idRistorante == null) {
            throw new AzioneDuplicataException("Parametri non validi per l'aggiunta ai preferiti.");
        }

        boolean successo = preferitiDAO.aggiungiPreferito(idUtente, idRistorante);
        if (!successo) {
            throw new AzioneDuplicataException("Il ristorante è già presente nei tuoi preferiti o non esiste.");
        }
    }

    public void rimuovi(Integer idUtente, Integer idRistorante) throws RistoranteNonTrovatoException {
        if (idUtente == null || idRistorante == null) {
            throw new RistoranteNonTrovatoException("Parametri non validi per la rimozione.");
        }

        boolean successo = preferitiDAO.rimuoviPreferito(idUtente, idRistorante);
        if (!successo) {
            throw new RistoranteNonTrovatoException("Impossibile rimuovere: il ristorante non è presente nei tuoi preferiti.");
        }
    }
}