package org.uninsubria.clientTK.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gestisce la navigazione tra le schermate dell'applicazione client, riusando
 * un unico {@link Stage} principale e sostituendone la {@link Scene}.
 * <p>
 * Evita di aprire una nuova finestra per ogni schermata: tutte le viste FXML
 * vengono caricate dal classpath a partire dal package
 * {@code org.uninsubria.clientTK.view}.
 *
 * @author TheKnife Team
 */
public final class SceneManager {

    private static Stage stagePrincipale;

    private SceneManager() {
        // classe di utilità, non istanziabile
    }

    /**
     * Deve essere chiamato una sola volta, da {@code ClientApp.start()},
     * per registrare lo Stage principale dell'applicazione.
     *
     * @param stage lo stage primario fornito da JavaFX
     */
    public static void init(Stage stage) {
        stagePrincipale = stage;
    }

    /**
     * Carica una vista FXML e la mostra sullo stage principale.
     *
     * @param nomeFxml nome del file FXML (senza path), es. {@code "login.fxml"}
     * @param titolo   titolo da mostrare sulla finestra
     * @throws IOException se il file FXML non viene trovato o non è valido
     */
    public static void mostraSchermata(String nomeFxml, String titolo) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/org/uninsubria/clientTK/view/" + nomeFxml));
        Parent root = loader.load();

        Scene scena = stagePrincipale.getScene();
        if (scena == null) {
            scena = new Scene(root, 900, 600);
            stagePrincipale.setScene(scena);
        } else {
            scena.setRoot(root);
        }
        stagePrincipale.setTitle("TheKnife - " + titolo);
        stagePrincipale.show();
    }
}
