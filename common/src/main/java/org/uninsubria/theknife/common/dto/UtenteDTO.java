/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Rappresentazione dei dati anagrafici e del ruolo di un utente.
 *
 * @param idUtente identificativo univoco dell'utente
 * @param nome nome dell'utente
 * @param cognome cognome dell'utente
 * @param email indirizzo email dell'utente
 * @param dataNascita data di nascita dell'utente
 * @param domicilio domicilio dell'utente
 * @param ruolo ruolo assegnato all'utente
 */
public record UtenteDTO(
        Integer idUtente,
        String nome,
        String cognome,
        String email,
        LocalDate dataNascita,
        String domicilio,
        RuoloUtente ruolo
) implements Serializable {
    /**
     * Ruoli disponibili per gli utenti della piattaforma.
     */
    public enum RuoloUtente {
        GUEST,
        CLIENTE,
        GESTORE
    }

    @Serial
    private static final long serialVersionUID = 1L;
}