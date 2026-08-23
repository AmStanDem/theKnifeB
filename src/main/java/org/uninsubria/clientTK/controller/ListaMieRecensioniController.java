package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.uninsubria.common.dto.RecensioneDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaMieRecensioniController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<String> ratingFilterCombo;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private Label resultsCountLabel;

    @FXML
    private ScrollPane reviewScrollPane;

    @FXML
    private VBox reviewContainer;

    private final ObservableList<RecensioneDTO> recensioniComplete =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        configuraFiltri();

        // TODO: sostituire con chiamata al server/socket
        caricaRecensioniDiTest();

        aggiornaLista(recensioniComplete);
    }

    /**
     * Configura il filtro per la valutazione.
     */
    private void configuraFiltri() {

        ratingFilterCombo.setItems(
                FXCollections.observableArrayList(
                        "3+",
                        "3.5+",
                        "4+",
                        "4.5+"
                )
        );
    }

    /**
     * Ricerca nelle proprie recensioni.
     *
     * Cerca nel testo della recensione
     * oppure nel nome dell'autore.
     */
    @FXML
    private void onSearch() {

        applicaFiltri();
    }

    /**
     * Richiamato quando cambia il filtro.
     */
    @FXML
    private void onFilterChanged() {

        applicaFiltri();
    }

    /**
     * Reset ricerca e filtri.
     */
    @FXML
    private void onResetFilters() {

        searchField.clear();

        ratingFilterCombo
                .getSelectionModel()
                .clearSelection();

        aggiornaLista(recensioniComplete);
    }

    /**
     * Applica ricerca + filtro valutazione.
     */
    private void applicaFiltri() {

        String query = searchField.getText() == null
                ? ""
                : searchField.getText()
                .trim()
                .toLowerCase();

        String ratingSelezionato =
                ratingFilterCombo.getValue();

        Double ratingMinimo = ratingSelezionato == null
                ? null
                : parseRatingMinimo(ratingSelezionato);

        List<RecensioneDTO> filtrate = new ArrayList<>();

        for (RecensioneDTO recensione : recensioniComplete) {

            // -------------------------
            // RICERCA TESTO
            // -------------------------

            boolean matchTesto =
                    recensione.testo() != null
                            && recensione.testo()
                            .toLowerCase()
                            .contains(query);

            // -------------------------
            // RICERCA AUTORE
            // -------------------------

            boolean matchAutore =
                    recensione.nomeAutore() != null
                            && recensione.nomeAutore()
                            .toLowerCase()
                            .contains(query);

            // -------------------------
            // FILTRO RATING
            // -------------------------

            boolean matchRating =
                    ratingMinimo == null
                            || (
                            recensione.valutazione() != null
                                    && recensione.valutazione()
                                    >= ratingMinimo
                    );

            // -------------------------
            // RISULTATO
            // -------------------------

            boolean matchRicerca =
                    query.isEmpty()
                            || matchTesto
                            || matchAutore;

            if (matchRicerca && matchRating) {
                filtrate.add(recensione);
            }
        }

        aggiornaLista(filtrate);
    }

    /**
     * Converte:
     *
     * "3+"   -> 3.0
     * "3.5+" -> 3.5
     * "4+"   -> 4.0
     * "4.5+" -> 4.5
     */
    private double parseRatingMinimo(String etichetta) {

        return Double.parseDouble(
                etichetta.replace("+", "")
        );
    }

    /**
     * Aggiorna la lista delle recensioni.
     */
    private void aggiornaLista(
            List<RecensioneDTO> nuovaLista) {

        reviewContainer.getChildren().clear();

        for (RecensioneDTO recensione : nuovaLista) {

            Node card = creaCardRecensione(recensione);

            if (card != null) {
                reviewContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(
                nuovaLista.size() + " recensioni trovate"
        );

        reviewScrollPane.setVvalue(0);
    }



    /**
     * Dati di test.
     *
     * TODO: rimuovere quando verranno
     * caricati dal server.
     */
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
                        java.time.LocalDateTime.of(
                                2026, 7, 30, 15, 30
                        ),
                        null
                ),

                new RecensioneDTO(
                        2,
                        4,
                        "Ottima esperienza, personale "
                                + "gentile e servizio veloce.",
                        "Giovanni",
                        java.time.LocalDateTime.of(
                                2026, 7, 25, 18, 20
                        ),
                        "Grazie mille per la recensione!"
                ),

                new RecensioneDTO(
                        3,
                        3,
                        "Il posto è carino, ma secondo me "
                                + "si potrebbe migliorare il servizio.",
                        "Giovanni",
                        java.time.LocalDateTime.of(
                                2026, 7, 20, 12, 10
                        ),
                        null
                )
        );
    }

    private Node creaCardRecensione(RecensioneDTO recensione) {

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
}