package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.uninsubria.clientTK.util.LuogoInput;
import org.uninsubria.clientTK.util.GeocodingService;
import org.uninsubria.clientTK.util.RicercaIniziale;
import org.uninsubria.clientTK.util.SceneManager;

public class RicercaAvanzataController {

    @FXML
    private LuogoInput txtLocalita;

    @FXML
    private void initialize() {
        txtLocalita.addTextFieldStyleClass("field-input");
        txtLocalita.setOnLuogoSelezionato(luogo -> { });
    }

    @FXML
    private void cercaRistorante(ActionEvent event) {
        GeocodingService.Luogo luogo = txtLocalita.getLuogoSelezionato();
        if (luogo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Località da selezionare");
            alert.setHeaderText(null);
            alert.setContentText("Seleziona una località dai suggerimenti prima di cercare.");
            alert.showAndWait();
            return;
        }
        RicercaIniziale.imposta("", luogo);
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/listaRistoranti.fxml");
    }

    @FXML
    private void reimpostaFiltri() {
        // Logica per reimpostare i campi dei filtri
    }
}