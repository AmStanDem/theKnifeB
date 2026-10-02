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

/**
 * Controller JavaFX dell'interfaccia dedicata alla visualizzazione
 * dell'elenco dei ristoranti.
 * <p>
 * Gestisce la visualizzazione dei ristoranti, la ricerca tramite testo
 * e l'applicazione dei filtri relativi a prezzo, valutazione e tipologia
 * di cucina.
 * </p>
 *
 * <p>
 * Il controller è associato alla relativa vista FXML tramite JavaFX
 * e utilizza i componenti definiti nella vista per ricevere gli input
 * dell'utente e aggiornare dinamicamente l'elenco dei ristoranti.
 * </p>
 */
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

    @FXML
    /**
     * Inizializza il controller dopo il caricamento della relativa vista FXML.
     * <p>
     * Configura i filtri disponibili, carica i dati iniziali dei ristoranti
     * e aggiorna la lista visualizzata all'interno dell'interfaccia.
     * </p>
     */
    public void initialize() {
        configuraFiltri();
        caricaRistorantiDiTest(); // TODO: sostituire con chiamata al servizio/socket reale verso il server
        aggiornaLista(ristorantiCompleti);
    }

    /**
     * Configura i filtri disponibili nell'interfaccia.
     * <p>
     * Inserisce nelle rispettive {@code ComboBox} le fasce di prezzo,
     * le valutazioni minime e le tipologie di cucina selezionabili
     * dall'utente.
     * </p>
     */
    private void configuraFiltri() {
        priceFilterCombo.setItems(FXCollections.observableArrayList("€", "€€", "€€€", "€€€€"));
        ratingFilterCombo.setItems(FXCollections.observableArrayList("3+", "3.5+", "4+", "4.5+"));
        cuisineFilterCombo.setItems(FXCollections.observableArrayList(
                "Italiana", "Giapponese", "Messicana", "Pizzeria", "Vegana"));
    }

    @FXML
    /**
     * Gestisce l'esecuzione della ricerca dei ristoranti.
     * <p>
     * Confronta il testo inserito dall'utente con il nome,
     * l'indirizzo e la tipologia di cucina dei ristoranti.
     * La ricerca non distingue tra lettere maiuscole e minuscole.
     * </p>
     *
     * <p>
     * Se il campo di ricerca è vuoto, vengono visualizzati tutti
     * i ristoranti disponibili.
     * </p>
     */
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
    /**
     * Gestisce la modifica dei filtri selezionati dall'utente.
     * <p>
     * Richiama il metodo che applica i filtri correntemente selezionati
     * alla lista completa dei ristoranti.
     * </p>
     */
    private void onFilterChanged() {
        applicaFiltri();
    }

    @FXML
    /**
     * Gestisce il reset dei filtri e della ricerca.
     * <p>
     * Rimuove le selezioni effettuate nelle {@code ComboBox}, cancella
     * il testo inserito nel campo di ricerca e ripristina la visualizzazione
     * dell'elenco completo dei ristoranti.
     * </p>
     */
    private void onResetFilters() {
        priceFilterCombo.getSelectionModel().clearSelection();
        ratingFilterCombo.getSelectionModel().clearSelection();
        cuisineFilterCombo.getSelectionModel().clearSelection();
        searchField.clear();
        aggiornaLista(ristorantiCompleti);
    }

    /**
     * Applica congiuntamente i filtri di prezzo, valutazione e tipologia
     * di cucina selezionati dall'utente.
     * <p>
     * Un ristorante viene inserito nella lista risultante solamente
     * se soddisfa tutti i filtri attualmente selezionati.
     * </p>
     */
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

    /**
     * Converte l'etichetta relativa alla valutazione minima in un valore
     * numerico utilizzabile per il confronto.
     *
     * @param etichetta etichetta della valutazione minima, ad esempio {@code "4+"}
     * @return valore numerico della valutazione minima
     */
    private double parseRatingMinimo(String etichetta) {
        return Double.parseDouble(etichetta.replace("+", ""));
    }

    /**
     * Determina la fascia di prezzo di un ristorante in base al suo
     * prezzo medio.
     * <p>
     * Il prezzo viene convertito in una fascia compresa tra 1 e 4,
     * corrispondente rispettivamente a {@code €}, {@code €€},
     * {@code €€€} e {@code €€€€}.
     * </p>
     *
     * <p>
     * Le soglie utilizzate sono indicative e possono essere modificate
     * in base ai dati reali utilizzati dall'applicazione.
     * </p>
     *
     * @param prezzoMedio prezzo medio del ristorante
     * @return numero della fascia di prezzo, compreso tra 1 e 4
     */
    static int fasciaPrezzoDaImporto(Double prezzoMedio) {
        if (prezzoMedio == null) return 1;
        if (prezzoMedio < 15) return 1;
        if (prezzoMedio < 30) return 2;
        if (prezzoMedio < 50) return 3;
        return 4;
    }

    /**
     * Aggiorna la lista dei ristoranti visualizzata nell'interfaccia.
     * <p>
     * Rimuove le card precedentemente visualizzate e crea una nuova card
     * per ogni ristorante presente nella lista ricevuta.
     * </p>
     *
     * <p>
     * Aggiorna inoltre il numero di risultati mostrati e riporta lo
     * {@code ScrollPane} all'inizio della lista.
     * </p>
     *
     * @param nuovaLista lista dei ristoranti da visualizzare
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

    /**
     * Crea la card grafica relativa a un singolo ristorante.
     * <p>
     * Carica la vista {@code RestaurantItem.fxml}, recupera il relativo
     * controller e associa ad esso i dati del ristorante e l'azione
     * da eseguire per accedere ai dettagli.
     * </p>
     *
     * @param ristorante ristorante da visualizzare nella card
     * @return nodo JavaFX contenente la card del ristorante,
     *         oppure {@code null} in caso di errore durante il caricamento
     */
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

    /**
     * Gestisce l'apertura dei dettagli relativi a un ristorante.
     * <p>
     * Attualmente il metodo visualizza solamente il nome del ristorante
     * nella console. La navigazione verso la schermata contenente i
     * dettagli verrà implementata successivamente.
     * </p>
     *
     * @param ristorante ristorante del quale visualizzare i dettagli
     */
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
                        "Via Roma 12",
                        "Milano",
                        "Italia",
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
                        "Corso Buenos Aires 5",
                        "Milano",
                        "Italia",
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
                        "Piazza Duomo 1",
                        "Milano",
                        "Italia",
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