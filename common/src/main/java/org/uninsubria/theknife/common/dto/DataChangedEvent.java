/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record DataChangedEvent(
        UUID subscriptionId,
        UUID eventId,
        Integer restaurantId,
        ChangeType type,
        Instant occurredAt
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ChangeType {
        RESTAURANT_CHANGED,
        REVIEW_CREATED,
        REVIEW_UPDATED,
        REVIEW_DELETED,
        REVIEW_REPLY_CHANGED,
        FAVORITES_CHANGED
    }

    public DataChangedEvent withSubscriptionId(UUID id) {
        return new DataChangedEvent(id, eventId, restaurantId, type, occurredAt);
    }
}
