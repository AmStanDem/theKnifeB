package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller JavaFX dell'interfaccia principale dell'area ristoratore.
 * <p>
 * Gestisce le interazioni dell'utente con la schermata dedicata al
 * ristoratore, occupandosi della gestione degli eventi associati agli
 * elementi dell'interfaccia e della navigazione tra le diverse viste
 * dell'applicazione.
 * </p>
 *
 * <p>
 * Il controller è associato alla relativa vista FXML tramite JavaFX
 * e utilizza i componenti definiti nella vista per ricevere gli input
 * dell'utente.
 * </p>
 */
public class AreaRistoratoreController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    /**
     * Gestisce l'accesso all'area personale del ristoratore.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code SignUp.fxml}.
     * </p>
     *
     * @param event evento generato dal click dell'utente
     */
    private void onAreaPersonaleClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/SignUp.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Gestisce l'accesso alla funzionalità di ricerca avanzata.
     * <p>
     * DA COMPLETARE
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {

    }

    /**
     * Gestisce l'accesso alla sezione dei preferiti.
     * <p>
     * DA COMPLETARE
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    /**
     * Gestisce l'accesso alla sezione delle recensioni.
     * <p>
     * DA COMPLETARE
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRecensioniClick(ActionEvent actionEvent) {
    }

    /**
     * Gestisce il logout del ristoratore.
     * <p>
     * Sostituisce la scena corrente caricando la vista
     * {@code MainLayout.fxml}, riportando l'utente alla schermata
     * principale dell'applicazione.
     * </p>
     *
     * @param actionEvent evento generato dal click del pulsante
     */
    public void onLogOutClick(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/MainLayout.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gestisce l'accesso alla sezione dedicata ai ristoranti.
     * <p>
     * DA COMPLETARE
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRistorantiClick(ActionEvent actionEvent) {
    }

    /**
     * Gestisce l'azione di aggiunta di un nuovo elemento.
     * <p>
     * DA COMPLETARE
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onAggiungiClick(ActionEvent actionEvent) {
    }

    /**
     * Gestisce il click sul logo dell'applicazione.
     * <p>
     * Riporta l'utente alla schermata principale dell'applicazione
     * mantenendo lo stato di autenticazione, caricando la vista
     * {@code MainLayoutLoggato.fxml}.
     * </p>
     *
     * @param mouseEvent evento generato dal click sul logo
     */
    public void handleLogoClick(MouseEvent mouseEvent) {
        try {
            Stage stage = (Stage) ((Node) mouseEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}