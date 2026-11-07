/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.uninsubria.theknife.client.util.SceneManager;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        Image icona = new Image(
                getClass().getResourceAsStream("/org/uninsubria/theknife/client/assets/icon.png")
        );
        stage.getIcons().add(icona);
        stage.setTitle("TheKnife");

        // Usa una finestra borderless a schermo intero.
        SceneManager.switchScene(stage, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
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