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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.RicercaSessione;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.client.util.components.CuisineSelector;
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
    @FXML private CuisineSelector cuisineSelector;
    @FXML private Button resetFiltersButton;

    @FXML private Label locationFilterLabel;
    @FXML private Label resultsCountLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ScrollPane restaurantScrollPane;
    @FXML private VBox restaurantContainer;
    @FXML private Button loadMoreButton;

    private final ObservableList<RistoranteDTO> ristorantiCompleti = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 20;

    private static final String TUTTI_PREZZI = "Tutti i prezzi";
    private static final String TUTTI_RATING = "Tutte le valutazioni";
    private PauseTransition searchDebounce;
    private PauseTransition retryTransition;
    private long requestGeneration;
    private boolean hasMore;
    private boolean caricamentoInCorso;
    private boolean tipologieCaricate;
    private boolean serverNonDisponibile;
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
            cuisineSelector.setSelectedCuisines(criteri.tipologieCucina());
            prezzoMinPersonalizzato = criteri.prezzoMin();
            prezzoMaxPersonalizzato = criteri.prezzoMax();
            if (prezzoMinPersonalizzato != null || prezzoMaxPersonalizzato != null) {
                etichettaPrezzoPersonalizzata = formattaEtichettaPrezzo(
                        prezzoMinPersonalizzato, prezzoMaxPersonalizzato);
            }
        }
        configuraFiltriFissi();
        applicaCriteriIniziali(criteri);
        searchDebounce = new PauseTransition(Duration.millis(350));
        searchDebounce.setOnFinished(event -> caricaPagina(true));
        retryTransition = new PauseTransition(Duration.seconds(2));
        restaurantContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                searchDebounce.stop();
                retryTransition.stop();
            }
        });

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
        aggiornaEtichettaPosizione();
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
    }



    private void configuraFiltriFissi() {
        priceFilterCombo.getItems().setAll(
                TUTTI_PREZZI,
                "€ (Economico ≤ 25€)",
                "€€ (Medio 25,01€ - 50€)",
                "€€€ (Medio-Alto 50,01€ - 80€)",
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
        if (nuovaRicerca) {
            requestGeneration++;
            retryTransition.stop();
        }
        if (latitudineRicerca == null || longitudineRicerca == null
                || localitaRicerca == null || localitaRicerca.isBlank()) {
            resultsCountLabel.setText("Seleziona una posizione prima di cercare i ristoranti.");
            caricamentoInCorso = false;
            serverNonDisponibile = false;
            nascondiCaricamento();
            retryTransition.stop();
            loadMoreButton.setVisible(false);
            loadMoreButton.setManaged(false);
            loadMoreButton.setDisable(true);
            return;
        }
        if (caricamentoInCorso && !nuovaRicerca) {
            return;
        }

        long generation = requestGeneration;
        int offset = nuovaRicerca ? 0 : ristorantiCompleti.size();
        RicercaRistorantiPaginaDTO richiesta = creaRichiesta(offset);
        boolean caricaTipologie = !tipologieCaricate;
        caricamentoInCorso = true;
        loadMoreButton.setDisable(true);
        mostraCaricamento("Caricamento ristoranti...");

        eseguiRicerca(richiesta, nuovaRicerca, offset, caricaTipologie, generation);
    }

    private void eseguiRicerca(
            RicercaRistorantiPaginaDTO richiesta,
            boolean nuovaRicerca,
            int offset,
            boolean caricaTipologie,
            long generation
    ) {
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
                    serverNonDisponibile = false;
                    retryTransition.stop();
                    nascondiCaricamento();
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
                if (e instanceof RemoteException) {
                    ServerConnection.invalidaConnessione();
                }
                Platform.runLater(() -> {
                    if (generation == requestGeneration) {
                        if (!serverNonDisponibile) {
                            System.err.println("[SEARCH_ERROR] Ricerca paginata non riuscita: "
                                    + e.getMessage());
                            serverNonDisponibile = true;
                        }
                        mostraCaricamento("Server non disponibile. Riprovo automaticamente...");
                        retryTransition.setOnFinished(event ->
                                eseguiRicerca(richiesta, nuovaRicerca, offset, caricaTipologie, generation));
                        retryTransition.playFromStart();
                    }
                });
            }
        });
    }

    private void mostraCaricamento(String messaggio) {
        resultsCountLabel.setText(messaggio);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
    }

    private void nascondiCaricamento() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
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
                prezzoMin = 25.01;
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
                "",
                searchField.getText(),
                cuisineSelector.getSelectedCuisines(),
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
        cuisineSelector.setCuisines(tipologie);
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
        aggiornaEtichettaPosizione();

        cuisineSelector.clearSelection();

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

    private String formattaEtichettaPrezzo(Double prezzoMin, Double prezzoMax) {
        String minimo = prezzoMin == null ? "qualsiasi" : "da " + formatoEuro(prezzoMin);
        String massimo = prezzoMax == null ? "350+ €" : "fino a " + formatoEuro(prezzoMax);
        return "Personalizzato: " + minimo + ", " + massimo;
    }

    private String formatoEuro(double valore) {
        return String.format(Locale.ROOT, "%.0f €", valore);
    }

    private void aggiornaEtichettaPosizione() {
        locationFilterLabel.setText(localitaRicerca == null || localitaRicerca.isBlank()
                ? "Posizione: non selezionata"
                : "Posizione: " + localitaRicerca + " (raggio "
                + (int) RicercaIniziale.RAGGIO_RICERCA_KM + " km)");
    }
}
