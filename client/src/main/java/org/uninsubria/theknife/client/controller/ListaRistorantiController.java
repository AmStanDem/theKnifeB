/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.RicercaSessione;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.common.dto.PaginaRistorantiDTO;
import org.uninsubria.theknife.common.dto.RicercaRistorantiPaginaDTO;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.ITheKnifeServer;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

public class ListaRistorantiController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private ComboBox<String> priceFilterCombo;
    @FXML private ComboBox<String> ratingFilterCombo;
    @FXML private CheckBox deliveryFilterCheckBox;
    @FXML private CheckBox bookingOnlineFilterCheckBox;
    @FXML private MenuButton cuisineMenuButton;
    @FXML private Button resetFiltersButton;

    @FXML private Label locationFilterLabel;
    @FXML private Label resultsCountLabel;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;
    @FXML private Button loadMoreButton;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();
    private final Set<String> selectedCuisines = new HashSet<>();
    private final List<CheckBox> cuisineCheckBoxes = new ArrayList<>();
    private static final int PAGE_SIZE = 20;

    private static final String TUTTI_PREZZI = "Tutti i prezzi";
    private static final String TUTTI_RATING = "Tutte le valutazioni";
    private PauseTransition searchDebounce;
    private long requestGeneration;
    private boolean hasMore;
    private boolean caricamentoInCorso;
    private boolean tipologieCaricate;
    private Double latitudineRicerca;
    private Double longitudineRicerca;
    private String localitaRicerca;
    private Double prezzoMinPersonalizzato;
    private Double prezzoMaxPersonalizzato;
    private String etichettaPrezzoPersonalizzata;

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Inizializzazione ListaRistorantiController...");
        RicercaIniziale.Criteri criteri = RicercaIniziale.consuma();
        if (criteri != null) {
            searchField.setText(criteri.testo() == null ? "" : criteri.testo());
            latitudineRicerca = criteri.latitudine();
            longitudineRicerca = criteri.longitudine();
            localitaRicerca = criteri.localita();
            selectedCuisines.addAll(criteri.tipologieCucina());
            prezzoMinPersonalizzato = criteri.prezzoMin();
            prezzoMaxPersonalizzato = criteri.prezzoMax();
            if (prezzoMinPersonalizzato != null || prezzoMaxPersonalizzato != null) {
                etichettaPrezzoPersonalizzata = formattaEtichettaPrezzo(
                        prezzoMinPersonalizzato, prezzoMaxPersonalizzato);
            }
        }
        configuraFiltriFissi();
        applicaCriteriIniziali(criteri);
        /*
        searchDebounce = new PauseTransition(javafx.util.Duration.millis(350));
        searchDebounce.setOnFinished(event -> caricaPagina(true));*/



        String nomeRistorante = RicercaSessione.getNomeRistorante();


        if (criteri == null && nomeRistorante != null && !nomeRistorante.isBlank()) {
            searchField.setText(nomeRistorante);
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());
        caricaDatiDalServer();
        RemoteRefreshManager.subscribe(
                this, SubscriptionTopic.CATALOG, null, event -> caricaPagina(true),
                () -> caricaPagina(true));
    }

    private void applicaCriteriIniziali(RicercaIniziale.Criteri criteri) {
        locationFilterLabel.setText(localitaRicerca == null || localitaRicerca.isBlank()
                ? "Posizione: ovunque"
                : "Posizione: " + localitaRicerca + " (raggio "
                + (int) RicercaIniziale.RAGGIO_RICERCA_KM + " km)");
        if (criteri == null) {
            return;
        }
        if (etichettaPrezzoPersonalizzata != null) {
            priceFilterCombo.getItems().add(etichettaPrezzoPersonalizzata);
            priceFilterCombo.setValue(etichettaPrezzoPersonalizzata);
        }
        if (criteri.mediaStelleMinima() != null) {
            String etichettaRating = String.format(Locale.ROOT, "%.1f+ ★", criteri.mediaStelleMinima());
            if (!ratingFilterCombo.getItems().contains(etichettaRating)) {
                ratingFilterCombo.getItems().add(etichettaRating);
            }
            ratingFilterCombo.setValue(etichettaRating);
        }
        deliveryFilterCheckBox.setSelected(criteri.delivery());
        bookingOnlineFilterCheckBox.setSelected(criteri.bookingOnline());
        aggiornaEtichettaMenuCucine();
    }



    private void configuraFiltriFissi() {
        priceFilterCombo.getItems().setAll(
                TUTTI_PREZZI,
                "€ (Economico < 25€)",
                "€€ (Medio 25€ - 50€)",
                "€€€ (Medio-Alto 50€ - 80€)",
                "€€€€ (Lusso > 80€)"
        );
        priceFilterCombo.setValue(TUTTI_PREZZI);

        ratingFilterCombo.getItems().setAll(
                TUTTI_RATING,
                "3.0+ ★",
                "3.5+ ★",
                "4.0+ ★",
                "4.5+ ★"
        );
        ratingFilterCombo.setValue(TUTTI_RATING);
    }

    private void caricaDatiDalServer() {
        caricaPagina(true);
    }

    private void caricaPagina(boolean nuovaRicerca) {
        if (caricamentoInCorso && !nuovaRicerca) {
            return;
        }

        if (nuovaRicerca) {
            requestGeneration++;
        }

        long generation = requestGeneration;
        int offset = nuovaRicerca ? 0 : ristorantiCompleti.size();
        RicercaRistorantiPaginaDTO richiesta = creaRichiesta(offset);
        boolean caricaTipologie = !tipologieCaricate;
        caricamentoInCorso = true;
        loadMoreButton.setDisable(true);
        resultsCountLabel.setText("Caricamento ristoranti...");

        Thread.startVirtualThread(() -> {
            try {
                ITheKnifeServer server = ServerConnection.getServer();
                PaginaRistorantiDTO pagina = server.cercaRistorantiPagina(richiesta);
                List<String> tipologie = null;
                if (caricaTipologie) {
                    try {
                        tipologie = server.ottieniTutteTipologieCucina();
                    } catch (SistemaIndisponibileException e) {
                        System.err.println("[SEARCH_WARN] Impossibile caricare le tipologie: " + e.getMessage());
                    }
                }
                List<String> tipologieRisultanti = tipologie;

                Platform.runLater(() -> {
                    if (generation != requestGeneration) {
                        return;
                    }
                    caricamentoInCorso = false;
                    if (tipologieRisultanti != null) {
                        popolaMenuCucine(tipologieRisultanti);
                        tipologieCaricate = true;
                    }
                    if (nuovaRicerca) {
                        ristorantiCompleti.clear();
                    }
                    ristorantiCompleti.addAll(pagina.ristoranti());
                    hasMore = pagina.hasMore();
                    aggiornaLista(nuovaRicerca, offset);
                });
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                System.err.println("[SEARCH_ERROR] Ricerca paginata non riuscita: " + e.getMessage());
                Platform.runLater(() -> {
                    if (generation == requestGeneration) {
                        caricamentoInCorso = false;
                        resultsCountLabel.setText(ristorantiCompleti.size() + " ristoranti caricati");
                        loadMoreButton.setVisible(hasMore);
                        loadMoreButton.setManaged(hasMore);
                        loadMoreButton.setDisable(false);
                        mostraErrore("Ricerca non riuscita", "Impossibile caricare i ristoranti dal server.");
                    }
                });
            }
        });
    }

    private RicercaRistorantiPaginaDTO creaRichiesta(int offset) {
        Double prezzoMin = null;
        Double prezzoMax = null;
        String prezzo = priceFilterCombo.getValue();
        if (prezzo != null && prezzo.equals(etichettaPrezzoPersonalizzata)) {
            prezzoMin = prezzoMinPersonalizzato;
            prezzoMax = prezzoMaxPersonalizzato;
        } else if (prezzo != null) {
            if (prezzo.startsWith("€ (Economico")) {
                prezzoMax = 25.0;
            } else if (prezzo.startsWith("€€ (Medio")) {
                prezzoMin = 25.0;
                prezzoMax = 50.0;
            } else if (prezzo.startsWith("€€€ (Medio-Alto")) {
                prezzoMin = 50.01;
                prezzoMax = 80.0;
            } else if (prezzo.startsWith("€€€€ (Lusso")) {
                prezzoMin = 80.01;
            }
        }

        Double valutazioneMinima = null;
        String valutazione = ratingFilterCombo.getValue();
        if (valutazione != null && !valutazione.equals(TUTTI_RATING)) {
            valutazioneMinima = Double.parseDouble(valutazione.substring(0, valutazione.indexOf('+')));
        }

        return new RicercaRistorantiPaginaDTO(
                latitudineRicerca == null ? RicercaSessione.getLocalita() : "",
                searchField.getText(),
                new ArrayList<>(selectedCuisines),
                prezzoMin,
                prezzoMax,
                valutazioneMinima,
                offset,
                PAGE_SIZE,
                latitudineRicerca,
                longitudineRicerca,
                latitudineRicerca == null ? null : RicercaIniziale.RAGGIO_RICERCA_KM,
                deliveryFilterCheckBox.isSelected(),
                bookingOnlineFilterCheckBox.isSelected()
        );
    }

    private void popolaMenuCucine(List<String> tipologie) {
        VBox menuContent = new VBox(8);
        menuContent.setPadding(new Insets(8));
        menuContent.setPrefWidth(260);

        // Barra di ricerca interna con classe CSS dedicata
        TextField searchCuisineInput = new TextField();
        searchCuisineInput.setPromptText("Filtra cucine...");
        searchCuisineInput.getStyleClass().add("search-cuisine-input");

        // Impedisce alla barra spaziatrice di attivare scorciatoie di menu
        searchCuisineInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                searchCuisineInput.insertText(searchCuisineInput.getCaretPosition(), " ");
                event.consume();
            }
        });

        VBox checkBoxesBox = new VBox(6);
        cuisineCheckBoxes.clear();

        for (String tipo : tipologie) {
            if (tipo == null || tipo.isBlank()) continue;

            CheckBox cb = new CheckBox(tipo.trim());
            cb.setStyle("-fx-text-fill: #1e293b;"); // Garanzia inline contro override di Modena
            cb.setSelected(selectedCuisines.contains(cb.getText()));

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedCuisines.add(cb.getText());
                } else {
                    selectedCuisines.remove(cb.getText());
                }
                aggiornaEtichettaMenuCucine();
                //caricaPagina(true);
            });

            cuisineCheckBoxes.add(cb);
            checkBoxesBox.getChildren().add(cb);
        }
        aggiornaEtichettaMenuCucine();

        // Filtro in tempo reale sui CheckBox
        searchCuisineInput.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = (newVal == null) ? "" : newVal.trim().toLowerCase();
            for (CheckBox cb : cuisineCheckBoxes) {
                boolean visibile = q.isEmpty() || cb.getText().toLowerCase().contains(q);
                cb.setVisible(visibile);
                cb.setManaged(visibile);
            }
        });

        ScrollPane scroll = new ScrollPane(checkBoxesBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(180);
        scroll.setMaxHeight(220);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button clearBtn = new Button("Deseleziona tutte");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            for (CheckBox cb : cuisineCheckBoxes) cb.setSelected(false);
            selectedCuisines.clear();
            aggiornaEtichettaMenuCucine();
            //caricaPagina(true);
        });

        menuContent.getChildren().addAll(searchCuisineInput, scroll, clearBtn);

        CustomMenuItem item = new CustomMenuItem(menuContent, false);
        item.setHideOnClick(false);
        cuisineMenuButton.getItems().setAll(item);

        // Quando si apre il menu, porta subito il focus sulla casella di testo
        cuisineMenuButton.setOnShowing(e -> Platform.runLater(searchCuisineInput::requestFocus));
    }

    private void aggiornaEtichettaMenuCucine() {
        if (selectedCuisines.isEmpty()) {
            cuisineMenuButton.setText("Tipo di cucina");
        } else if (selectedCuisines.size() == 1) {
            cuisineMenuButton.setText(selectedCuisines.iterator().next());
        } else {
            cuisineMenuButton.setText("Cucine (" + selectedCuisines.size() + ")");
        }
    }

    @FXML
    private void onSearch() {
        caricaPagina(true);
    }

    @FXML
    private void onFilterChanged() {
        //caricaPagina(true);
    }

    @FXML
    private void onResetFilters() {
        searchDebounce.stop();
        searchField.clear();
        searchDebounce.stop();
        priceFilterCombo.setValue(TUTTI_PREZZI);
        if (etichettaPrezzoPersonalizzata != null) {
            priceFilterCombo.getItems().remove(etichettaPrezzoPersonalizzata);
            etichettaPrezzoPersonalizzata = null;
        }
        prezzoMinPersonalizzato = null;
        prezzoMaxPersonalizzato = null;
        ratingFilterCombo.setValue(TUTTI_RATING);
        deliveryFilterCheckBox.setSelected(false);
        bookingOnlineFilterCheckBox.setSelected(false);
        latitudineRicerca = null;
        longitudineRicerca = null;
        localitaRicerca = null;
        locationFilterLabel.setText("Posizione: ovunque");

        for (CheckBox cb : cuisineCheckBoxes) cb.setSelected(false);
        selectedCuisines.clear();
        aggiornaEtichettaMenuCucine();

        caricaPagina(true);
    }

    @FXML
    private void onLoadMore() {
        caricaPagina(false);
    }

    @FXML
    private void onHomeClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    private void aggiornaLista(boolean nuovaRicerca, int offset) {
        if (nuovaRicerca) {
            restaurantContainer.getChildren().clear();
        }
        for (int i = offset; i < ristorantiCompleti.size(); i++) {
            RistoranteDTO r = ristorantiCompleti.get(i);
            Node card = creaCardRistorante(r);
            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(ristorantiCompleti.size() + " ristoranti caricati");
        loadMoreButton.setVisible(hasMore);
        loadMoreButton.setManaged(hasMore);
        loadMoreButton.setDisable(false);
        if (nuovaRicerca) {
            restaurantScrollPane.setVvalue(0);
        }
    }

    private Node creaCardRistorante(RistoranteDTO r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/uninsubria/theknife/client/views/RestaurantItem.fxml"));
            Pane card = loader.load();

            RestaurantItemController controller = loader.getController();
            controller.setRistorante(r);
            controller.setOnDettagliAction(this::apriDettagliRistorante);

            return card;
        } catch (IOException e) {
            System.err.println("[DEBUG_ERROR] Errore caricamento FXML card: " + e.getMessage());
            return null;
        }
    }

    private void apriDettagliRistorante(RistoranteDTO r) {
        System.out.println("Apertura dettagli: " + r.nome());
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private String formattaEtichettaPrezzo(Double prezzoMin, Double prezzoMax) {
        String minimo = prezzoMin == null ? "qualsiasi" : "da " + formatoEuro(prezzoMin);
        String massimo = prezzoMax == null ? "350+ €" : "fino a " + formatoEuro(prezzoMax);
        return "Personalizzato: " + minimo + ", " + massimo;
    }

    private String formatoEuro(double valore) {
        return String.format(Locale.ROOT, "%.0f €", valore);
    }
}
