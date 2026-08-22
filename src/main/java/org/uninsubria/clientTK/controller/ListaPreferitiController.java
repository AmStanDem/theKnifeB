package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaPreferitiController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private ComboBox<String> priceFilterCombo;
    @FXML private ComboBox<String> ratingFilterCombo;
    @FXML private ComboBox<String> cuisineFilterCombo;
    @FXML private Button resetFiltersButton;

    @FXML private Label resultsCountLabel;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configuraFiltri();
        caricaRistorantiDiTest(); // TODO: sostituire con chiamata al servizio/socket reale verso il server
        aggiornaLista(ristorantiCompleti);
    }

    /** Popola le ComboBox dei filtri. */
    private void configuraFiltri() {
        priceFilterCombo.setItems(FXCollections.observableArrayList("€", "€€", "€€€", "€€€€"));
        ratingFilterCombo.setItems(FXCollections.observableArrayList("3+", "3.5+", "4+", "4.5+"));
        cuisineFilterCombo.setItems(FXCollections.observableArrayList(
                "Italiana", "Giapponese", "Messicana", "Pizzeria", "Vegana"));
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<RistoranteDTO> filtrati = new ArrayList<>();
        for (RistoranteDTO r : ristorantiCompleti) {
            boolean matchNome = r.nome() != null && r.nome().toLowerCase().contains(query);
            boolean matchIndirizzo = r.indirizzo() != null && r.indirizzo().toLowerCase().contains(query);
            boolean matchCucina = r.tipoCucina() != null && r.tipoCucina().toLowerCase().contains(query);

            if (query.isEmpty() || matchNome || matchIndirizzo || matchCucina) {
                filtrati.add(r);
            }
        }
        aggiornaLista(filtrati);
    }

    @FXML
    private void onFilterChanged() {
        applicaFiltri();
    }

    @FXML
    private void onResetFilters() {
        priceFilterCombo.getSelectionModel().clearSelection();
        ratingFilterCombo.getSelectionModel().clearSelection();
        cuisineFilterCombo.getSelectionModel().clearSelection();
        searchField.clear();
        aggiornaLista(ristorantiCompleti);
    }

    /** Applica congiuntamente i filtri di prezzo, rating e cucina selezionati. */
    private void applicaFiltri() {
        String prezzoSelezionato = priceFilterCombo.getValue();
        String ratingSelezionato = ratingFilterCombo.getValue();
        String cucinaSelezionata = cuisineFilterCombo.getValue();

        List<RistoranteDTO> filtrati = new ArrayList<>();
        for (RistoranteDTO r : ristorantiCompleti) {

            boolean okPrezzo = prezzoSelezionato == null
                    || prezzoSelezionato.length() == fasciaPrezzoDaImporto(r.prezzoMedio());

            boolean okRating = ratingSelezionato == null
                    || (r.mediaStelle() != null && r.mediaStelle() >= parseRatingMinimo(ratingSelezionato));

            boolean okCucina = cucinaSelezionata == null
                    || (r.tipoCucina() != null && r.tipoCucina().equalsIgnoreCase(cucinaSelezionata));

            if (okPrezzo && okRating && okCucina) {
                filtrati.add(r);
            }
        }
        aggiornaLista(filtrati);
    }

    private double parseRatingMinimo(String etichetta) {
        return Double.parseDouble(etichetta.replace("+", ""));
    }

    /**
     * Converte il prezzo medio reale (es. 12.50€) in una fascia 1-4,
     * usata sia per il filtro sia per la visualizzazione nelle card.
     * Soglie indicative, personalizzabili in base ai dati reali.
     */
    static int fasciaPrezzoDaImporto(Double prezzoMedio) {
        if (prezzoMedio == null) return 1;
        if (prezzoMedio < 15) return 1;
        if (prezzoMedio < 30) return 2;
        if (prezzoMedio < 50) return 3;
        return 4;
    }

    /**
     * Ricostruisce il contenuto del VBox all'interno dello ScrollPane,
     * caricando una card RestaurantItem.fxml per ogni ristorante.
     */
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
                    "/org/uninsubria/clientTK/views/PreferitoItem.fxml"));
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