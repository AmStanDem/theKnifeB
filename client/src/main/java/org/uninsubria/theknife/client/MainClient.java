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

/**
 * Applicazione JavaFX principale del client TheKnife.
 * <p>
 * Configura la finestra principale, carica la schermata iniziale e gestisce
 * l'avvio e l'arresto dei servizi condivisi del client. Fornisce inoltre
 * l'accesso ai servizi JavaFX per aprire collegamenti esterni.
 * </p>
 */
public class MainClient extends Application {
    private static HostServices hostServices;

    /**
     * Configura e mostra la finestra principale dell'applicazione.
     * <p>
     * Inizializza i servizi host, applica lo stile della finestra, carica
     * l'icona e la vista iniziale e avvia la modalità a schermo intero.
     * </p>
     *
     * @param stage finestra primaria fornita da JavaFX
     */
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
        SceneManager.avviaFullscreen(stage);
    }

    /**
     * Arresta i servizi del client prima della chiusura dell'applicazione.
     * <p>
     * Interrompe il gestore degli aggiornamenti remoti.
     * </p>
     */
    @Override
    public void stop() {
        RemoteRefreshManager.shutdown();
    }

    /**
     * Avvia l'applicazione JavaFX.
     *
     * @param args argomenti da riga di comando passati all'applicazione
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Apre un URI tramite il servizio del sistema operativo fornito da JavaFX.
     *
     * @param uri URI da aprire, ad esempio un indirizzo web o un collegamento
     *            telefonico
     * @throws IllegalStateException se i servizi JavaFX non sono ancora
     *                               disponibili
     */
    public static void apriCollegamento(String uri) {
        if (hostServices == null) {
            throw new IllegalStateException("I servizi del sistema non sono disponibili.");
        }
        hostServices.showDocument(uri);
    }
}