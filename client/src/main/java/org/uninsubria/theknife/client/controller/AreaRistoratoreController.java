/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

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
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml");
    }

    public void onPreferitiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/Preferiti.fxml");
    }

    public void onRecensioniClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MieRecensioni.fxml");
    }

    public void onLogOutClick(ActionEvent event) {
        SessioneUtente.logout();
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    public void onRistorantiClick(ActionEvent event) {
    }

    @FXML
    public void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/AggiungiRistorante.fxml");
    }

    @FXML
    public void ListaRistorantiRistoratore(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }
}