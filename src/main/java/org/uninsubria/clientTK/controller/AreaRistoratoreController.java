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
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {

    }


    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    public void onRecensioniClick(ActionEvent actionEvent) {
    }

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

    public void onRistorantiClick(ActionEvent actionEvent) {
    }

    @FXML
    public void onAggiungiClick(ActionEvent actionEvent) {
        try {
            // Recupera la finestra corrente
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            // Carica la nuova scena da AggiungiRistorante.fxml
            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/AggiungiRistorante.fxml")
                    )
            );

            // Imposta la scena sullo stage
            stage.setScene(scene);

        } catch (IOException e) {
            System.err.println("Errore durante il caricamento di AggiungiRistorante.fxml");
            e.printStackTrace();
        }
    }
    @FXML
    public void ListaRistorantiRistoratore(ActionEvent actionEvent) {
        try {
            // Recupera la finestra corrente
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            // Carica la nuova scena da AggiungiRistorante.fxml
            Scene scene = new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/RistorantiRistoratore.fxml")
                    )
            );

            // Imposta la scena sullo stage
            stage.setScene(scene);

        } catch (IOException e) {
            System.err.println("Errore durante il caricamento di RistorantiRistoratore.fxml");
            e.printStackTrace();
        }
    }



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