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
 * Controller JavaFX dell'interfaccia principale dell'applicazione
 * per gli utenti autenticati.
 * <p>
 * Gestisce le interazioni dell'utente con la schermata principale
 * dell'area autenticata, occupandosi delle funzionalità di ricerca,
 * dell'accesso all'area personale, della geolocalizzazione e della
 * navigazione tra le diverse viste dell'applicazione.
 * </p>
 *
 * <p>
 * Il controller è associato alla relativa vista FXML tramite JavaFX
 * e utilizza i componenti definiti nella vista per ricevere gli input
 * dell'utente.
 * </p>
 */
public class MainLayoutLoggatoController {

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
        System.out.println("Ricerca cliccata");
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
     * Gestisce l'accesso all'area personale dell'utente autenticato.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code AreaCliente.fxml}.
     * </p>
     *
     * @param event evento generato dal click dell'utente
     */
    private void onAreaPersonaleClick(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/AreaCliente.fxml")
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
     * per gli utenti autenticati, tramite
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