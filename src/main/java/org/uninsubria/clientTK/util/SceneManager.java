package org.uninsubria.clientTK.util;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    /**
     * Cambia vista ricavando lo Stage dall'evento (ActionEvent, MouseEvent, ecc.)
     * mantenendo lo stato di Fullscreen o Massimizzato.
     */
    public static void switchScene(Event event, String fxmlPath) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchScene(stage, fxmlPath);
    }

    /**
     * Cambia la root della scena corrente mantenendo lo stato dello Stage.
     */
    public static void switchScene(Stage stage, String fxmlPath) {
        try {
            boolean isFullScreen = stage.isFullScreen();
            boolean isMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }

            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}