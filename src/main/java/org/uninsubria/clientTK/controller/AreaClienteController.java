package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;

public class AreaClienteController {

    @FXML
    private Label lblNome;

    @FXML
    private Label lblCognome;

    @FXML
    private Label lblData;

    @FXML
    private Label lblDomicilio;

    @FXML
    private Label lblRuolo;

    @FXML
    private Label lblEmail;


    @FXML
    private void initialize() {

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

        // Controllo di sicurezza
        if (utente == null) {
            return;
        }

        lblNome.setText(utente.nome());
        lblCognome.setText(utente.cognome());
        lblEmail.setText(utente.email());
        lblDomicilio.setText(utente.domicilio());

        // Data di nascita
        if (utente.dataNascita() != null) {
            lblData.setText(utente.dataNascita().toString());
        } else {
            lblData.setText("-");
        }

        // Ruolo
        if (utente.ruolo() != null) {
            if (utente.ruolo() == RuoloUtente.CLIENTE) {
                lblRuolo.setText("Cliente");
            } else if (utente.ruolo() == RuoloUtente.GESTORE) {
                lblRuolo.setText("Ristoratore");
            }
        } else {
            lblRuolo.setText("-");
        }
    }

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
        SceneManager.switchScene(actionEvent,
                "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }


    public void onPreferitiClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/Preferiti.fxml");
    }

    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MieRecensioni.fxml");
    }

    public void onLogOutClick(ActionEvent actionEvent) {
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}