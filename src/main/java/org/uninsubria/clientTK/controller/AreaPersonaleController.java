package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;

import java.io.IOException;

public class AreaPersonaleController {

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
    private Button btnPreferiti;

    @FXML
    private Button btnRecensioni;

    @FXML
    private Button btnRistoranti;

    @FXML
    private Button btnAggiungi;


    @FXML
    private void initialize() {

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

        if (utente == null) {
            return;
        }

        // Dati comuni
        lblNome.setText(utente.nome());
        lblCognome.setText(utente.cognome());
        lblEmail.setText(utente.email());


        // =========================
        // CLIENTE
        // =========================

        if (utente.ruolo() == RuoloUtente.CLIENTE) {

            lblRuolo.setText("Cliente");

            // Data di nascita
            if (utente.dataNascita() != null) {
                lblData.setText(utente.dataNascita().toString());
            } else {
                lblData.setText("-");
            }

            // Domicilio
            if (utente.domicilio() != null) {
                lblDomicilio.setText(utente.domicilio());
            } else {
                lblDomicilio.setText("-");
            }

            // Mostra data e domicilio
            lblData.getParent().setVisible(true);
            lblData.getParent().setManaged(true);

            lblDomicilio.getParent().setVisible(true);
            lblDomicilio.getParent().setManaged(true);

            // Mostra pulsanti cliente
            btnPreferiti.setVisible(true);
            btnPreferiti.setManaged(true);

            btnRecensioni.setVisible(true);
            btnRecensioni.setManaged(true);

            // Nasconde pulsanti ristoratore
            btnRistoranti.setVisible(false);
            btnRistoranti.setManaged(false);

            btnAggiungi.setVisible(false);
            btnAggiungi.setManaged(false);
        }


        // =========================
        // RISTORATORE
        // =========================

        else if (utente.ruolo() == RuoloUtente.GESTORE) {

            lblRuolo.setText("Ristoratore");

            // Data di nascita
            if (utente.dataNascita() != null) {
                lblData.setText(utente.dataNascita().toString());
            } else {
                lblData.setText("-");
            }

            // Domicilio
            if (utente.domicilio() != null) {
                lblDomicilio.setText(utente.domicilio());
            } else {
                lblDomicilio.setText("-");
            }

            // Mostra data e domicilio
            lblData.getParent().setVisible(true);
            lblData.getParent().setManaged(true);

            lblDomicilio.getParent().setVisible(true);
            lblDomicilio.getParent().setManaged(true);

            // Nasconde pulsanti cliente
            btnPreferiti.setVisible(false);
            btnPreferiti.setManaged(false);

            btnRecensioni.setVisible(false);
            btnRecensioni.setManaged(false);

            // Mostra pulsanti ristoratore
            btnRistoranti.setVisible(true);
            btnRistoranti.setManaged(true);

            btnAggiungi.setVisible(true);
            btnAggiungi.setManaged(true);
        }
    }


    @FXML
    private void onPreferitiClick(ActionEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/Preferiti.fxml"
        );
    }


    @FXML
    private void onRecensioniClick(ActionEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/MieRecensioni.fxml"
        );
    }


    @FXML
    private void onAggiungiClick(ActionEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/AggiungiRistorante.fxml"
        );
    }


    @FXML
    private void onRistorantiClick(ActionEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/ListaRistoranti.fxml"
        );
    }


    @FXML
    private void onLogOutClick(ActionEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/MainLayout.fxml"
        );
    }


    @FXML
    private void handleLogoClick(MouseEvent event) {

        cambiaPagina(
                event,
                "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml"
        );
    }


    private void cambiaPagina(
            javafx.event.Event event,
            String percorso) {

        try {

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(
                    new Scene(
                            FXMLLoader.load(
                                    getClass().getResource(percorso)
                            )
                    )
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}