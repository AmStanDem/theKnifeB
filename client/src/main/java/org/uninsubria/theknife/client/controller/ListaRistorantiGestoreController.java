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
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class ListaRistorantiGestoreController {

    private static final String VISTA_PROFILO =
            "/org/uninsubria/theknife/client/views/AreaPersonale.fxml";

    @FXML
    private Label resultsCountLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox restaurantContainer;

    @FXML
    private void initialize() {
        UtenteDTO gestore = SessioneUtente.getUtenteCorrente();
        if (gestore == null || !SessioneUtente.isGestore()) {
            emptyStateLabel.setText("Accedi come ristoratore per visualizzare i tuoi ristoranti.");
            showError("Accesso non consentito", "Questa pagina è riservata ai ristoratori.");
            return;
        }

        caricaRistoranti(gestore);
    }

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

    @FXML
    private void onProfiloClick(ActionEvent event) {
        SceneManager.switchScene(event, VISTA_PROFILO);
    }

    @FXML
    private void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/AggiungiRistorante.fxml");
    }

    private void showError(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
