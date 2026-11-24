/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.server.rmi;

import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.rmi.RemoteObserver;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestisce le sottoscrizioni dei client RMI e distribuisce in modo asincrono
 * gli eventi di modifica agli osservatori interessati.
 */
public final class RemoteSubscriptionManager implements AutoCloseable {

    /**
     * Sottoscrizione registrata da un osservatore remoto.
     *
     * @param id identificativo della sottoscrizione
     * @param topic ambito degli eventi a cui è sottoscritto l'osservatore
     * @param targetId identificativo dell'entità interessata, se previsto dall'ambito
     * @param observer osservatore remoto da notificare
     */
    private record Subscription(
            UUID id,
            SubscriptionTopic topic,
            Integer targetId,
            RemoteObserver observer
    ) {}

    private final ConcurrentMap<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService callbacks = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Registra un osservatore per il tipo di eventi e l'entità specificati.
     *
     * @param topic ambito degli eventi a cui sottoscriversi
     * @param targetId identificativo dell'entità interessata, oppure {@code null}
     * @param observer osservatore remoto da notificare
     * @return identificativo univoco della sottoscrizione
     */
    public UUID subscribe(SubscriptionTopic topic, Integer targetId, RemoteObserver observer) {
        UUID id = UUID.randomUUID();
        subscriptions.put(id, new Subscription(id, topic, targetId, observer));
        return id;
    }

    /**
     * Annulla la sottoscrizione indicata; un identificativo {@code null} viene ignorato.
     *
     * @param subscriptionId identificativo della sottoscrizione da rimuovere
     */
    public void unsubscribe(UUID subscriptionId) {
        if (subscriptionId != null) {
            subscriptions.remove(subscriptionId);
        }
    }

    /**
     * Pubblica un evento di modifica e accoda una notifica per ogni sottoscrizione corrispondente.
     *
     * @param restaurantId identificativo del ristorante coinvolto, oppure {@code null}
     * @param userId identificativo dell'utente coinvolto, oppure {@code null}
     * @param type tipo di modifica pubblicata
     */
    public void publish(
            Integer restaurantId,
            Integer userId,
            DataChangedEvent.ChangeType type
    ) {
        DataChangedEvent event = new DataChangedEvent(
                null, UUID.randomUUID(), restaurantId, type, Instant.now());

        for (Subscription subscription : subscriptions.values()) {
            if (!matches(subscription, restaurantId, userId, type)) {
                continue;
            }
            try {
                callbacks.execute(() -> notifyObserver(subscription, event));
            } catch (java.util.concurrent.RejectedExecutionException e) {
                System.err.println("[RMI_CALLBACK] Notifica non accodata: executor in chiusura.");
                return;
            }
        }
    }

    private boolean matches(
            Subscription subscription,
            Integer restaurantId,
            Integer userId,
            DataChangedEvent.ChangeType type
    ) {
        return switch (subscription.topic()) {
            case CATALOG -> restaurantId != null
                    && type != DataChangedEvent.ChangeType.FAVORITES_CHANGED;
            case RESTAURANT -> restaurantId != null
                    && restaurantId.equals(subscription.targetId());
            case USER -> userId != null && userId.equals(subscription.targetId());
        };
    }

    private void notifyObserver(Subscription subscription, DataChangedEvent event) {
        if (!subscriptions.containsKey(subscription.id())) {
            return;
        }
        try {
            subscription.observer().onDataChanged(event.withSubscriptionId(subscription.id()));
        } catch (RemoteException e) {
            subscriptions.remove(subscription.id(), subscription);
            System.err.println("[RMI_CALLBACK] Sottoscrizione " + subscription.id()
                    + " rimossa dopo disconnessione del client: " + e.getMessage());
        }
    }

    /**
     * Rimuove tutte le sottoscrizioni e arresta l'esecutore delle notifiche.
     */
    @Override
    public void close() {
        subscriptions.clear();
        callbacks.shutdownNow();
    }
}
