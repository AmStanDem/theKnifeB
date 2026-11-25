/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.uninsubria.theknife.client.controller.BaseLayoutController;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

// TODO: sistemare i bordi del testo in hover per x e - . credo sia un problema di ombre che però non sono riuscito a risolvere
/**
 * Gestisce la navigazione tra le viste JavaFX dell'applicazione.
 * Carica le schermate FXML nel layout principale, conserva lo storico per ciascuno stage
 * e mantiene la finestra principale a schermo intero monitorando le finestre popup associate.
 */
public class SceneManager {

    private static final Map<Stage, Stack<String>> historyMap = new HashMap<>();
    private static final Map<Stage, String> currentFxmlMap = new HashMap<>();
    private static final Map<Stage, Object> currentControllers = new HashMap<>();
    private static final Map<Stage, Set<Window>> popupWindows = new HashMap<>();
    private static final Set<Stage> configuredStages = new HashSet<>();
    private static Stage applicationStage;
    private static boolean popupListenerInstalled;

    private static final String BASE_LAYOUT_FXML = "/org/uninsubria/theknife/client/views/BaseLayout.fxml";
    private static final String LOGIN_VIEW_FXML = "/org/uninsubria/theknife/client/views/AuthView.fxml";
    private static final String HOME_VIEW_FXML = "/org/uninsubria/theknife/client/views/MainLayout.fxml";
    private static final String GLOBAL_CSS = "/org/uninsubria/theknife/client/styles/style.css";

