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
 * Controller JavaFX dell'interfaccia principale dell'applicazione.
 * <p>
 * Gestisce le interazioni dell'utente con la schermata principale,
 * occupandosi delle funzionalità di ricerca, dell'accesso all'area
 * personale, della geolocalizzazione e della navigazione verso le
 * diverse viste dell'applicazione.
 * </p>
 *
 * <p>
 * Il controller è associato alla relativa vista FXML tramite JavaFX
 * e utilizza i componenti definiti nella vista per ricevere gli input
 * dell'utente.
 * </p>
 */
public class MainLayoutController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    /**
     * Gestisce l'avvio della ricerca dei ristoranti.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code listaRistoranti.fxml},
     * contenente l'elenco dei ristoranti disponibili.
     * </p>
     *
     * @param event evento generato dal click dell'utente
     */
    private void onRicercaClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/listaRistoranti.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    /**
     * Gestisce l'accesso all'area personale dell'utente.
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
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista
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
     * Gestisce il click sul logo dell'applicazione.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando nuovamente la vista principale
     * {@code MainLayout.fxml}.
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
                            getClass().getResource("/org/uninsubria/clientTK/views/MainLayout.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}