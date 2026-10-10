package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.common.dto.RecensioneDTO;

import java.util.List;

public class AreaRistoranteController {

    // =========================================================
    // COMPONENTI FXML
    // =========================================================

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

    // =========================================================
    // RECENSIONI
    // =========================================================

    @FXML
    private VBox reviewContainer;

    @FXML
    private Label resultsCountLabel;

    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    @FXML
    public void initialize() {
        System.out.println("AreaRistoranteController inizializzato");
        caricaRecensioni();
    }

    private void caricaRecensioni() {
        System.out.println("caricaRecensioni() chiamato");

        if (reviewContainer == null) {
            System.out.println("ERRORE: reviewContainer è NULL");
            return;
        }

        System.out.println("reviewContainer trovato!");
        reviewContainer.getChildren().clear();

        List<RecensioneDTO> recensioni = List.of(
                new RecensioneDTO(
                        1,
                        5,
                        "Ottimo ristorante, cibo davvero molto buono!",
                        "Thomas Panero",
                        java.time.LocalDateTime.of(2026, 7, 30, 12, 30),
                        "Grazie mille per la recensione!"
                ),
                new RecensioneDTO(
                        2,
                        4,
                        "Personale gentile e servizio molto veloce.",
                        "Marco Rossi",
                        java.time.LocalDateTime.of(2026, 7, 28, 19, 15),
                        null
                ),
                new RecensioneDTO(
                        3,
                        5,
                        "Piatti molto buoni, sicuramente ci tornerò.",
                        "Luca Bianchi",
                        java.time.LocalDateTime.of(2026, 7, 25, 20, 45),
                        null
                )
        );

        System.out.println("Numero recensioni: " + recensioni.size());
        resultsCountLabel.setText(recensioni.size() + " recensioni trovate");

        for (RecensioneDTO recensione : recensioni) {
            try {
                System.out.println("Carico recensione di: " + recensione.nomeAutore());

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/org/uninsubria/clientTK/views/RecensioneItem.fxml")
                );

                Node item = loader.load();
                RecensioneItemController controller = loader.getController();

                controller.setDati(
                        recensione.nomeAutore(),
                        false,
                        recensione.dataCreazione().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        ),
                        recensione.valutazione(),
                        recensione.testo(),
                        recensione.rispostaGestore()
                );

                reviewContainer.getChildren().add(item);
                System.out.println("Recensione aggiunta!");

            } catch (Exception e) {
                System.out.println("ERRORE caricando la recensione:");
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // NAVIGAZIONE
    // =========================================================

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    // =========================================================
    // SCRIVI RECENSIONE
    // =========================================================

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/ScriviRecensione.fxml");
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    // =========================================================
    // LOGO
    // =========================================================

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }
}