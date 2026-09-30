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
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

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
     * Gestisce l'accesso alla schermata di ricerca avanzata.
     * <p>
     * Sostituisce la scena corrente caricando la vista
     * {@code RicercaAvanzataView.fxml}.
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'accesso alla sezione dei preferiti dell'utente.
     * <p>
     * DA COMPLETARE
     * </p>
     */
    public void onPreferitiClick(ActionEvent actionEvent) {
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
        try {
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/RecensioneItem.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
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