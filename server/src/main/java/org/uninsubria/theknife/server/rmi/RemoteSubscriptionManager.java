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

public final class RemoteSubscriptionManager implements AutoCloseable {

    private record Subscription(
            UUID id,
            SubscriptionTopic topic,
            Integer targetId,
            RemoteObserver observer
    ) {}

    private final ConcurrentMap<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService callbacks = Executors.newVirtualThreadPerTaskExecutor();

    public UUID subscribe(SubscriptionTopic topic, Integer targetId, RemoteObserver observer) {
        UUID id = UUID.randomUUID();
        subscriptions.put(id, new Subscription(id, topic, targetId, observer));
        return id;
    }

    public void unsubscribe(UUID subscriptionId) {
        if (subscriptionId != null) {
            subscriptions.remove(subscriptionId);
        }
    }

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

    @Override
    public void close() {
        subscriptions.clear();
        callbacks.shutdownNow();
    }
}
