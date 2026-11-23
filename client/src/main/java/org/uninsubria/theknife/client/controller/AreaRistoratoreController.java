/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

/**
 * Controller JavaFX dell'area riservata ai ristoratori.
 * <p>
 * Gestisce la navigazione verso le schermate di gestione dei ristoranti,
 * aggiunta e modifica dei dati, modifica dell'account e logout.
 * </p>
 */
public class AreaRistoratoreController {

    /**
     * Gestisce il logout dell'utente ristoratore.
     * <p>
     * Cancella la cronologia di navigazione della finestra, termina la
     * sessione corrente e torna alla schermata principale.
     * </p>
     *
     * @param event evento generato dal click sul pulsante di logout
     */
    public void onLogOutClick(ActionEvent event) {
        SceneManager.clearHistory(SceneManager.getStage(event));
        SessioneUtente.logout();
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    /**
     * Apre la schermata per aggiungere un nuovo ristorante.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/AggiungiRistorante.fxml");
    }

    /**
     * Apre la schermata per modificare un ristorante esistente.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onModificaRistoranteClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/ModificaRistorante.fxml");
    }

    /**
     * Apre la schermata per modificare i dati dell'account.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onModificaCredenzialiClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/ModificaAccount.fxml");
    }

    /**
     * Apre la lista dei ristoranti gestiti dal ristoratore autenticato.
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    public void ListaRistorantiRistoratore(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml");
    }

}