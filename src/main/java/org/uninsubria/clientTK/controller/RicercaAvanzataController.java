package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.uninsubria.clientTK.util.SceneManager;

public class RicercaAvanzataController {

    @FXML
    private void cercaRistorante(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/listaRistoranti.fxml");
    }

    @FXML
    private void reimpostaFiltri() {
        // Logica per reimpostare i campi dei filtri
    }
}