/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

public class MainLayoutController {

    @FXML
    private LuogoInput txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnAccediRegistrati;

    @FXML
    private ImageView imgUtente;

    @FXML
    public void initialize() {
        boolean loggato = SessioneUtente.getUtenteCorrente() != null;
        aggiornaHeader(loggato);

        if(loggato) {
            String domicilio = SessioneUtente.getUtenteCorrente().domicilio();
            if (domicilio != null && !domicilio.isBlank()) {
                txtLocalita.setText(domicilio);
            }
        }

        txtLocalita.setTextFieldPrefHeight(65);
        txtLocalita.setMapButtonPrefWidth(65);
    }



    private void aggiornaHeader(boolean loggato) {
        btnAccediRegistrati.setVisible(!loggato);
        btnAccediRegistrati.setManaged(!loggato);
        imgUtente.setVisible(loggato);
        imgUtente.setManaged(loggato);
    }

    @FXML
    private void onRicercaClick(ActionEvent event) {
        GeocodingService.Luogo luogo = txtLocalita.getLuogoSelezionato();
        if (!txtLocalita.getText().isBlank() && luogo == null) {
            mostraErrore(
                    "Località da selezionare",
                    "Scegli una località tra i suggerimenti oppure usa la mappa."
            );
            return;
        }

        RicercaIniziale.imposta(txtRicerca.getText(), luogo);
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/listaRistoranti.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/LoginView.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleLoggatoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/AreaPersonale.fxml"
        );
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml"
        );
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
