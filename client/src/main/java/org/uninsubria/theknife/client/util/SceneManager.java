/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.event.Event;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCombination;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import java.io.IOException;



public class SceneManager {

    private static final Map<Stage, Stack<String>> historyMap = new HashMap<>();
    private static final Map<Stage, String> currentFxmlMap = new HashMap<>();
    private static final Map<Stage, Set<Window>> popupWindows = new HashMap<>();
    private static boolean popupListenerInstalled;

    private static final String GLOBAL_CSS = "/org/uninsubria/theknife/client/styles/style.css";

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
     * Cambia la scena mantenendo la finestra principale a schermo intero.
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
            StackPane applicationRoot = creaRootApplicativa(stage, root);

            if (stage.getScene() != null) {
                stage.getScene().setRoot(applicationRoot);
            } else {
                Scene scene = new Scene(applicationRoot);
                applyCssIfAvailable(scene);
                stage.setScene(scene);
                stage.sizeToScene();
            }

            mantieneFullscreen(stage);
            return loader.getController();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossibile caricare la schermata FXML: " + fxmlPath,
                    e
            );
        }
    }

    private static void mantieneFullscreen(Stage stage) {
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        if (!popupWindows.containsKey(stage)) {
            popupWindows.put(stage, new HashSet<>());
            stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> popupWindows.remove(stage));
        }

        if (!popupListenerInstalled) {
            Window.getWindows().addListener((ListChangeListener<Window>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (Window window : change.getAddedSubList()) {
                            monitoraPopup(window);
                        }
                    }
                }
            });
            popupListenerInstalled = true;
        }

        for (Window window : Window.getWindows()) {
            monitoraPopup(stage, window);
        }

        Platform.runLater(() -> {
            if (stage.isShowing()) {
                stage.setFullScreen(true);
            }
        });
    }

    private static void monitoraPopup(Window window) {
        for (Stage stage : Set.copyOf(popupWindows.keySet())) {
            monitoraPopup(stage, window);
        }
    }

    private static void monitoraPopup(Stage stage, Window window) {
        if (window == stage || !appartieneA(window, stage)) {
            return;
        }
        Set<Window> monitorate = popupWindows.get(stage);
        if (monitorate == null || !monitorate.add(window)) {
            return;
        }

        window.showingProperty().addListener((observable, precedente, visibile) -> {
            if (!visibile) {
                Platform.runLater(() -> {
                    if (stage.isShowing()
                            && !stage.isFullScreen()
                            && popupWindows.getOrDefault(stage, Set.of()).stream()
                                    .noneMatch(Window::isShowing)) {
                        stage.setFullScreen(true);
                    }
                });
            }
        });
    }

    private static boolean appartieneA(Window window, Stage stage) {
        Window owner = proprietario(window);
        while (owner != null) {
            if (owner == stage) {
                return true;
            }
            owner = proprietario(owner);
        }
        return false;
    }

    private static Window proprietario(Window window) {
        if (window instanceof PopupWindow popup) {
            return popup.getOwnerWindow();
        }
        if (window instanceof Stage stage) {
            return stage.getOwner();
        }
        return null;
    }

    private static StackPane creaRootApplicativa(Stage stage, Parent contenuto) {
        StackPane root = new StackPane(contenuto);
        root.setPickOnBounds(false);

        Button esci = new Button("×");
        esci.getStyleClass().add("app-exit-button");
        esci.setAccessibleText("Esci dall'applicazione");
        esci.setTooltip(new javafx.scene.control.Tooltip("Esci dall'applicazione"));
        esci.setOnAction(event -> {
            stage.close();
            Platform.exit();
        });

        root.getChildren().add(esci);
        StackPane.setAlignment(esci, Pos.TOP_RIGHT);
        StackPane.setMargin(esci, new Insets(10, 12, 0, 0));
        return root;
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