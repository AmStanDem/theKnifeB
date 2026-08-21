package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;

public class AggiungiRistoranteController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtIndirizzo;

    @FXML
    private TextField txtCitta;

    @FXML
    private TextField txtNazione;

    @FXML
    private TextField txtPrezzoMedio;

    @FXML
    private TextField txtLatitudine;

    @FXML
    private TextField txtLongitudine;

    @FXML
    private CheckComboBox<String> comboCucine;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkBookingOnline;

    @FXML
    private Button btnSalva;

    @FXML
    private Button btnAnnulla;

    @FXML
    public void initialize() {
        // Popola la CheckComboBox di ControlsFX con le tipologie di cucina
        ObservableList<String> tipologieCucina = FXCollections.observableArrayList(
                "Italiana", "Pizzeria", "Giapponese", "Cinese",
                "Messicana", "Indiana", "Vegetariana", "Vegan"
        );
        comboCucine.getItems().addAll(tipologieCucina);
    }

    @FXML
    public void onSalvaClick(ActionEvent event) {
        // 1. Validazione campi obbligatori
        if (txtNome.getText().trim().isEmpty() ||
                txtIndirizzo.getText().trim().isEmpty() ||
                txtCitta.getText().trim().isEmpty() ||
                txtNazione.getText().trim().isEmpty() ||
                txtPrezzoMedio.getText().trim().isEmpty() ||
                txtLatitudine.getText().trim().isEmpty() ||
                txtLongitudine.getText().trim().isEmpty()) {

            mostraMessaggio(Alert.AlertType.WARNING, "Campi Mancanti", "Compila tutti i campi obbligatori (*).");
            return;
        }

        // 2. Validazione e parsing dei campi numerici
        try {
            double prezzoMedio = Double.parseDouble(txtPrezzoMedio.getText().trim().replace(",", "."));
            double latitudine = Double.parseDouble(txtLatitudine.getText().trim().replace(",", "."));
            double longitudine = Double.parseDouble(txtLongitudine.getText().trim().replace(",", "."));

            if (prezzoMedio < 0) {
                mostraMessaggio(Alert.AlertType.WARNING, "Dati Non Validi", "Il prezzo medio non può essere negativo.");
                return;
            }

            // Recupera la lista delle cucine selezionate
            ObservableList<String> cucineSelezionate = comboCucine.getCheckModel().getCheckedItems();
            if (cucineSelezionate.isEmpty()) {
                mostraMessaggio(Alert.AlertType.WARNING, "Selezione Mancante", "Seleziona almeno una tipologia di cucina.");
                return;
            }

            boolean delivery = chkDelivery.isSelected();
            boolean bookingOnline = chkBookingOnline.isSelected();

            // TODO: Invia i dati al servizio/DAO per l'inserimento nel database
            System.out.println("Ristorante salvato: " + txtNome.getText() + " | Prezzo: " + prezzoMedio + "€");

            mostraMessaggio(Alert.AlertType.INFORMATION, "Successo", "Ristorante registrato con successo!");
            tornaAllaHome(event);

        } catch (NumberFormatException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di Formato", "Prezzo, Latitudine e Longitudine devono essere valori numerici validi.");
        }
    }

    @FXML
    public void onAnnullaClick(ActionEvent event) {
        tornaAllaHome(event);
    }

    private void tornaAllaHome(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(
                    FXMLLoader.load(getClass().getResource("/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml"))
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostraMessaggio(Alert.AlertType alertType, String titolo, String contenuto) {
        Alert alert = new Alert(alertType);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}