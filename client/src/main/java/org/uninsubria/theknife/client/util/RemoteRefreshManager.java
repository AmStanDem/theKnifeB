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

/**
 * Gestisce le sottoscrizioni RMI del client e inoltra gli eventi ricevuti alle callback JavaFX.
 */
public final class RemoteRefreshManager {

    private static volatile RemoteRefreshManager istanza;

    private static final class Sottoscrizione {
        private final UUID idLocale = UUID.randomUUID();
        private final Object proprietario;
        private final SubscriptionTopic ambito;
        private final Integer idDestinazione;
        private final Consumer<DataChangedEvent> ascoltatore;
        private final Runnable allaSottoscrizione;
        private volatile UUID idRemoto;
        private volatile boolean chiusa;

        private Sottoscrizione(
                Object proprietario,
                SubscriptionTopic ambito,
                Integer idDestinazione,
                Consumer<DataChangedEvent> ascoltatore,
                Runnable allaSottoscrizione
        ) {
            this.proprietario = proprietario;
            this.ambito = ambito;
            this.idDestinazione = idDestinazione;
            this.ascoltatore = ascoltatore;
            this.allaSottoscrizione = allaSottoscrizione;
        }
    }

    private static final class OsservatoreClient extends UnicastRemoteObject implements RemoteObserver {
        private OsservatoreClient() throws RemoteException {
            super(0);
        }

        @Override
        public void datiCambiati(DataChangedEvent evento) throws RemoteException {
            ottieniIstanza().distribuisci(evento);
        }
    }

    private final ConcurrentMap<UUID, Sottoscrizione> sottoscrizioni = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Sottoscrizione> sottoscrizioniPerIdRemoto = new ConcurrentHashMap<>();
    private final ExecutorService esecutoreSfondo = Executors.newVirtualThreadPerTaskExecutor();
    private final RemoteObserver osservatore;

    /**
     * Impedisce l'istanziazione diretta del gestore singleton.
     */
    private RemoteRefreshManager() {
        try {
            osservatore = new OsservatoreClient();
        } catch (RemoteException e) {
            throw new ExceptionInInitializerError(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::chiudi, "theknife-client-callback-arresta"));
    }

    /**
     * Sottoscrive un proprietario agli eventi del server.
     *
     * @param proprietario oggetto proprietario della sottoscrizione, usato per annullarla in gruppo
     * @param ambito tipo di evento a cui sottoscriversi
     * @param idDestinazione identificativo dell'entità interessata, oppure {@code null}
     * @param ascoltatore callback invocata quando viene ricevuta una notifica
     * @param allaSottoscrizione callback invocata quando la sottoscrizione remota è stata completata
     * @return identificativo locale della sottoscrizione
     * @throws IllegalArgumentException se proprietario, argomento o callback obbligatori sono {@code null}
     */
    public static UUID sottoscrivi(
            Object proprietario,
            SubscriptionTopic ambito,
            Integer idDestinazione,
            Consumer<DataChangedEvent> ascoltatore,
            Runnable allaSottoscrizione
    ) {
        return ottieniIstanza().aggiungiSottoscrizione(proprietario, ambito, idDestinazione, ascoltatore, allaSottoscrizione);
    }

    private UUID aggiungiSottoscrizione(
            Object proprietario,
            SubscriptionTopic ambito,
            Integer idDestinazione,
            Consumer<DataChangedEvent> ascoltatore,
            Runnable allaSottoscrizione
    ) {
        if (proprietario == null || ambito == null || ascoltatore == null || allaSottoscrizione == null) {
            throw new IllegalArgumentException("Owner, ambito, ascoltatore e callback di registrazione sono obbligatori.");
        }

        Sottoscrizione sottoscrizione = new Sottoscrizione(proprietario, ambito, idDestinazione, ascoltatore, allaSottoscrizione);
        sottoscrizioni.put(sottoscrizione.idLocale, sottoscrizione);
        esecutoreSfondo.execute(() -> registra(sottoscrizione));
        return sottoscrizione.idLocale;
    }

