package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.SceneManager;

public class ScriviRispostaController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private TextArea txtRecensione;

    @FXML
    private Rating ratingRecensione;

    @FXML
    private Button btnInviaRecensione;

    @FXML
    private Button btnAnnulla;

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    @FXML
    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RecensioneItem.fxml");
    }

    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }

    @FXML
    public void onInviaRispostaClick(ActionEvent actionEvent) {
        // Logica di invio risposta
    }

    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        if (txtRecensione != null) {
            txtRecensione.clear();
        }
        if (ratingRecensione != null) {
            ratingRecensione.setRating(0);
        }

        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/Ristorante.fxml");
    }
}