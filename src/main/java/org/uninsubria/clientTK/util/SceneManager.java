package org.uninsubria.clientTK.util;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import java.io.IOException;



public class SceneManager {

    private static final Map<Stage, Stack<String>> historyMap = new HashMap<>();
    private static final Map<Stage, String> currentFxmlMap = new HashMap<>();

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
        switchSceneAndGetController(stage, fxmlPath, true);
    }

    /**
     * Cambia la scena, applica il CSS globale, restituisce il Controller tipizzato
     * e forza l'apertura a schermo intero/massimizzato di default.
     *
     * DA ELIMINARE DOPO LA PULIZIA
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath) {
        return switchSceneAndGetController(stage, fxmlPath, true);
    }

    /**
     * Metodo interno sovraccarico per gestire il salvataggio nello storico.
     * @param saveToHistory Se true, salva la pagina precedente nello stack. Se false (es. durante il "Back"), evita i loop.
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath, boolean saveToHistory) {
        try {
            String currentFxml = currentFxmlMap.get(stage);

            if (saveToHistory && currentFxml != null && !currentFxml.equals(fxmlPath)) {
                historyMap.computeIfAbsent(stage, k -> new Stack<>()).push(currentFxml);
            }

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
     * Torna alla schermata precedente usando lo Stage ricavato dall'evento.
     */
    public static void goBack(Event event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        goBack(stage);
    }

    /**
     * Torna alla schermata precedente sullo Stage specificato.
     */
    public static void goBack(Stage stage) {
        Stack<String> history = historyMap.get(stage);

        if (history != null && !history.isEmpty()) {
            String previousFxml = history.pop();
            // Carichiamo la pagina precedente impostando saveToHistory a false per non sporcare la pila
            switchSceneAndGetController(stage, previousFxml, false);
        } else {
            System.out.println("Nessuna pagina precedente nello storico per questo stage.");
        }
    }

    /**
     * Pulisce lo storico (utile ad esempio quando si fa il Logout o si torna al Menu Principale).
     */
    public static void clearHistory(Stage stage) {
        if (historyMap.containsKey(stage)) {
            historyMap.get(stage).clear();
        }
        currentFxmlMap.remove(stage);
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