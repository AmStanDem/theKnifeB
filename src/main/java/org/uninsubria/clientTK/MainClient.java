package org.uninsubria.clientTK;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.uninsubria.clientTK.util.SceneManager;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) {
        Image icona = new Image(
                getClass().getResourceAsStream("/assets/icon.png")
        );
        stage.getIcons().add(icona);
        stage.setTitle("TheKnife");

        // Carica la prima schermata tramite il SceneManager per ereditare subito il CSS globale e la gestione corretta
        SceneManager.switchScene(stage, "/org/uninsubria/clientTK/views/MainLayout.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}