package org.uninsubria.clientTK.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.rmi.ITheKnifeServer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class ListaRistorantiController {

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

    // Riferimento al server RMI
    private ITheKnifeServer serverRMI;

    @FXML
    public void initialize() {
        configuraFiltri();
        connettiAlServer();
        caricaRistorantiDalServer();
    }

    /**
     * Stabilisce la connessione RMI col server.
     * Assicurati che l'IP, la porta e il nome dell'oggetto registrato corrispondano a quelli del server.
     */
    private void connettiAlServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            serverRMI = (ITheKnifeServer) registry.lookup("TheKnifeServer");
        } catch (Exception e) {
            System.err.println("Errore di connessione al server RMI: " + e.getMessage());
            mostraErrore("Connessione fallita", "Impossibile collegarsi al server TheKnife.");
        }
    }

    /**
     * Recupera l'elenco dei ristoranti dal database tramite RMI.
     */
    private void caricaRistorantiDalServer() {
        if (serverRMI == null) return;

        try {
            FiltriRicercaDTO filtri = new FiltriRicercaDTO("Modena",null,null,null,null,null,null,null, null,null);
            // Chiede al server tutti i ristoranti (filtri null)
            List<RistoranteDTO> datiDalDB = serverRMI.cercaRistoranti(filtri);

            if (datiDalDB != null) {
                ristorantiCompleti.clear();
                ristorantiCompleti.addAll(datiDalDB);
                aggiornaLista(ristorantiCompleti);
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento dei ristoranti: " + e.getMessage());
            mostraErrore("Errore dati", "Impossibile caricare la lista dei ristoranti dal server.");
        }
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
            boolean matchCucina = r.tipologieCucina() != null &&
                    r.tipologieCucina().stream()
                            .anyMatch(c -> c != null && c.toLowerCase().contains(query));

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

    /** Applica congiuntamente i filtri di prezzo, rating e cucina selezionati localmente. */
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
                    || (r.tipologieCucina() != null &&
                    r.tipologieCucina().stream()
                            .anyMatch(c -> c.equalsIgnoreCase(cucinaSelezionata)));

            if (okPrezzo && okRating && okCucina) {
                filtrati.add(r);
            }
        }
        aggiornaLista(filtrati);
    }

    private double parseRatingMinimo(String etichetta) {
        return Double.parseDouble(etichetta.replace("+", ""));
    }

    static int fasciaPrezzoDaImporto(Double prezzoMedio) {
        if (prezzoMedio == null) return 1;
        if (prezzoMedio < 15) return 1;
        if (prezzoMedio < 30) return 2;
        if (prezzoMedio < 50) return 3;
        return 4;
    }

    /**
     * Ricostruisce il contenuto del VBox all'interno dello ScrollPane.
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
        restaurantScrollPane.setVvalue(0);
    }

    /** Carica la card FXML per un singolo ristorante e collega il controller. */
    private Node creaCardRistorante(RistoranteDTO ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/uninsubria/clientTK/views/RestaurantItem.fxml"));
            Pane cardPane = loader.load();

            // Scommentare e adattare se RestaurantItemController è stato creato
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
        // TODO: navigazione verso la schermata di dettaglio ristorante
        System.out.println("Apertura dettagli per: " + ristorante.nome());
    }

    /** Mostra un alert all'utente in caso di problemi di rete. */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}