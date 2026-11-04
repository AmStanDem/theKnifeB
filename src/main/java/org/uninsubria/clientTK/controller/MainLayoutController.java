package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.uninsubria.clientTK.util.GeocodingService;
import org.uninsubria.clientTK.util.LuogoInput;
import org.uninsubria.clientTK.util.RicercaIniziale;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;

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
                "/org/uninsubria/clientTK/views/listaRistoranti.fxml"
        );
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml"
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
