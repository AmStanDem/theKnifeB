/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Controller JavaFX della lista dei ristoranti gestiti da un ristoratore.
 * <p>
 * Verifica che l'utente autenticato abbia il ruolo di gestore, recupera i
 * ristoranti associati al suo account tramite RMI e crea una card per ogni
 * ristorante utilizzando la relativa vista FXML.
 * </p>
 * <p>
 * La vista permette di aprire i dettagli e accedere alla modifica di un
 * ristorante, oltre a tornare al profilo o alla schermata di inserimento.
 * </p>
 */
public class ListaRistorantiGestoreController {

    private static final String VISTA_PROFILO =
            "/org/uninsubria/theknife/client/views/AreaPersonale.fxml";

    @FXML
    private Label resultsCountLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox restaurantContainer;

    /**
     * Inizializza la lista dei ristoranti del gestore autenticato.
     * <p>
     * Verifica il ruolo dell'utente corrente. Se l'accesso non è valido,
     * mostra uno stato vuoto e un avviso; altrimenti carica i ristoranti e
     * attiva l'aggiornamento automatico del catalogo.
     * </p>
     */
    @FXML
    private void initialize() {
        UtenteDTO gestore = SessioneUtente.getUtenteCorrente();
        if (gestore == null || !SessioneUtente.isGestore()) {
            emptyStateLabel.setText("Accedi come ristoratore per visualizzare i tuoi ristoranti.");
            showError("Accesso non consentito", "Questa pagina è riservata ai ristoratori.");
            return;
        }

        caricaRistoranti(gestore);
        RemoteRefreshManager.subscribe(
                this, SubscriptionTopic.CATALOG, null, event -> caricaRistoranti(gestore),
                () -> caricaRistoranti(gestore));
    }

    /**
     * Recupera dal server i ristoranti associati al gestore e aggiorna la vista.
     * <p>
     * Le card vengono configurate per aprire i dettagli o la schermata di
     * modifica. Se non sono presenti ristoranti, viene mostrato lo stato vuoto.
     * </p>
     *
     * @param gestore gestore autenticato proprietario dei ristoranti
     */
    private void caricaRistoranti(UtenteDTO gestore) {
        try {
            List<RistoranteDTO> ristoranti =
                    ServerConnection.getServer().getRistorantiGestore(gestore.idUtente());
            restaurantContainer.getChildren().clear();
            resultsCountLabel.setText(ristoranti.size()
                    + (ristoranti.size() == 1 ? " ristorante" : " ristoranti"));

            if (ristoranti.isEmpty()) {
                emptyStateLabel.setText("Non hai ancora aggiunto ristoranti.");
                emptyStateLabel.setVisible(true);
                emptyStateLabel.setManaged(true);
                return;
            }

            emptyStateLabel.setVisible(false);
            emptyStateLabel.setManaged(false);
            for (RistoranteDTO ristorante : ristoranti) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/org/uninsubria/theknife/client/views/RestaurantItem.fxml"));
                Node card = loader.load();
                RestaurantItemController controller = loader.getController();
                controller.setRistorante(ristorante);
                controller.setOnDettagliAction(ristoranteSelezionato ->
                        RistoranteSelezionato.setRitornoAiMieiRistoranti(true));
                controller.setOnModificaAction((event, ristoranteSelezionato) -> {
                    RistoranteSelezionato.setRistorante(ristoranteSelezionato);
                    SceneManager.switchScene(event,
                            "/org/uninsubria/theknife/client/views/ModificaRistorante.fxml");
                });
                restaurantContainer.getChildren().add(card);
            }
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            resultsCountLabel.setText("Impossibile caricare i ristoranti.");
            showError("Errore di connessione",
                    "Non è stato possibile recuperare i ristoranti associati al tuo account.");
        } catch (IOException e) {
            resultsCountLabel.setText("Impossibile visualizzare i ristoranti.");
            showError("Errore di visualizzazione",
                    "Non è stato possibile caricare la scheda di un ristorante.");
        }
    }

    /**
     * Apre l'area personale dell'utente.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onProfiloClick(ActionEvent event) {
        SceneManager.switchScene(event, VISTA_PROFILO);
    }

    /**
     * Apre la schermata di inserimento di un nuovo ristorante.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/AggiungiRistorante.fxml");
    }

    /**
     * Visualizza una finestra di errore configurabile.
     *
     * @param titolo titolo della finestra
     * @param messaggio descrizione dell'errore da mostrare
     */
    private void showError(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
