package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;

/**
 * Controller JavaFX dell'interfaccia principale dell'area cliente.
 * <p>
 * Gestisce le interazioni dell'utente con la schermata dell'area cliente,
 * occupandosi principalmente della gestione degli eventi associati ai
 * pulsanti dell'interfaccia e della navigazione tra le diverse viste
 * dell'applicazione.
 * </p>
 *
 * <p>
 * Il controller è associato alla relativa vista FXML tramite JavaFX e
 * utilizza i componenti dichiarati nella vista per ricevere input
 * dall'utente e modificare la schermata visualizzata.
 * </p>
 */
public class AreaClienteController {

    @FXML
    private Label lblNome;

    @FXML
    private Label lblCognome;

    @FXML
    private Label lblData;

    @FXML
    private Label lblDomicilio;

    @FXML
    private Label lblRuolo;

    @FXML
    private Label lblEmail;


    @FXML
    private void initialize() {

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

        // Controllo di sicurezza
        if (utente == null) {
            return;
        }

        lblNome.setText(utente.nome());
        lblCognome.setText(utente.cognome());
        lblEmail.setText(utente.email());
        lblDomicilio.setText(utente.domicilio());

        // Data di nascita
        if (utente.dataNascita() != null) {
            lblData.setText(utente.dataNascita().toString());
        } else {
            lblData.setText("-");
        }

        // Ruolo
        if (utente.ruolo() != null) {
            if (utente.ruolo() == RuoloUtente.CLIENTE) {
                lblRuolo.setText("Cliente");
            } else if (utente.ruolo() == RuoloUtente.GESTORE) {
                lblRuolo.setText("Ristoratore");
            }
        } else {
            lblRuolo.setText("-");
        }
    }

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    /**
     * Gestisce l'accesso all'area personale dell'utente.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code SignUp.fxml}.
     * </p>
     *
     * @param event evento generato dal click sul pulsante
     */
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    /**
     * Gestisce il click sul pulsante di geolocalizzazione.
     * <p>
     * DA COMPLETARE
     * </p>
     */
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    /**
     * Gestisce l'accesso alla schermata di ricerca avanzata.
     * <p>
     * Sostituisce la scena corrente caricando la vista
     * {@code RicercaAvanzataView.fxml}.
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent,
                "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    /**
     * Gestisce l'accesso alla sezione dei preferiti dell'utente.
     * <p>
     * DA COMPLETARE
     * </p>
     */
    public void onPreferitiClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/Preferiti.fxml");
    }

    /**
     * Gestisce l'accesso alla sezione delle recensioni.
     * <p>
     * Sostituisce la scena corrente caricando la vista
     * {@code RecensioneItem.fxml}.
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MieRecensioni.fxml");
    }

    /**
     * Gestisce il logout dell'utente.
     * <p>
     * Termina la visualizzazione dell'area autenticata caricando
     * la schermata principale dell'applicazione tramite
     * {@code MainLayout.fxml}.
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onLogOutClick(ActionEvent actionEvent) {
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    /**
     * Gestisce il click sul logo dell'applicazione.
     * <p>
     * Riporta l'utente alla schermata principale dell'applicazione
     * mantenendo lo stato di autenticazione, caricando
     * {@code MainLayoutLoggato.fxml}.
     * </p>
     *
     * @param mouseEvent evento generato dal click sul logo
     */
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}