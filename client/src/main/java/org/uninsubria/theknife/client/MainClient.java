/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.SceneManager;

import java.io.InputStream;

public class MainClient extends Application {
    private static HostServices hostServices;

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();
        stage.initStyle(StageStyle.UNDECORATED);
        InputStream resourceAsStream = getClass().getResourceAsStream("/org/uninsubria/theknife/client/assets/icon.png");
        assert resourceAsStream != null;
        Image icona = new Image(
                resourceAsStream
        );
        stage.getIcons().add(icona);
        stage.setTitle("TheKnife");

        SceneManager.switchScene(stage, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
        stage.show();
    }

    @Override
    public void stop() {
        RemoteRefreshManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void apriCollegamento(String uri) {
        if (hostServices == null) {
            throw new IllegalStateException("I servizi del sistema non sono disponibili.");
        }
        hostServices.showDocument(uri);
    }
}