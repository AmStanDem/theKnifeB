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
     * @param ambito ambito degli eventi a cui è sottoscritto l'osservatore
     * @param idDestinazione identificativo dell'entità interessata, se previsto dall'ambito
     * @param osservatore osservatore remoto da notificare
     */
    private record Sottoscrizione(
            UUID id,
            SubscriptionTopic ambito,
            Integer idDestinazione,
            RemoteObserver osservatore
    ) {}

    private final ConcurrentMap<UUID, Sottoscrizione> sottoscrizioni = new ConcurrentHashMap<>();
    private final ExecutorService notifiche = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Registra un osservatore per il tipo di eventi e l'entità specificati.
     *
     * @param ambito ambito degli eventi a cui sottoscriversi
     * @param idDestinazione identificativo dell'entità interessata, oppure {@code null}
     * @param osservatore osservatore remoto da notificare
     * @return identificativo univoco della sottoscrizione
     */
    public UUID sottoscrivi(SubscriptionTopic ambito, Integer idDestinazione, RemoteObserver osservatore) {
        UUID id = UUID.randomUUID();
        sottoscrizioni.put(id, new Sottoscrizione(id, ambito, idDestinazione, osservatore));
        return id;
    }

    /**
     * Annulla la sottoscrizione indicata; un identificativo {@code null} viene ignorato.
     *
     * @param idSottoscrizione identificativo della sottoscrizione da rimuovere
     */
    public void annullaSottoscrizione(UUID idSottoscrizione) {
        if (idSottoscrizione != null) {
            sottoscrizioni.remove(idSottoscrizione);
        }
    }

    /**
     * Pubblica un evento di modifica e accoda una notifica per ogni sottoscrizione corrispondente.
     *
     * @param idRistorante identificativo del ristorante coinvolto, oppure {@code null}
     * @param idUtente identificativo dell'utente coinvolto, oppure {@code null}
     * @param tipo tipo di modifica pubblicata
     */
    public void pubblica(
            Integer idRistorante,
            Integer idUtente,
            DataChangedEvent.ChangeType tipo
    ) {
        DataChangedEvent evento = new DataChangedEvent(
                null, UUID.randomUUID(), idRistorante, tipo, Instant.now());

        for (Sottoscrizione sottoscrizione : sottoscrizioni.values()) {
            if (!corrisponde(sottoscrizione, idRistorante, idUtente, tipo)) {
                continue;
            }
            try {
                notifiche.execute(() -> notificaOsservatore(sottoscrizione, evento));
            } catch (java.util.concurrent.RejectedExecutionException e) {
                System.err.println("[RMI_CALLBACK] Notifica non accodata: executor in chiusura.");
                return;
            }
        }
    }

    private boolean corrisponde(
            Sottoscrizione sottoscrizione,
            Integer idRistorante,
            Integer idUtente,
            DataChangedEvent.ChangeType tipo
    ) {
        return switch (sottoscrizione.ambito()) {
            case CATALOGO -> idRistorante != null
                    && tipo != DataChangedEvent.ChangeType.PREFERITI_MODIFICATI;
            case RISTORANTE -> idRistorante != null
                    && idRistorante.equals(sottoscrizione.idDestinazione());
            case UTENTE -> idUtente != null && idUtente.equals(sottoscrizione.idDestinazione());
        };
    }

    private void notificaOsservatore(Sottoscrizione sottoscrizione, DataChangedEvent evento) {
        if (!sottoscrizioni.containsKey(sottoscrizione.id())) {
            return;
        }
        try {
            sottoscrizione.osservatore().datiCambiati(evento.conIdSottoscrizione(sottoscrizione.id()));
        } catch (RemoteException e) {
            sottoscrizioni.remove(sottoscrizione.id(), sottoscrizione);
            System.err.println("[RMI_CALLBACK] Sottoscrizione " + sottoscrizione.id()
                    + " rimossa dopo disconnessione del client: " + e.getMessage());
        }
    }

    /**
     * Rimuove tutte le sottoscrizioni e arresta l'esecutore delle notifiche.
     */
    @Override
    public void close() {
        sottoscrizioni.clear();
        notifiche.shutdownNow();
    }
}
