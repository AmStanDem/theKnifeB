/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.service;

import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.exceptions.AzioneDuplicataException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.server.dao.PreferitiDAO;

import java.sql.SQLException;
import java.util.List;

public class PreferitiService {

    private final PreferitiDAO preferitiDAO = new PreferitiDAO();

    public List<RistoranteDTO> getPreferiti(Integer idUtente) throws SistemaIndisponibileException {
        if (idUtente == null) {
            throw new SistemaIndisponibileException();
        }
        try {
            return preferitiDAO.ottieniPreferitiUtente(idUtente);
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore recupero lista preferiti: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    public void aggiungi(Integer idUtente, Integer idRistorante)
            throws AzioneDuplicataException, SistemaIndisponibileException {
        if (idUtente == null || idRistorante == null) {
            throw new AzioneDuplicataException("Parametri non validi per l'aggiunta ai preferiti.");
        }

        try {
            boolean successo = preferitiDAO.aggiungiPreferito(idUtente, idRistorante);
            if (!successo) {
                throw new AzioneDuplicataException("Il ristorante è già presente nei tuoi preferiti o non esiste.");
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore aggiunta preferito: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }

    public void rimuovi(Integer idUtente, Integer idRistorante)
            throws RistoranteNonTrovatoException, SistemaIndisponibileException {
        if (idUtente == null || idRistorante == null) {
            throw new RistoranteNonTrovatoException();
        }

        try {
            boolean successo = preferitiDAO.rimuoviPreferito(idUtente, idRistorante);
            if (!successo) {
                throw new RistoranteNonTrovatoException(idRistorante);
            }
        } catch (SQLException e) {
            System.err.println("[DAO_ERROR] Errore rimozione preferito: " + e.getMessage());
            throw new SistemaIndisponibileException(e);
        }
    }
}