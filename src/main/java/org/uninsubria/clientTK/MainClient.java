package org.uninsubria.clientTK;

import atlantafx.base.theme.PrimerDark;
// Oppure per il tema chiaro: import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Iniezione globale del Design System di AtlantaFX
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // 2. Prosegui con il normale ciclo di vita della GUI
        // SceneManager.cambiaScena("LoginView.fxml");
        // primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}