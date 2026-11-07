/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.application.Platform;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.rmi.RemoteObserver;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class RemoteRefreshManager {

    private static final RemoteRefreshManager INSTANCE = new RemoteRefreshManager();

    private static final class Subscription {
        private final UUID localId = UUID.randomUUID();
        private final Object owner;
        private final SubscriptionTopic topic;
        private final Integer targetId;
        private final Consumer<DataChangedEvent> listener;
        private final Runnable onSubscribed;
        private volatile UUID remoteId;
        private volatile boolean closed;

        private Subscription(
                Object owner,
                SubscriptionTopic topic,
                Integer targetId,
                Consumer<DataChangedEvent> listener,
                Runnable onSubscribed
        ) {
            this.owner = owner;
            this.topic = topic;
            this.targetId = targetId;
            this.listener = listener;
            this.onSubscribed = onSubscribed;
        }
    }

    private static final class ClientObserver extends UnicastRemoteObject implements RemoteObserver {
        private ClientObserver() throws RemoteException {
            super(0);
        }

        @Override
        public void onDataChanged(DataChangedEvent event) throws RemoteException {
            INSTANCE.dispatch(event);
        }
    }

    private final ConcurrentMap<UUID, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Subscription> subscriptionsByRemoteId = new ConcurrentHashMap<>();
    private final ExecutorService background = Executors.newVirtualThreadPerTaskExecutor();
    private final RemoteObserver observer;

    private RemoteRefreshManager() {
        try {
            observer = new ClientObserver();
        } catch (RemoteException e) {
            throw new ExceptionInInitializerError(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "theknife-client-callback-shutdown"));
    }

    public static UUID subscribe(
            Object owner,
            SubscriptionTopic topic,
            Integer targetId,
            Consumer<DataChangedEvent> listener,
            Runnable onSubscribed
    ) {
        return INSTANCE.addSubscription(owner, topic, targetId, listener, onSubscribed);
    }

    private UUID addSubscription(
            Object owner,
            SubscriptionTopic topic,
            Integer targetId,
            Consumer<DataChangedEvent> listener,
            Runnable onSubscribed
    ) {
        if (owner == null || topic == null || listener == null || onSubscribed == null) {
            throw new IllegalArgumentException("Owner, topic, listener e callback di registrazione sono obbligatori.");
        }

        Subscription subscription = new Subscription(owner, topic, targetId, listener, onSubscribed);
        subscriptions.put(subscription.localId, subscription);
        background.execute(() -> register(subscription));
        return subscription.localId;
    }

    private void register(Subscription subscription) {
        try {
            UUID remoteId = ServerConnection.getServer()
                    .subscribe(subscription.topic, subscription.targetId, observer);
            subscription.remoteId = remoteId;
            if (subscription.closed) {
                unsubscribeRemotely(remoteId);
            } else {
                subscriptionsByRemoteId.put(remoteId, subscription);
                try {
                    Platform.runLater(() -> {
                        if (!subscription.closed && subscriptions.containsKey(subscription.localId)) {
                            subscription.onSubscribed.run();
                        }
                    });
                } catch (IllegalStateException e) {
                    removeSubscription(subscription);
                }
            }
        } catch (RemoteException | NotBoundException | DatiMancantiException e) {
            if (!subscription.closed) {
                System.err.println("[RMI_CALLBACK] Sottoscrizione non riuscita: " + e.getMessage());
            }
        }
    }

    public static void unsubscribeOwner(Object owner) {
        INSTANCE.removeSubscriptions(owner);
    }

    private void removeSubscriptions(Object owner) {
        subscriptions.values().stream()
                .filter(subscription -> subscription.owner == owner)
                .toList()
                .forEach(this::removeSubscription);
    }

    private void removeSubscription(Subscription subscription) {
        if (!subscriptions.remove(subscription.localId, subscription)) {
            return;
        }
        subscription.closed = true;
        UUID remoteId = subscription.remoteId;
        if (remoteId != null) {
            subscriptionsByRemoteId.remove(remoteId, subscription);
            background.execute(() -> unsubscribeRemotely(remoteId));
        }
    }

    private void unsubscribeRemotely(UUID remoteId) {
        try {
            ServerConnection.getServer().unsubscribe(remoteId);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("[RMI_CALLBACK] Annullamento sottoscrizione non riuscito: " + e.getMessage());
        }
    }

    private void dispatch(DataChangedEvent event) throws RemoteException {
        if (event == null || event.subscriptionId() == null) {
            return;
        }
        Subscription subscription = subscriptionsByRemoteId.get(event.subscriptionId());
        if (subscription == null || subscription.closed) {
            return;
        }
        try {
            Platform.runLater(() -> {
                if (!subscription.closed && subscriptions.containsKey(subscription.localId)) {
                    subscription.listener.accept(event);
                }
            });
        } catch (IllegalStateException e) {
            throw new RemoteException("JavaFX non è più attivo per ricevere notifiche.", e);
        }
    }

    private void close() {
        subscriptions.values().forEach(subscription -> subscription.closed = true);
        subscriptions.clear();
        subscriptionsByRemoteId.clear();
        background.shutdownNow();
        try {
            UnicastRemoteObject.unexportObject((java.rmi.Remote) observer, true);
        } catch (java.rmi.NoSuchObjectException ignored) {
            // The callback may already have been unexported during JVM shutdown.
        }
    }
}
