package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.uninsubria.clientTK.util.SceneManager;

/**
 * Controller per il guscio dell'applicazione (Application Shell).
 * Gestisce la navigazione di primo livello e l'orchestrazione del layout principale.
 */
public class MainLayoutController {

    // Iniezione del contenitore centrale dove verranno renderizzate le viste
    @FXML
    private StackPane contentArea;

    // Iniezione del bottone di accesso per poterne modificare il testo a runtime
    @FXML
    private Button btnAreaPersonale;

    /**
     * Metodo invocato automaticamente dall'FXMLLoader subito dopo l'iniezione dei nodi.
     * È il punto ideale per verificare se esiste una sessione attiva.
     */
    @FXML
    public void initialize() {
        // TODO: Inserire qui un controllo sul SessionManager (se presente).
        // Se un utente è loggato, aggiornare il bottone:
        // btnAreaPersonale.setText("Area Personale");
    }

    @FXML
    void onHomeClick(ActionEvent event) {
        // Delega la transizione visiva allo SceneManager
        //SceneManager.cambiaScena("HomeVicinanzeView.fxml", "Esplora");
    }

    @FXML
    void onRicercaClick(ActionEvent event) {
        // Delega la transizione visiva allo SceneManager
        //SceneManager.class("RicercaView.fxml", "Ricerca");
    }

    @FXML
    void onAreaPersonaleClick(ActionEvent event) {
        // TODO: Logica di routing condizionale.
        // Se l'utente non è autenticato:
        //SceneManager.cambiaScena("LoginView.fxml", "Accesso");

        // Se l'utente è autenticato come Cliente:
        // SceneManager.cambiaScena("AreaClienteView.fxml", "La Mia Area");

        // Se l'utente è autenticato come Gestore:
        // SceneManager.cambiaScena("DashboardGestoreView.fxml", "Gestione");
    }
}