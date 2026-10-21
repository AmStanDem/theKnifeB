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
     */
    public static void switchScene(Event event, String fxmlPath) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchScene(stage, fxmlPath);
    }

    /**
     * Cambia la root della scena corrente sullo Stage specificato.
     */
    public static void switchScene(Stage stage, String fxmlPath) {
        switchSceneAndGetController(stage, fxmlPath);
    }

    /**
     * Cambia la scena, applica il CSS globale, restituisce il Controller tipizzato
     * e forza l'apertura a schermo intero/massimizzato di default.
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                applyCssIfAvailable(scene);
                stage.setScene(scene);
            }

            // IMPOSTAZIONE DI DEFAULT: Apre sempre massimizzato
            // (l'utente potrà comunque ridurorlo con il tasto riduci della finestra se vuole)
            stage.setMaximized(true);

            return loader.getController();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file FXML: " + fxmlPath);
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