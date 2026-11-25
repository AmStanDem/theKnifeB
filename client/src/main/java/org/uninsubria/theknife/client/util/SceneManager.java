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

    private static final Map<Stage, Stack<String>> cronologiaPerStage = new HashMap<>();
    private static final Map<Stage, String> fxmlCorrentePerStage = new HashMap<>();
    private static final Map<Stage, Object> controllerCorrenti = new HashMap<>();
    private static final Map<Stage, Set<Window>> finestrePopup = new HashMap<>();
    private static final Set<Stage> stageConfigurati = new HashSet<>();
    private static Stage stageApplicazione;
    private static boolean ascoltatorePopupInstallato;

    private static final String FXML_LAYOUT_BASE = "/org/uninsubria/theknife/client/views/BaseLayout.fxml";
    private static final String FXML_VISTA_ACCESSO = "/org/uninsubria/theknife/client/views/AuthView.fxml";
    private static final String FXML_VISTA_HOME = "/org/uninsubria/theknife/client/views/MainLayout.fxml";
    private static final String CSS_GLOBALE = "/org/uninsubria/theknife/client/styles/style.css";

    /**
     * Carica una vista FXML nello stage associato all'evento e registra la vista corrente
     * nello storico di navigazione, se diversa dalla destinazione.
     *
     * @param evento evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void cambiaScena(Event evento, String fxmlPath) {
        cambiaScena(evento, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage associato all'evento, scegliendo se aggiungere
     * la vista corrente allo storico di navigazione.
     *
     * @param evento evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param salvaInCronologia {@code true} per salvare la vista corrente nello storico
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void cambiaScena(Event evento, String fxmlPath, boolean salvaInCronologia) {
        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        cambiaScenaEOttieniController(stage, fxmlPath, salvaInCronologia);
    }

    /**
     * Sostituisce la vista corrente senza conservarla nello storico e rimuove
     * un'eventuale voce duplicata della destinazione.
     *
     * @param evento evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void sostituisciScenaCorrente(Event evento, String fxmlPath) {
        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        sostituisciScenaCorrente(stage, fxmlPath);
    }

    /**
     * Sostituisce la vista corrente senza conservarla nello storico e rimuove
     * un'eventuale voce duplicata della destinazione.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void sostituisciScenaCorrente(Stage stage, String fxmlPath) {
        Stack<String> cronologia = cronologiaPerStage.get(stage);
        if (cronologia != null && !cronologia.isEmpty() && fxmlPath.equals(cronologia.peek())) {
            cronologia.pop();
        }
        cambiaScenaEOttieniController(stage, fxmlPath, false);
    }

    /**
     * Restituisce lo stage della finestra che contiene la sorgente dell'evento.
     *
     * @param evento evento JavaFX associato a un nodo di una scena
     * @return stage che contiene il nodo sorgente dell'evento
     */
    public static Stage ottieniStage(Event evento) {
        return (Stage) ((Node) evento.getSource()).getScene().getWindow();
    }

    /**
     * Carica una vista FXML nello stage specificato e salva la vista corrente nello storico,
     * se diversa dalla destinazione.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static void cambiaScena(Stage stage, String fxmlPath) {
        cambiaScenaEOttieniController(stage, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage specificato, mantenendo la finestra principale
     * a schermo intero, e restituisce il controllore della vista caricata.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param <T> tipo del controllore della vista
     * @return controllore associato alla vista caricata
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static <T> T cambiaScenaEOttieniController(Stage stage, String fxmlPath) {
        return cambiaScenaEOttieniController(stage, fxmlPath, true);
    }

    /**
     * Carica una vista FXML nello stage, la inserisce nel layout principale e restituisce
     * il relativo controllore. La vista precedente viene aggiunta allo storico solo se
     * {@code salvaInCronologia} è {@code true} e la destinazione è diversa.
     *
     * @param stage stage in cui caricare la vista
     * @param fxmlPath percorso assoluto della risorsa FXML da caricare
     * @param salvaInCronologia {@code true} per salvare la vista corrente nello storico
     * @param <T> tipo del controllore della vista
     * @return controllore associato alla vista caricata
     * @throws IllegalStateException se la risorsa FXML non può essere caricata
     */
    public static <T> T cambiaScenaEOttieniController(Stage stage, String fxmlPath, boolean salvaInCronologia) {
        impostaStageApplicazione(stage);
        try {
            String fxmlCorrente = fxmlCorrentePerStage.get(stage);

            FXMLLoader caricatore = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent pagina = caricatore.load();

            FXMLLoader caricatoreLayoutBase = new FXMLLoader(SceneManager.class.getResource(FXML_LAYOUT_BASE));
            Parent layoutBase = caricatoreLayoutBase.load();
            BaseLayoutController controlloreLayoutBase = caricatoreLayoutBase.getController();
            controlloreLayoutBase.impostaContenuto(pagina);
            controlloreLayoutBase.impostaPaginaAccesso(FXML_VISTA_ACCESSO.equals(fxmlPath));
            controlloreLayoutBase.impostaPuoTornareIndietro(haPaginaPrecedente(stage)
                    || (salvaInCronologia && fxmlCorrente != null && !fxmlCorrente.equals(fxmlPath)));

            if (salvaInCronologia && fxmlCorrente != null && !fxmlCorrente.equals(fxmlPath)) {
                cronologiaPerStage.computeIfAbsent(stage, k -> new Stack<>()).push(fxmlCorrente);
            }
            fxmlCorrentePerStage.put(stage, fxmlPath);

            StackPane radiceApplicativa = creaRadiceApplicativa(stage, layoutBase);

            if (stage.getScene() != null) {
                stage.getScene().setRoot(radiceApplicativa);
            } else {
                Scene scena = new Scene(radiceApplicativa);
                applicaCssSeDisponibile(scena);
                stage.setScene(scena);
                stage.sizeToScene();
            }

            Object controllerPrecedente = controllerCorrenti.remove(stage);
            if (controllerPrecedente != null) {
                RemoteRefreshManager.annullaSottoscrizioniProprietario(controllerPrecedente);
            }
            controllerCorrenti.put(stage, caricatore.getController());
            fxmlCorrentePerStage.put(stage, fxmlPath);
            mantieneSchermoIntero(stage);
            return caricatore.getController();

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
     * @param dialogo dialogo a cui assegnare lo stage proprietario
     */
    public static void impostaProprietario(Dialog<?> dialogo) {
        if (stageApplicazione != null) {
            dialogo.initOwner(stageApplicazione);
        }
    }

    /**
     * Imposta lo stage principale dell'applicazione e ne configura le proprietà della finestra.
     *
     * @param stage stage principale dell'applicazione
     */
    public static void impostaStageApplicazione(Stage stage) {
        stageApplicazione = stage;
        configuraStagePrincipale(stage);
    }

    /**
     * Attiva la modalità a schermo intero se lo stage è visibile e non è già a schermo intero.
     *
     * @param stage stage da visualizzare a schermo intero
     */
    public static void avviaSchermoIntero(Stage stage) {
        if (stage.isShowing() && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    private static void mantieneSchermoIntero(Stage stage) {
        configuraStagePrincipale(stage);
        if (!finestrePopup.containsKey(stage)) {
            finestrePopup.put(stage, new HashSet<>());
            stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, evento -> {
                Object controllore = controllerCorrenti.remove(stage);
                if (controllore != null) {
                    RemoteRefreshManager.annullaSottoscrizioniProprietario(controllore);
                }
                finestrePopup.remove(stage);
                stageConfigurati.remove(stage);
                cronologiaPerStage.remove(stage);
                fxmlCorrentePerStage.remove(stage);
            });
        }

        if (!ascoltatorePopupInstallato) {
            Window.getWindows().addListener((ListChangeListener<Window>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (Window finestra : change.getAddedSubList()) {
                            monitoraPopup(finestra);
                        }
                    }
                }
            });
            ascoltatorePopupInstallato = true;
        }

        for (Window finestra : Window.getWindows()) {
            if (finestra != stage) {
                monitoraPopup(stage, finestra);
            }
        }

        ripristinaSchermoIntero(stage);
    }

    private static void monitoraPopup(Window finestra) {
        for (Stage stage : Set.copyOf(finestrePopup.keySet())) {
            monitoraPopup(stage, finestra);
        }
    }

    private static void monitoraPopup(Stage stage, Window finestra) {
        if (finestra == stage || !appartieneA(finestra, stage)) {
            return;
        }
        Set<Window> finestreMonitorate = finestrePopup.get(stage);
        if (finestreMonitorate == null || !finestreMonitorate.add(finestra)) {
            return;
        }

        finestra.showingProperty().addListener((osservabile, precedente, visibile) -> {
            if (!visibile) {
                Platform.runLater(() -> ripristinaSchermoIntero(stage));
            } else {
                // Windows può uscire dal fullscreen quando un dialogo prende il focus.
                Platform.runLater(() -> ripristinaSchermoIntero(stage));
            }
        });
    }

    private static void configuraStagePrincipale(Stage stage) {

        if (!stageConfigurati.add(stage)) {
            return;
        }

        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.setResizable(false);

        // Quando un dialogo modale (Alert, ecc.) viene mostrato con showAndWait(),
        // blocca il JavaFX Application Thread impedendo l'esecuzione dei task
        // accodati con Platform.runLater(). Su alcune piattaforme il focus del
        // dialogo può far uscire momentaneamente dal fullscreen.
        // Ascoltando fullScreenProperty intercettiamo subito il cambiamento
        // e lo ripristiamo prima che Platform.runLater() possa eseguire,
        // superando il blocco di showAndWait().
        stage.fullScreenProperty().addListener((osservabile, precedente, corrente) -> {
            if (!corrente && stage.isShowing()) {
                stage.setFullScreen(true);
            }
        });

        stage.iconifiedProperty().addListener((osservabile, precedente, minimizzata) -> {
            if (!minimizzata) {
                Platform.runLater(() -> {
                    if (stage.isShowing()) {
                        applicaSchermoIntero(stage);
                    }
                });
            }
        });
    }

    private static void ripristinaSchermoIntero(Stage stage) {

        if (!stage.isShowing()) {
            return;
        }

        applicaSchermoIntero(stage);
    }

    private static void applicaSchermoIntero(Stage stage) {
        if (stage.isShowing() && !stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    private static boolean appartieneA(Window finestra, Stage stage) {
        Window possessore = proprietario(finestra);
        while (possessore != null) {
            if (possessore == stage) {
                return true;
            }
            possessore = proprietario(possessore);
        }
        return false;
    }

    private static Window proprietario(Window finestra) {
        if (finestra instanceof PopupWindow popup) {
            return popup.getOwnerWindow();
        }
        if (finestra instanceof Stage stage) {
            return stage.getOwner();
        }
        return null;
    }

    private static StackPane creaRadiceApplicativa(Stage stage, Parent contenuto) {
        StackPane radice = new StackPane(contenuto);
        radice.setPickOnBounds(false);

        Button minimizza = new Button();
        SVGPath iconaMinimizza = new SVGPath();
        iconaMinimizza.setContent("M 1 4 H 9 V 6 H 1 Z");
        iconaMinimizza.getStyleClass().add("app-control-icon");
        minimizza.setGraphic(iconaMinimizza);
        minimizza.getStyleClass().add("app-minimize-button");
        minimizza.setAccessibleText("Minimizza applicazione");
        minimizza.setTooltip(creaTooltip("Minimizza applicazione"));
        minimizza.setOnAction(evento -> stage.setIconified(true));

        Button esci = new Button();
        SVGPath iconaEsci = new SVGPath();
        iconaEsci.setContent("M 2 1 L 1 2 L 4 5 L 1 8 L 2 9 L 5 6 L 8 9 L 9 8 L 6 5 L 9 2 L 8 1 L 5 4 Z");
        iconaEsci.getStyleClass().add("app-control-icon");
        esci.setGraphic(iconaEsci);
        esci.getStyleClass().add("app-exit-button");
        esci.setAccessibleText("Esci dall'applicazione");
        esci.setTooltip(creaTooltip("Esci dall'applicazione"));
        esci.setOnAction(evento -> {
            stage.close();
            Platform.exit();
        });

        HBox controlliFinestra = new HBox(6, minimizza, esci);
        controlliFinestra.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        radice.getChildren().add(controlliFinestra);
        StackPane.setAlignment(controlliFinestra, Pos.TOP_RIGHT);
        StackPane.setMargin(controlliFinestra, new Insets(10, 12, 0, 0));
        return radice;
    }

    private static Tooltip creaTooltip(String testo) {
        Tooltip suggerimento = new Tooltip(testo);

        suggerimento.setStyle(
                "-fx-background-color: #343434;" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-padding: 8px 12px;" +
                        "-fx-effect: null;"
        );

        return suggerimento;
    }

    /**
     * Torna alla vista precedente nello storico dello stage associato all'evento.
     *
     * @param evento evento JavaFX la cui sorgente appartiene allo stage da aggiornare
     */
    public static void tornaIndietro(Event evento) {
        Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        tornaIndietro(stage);
    }

    /**
     * Torna alla vista precedente nello storico dello stage specificato, se disponibile.
     * <p>
     * Prima di navigare, la richiesta viene offerta al controllore corrente: se
     * implementa {@link BackHandler} e gestisce localmente il comando (ad
     * esempio chiudendo una sezione di modifica o chiedendo conferma per dati
     * non salvati), la navigazione viene annullata. Quando lo storico è vuoto
     * viene mostrata la home, evitando di lasciare l'utente su una pagina non
     * più valida.
     * </p>
     *
     * @param stage stage per cui ripristinare la vista precedente
     */
    public static void tornaIndietro(Stage stage) {
        Object controllore = controllerCorrenti.get(stage);
        if (controllore instanceof BackHandler backHandler && backHandler.richiestaIndietro()) {
            return;
        }

        Stack<String> cronologia = cronologiaPerStage.get(stage);

        if (cronologia != null && !cronologia.isEmpty()) {
            String fxmlPrecedente = cronologia.pop();
            // Carichiamo la pagina precedente impostando salvaInCronologia a false per non sporcare la pila
            cambiaScenaEOttieniController(stage, fxmlPrecedente, false);
        } else {
            cambiaScenaEOttieniController(stage, FXML_VISTA_HOME, false);
        }
    }

    /**
     * Indica se per lo stage è disponibile una vista precedente nello storico.
     *
     * @param stage stage di cui verificare lo storico
     * @return {@code true} se è possibile tornare a una vista precedente
     */
    public static boolean haPaginaPrecedente(Stage stage) {
        Stack<String> cronologia = cronologiaPerStage.get(stage);
        return cronologia != null && !cronologia.isEmpty();
    }

    /**
     * Svuota lo storico di navigazione dello stage e rimuove la vista corrente registrata.
     *
     * @param stage stage di cui azzerare lo stato di navigazione
     */
    public static void pulisciCronologia(Stage stage) {
        if (cronologiaPerStage.containsKey(stage)) {
            cronologiaPerStage.get(stage).clear();
        }
        fxmlCorrentePerStage.remove(stage);
    }

    private static void applicaCssSeDisponibile(Scene scena) {
        if (SceneManager.class.getResource(CSS_GLOBALE) != null) {
            scena.getStylesheets().add(SceneManager.class.getResource(CSS_GLOBALE).toExternalForm());
        }
    }
}