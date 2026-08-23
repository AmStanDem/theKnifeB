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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.uninsubria.common.dto.RecensioneDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MieRecensioniController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private Label resultsCountLabel;

    @FXML
    private ScrollPane restaurantScrollPane;

    @FXML
    private VBox restaurantContainer;

    private final ObservableList<RecensioneDTO> recensioniComplete =
            FXCollections.observableArrayList();


    // =========================================================
    // NAVIGAZIONE
    // =========================================================

    @FXML
    private void onAreaPersonaleClick(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/org/uninsubria/clientTK/views/areaCliente.fxml"
                            )
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
                            getClass().getResource(
                                    "/org/uninsubria/clientTK/views/AreaRistoratore.fxml"
                            )
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGeolocalizzazioneClick() {
        // TODO
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml"
                            )
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/org/uninsubria/clientTK/views/MainLayout.fxml"
                            )
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    @FXML
    public void initialize() {

        // TODO:
        // sostituire con chiamata al server/socket reale
        caricaRecensioniDiTest();

        aggiornaLista(recensioniComplete);
    }


    // =========================================================
    // RICERCA
    // =========================================================

    @FXML
    private void onSearch() {

        String query = txtRicerca.getText() == null
                ? ""
                : txtRicerca.getText()
                .trim()
                .toLowerCase();

        if (query.isEmpty()) {
            aggiornaLista(recensioniComplete);
            return;
        }

        List<RecensioneDTO> filtrate = new ArrayList<>();

        for (RecensioneDTO recensione : recensioniComplete) {

            boolean matchTesto =
                    recensione.testo() != null
                            && recensione.testo()
                            .toLowerCase()
                            .contains(query);

            boolean matchAutore =
                    recensione.nomeAutore() != null
                            && recensione.nomeAutore()
                            .toLowerCase()
                            .contains(query);

            if (matchTesto || matchAutore) {
                filtrate.add(recensione);
            }
        }

        aggiornaLista(filtrate);
    }


    // =========================================================
    // AGGIORNAMENTO LISTA
    // =========================================================

    private void aggiornaLista(
            List<RecensioneDTO> nuovaLista) {

        restaurantContainer.getChildren().clear();

        for (RecensioneDTO recensione : nuovaLista) {

            Node card = creaCardRecensione(recensione);

            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(
                nuovaLista.size() + " recensioni trovate"
        );

        restaurantScrollPane.setVvalue(0);
    }


    // =========================================================
    // CREAZIONE CARD RECENSIONE
    // =========================================================

    private Node creaCardRecensione(
            RecensioneDTO recensione) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/uninsubria/clientTK/views/RecensioneItem.fxml"
                    )
            );

            Node card = loader.load();

            RecensioneItemController controller =
                    loader.getController();

            controller.setRecensione(recensione);

            return card;

        } catch (IOException e) {

            e.printStackTrace();

            return null;
        }
    }


    // =========================================================
    // DATI DI TEST
    // =========================================================

    private void caricaRecensioniDiTest() {

        recensioniComplete.addAll(

                new RecensioneDTO(
                        1,
                        5,
                        "Credo sia il gioco migliore "
                                + "a cui abbia mai giocato. "
                                + "Ambientazioni, personaggi, "
                                + "dialoghi e storia impeccabili.",
                        "Giovanni",
                        LocalDateTime.of(
                                2026,
                                7,
                                30,
                                15,
                                30
                        ),
                        null
                ),

                new RecensioneDTO(
                        2,
                        4,
                        "Ottima esperienza, personale "
                                + "gentile e servizio veloce.",
                        "Giovanni",
                        LocalDateTime.of(
                                2026,
                                7,
                                25,
                                18,
                                20
                        ),
                        "Grazie mille per la recensione!"
                ),

                new RecensioneDTO(
                        3,
                        3,
                        "Il posto è carino, ma secondo me "
                                + "si potrebbe migliorare il servizio.",
                        "Giovanni",
                        LocalDateTime.of(
                                2026,
                                7,
                                20,
                                12,
                                10
                        ),
                        null
                )
        );
    }
}