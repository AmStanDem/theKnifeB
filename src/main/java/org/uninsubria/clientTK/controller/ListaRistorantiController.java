package org.uninsubria.clientTK.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.common.dto.FiltriRicercaDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.rmi.ITheKnifeServer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ListaRistorantiController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private ComboBox<String> priceFilterCombo;
    @FXML private ComboBox<String> ratingFilterCombo;
    @FXML private MenuButton cuisineMenuButton;
    @FXML private Button resetFiltersButton;

    @FXML private Label resultsCountLabel;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();
    private final Set<String> selectedCuisines = new HashSet<>();
    private final List<CheckBox> cuisineCheckBoxes = new ArrayList<>();

    private ITheKnifeServer serverRMI;

    private static final String TUTTI_PREZZI = "Tutti i prezzi";
    private static final String TUTTI_RATING = "Tutti i rating";

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Inizializzazione ListaRistorantiController...");
        configuraFiltriFissi();
        connettiAlServer();
        caricaDatiDalServer();

        // Filtro real-time mentre si scrive nella casella principale
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri());
    }

    private void connettiAlServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            serverRMI = (ITheKnifeServer) registry.lookup("TheKnifeServer");
            System.out.println("[DEBUG] Connessione RMI stabilita con successo.");
        } catch (Exception e) {
            System.err.println("[DEBUG_ERROR] Connessione fallita: " + e.getMessage());
            mostraErrore("Connessione fallita", "Impossibile collegarsi al server.");
        }
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
        if (serverRMI == null) return;

        new Thread(() -> {
            List<RistoranteDTO> datiScaricati = null;

            // 1. Recupero dei ristoranti con fallback di sicurezza
            try {
                System.out.println("[DEBUG] Tentativo recupero ristoranti con filtri null...");
                datiScaricati = serverRMI.cercaRistoranti(null);
            } catch (Exception e) {
                System.err.println("[DEBUG_WARN] cercaRistoranti(null) ha fallito: " + e.getMessage());
            }

            // Se il server richiede un DTO non nullo o una città
            if (datiScaricati == null || datiScaricati.isEmpty()) {
                try {
                    System.out.println("[DEBUG] Fallback: richiesta ristoranti per città 'Modena'...");
                    FiltriRicercaDTO filtriModena = new FiltriRicercaDTO(
                            "Modena", null, null, null, null, null, null, null, null, null
                    );
                    datiScaricati = serverRMI.cercaRistoranti(filtriModena);
                } catch (Exception ex) {
                    System.err.println("[DEBUG_ERROR] Errore anche nel fallback con 'Modena': " + ex.getMessage());
                }
            }

            final List<RistoranteDTO> elencoFinale = datiScaricati != null ? datiScaricati : Collections.emptyList();
            System.out.println("[DEBUG] Ristoranti scaricati dal server: " + elencoFinale.size());

            // 2. Recupero cucine dal DB o per estrazione diretta dai dati
            List<String> tipologie = new ArrayList<>();
            try {
                tipologie = serverRMI.ottieniTutteTipologieCucina();
                System.out.println("[DEBUG] Cucine lette via RMI: " + (tipologie != null ? tipologie.size() : 0));
            } catch (Throwable t) {
                System.out.println("[DEBUG] Metodo RMI cucine non pronto sul server, estrazione automatica dai ristoranti.");
            }

            // Fallback estrazione: legge le tipologie direttamente dai ristoranti caricati
            if (tipologie == null || tipologie.isEmpty()) {
                Set<String> setUnico = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                for (RistoranteDTO r : elencoFinale) {
                    if (r.tipologieCucina() != null) {
                        for (String c : r.tipologieCucina()) {
                            if (c != null && !c.isBlank()) setUnico.add(c.trim());
                        }
                    }
                }
                tipologie = new ArrayList<>(setUnico);
            }

            final List<String> cucineFinali = tipologie;

            Platform.runLater(() -> {
                popolaMenuCucine(cucineFinali);
                ristorantiCompleti.setAll(elencoFinale);
                applicaFiltri();
            });
        }).start();
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

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedCuisines.add(cb.getText());
                } else {
                    selectedCuisines.remove(cb.getText());
                }
                aggiornaEtichettaMenuCucine();
                applicaFiltri();
            });

            cuisineCheckBoxes.add(cb);
            checkBoxesBox.getChildren().add(cb);
        }

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
            applicaFiltri();
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
        applicaFiltri();
    }

    @FXML
    private void onFilterChanged() {
        applicaFiltri();
    }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        priceFilterCombo.setValue(TUTTI_PREZZI);
        ratingFilterCombo.setValue(TUTTI_RATING);

        for (CheckBox cb : cuisineCheckBoxes) cb.setSelected(false);
        selectedCuisines.clear();
        aggiornaEtichettaMenuCucine();

        applicaFiltri();
    }

    private void applicaFiltri() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String prezzoSel = priceFilterCombo.getValue();
        String ratingSel = ratingFilterCombo.getValue();

        List<RistoranteDTO> filtrati = new ArrayList<>();

        for (RistoranteDTO r : ristorantiCompleti) {
            // Testo
            boolean matchTesto = query.isEmpty()
                    || (r.nome() != null && r.nome().toLowerCase().contains(query))
                    || (r.indirizzo() != null && r.indirizzo().toLowerCase().contains(query))
                    || (r.citta() != null && r.citta().toLowerCase().contains(query))
                    || (r.tipologieCucina() != null && r.tipologieCucina().stream()
                    .anyMatch(c -> c != null && c.toLowerCase().contains(query)));

            // Prezzo
            boolean matchPrezzo = true;
            if (prezzoSel != null && !prezzoSel.equals(TUTTI_PREZZI)) {
                double p = (r.prezzoMedio() != null) ? r.prezzoMedio() : 0.0;
                if (prezzoSel.startsWith("€ (Economico")) matchPrezzo = p < 25.0;
                else if (prezzoSel.startsWith("€€ (Medio")) matchPrezzo = p >= 25.0 && p <= 50.0;
                else if (prezzoSel.startsWith("€€€ (Medio-Alto")) matchPrezzo = p > 50.0 && p <= 80.0;
                else if (prezzoSel.startsWith("€€€€ (Lusso")) matchPrezzo = p > 80.0;
            }

            // Rating
            boolean matchRating = true;
            if (ratingSel != null && !ratingSel.equals(TUTTI_RATING)) {
                try {
                    double soglia = Double.parseDouble(ratingSel.substring(0, 3));
                    matchRating = (r.mediaStelle() != null && r.mediaStelle() >= soglia);
                } catch (Exception ignored) {
                    matchRating = true;
                }
            }

            // Cucine
            boolean matchCucine = true;
            if (!selectedCuisines.isEmpty()) {
                matchCucine = r.tipologieCucina() != null && r.tipologieCucina().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .anyMatch(c -> selectedCuisines.stream().anyMatch(s -> s.equalsIgnoreCase(c)));
            }

            if (matchTesto && matchPrezzo && matchRating && matchCucine) {
                filtrati.add(r);
            }
        }

        System.out.println("[DEBUG] Ristoranti visualizzati dopo i filtri: " + filtrati.size());
        aggiornaLista(filtrati);
    }

    private void aggiornaLista(List<RistoranteDTO> lista) {
        restaurantContainer.getChildren().clear();

        for (RistoranteDTO r : lista) {
            Node card = creaCardRistorante(r);
            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(lista.size() + " ristoranti trovati");
        restaurantScrollPane.setVvalue(0);
    }

    private Node creaCardRistorante(RistoranteDTO r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/uninsubria/clientTK/views/RestaurantItem.fxml"));
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
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
