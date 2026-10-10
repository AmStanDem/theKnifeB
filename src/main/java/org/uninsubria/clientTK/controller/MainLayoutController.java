package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;

public class MainLayoutController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private void onRicercaClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/listaRistoranti.fxml");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    private void onAreaRistoratoreClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/AreaRistoratore.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
        // Logica geolocalizzazione
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}