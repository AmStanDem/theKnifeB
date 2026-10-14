package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;

public class MainLayoutController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private Button btnAccediRegistrati;

    @FXML
    private ImageView imgUtente;

    @FXML
    public void initialize() {
        boolean loggato =
                SessioneUtente.getUtenteCorrente() != null;

        aggiornaHeader(loggato);
    }

    private void aggiornaHeader(boolean loggato) {
        btnAccediRegistrati.setVisible(!loggato);
        btnAccediRegistrati.setManaged(!loggato);

        imgUtente.setVisible(loggato);
        imgUtente.setManaged(loggato);
    }

    @FXML
    private void onRicercaClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/listaRistoranti.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/SignUp.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleLoggatoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/AreaPersonale.fxml"
        );
    }

    @FXML
    private void onGeolocalizzazioneClick() {
        // Logica geolocalizzazione
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml"
        );
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/MainLayout.fxml"
        );
    }
}