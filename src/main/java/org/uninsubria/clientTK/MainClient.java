package org.uninsubria.clientTK;


// Oppure per il tema chiaro: import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(MainClient.class.getResource("/org/uninsubria/clientTK/views/MainLayout.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("TheKnife");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}