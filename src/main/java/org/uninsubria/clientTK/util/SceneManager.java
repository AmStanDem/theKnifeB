package org.uninsubria.clientTK.util;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static final String GLOBAL_CSS = "/org/uninsubria/clientTK/styles/style.css";

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
        switchSceneAndGetController(stage, fxmlPath);
    }

    /**
     * Cambia la scena e restituisce il Controller tipizzato della nuova vista.
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath) {
        try {
            boolean isFullScreen = stage.isFullScreen();
            boolean isMaximized = stage.isMaximized();

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                applyCssIfAvailable(scene);
                stage.setScene(scene);
            }

            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }

            return loader.getController();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sovrapposizione: Apri una nuova finestra Modale (Popup).
     */
    public static <T> T openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);

            Scene scene = new Scene(root);
            applyCssIfAvailable(scene);
            modalStage.setScene(scene);

            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.show();

            return loader.getController();

        } catch (IOException e) {
            System.err.println("Errore nell'apertura della finestra modale: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper privato per applicare il CSS globale se presente.
     */
    private static void applyCssIfAvailable(Scene scene) {
        if (SceneManager.class.getResource(GLOBAL_CSS) != null) {
            scene.getStylesheets().add(SceneManager.class.getResource(GLOBAL_CSS).toExternalForm());
        }
    }
}