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
 * @param subscriptionId identificativo della sottoscrizione interessata
 * @param eventId identificativo univoco dell'evento
 * @param restaurantId identificativo del ristorante coinvolto
 * @param type tipo di modifica avvenuta
 * @param occurredAt istante in cui si è verificata la modifica
 */
public record DataChangedEvent(
        UUID subscriptionId,
        UUID eventId,
        Integer restaurantId,
        ChangeType type,
        Instant occurredAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Tipi di modifica notificabili ai sottoscrittori.
     */
    public enum ChangeType {
        RESTAURANT_CHANGED,
        REVIEW_CREATED,
        REVIEW_UPDATED,
        REVIEW_DELETED,
        REVIEW_REPLY_CHANGED,
        FAVORITES_CHANGED
    }

    /**
     * Crea una copia dell'evento associata alla sottoscrizione indicata.
     *
     * @param id nuovo identificativo della sottoscrizione
     * @return una copia con il nuovo identificativo di sottoscrizione
     */
    public DataChangedEvent withSubscriptionId(UUID id) {
        return new DataChangedEvent(id, eventId, restaurantId, type, occurredAt);
    }
}
