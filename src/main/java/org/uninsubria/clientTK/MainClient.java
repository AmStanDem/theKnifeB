package org.uninsubria.clientTK;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.uninsubria.clientTK.util.SceneManager;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        Image icona = new Image(
                getClass().getResourceAsStream("/assets/icon.png")
        );
        stage.getIcons().add(icona);
        stage.setTitle("TheKnife");

        // Usa una finestra borderless a schermo intero, senza la modalita fullscreen esclusiva.
        SceneManager.switchScene(stage, "/org/uninsubria/clientTK/views/MainLayout.fxml");
        var bounds = Screen.getPrimary().getBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}