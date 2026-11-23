/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

/**
 * Controller JavaFX della lista dei ristoranti preferiti dall'utente cliente.
 * <p>
 * Recupera i preferiti dal server, crea una card per ciascun ristorante e
 * consente di rimuovere un elemento dalla lista. I dati vengono aggiornati
 * automaticamente in seguito alle modifiche dei preferiti o del catalogo.
 * </p>
 */
public class PreferitiController {

    @FXML private Label resultsCountLabel;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();

    /**
     * Inizializza la vista dei preferiti.
     * <p>
     * Carica subito l'elenco e attiva le sottoscrizioni agli aggiornamenti
     * dell'utente e del catalogo dei ristoranti.
     * </p>
     */
    @FXML
    public void initialize() {
        caricaPreferiti();
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente != null) {
            RemoteRefreshManager.subscribe(this, SubscriptionTopic.USER, utente.idUtente(), event -> {
                if (event.type() == DataChangedEvent.ChangeType.FAVORITES_CHANGED) {
                    caricaPreferiti();
                }
            }, this::caricaPreferiti);
            RemoteRefreshManager.subscribe(
                    this, SubscriptionTopic.CATALOG, null, event -> caricaPreferiti(), this::caricaPreferiti);
        }
    }

    /**
     * Recupera dal server i ristoranti preferiti dell'utente autenticato.
     * <p>
     * L'accesso richiede un cliente autenticato. In caso contrario o in caso
     * di errore di comunicazione viene mostrato un messaggio all'utente.
     * </p>
     */
    private void caricaPreferiti() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per visualizzare i tuoi ristoranti preferiti.");
            aggiornaLista(List.of());
            return;
        }

        try {
            List<RistoranteDTO> preferiti = ServerConnection.getServer().getPreferitiUtente(utente.idUtente());
            ristorantiCompleti.setAll(preferiti);
            aggiornaLista(ristorantiCompleti);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile caricare i ristoranti preferiti. Riprova più tardi.");
        }
    }

    /**
     * Ricrea la lista grafica dei ristoranti preferiti forniti.
     *
     * @param nuovaLista ristoranti da visualizzare
     */
    private void aggiornaLista(List<RistoranteDTO> nuovaLista) {
        restaurantContainer.getChildren().clear();

        for (RistoranteDTO ristorante : nuovaLista) {
            Node card = creaCardRistorante(ristorante);
            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(nuovaLista.size() + " ristoranti trovati");
        restaurantScrollPane.setVvalue(0);
    }

    /**
     * Crea la card grafica di un ristorante preferito.
     * <p>
     * Collega il pulsante di rimozione della card al metodo del controller
     * responsabile dell'aggiornamento del preferito sul server.
     * </p>
     *
     * @param ristorante ristorante da rappresentare
     * @return nodo JavaFX della card, oppure {@code null} se il caricamento fallisce
     */
    private Node creaCardRistorante(RistoranteDTO ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/uninsubria/theknife/client/views/PreferitoItem.fxml"));
            Pane cardPane = loader.load();

            PreferitoItemController controller = loader.getController();
            controller.setRistorante(ristorante);
            controller.setOnRemoveFavoriteAction(this::rimuoviPreferito);

            return cardPane;

        } catch (IOException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore",
                    "Impossibile visualizzare una scheda ristorante.");
            return null;
        }
    }

    /**
     * Rimuove un ristorante dall'elenco dei preferiti dell'utente.
     * <p>
     * Dopo l'esito positivo aggiorna immediatamente la lista locale. Se il
     * ristorante non è più disponibile ricarica i preferiti dal server.
     * </p>
     *
     * @param ristorante ristorante da rimuovere dai preferiti
     */
    private void rimuoviPreferito(RistoranteDTO ristorante) {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per modificare i preferiti.");
            return;
        }

        try {
            ServerConnection.getServer().rimuoviPreferito(utente.idUtente(), ristorante.idRistorante());
            ristorantiCompleti.removeIf(preferito ->
                    Objects.equals(preferito.idRistorante(), ristorante.idRistorante()));
            aggiornaLista(ristorantiCompleti);
        } catch (RistoranteNonTrovatoException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Preferito non trovato",
                    "Questo ristorante non risulta più presente nei tuoi preferiti.");
            caricaPreferiti();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile rimuovere il ristorante dai preferiti. Riprova più tardi.");
        }
    }

    /**
     * Visualizza una finestra di dialogo con messaggio configurabile.
     *
     * @param tipo tipo dell'avviso
     * @param titolo titolo della finestra
     * @param messaggio testo da mostrare all'utente
     */
    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}