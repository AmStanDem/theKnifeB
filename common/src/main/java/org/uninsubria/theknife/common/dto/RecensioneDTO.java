/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresentazione di una recensione pubblicata da un utente.
 *
 * @param idRecensione identificativo univoco della recensione
 * @param valutazione punteggio assegnato dall'autore
 * @param testo contenuto testuale della recensione
 * @param nomeAutore nome dell'autore della recensione
 * @param dataCreazione data e ora di pubblicazione
 * @param rispostaGestore risposta del gestore del ristorante, oppure {@code null}
 * @param idRistorante identificativo del ristorante recensito, se disponibile
 * @param nomeRistorante nome del ristorante recensito, se disponibile
 * @param versione versione del dato della recensione
 */
public record RecensioneDTO(
        Integer idRecensione,
        Double valutazione,
        String testo,
        String nomeAutore,         // Denormalizzato dalla tabella Utenti
        LocalDateTime dataCreazione,
        String rispostaGestore,    // Null se il gestore non ha ancora risposto
        Integer idRistorante,
        String nomeRistorante,
        Integer versione
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Crea una recensione senza informazioni sul ristorante.
     *
     * @param idRecensione identificativo univoco della recensione
     * @param valutazione punteggio assegnato dall'autore
     * @param testo contenuto testuale della recensione
     * @param nomeAutore nome dell'autore della recensione
     * @param dataCreazione data e ora di pubblicazione
     * @param rispostaGestore risposta del gestore, oppure {@code null}
     */
    public RecensioneDTO(
            Integer idRecensione,
            Double valutazione,
            String testo,
            String nomeAutore,
            LocalDateTime dataCreazione,
            String rispostaGestore
    ) {
        this(idRecensione, valutazione, testo, nomeAutore, dataCreazione,
                rispostaGestore, null, null);
    }

    /**
     * Crea una recensione associata alle informazioni disponibili sul ristorante.
     *
     * @param idRecensione identificativo univoco della recensione
     * @param valutazione punteggio assegnato dall'autore
     * @param testo contenuto testuale della recensione
     * @param nomeAutore nome dell'autore della recensione
     * @param dataCreazione data e ora di pubblicazione
     * @param rispostaGestore risposta del gestore, oppure {@code null}
     * @param idRistorante identificativo del ristorante recensito
     * @param nomeRistorante nome del ristorante recensito
     */
    public RecensioneDTO(
            Integer idRecensione,
            Double valutazione,
            String testo,
            String nomeAutore,
            LocalDateTime dataCreazione,
            String rispostaGestore,
            Integer idRistorante,
            String nomeRistorante
    ) {
        this(idRecensione, valutazione, testo, nomeAutore, dataCreazione,
                rispostaGestore, idRistorante, nomeRistorante, 0);
    }
}