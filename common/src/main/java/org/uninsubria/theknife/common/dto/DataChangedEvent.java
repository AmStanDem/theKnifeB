/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento pubblicato quando i dati associati a una sottoscrizione cambiano.
 *
 * @param idSottoscrizione identificativo della sottoscrizione interessata
 * @param idEvento identificativo univoco dell'evento
 * @param idRistorante identificativo del ristorante coinvolto, oppure {@code null} per eventi non riferiti a un ristorante
 * @param tipo tipo di modifica avvenuta
 * @param istanteOccorrenza istante in cui si è verificata la modifica
 */
public record DataChangedEvent(
        UUID idSottoscrizione,
        UUID idEvento,
        Integer idRistorante,
        ChangeType tipo,
        Instant istanteOccorrenza
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Tipi di modifica notificabili ai sottoscrittori.
     */
    public enum ChangeType {
        /** Un ristorante è stato aggiunto, modificato o eliminato. */
        RISTORANTE_MODIFICATO,
        /** È stata creata una recensione. */
        RECENSIONE_CREATA,
        /** Sono stati modificati testo o valutazione di una recensione. */
        RECENSIONE_MODIFICATA,
        /** È stata eliminata una recensione. */
        RECENSIONE_ELIMINATA,
        /** È stata aggiunta una risposta del gestore a una recensione. */
        RISPOSTA_RECENSIONE_MODIFICATA,
        /** È cambiata l'associazione tra un utente e uno dei suoi ristoranti preferiti. */
        PREFERITI_MODIFICATI,
        /** Sono stati aggiornati i dati o le credenziali di un utente. */
        UTENTE_MODIFICATO
    }

    /**
     * Crea una copia dell'evento associata alla sottoscrizione indicata.
     *
     * @param id nuovo identificativo della sottoscrizione
     * @return una copia con il nuovo identificativo di sottoscrizione
     */
    public DataChangedEvent conIdSottoscrizione(UUID id) {
        return new DataChangedEvent(id, idEvento, idRistorante, tipo, istanteOccorrenza);
    }
}
