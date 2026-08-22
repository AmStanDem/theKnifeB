package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;
import java.util.ArrayList;
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
    private void onAreaRistoratoreClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/AreaRistoratore.fxml")
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

    @FXML
    public void initialize() {
        caricaRistorantiDiTest(); // TODO: sostituire con chiamata al servizio/socket reale verso il server
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
        restaurantScrollPane.setVvalue(0); // torna in cima alla lista dopo ricerca/filtro
    }

    /** Carica la card FXML per un singolo ristorante e collega il controller. */
    private Node creaCardRistorante(RistoranteDTO ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/uninsubria/clientTK/views/RestaurantItem.fxml"));
            Pane cardPane = loader.load();

            RestaurantItemController controller = loader.getController();
            controller.setRistorante(ristorante);
            controller.setOnDettagliAction(this::apriDettagliRistorante);

            return cardPane;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void apriDettagliRistorante(RistoranteDTO ristorante) {
        // TODO: navigazione verso la schermata di dettaglio ristorante (usare idRistorante())
        System.out.println("Apertura dettagli per: " + ristorante.nome());
    }

    /** Dati di esempio — da rimuovere quando si collega il livello dati reale. */
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