package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

import java.util.Objects;

/**
 * Controller JavaFX del layout principale condiviso dalle viste dell'applicazione.
 * <p>
 * Gestisce la barra di navigazione comune, il pulsante per tornare alla pagina
 * precedente, l'accesso dell'utente e la visualizzazione dinamica
 * del contenuto della vista corrente.
 * </p>
 * <p>
 * Il contenuto interno viene sostituito tramite {@link #setContent(Node)}, mentre
 * lo stato dei controlli viene adattato alla sessione utente e alla pagina
 * visualizzata.
 * </p>
 */
public class BaseLayoutController {

    @FXML
    private StackPane contentContainer;

    @FXML
    private Button btnAccediRegistrati;

    @FXML
    private Button btnBack;

    @FXML
    private ImageView imgUtente;

    private boolean paginaLogin;
    private boolean loggato;

    /**
     * Inizializza i componenti comuni del layout.
     * <p>
     * Verifica lo stato della sessione utente, configura la visibilità
     * dell'icona profilo e aggiorna il pulsante
     * di accesso o registrazione.
     * </p>
     */
    @FXML
    private void initialize() {
        loggato = SessioneUtente.getUtenteCorrente() != null;
        imgUtente.setVisible(loggato);
        imgUtente.setManaged(loggato);
        aggiornaPulsanteAccesso();
    }

    /**
     * Gestisce il comando per tornare alla pagina precedente.
     *
     * @param event evento generato dal pulsante indietro
     */
    @FXML
    private void goBack(ActionEvent event) {
        SceneManager.goBack(event);
    }

    /**
     * Apre la schermata di login o registrazione per un utente non autenticato.
     *
     * @param event evento generato dal pulsante di accesso
     */
    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/LoginView.fxml"
        );
    }

    /**
     * Gestisce il click sul logo dell'applicazione.
     * <p>
     * Dalla posizione corrente viene aperta la schermata principale
     * dell'applicazione.
     * </p>
     *
     * @param event evento generato dal click sul logo
     */
    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }

    /**
     * Apre l'area personale dell'utente autenticato.
     *
     * @param event evento generato dal click sull'icona utente
     */
    @FXML
    private void onAreaPersonaleLoggatoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/AreaPersonale.fxml"
        );
    }

    /**
     * Sostituisce il contenuto visualizzato nel contenitore centrale del layout.
     *
     * @param content nodo da visualizzare nel layout
     * @throws NullPointerException se {@code content} è {@code null}
     */
    public void setContent(Node content) {
        contentContainer.getChildren().setAll(Objects.requireNonNull(content));
    }

    /**
     * Aggiorna la disponibilità del pulsante per tornare alla pagina precedente.
     *
     * @param canGoBack {@code true} se è disponibile una pagina precedente
     */
    public void setCanGoBack(boolean canGoBack) {
        btnBack.setDisable(!canGoBack);
    }

    /**
     * Imposta se la vista corrente è la pagina di login.
     * <p>
     * Questo valore viene utilizzato per evitare di mostrare il pulsante
     * di accesso quando l'utente si trova già sulla relativa schermata.
     * </p>
     *
     * @param loginPage {@code true} se la vista corrente è la pagina di login
     */
    public void setLoginPage(boolean loginPage) {
        paginaLogin = loginPage;
        aggiornaPulsanteAccesso();
    }

    /**
     * Aggiorna la visibilità del pulsante di accesso o registrazione.
     * <p>
     * Il pulsante viene mostrato soltanto agli utenti non autenticati che
     * non si trovano già nella pagina di login.
     * </p>
     */
    private void aggiornaPulsanteAccesso() {
        boolean mostraPulsante = !loggato && !paginaLogin;
        btnAccediRegistrati.setVisible(mostraPulsante);
        btnAccediRegistrati.setManaged(mostraPulsante);
    }
}
