package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;

public class HeaderController {

    @FXML
    private void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }

    @FXML
    private void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/AreaCliente.fxml");
    }

    @FXML
    private void onLogOutClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}
