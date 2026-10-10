package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;

public class AreaRistoratoreController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

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
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    public void onPreferitiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/Preferiti.fxml");
    }

    public void onRecensioniClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MieRecensioni.fxml");
    }

    public void onLogOutClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    public void onRistorantiClick(ActionEvent event) {
    }

    @FXML
    public void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/AggiungiRistorante.fxml");
    }

    @FXML
    public void ListaRistorantiRistoratore(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/ListaRistoranti.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }
}