/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
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
    public void initialize() {
        if (SessioneUtente.getUtenteCorrente() != null) {
            String domicilio = SessioneUtente.getUtenteCorrente().domicilio();
            if (domicilio != null && !domicilio.isBlank()) {
                txtLocalita.setText(domicilio);
            }
        }

        txtLocalita.setTextFieldPrefHeight(65);
        txtLocalita.setMapButtonPrefWidth(65);
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
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml"
        );
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