    /**
     * Carica una vista FXML nello stage associato all'evento e registra la vista corrente
     * nello storico di navigazione, se diversa dalla destinazione.
     *
     * @param event evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void switchScene(Event event, String fxmlPath) {
        switchScene(event, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage associato all'evento, scegliendo se aggiungere
     * la vista corrente allo storico di navigazione.
     *
     * @param event evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param saveToHistory {@code true} per salvare la vista corrente nello storico
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void switchScene(Event event, String fxmlPath, boolean saveToHistory) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        switchSceneAndGetController(stage, fxmlPath, saveToHistory);
    }

    /**
     * Sostituisce la vista corrente senza conservarla nello storico e rimuove
     * un'eventuale voce duplicata della destinazione.
     *
     * @param event evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void replaceCurrentScene(Event event, String fxmlPath) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        replaceCurrentScene(stage, fxmlPath);
    }

    /**
     * Sostituisce la vista corrente senza conservarla nello storico e rimuove
     * un'eventuale voce duplicata della destinazione.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void replaceCurrentScene(Stage stage, String fxmlPath) {
        Stack<String> history = historyMap.get(stage);
        if (history != null && !history.isEmpty() && fxmlPath.equals(history.peek())) {
            history.pop();
        }
        switchSceneAndGetController(stage, fxmlPath, false);
    }

    /**
     * Restituisce lo stage della finestra che contiene la sorgente dell'evento.
     *
     * @param event evento JavaFX associato a un nodo di una scena
     * @return stage che contiene il nodo sorgente dell'evento
     */
    public static Stage getStage(Event event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /**
     * Carica una vista FXML nello stage specificato e salva la vista corrente nello storico,
     * se diversa dalla destinazione.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void switchScene(Stage stage, String fxmlPath) {
        switchSceneAndGetController(stage, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage specificato, mantenendo la finestra principale
     * a schermo intero, e restituisce il controller della vista caricata.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param <T> tipo del controller della vista
     * @return controller associato alla vista caricata
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath) {
        return switchSceneAndGetController(stage, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage, la inserisce nel layout principale e restituisce
     * il relativo controller. La vista precedente viene aggiunta allo storico solo se
     * {@code saveToHistory} è {@code true} e la destinazione è diversa.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param saveToHistory {@code true} per salvare la vista corrente nello storico
     * @param <T> tipo del controller della vista
     * @return controller associato alla vista caricata
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static <T> T switchSceneAndGetController(Stage stage, String fxmlPath, boolean saveToHistory) {
        setApplicationStage(stage);
        try {
            String currentFxml = currentFxmlMap.get(stage);

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent page = loader.load();

            FXMLLoader baseLayoutLoader = new FXMLLoader(SceneManager.class.getResource(BASE_LAYOUT_FXML));
            Parent baseLayout = baseLayoutLoader.load();
            BaseLayoutController baseLayoutController = baseLayoutLoader.getController();
            baseLayoutController.setContent(page);
            baseLayoutController.setLoginPage(LOGIN_VIEW_FXML.equals(fxmlPath));
            baseLayoutController.setCanGoBack(hasPreviousPage(stage)
                    || (saveToHistory && currentFxml != null && !currentFxml.equals(fxmlPath)));

            if (saveToHistory && currentFxml != null && !currentFxml.equals(fxmlPath)) {
                historyMap.computeIfAbsent(stage, k -> new Stack<>()).push(currentFxml);
            }
            currentFxmlMap.put(stage, fxmlPath);

            StackPane applicationRoot = creaRootApplicativa(stage, baseLayout);

            if (stage.getScene() != null) {
                stage.getScene().setRoot(applicationRoot);
            } else {
                Scene scene = new Scene(applicationRoot);
                applyCssIfAvailable(scene);
                stage.setScene(scene);
                stage.sizeToScene();
            }

            Object previousController = currentControllers.remove(stage);
            if (previousController != null) {
                RemoteRefreshManager.unsubscribeOwner(previousController);
            }
            currentControllers.put(stage, loader.getController());
            currentFxmlMap.put(stage, fxmlPath);
            mantieneFullscreen(stage);
            return loader.getController();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossibile caricare la schermata FXML: " + fxmlPath,
                    e
            );
        }
    }

    /**
     * Imposta lo stage dell'applicazione come proprietario del dialogo, se già configurato.
     *
     * @param dialog dialogo a cui assegnare lo stage proprietario
     */
    public static void initOwner(Dialog<?> dialog) {
        if (applicationStage != null) {
            dialog.initOwner(applicationStage);
        }
    }

    /**
     * Imposta lo stage principale dell'applicazione e ne configura le proprietà della finestra.
     *
     * @param stage stage principale dell'applicazione
     */
    public static void setApplicationStage(Stage stage) {
        applicationStage = stage;
        configuraStagePrincipale(stage);
    }

    /**
     * Attiva la modalità a schermo intero se lo stage è visibile e non è già a schermo intero.
     *
     * @param stage stage da visualizzare a schermo intero
     */
    public static void avviaFullscreen(Stage stage) {
        if (stage.isShowing() && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    private static void mantieneFullscreen(Stage stage) {
        configuraStagePrincipale(stage);
        if (!popupWindows.containsKey(stage)) {
            popupWindows.put(stage, new HashSet<>());
            stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
                Object controller = currentControllers.remove(stage);
                if (controller != null) {
                    RemoteRefreshManager.unsubscribeOwner(controller);
                }
                popupWindows.remove(stage);
                configuredStages.remove(stage);
                historyMap.remove(stage);
                currentFxmlMap.remove(stage);
            });
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
            if (window != stage) {
                monitoraPopup(stage, window);
            }
        }

        ripristinaFullscreen(stage);
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
                Platform.runLater(() -> ripristinaFullscreen(stage));
            } else {
                // Windows può uscire dal fullscreen quando un dialog prende il focus.
                Platform.runLater(() -> ripristinaFullscreen(stage));
            }
        });
    }

    private static void configuraStagePrincipale(Stage stage) {

        if (!configuredStages.add(stage)) {
            return;
        }

        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.setResizable(false);

        // Quando un dialog modale (Alert, ecc.) viene mostrato con showAndWait(),
        // blocca il JavaFX Application Thread impedendo l'esecuzione dei task
        // accodati con Platform.runLater(). Su alcune piattaforme il focus del
        // dialog può far uscire momentaneamente dal fullscreen.
        // Ascoltando fullScreenProperty intercettiamo subito il cambiamento
        // e lo ripristiamo prima che Platform.runLater() possa eseguire,
        // superando il blocco di showAndWait().
        stage.fullScreenProperty().addListener((observable, precedente, corrente) -> {
            if (!corrente && stage.isShowing()) {
                stage.setFullScreen(true);
            }
        });

        stage.iconifiedProperty().addListener((observable, precedente, minimizzata) -> {
            if (!minimizzata) {
                Platform.runLater(() -> {
                    if (stage.isShowing()) {
                        applicaFullscreen(stage);
                    }
                });
            }
        });
    }

    private static void ripristinaFullscreen(Stage stage) {

        if (!stage.isShowing()) {
            return;
        }

        applicaFullscreen(stage);
    }

    private static void applicaFullscreen(Stage stage) {
        if (stage.isShowing() && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
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

        Button minimizza = new Button();
        SVGPath iconaMinimizza = new SVGPath();
        iconaMinimizza.setContent("M 1 4 H 9 V 6 H 1 Z");
        iconaMinimizza.getStyleClass().add("app-control-icon");
        minimizza.setGraphic(iconaMinimizza);
        minimizza.getStyleClass().add("app-minimize-button");
        minimizza.setAccessibleText("Minimizza applicazione");
        minimizza.setTooltip(creaTooltip("Minimizza applicazione"));
        minimizza.setOnAction(event -> stage.setIconified(true));

        Button esci = new Button();
        SVGPath iconaEsci = new SVGPath();
        iconaEsci.setContent("M 2 1 L 1 2 L 4 5 L 1 8 L 2 9 L 5 6 L 8 9 L 9 8 L 6 5 L 9 2 L 8 1 L 5 4 Z");
        iconaEsci.getStyleClass().add("app-control-icon");
        esci.setGraphic(iconaEsci);
        esci.getStyleClass().add("app-exit-button");
        esci.setAccessibleText("Esci dall'applicazione");
        esci.setTooltip(creaTooltip("Esci dall'applicazione"));
        esci.setOnAction(event -> {
            stage.close();
            Platform.exit();
        });

        HBox controlliFinestra = new HBox(6, minimizza, esci);
        controlliFinestra.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        root.getChildren().add(controlliFinestra);
        StackPane.setAlignment(controlliFinestra, Pos.TOP_RIGHT);
        StackPane.setMargin(controlliFinestra, new Insets(10, 12, 0, 0));
        return root;
    }

    private static Tooltip creaTooltip(String testo) {
        Tooltip tooltip = new Tooltip(testo);

        tooltip.setStyle(
                "-fx-background-color: #343434;" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 8px 12px;" +
                        "-fx-effect: null;"
        );

        return tooltip;
    }

    /**
     * Torna alla vista precedente nello storico dello stage associato all'evento.
     *
     * @param event evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     */
    public static void goBack(Event event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        goBack(stage);
    }

    /**
     * Torna alla vista precedente nello storico dello stage specificato, se disponibile.
     * <p>
     * Prima di navigare, la richiesta viene offerta al controller corrente: se
     * implementa {@link BackHandler} e gestisce localmente il comando (ad
     * esempio chiudendo una sezione di modifica o chiedendo conferma per dati
     * non salvati), la navigazione viene annullata. Quando lo storico è vuoto
     * viene mostrata la home, evitando di lasciare l'utente su una pagina non
     * più valida.
     * </p>
     *
     * @param stage stage per cui ripristinare la vista precedente
     */
    public static void goBack(Stage stage) {
        Object controller = currentControllers.get(stage);
        if (controller instanceof BackHandler backHandler && backHandler.onBackRequested()) {
            return;
        }

        Stack<String> history = historyMap.get(stage);

        if (history != null && !history.isEmpty()) {
            String previousFxml = history.pop();
            // Carichiamo la pagina precedente impostando saveToHistory a false per non sporcare la pila
            switchSceneAndGetController(stage, previousFxml, false);
        } else {
            switchSceneAndGetController(stage, HOME_VIEW_FXML, false);
        }
    }

    /**
     * Indica se per lo stage è disponibile una vista precedente nello storico.
     *
     * @param stage stage di cui verificare lo storico
     * @return {@code true} se è possibile tornare a una vista precedente
     */
    public static boolean hasPreviousPage(Stage stage) {
        Stack<String> history = historyMap.get(stage);
        return history != null && !history.isEmpty();
    }

    /**
     * Svuota lo storico di navigazione dello stage e rimuove la vista corrente registrata.
     *
     * @param stage stage di cui azzerare lo stato di navigazione
     */
    public static void clearHistory(Stage stage) {
        if (historyMap.containsKey(stage)) {
            historyMap.get(stage).clear();
        }
        currentFxmlMap.remove(stage);
    }

    private static void applyCssIfAvailable(Scene scene) {
        if (SceneManager.class.getResource(GLOBAL_CSS) != null) {
            scene.getStylesheets().add(SceneManager.class.getResource(GLOBAL_CSS).toExternalForm());
        }
    }
}