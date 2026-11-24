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
import javafx.scene.layout.Pane;
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
 * Controller JavaFX della dashboard del gestore.
 * <p>
 * Mostra in un'unica schermata il profilo del gestore (nome, cognome, email, ruolo)
 * nella parte superiore e la lista dei ristoranti gestiti nella parte inferiore.
 * Consente di aggiungere, modificare e visualizzare i dettagli dei ristoranti,
 * nonche' di accedere al profilo utente e di effettuare il logout.
 * </p>
 */
public class DashboardGestoreController {

    private static final String VISTA_PROFILO =
            "/org/uninsubria/theknife/client/views/ProfiloView.fxml";

    @FXML
    private Label resultsCountLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox restaurantContainer;

    @FXML
    private Label lblNome;

    @FXML
    private Label lblCognome;

    @FXML
    private Label lblRuolo;

    @FXML
    private Label lblEmail;

    /**
     * Inizializza la dashboard del gestore.
     * <p>
     * Verifica il ruolo dell'utente corrente, popola i dati del profilo,
     * carica i ristoranti associati e attiva l'aggiornamento automatico
     * del catalogo.
     * </p>
     */
    @FXML
    private void initialize() {
        UtenteDTO gestore = SessioneUtente.getUtenteCorrente();
        if (gestore == null || !SessioneUtente.isGestore()) {
            emptyStateLabel.setText("Accedi come ristoratore per visualizzare i tuoi ristoranti.");
            showError("Accesso non consentito", "Questa pagina e' riservata ai ristoratori.");
            return;
        }

        lblNome.setText(gestore.nome());
        lblCognome.setText(gestore.cognome());
        lblRuolo.setText(gestore.ruolo().name());
        lblEmail.setText(gestore.email());

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
                        "/org/uninsubria/theknife/client/views/RistoranteItem.fxml"));
                Node card = loader.load();
                RistoranteItemController controller = loader.getController();
                controller.setRistorante(ristorante);
                controller.setOnDettagliAction(ristoranteSelezionato -> {
                    RistoranteSelezionato.setRitornoAiMieiRistoranti(true);
                });
                controller.setOnModificaAction((event, ristoranteSelezionato) -> {
                    RistoranteSelezionato.setRistorante(ristoranteSelezionato);
                    SceneManager.switchScene(event,
                            "/org/uninsubria/theknife/client/views/FormRistorante.fxml");
                });
                restaurantContainer.getChildren().add(card);
            }
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            resultsCountLabel.setText("Impossibile caricare i ristoranti.");
            showError("Errore di connessione",
                    "Non e' stato possibile recuperare i ristoranti associati al tuo account.");
        } catch (IOException e) {
            resultsCountLabel.setText("Impossibile visualizzare i ristoranti.");
            showError("Errore di visualizzazione",
                    "Non e' stato possibile caricare la scheda di un ristorante.");
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
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/FormRistorante.fxml");
    }

    /**
     * Effettua il logout dell'utente corrente e torna alla schermata principale.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onLogOutClick(ActionEvent event) {
        SceneManager.clearHistory(SceneManager.getStage(event));
        SessioneUtente.logout();
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    /**
     * Visualizza una finestra di errore configurabile.
     *
     * @param titolo    titolo della finestra
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