    private void registra(Sottoscrizione sottoscrizione) {
        try {
            UUID idRemoto = ServerConnection.ottieniServer()
                    .sottoscrivi(sottoscrizione.ambito, sottoscrizione.idDestinazione, osservatore);
            sottoscrizione.idRemoto = idRemoto;
            if (sottoscrizione.chiusa) {
                annullaSottoscrizioneRemota(idRemoto);
            } else {
                sottoscrizioniPerIdRemoto.put(idRemoto, sottoscrizione);
                try {
                    Platform.runLater(() -> {
                        if (!sottoscrizione.chiusa && sottoscrizioni.containsKey(sottoscrizione.idLocale)) {
                            sottoscrizione.allaSottoscrizione.run();
                        }
                    });
                } catch (IllegalStateException e) {
                    rimuoviSottoscrizione(sottoscrizione);
                }
            }
        } catch (RemoteException | NotBoundException | DatiMancantiException e) {
            if (!sottoscrizione.chiusa) {
                System.err.println("[RMI_CALLBACK] Sottoscrizione non riuscita: " + e.getMessage());
            }
        }
    }

    /**
     * Annulla tutte le sottoscrizioni associate al proprietario indicato.
     *
     * @param proprietario proprietario delle sottoscrizioni da annullare
     */
    public static void annullaSottoscrizioniProprietario(Object proprietario) {
        RemoteRefreshManager gestore = istanza;
        if (gestore != null) {
            gestore.rimuoviSottoscrizioni(proprietario);
        }
    }

    private static synchronized RemoteRefreshManager ottieniIstanza() {
        if (istanza == null) {
            istanza = new RemoteRefreshManager();
        }
        return istanza;
    }

    private void rimuoviSottoscrizioni(Object proprietario) {
        sottoscrizioni.values().stream()
                .filter(sottoscrizione -> sottoscrizione.proprietario == proprietario)
                .toList()
                .forEach(this::rimuoviSottoscrizione);
    }

    private void rimuoviSottoscrizione(Sottoscrizione sottoscrizione) {
        if (!sottoscrizioni.remove(sottoscrizione.idLocale, sottoscrizione)) {
            return;
        }
        sottoscrizione.chiusa = true;
        UUID idRemoto = sottoscrizione.idRemoto;
        if (idRemoto != null) {
            sottoscrizioniPerIdRemoto.remove(idRemoto, sottoscrizione);
            esecutoreSfondo.execute(() -> annullaSottoscrizioneRemota(idRemoto));
        }
    }

    private void annullaSottoscrizioneRemota(UUID idRemoto) {
        try {
            ServerConnection.ottieniServer().annullaSottoscrizione(idRemoto);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("[RMI_CALLBACK] Annullamento sottoscrizione non riuscito: " + e.getMessage());
        }
    }

    private void distribuisci(DataChangedEvent evento) throws RemoteException {
        if (evento == null || evento.idSottoscrizione() == null) {
            return;
        }
        Sottoscrizione sottoscrizione = sottoscrizioniPerIdRemoto.get(evento.idSottoscrizione());
        if (sottoscrizione == null || sottoscrizione.chiusa) {
            return;
        }
        try {
            Platform.runLater(() -> {
                if (!sottoscrizione.chiusa && sottoscrizioni.containsKey(sottoscrizione.idLocale)) {
                    sottoscrizione.ascoltatore.accept(evento);
                }
            });
        } catch (IllegalStateException e) {
            throw new RemoteException("JavaFX non è più attivo per ricevere notifiche.", e);
        }
    }

    /**
     * Arresta il gestore e rilascia le risorse RMI e i thread usati per le sottoscrizioni.
     */
    public static void arresta() {
        RemoteRefreshManager gestore = istanza;
        if (gestore != null) {
            gestore.chiudi();
        }
    }

    private void chiudi() {
        sottoscrizioni.values().forEach(sottoscrizione -> sottoscrizione.chiusa = true);
        sottoscrizioni.clear();
        sottoscrizioniPerIdRemoto.clear();
        esecutoreSfondo.shutdownNow();
        try {
            UnicastRemoteObject.unexportObject((java.rmi.Remote) osservatore, true);
        } catch (java.rmi.NoSuchObjectException ignored) {
            // The callback may already have been unexported during JVM arresta.
        }
    }
}
