package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;
import java.util.List;

public class PreferitiController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML private Label resultsCountLabel;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();

    @FXML
    private void onRicercaClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/listaPreferiti.fxml");
    }

    @FXML
    private void onAreaPersonaleClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/areaCliente.fxml");
    }

    @FXML
    private void onAreaRistoratoreClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/AreaRistoratore.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    @FXML
    public void initialize() {
        caricaRistorantiDiTest();
        aggiornaLista(ristorantiCompleti);
    }

    private void aggiornaLista(List<RistoranteDTO> nuovaLista) {
        restaurantContainer.getChildren().clear();

        for (RistoranteDTO ristorante : nuovaLista) {
            Node card = creaCardRistorante(ristorante);
            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(nuovaLista.size() + " ristoranti trovati");
        restaurantScrollPane.setVvalue(0);
    }

    private Node creaCardRistorante(RistoranteDTO ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/uninsubria/clientTK/views/PreferitoItem.fxml"));
            Pane cardPane = loader.load();

            PreferitoItemController controller = loader.getController();
            controller.setRistorante(ristorante);
            controller.setOnDettagliAction(this::apriDettagliRistorante);

            return cardPane;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void apriDettagliRistorante(RistoranteDTO ristorante) {
        System.out.println("Apertura dettagli per: " + ristorante.nome());
    }

    private void caricaRistorantiDiTest() {
        ristorantiCompleti.addAll(
                new RistoranteDTO(
                        1,
                        "Osteria del Borgo",
                        "Via Roma 12, Milano",
                        "Italia",
                        "Milano",
                        45.4642,
                        9.1900,
                        "Italiana",
                        28.0,
                        false,
                        true,
                        4.5
                ),
                new RistoranteDTO(
                        2,
                        "Sakura Sushi",
                        "Corso Buenos Aires 5, Milano",
                        "Italia",
                        "Milano",
                        45.4780,
                        9.2050,
                        "Giapponese",
                        35.0,
                        true,
                        true,
                        4.2
                ),
                new RistoranteDTO(
                        3,
                        "La Piadineria",
                        "Piazza Duomo 1, Milano",
                        "Italia",
                        "Milano",
                        45.4640,
                        9.1900,
                        "Pizzeria",
                        12.0,
                        true,
                        false,
                        3.8
                )
        );
    }
}