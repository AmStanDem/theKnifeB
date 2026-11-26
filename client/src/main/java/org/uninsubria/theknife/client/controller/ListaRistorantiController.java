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

/**
 * Controller JavaFX della lista dei ristoranti risultanti da una ricerca.
 * <p>
 * Gestisce i criteri di testo, posizione, prezzo, valutazione, cucina e
 * servizi, interrogando il server in modalità paginata. La vista supporta
 * il caricamento progressivo dei risultati e il caricamento automatico
 * delle tipologie di cucina.
 * </p>
 * <p>
 * Il controllore aggiorna anche i risultati in seguito agli eventi remoti
 * del catalogo e adotta un ritardo temporale per evitare ricerche a ogni
 * modifica del testo digitato dall'utente.
 * </p>
 */
public class ListaRistorantiController {

    private static SearchSnapshot snapshotRicerca;

    private record SearchSnapshot(
            String testo,
            String localita,
            Double latitudine,
            Double longitudine,
            String filtroPrezzo,
            String filtroValutazione,
            Double prezzoMinPersonalizzato,
            Double prezzoMaxPersonalizzato,
            List<String> tipologieCucina,
            List<String> tipologieDisponibili,
            boolean consegna,
            boolean prenotazioneOnline,
            List<RistoranteDTO> ristoranti,
            boolean ciSonoAltri,
            double posizioneScroll
    ) {
    }

    /**
     * Elimina la ricerca salvata in attesa di ripristino.
     * <p>
     * Va invocato al logout per non applicare i filtri di un utente precedente
     * al primo caricamento della lista da parte di un utente successivo.
     * </p>
     */
    public static void pulisciRicercaSalvata() {
        snapshotRicerca = null;
    }

    @FXML private TextField searchField;

    @FXML private ComboBox<String> priceFilterCombo;
    @FXML private ComboBox<String> ratingFilterCombo;
    @FXML private CheckBox deliveryFilterCheckBox;
    @FXML private CheckBox bookingOnlineFilterCheckBox;
    @FXML private CuisineSelector cuisineSelector;

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
    private PauseTransition retryTransition;
    private long requestGeneration;
    private boolean ciSonoAltri;
    private boolean caricamentoInCorso;
    private boolean tipologieCaricate;
    private boolean serverNonDisponibile;
    private List<String> tipologieDisponibili = List.of();
    private Double latitudineRicerca;
    private Double longitudineRicerca;
    private String localitaRicerca;
    private Double prezzoMinPersonalizzato;
    private Double prezzoMaxPersonalizzato;
    private String etichettaPrezzoPersonalizzata;

    /**
     * Inizializza i filtri, ripristina i criteri salvati e avvia il primo
     * caricamento dei ristoranti.
     * <p>
     * Configura il debounce della ricerca, l'aggiornamento automatico remoto
     * e il comportamento di arresto delle operazioni quando la vista viene
     * rimossa dalla scena.
     * </p>
     */
    @FXML
    public void initialize() {
        RicercaIniziale.Criteri criteri = RicercaIniziale.consuma();
        SearchSnapshot istantanea = snapshotRicerca;
        snapshotRicerca = null;
        if (criteri != null) {
            searchField.setText(criteri.testo() == null ? "" : criteri.testo());
            latitudineRicerca = criteri.latitudine();
            longitudineRicerca = criteri.longitudine();
            localitaRicerca = criteri.localita();
            cuisineSelector.impostaCucineSelezionate(criteri.tipologieCucina());
            prezzoMinPersonalizzato = criteri.prezzoMin();
            prezzoMaxPersonalizzato = criteri.prezzoMax();
            if (prezzoMinPersonalizzato != null || prezzoMaxPersonalizzato != null) {
                etichettaPrezzoPersonalizzata = formattaEtichettaPrezzo(
                        prezzoMinPersonalizzato, prezzoMaxPersonalizzato);
            }
        }
        configuraFiltriFissi();
        applicaCriteriIniziali(criteri);
        retryTransition = new PauseTransition(Duration.seconds(2));
        restaurantContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                retryTransition.stop();
            }
        });

        if (istantanea != null) {
            ripristinaRicerca(istantanea);
        }

        // Non attiviamo la ricerca automatica durante la digitazione:
        // l'utente deve premere il pulsante di ricerca o premere Invio.
        if (istantanea == null) {
            caricaDatiDalServer();
        }
        RemoteRefreshManager.sottoscrivi(
                this, SubscriptionTopic.CATALOGO, null, evento -> caricaPagina(true),
                () -> caricaPagina(true));
    }

    /**
     * Applica alla vista i criteri iniziali recuperati dalla ricerca precedente.
     *
     * @param criteri criteri di ricerca iniziali da applicare
     */
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
        deliveryFilterCheckBox.setSelected(criteri.consegna());
        bookingOnlineFilterCheckBox.setSelected(criteri.prenotazioneOnline());
    }



    /**
     * Configura le opzioni fisse dei filtri prezzo e valutazione.
     */
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

    /**
     * Avvia il caricamento iniziale dei dati dal server.
     */
    private void caricaDatiDalServer() {
        caricaPagina(true);
    }

    /**
     * Avvia il caricamento di una pagina dei ristoranti.
     * <p>
     * Una nuova ricerca riparte dall'inizio; il caricamento successivo usa
     * il numero di elementi già ottenuti come offset. Se manca una posizione
     * valida, la ricerca viene sospesa.
     * </p>
     *
     * @param nuovaRicerca {@code true} per avviare una nuova ricerca
     */
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

    /**
     * Esegue la richiesta paginata in un thread virtuale.
     * <p>
     * Dopo il risultato aggiorna la vista sul thread JavaFX e può ritentare
     * automaticamente la ricerca quando il server non è disponibile.
     * </p>
     *
     * @param richiesta criteri e paginazione da inviare al server
     * @param nuovaRicerca indica se la lista deve essere azzerata
     * @param offset posizione iniziale della pagina richiesta
     * @param caricaTipologie indica se devono essere caricate anche le cucine
     * @param generation identificatore della ricerca per ignorare risposte obsolete
     */
    private void eseguiRicerca(
            RicercaRistorantiPaginaDTO richiesta,
            boolean nuovaRicerca,
            int offset,
            boolean caricaTipologie,
            long generation
    ) {
        Thread.startVirtualThread(() -> {
            try {
                ITheKnifeServer server = ServerConnection.ottieniServer();
                PaginaRistorantiDTO pagina = server.cercaRistorantePaginato(richiesta);
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
                    ciSonoAltri = pagina.ciSonoAltri();
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
                        retryTransition.setOnFinished(evento ->
                                eseguiRicerca(richiesta, nuovaRicerca, offset, caricaTipologie, generation));
                        retryTransition.playFromStart();
                    }
                });
            }
        });
    }

    /**
     * Mostra lo stato di caricamento nella vista.
     *
     * @param messaggio testo da visualizzare durante il caricamento
     */
    private void mostraCaricamento(String messaggio) {
        resultsCountLabel.setText(messaggio);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
    }

    /**
     * Nasconde l'indicatore di caricamento.
     */
    private void nascondiCaricamento() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
    }

    /**
     * Crea il DTO della ricerca convertendo i valori selezionati nei filtri.
     *
     * @param offset posizione iniziale dei risultati richiesti
     * @return criterio completo di ricerca paginata
     */
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
                localitaRicerca,
                searchField.getText(),
                cuisineSelector.ottieniCucineSelezionate(),
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

    /**
     * Popola il selettore delle tipologie di cucina disponibili.
     *
     * @param tipologie lista delle cucine recuperate dal server
     */
    private void popolaMenuCucine(List<String> tipologie) {
        tipologieDisponibili = tipologie == null ? List.of() : List.copyOf(tipologie);
        cuisineSelector.impostaCucine(tipologieDisponibili);
    }

    private void ripristinaRicerca(SearchSnapshot istantanea) {
        searchField.setText(istantanea.testo());
        localitaRicerca = istantanea.localita();
        latitudineRicerca = istantanea.latitudine();
        longitudineRicerca = istantanea.longitudine();
        prezzoMinPersonalizzato = istantanea.prezzoMinPersonalizzato();
        prezzoMaxPersonalizzato = istantanea.prezzoMaxPersonalizzato();
        etichettaPrezzoPersonalizzata = prezzoMinPersonalizzato != null || prezzoMaxPersonalizzato != null
                ? formattaEtichettaPrezzo(prezzoMinPersonalizzato, prezzoMaxPersonalizzato)
                : null;
        if (etichettaPrezzoPersonalizzata != null
                && !priceFilterCombo.getItems().contains(etichettaPrezzoPersonalizzata)) {
            priceFilterCombo.getItems().add(etichettaPrezzoPersonalizzata);
        }
        priceFilterCombo.setValue(istantanea.filtroPrezzo());
        if (istantanea.filtroValutazione() != null
                && !ratingFilterCombo.getItems().contains(istantanea.filtroValutazione())) {
            ratingFilterCombo.getItems().add(istantanea.filtroValutazione());
        }
        ratingFilterCombo.setValue(istantanea.filtroValutazione());
        deliveryFilterCheckBox.setSelected(istantanea.consegna());
        bookingOnlineFilterCheckBox.setSelected(istantanea.prenotazioneOnline());
        tipologieDisponibili = istantanea.tipologieDisponibili();
        cuisineSelector.impostaCucine(tipologieDisponibili);
        cuisineSelector.impostaCucineSelezionate(istantanea.tipologieCucina());
        aggiornaEtichettaPosizione();

        ristorantiCompleti.setAll(istantanea.ristoranti());
        ciSonoAltri = istantanea.ciSonoAltri();
        tipologieCaricate = true;
        aggiornaLista(true, 0);
        Platform.runLater(() -> restaurantScrollPane.setVvalue(istantanea.posizioneScroll()));
    }

    /**
     * Avvia una nuova ricerca usando il testo e i filtri correnti.
     */
    @FXML
    private void cerca() {
        caricaPagina(true);
    }

    /**
     * Gestisce la modifica dei filtri dell'interfaccia.
     * <p>
     * Il metodo è predisposto per eventuali azioni immediate sui filtri;
     * al momento la ricarica avviene tramite il comando di ricerca o reset.
     * </p>
     */
    @FXML
    private void filtriCambiati() {
        //caricaPagina(true);
    }

    /**
     * Ripristina i filtri e avvia una nuova ricerca.
     */
    @FXML
    private void reimpostaFiltri() {
        searchField.clear();
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

        cuisineSelector.svuotaSelezione();

        caricaPagina(true);
    }

    /**
     * Carica la pagina successiva dei risultati.
     */
    @FXML
    private void caricaAltri() {
        caricaPagina(false);
    }

    /**
     * Torna alla schermata principale dell'applicazione.
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void tornaAllaHome(ActionEvent evento) {
        SceneManager.cambiaScena(evento, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    /**
     * Aggiorna il contenitore delle scheda con i risultati caricati.
     *
     * @param nuovaRicerca indica se la lista deve essere ricostruita
     * @param offset posizione iniziale dei risultati da aggiungere
     */
    private void aggiornaLista(boolean nuovaRicerca, int offset) {
        if (nuovaRicerca) {
            restaurantContainer.getChildren().clear();
        }
        for (int i = offset; i < ristorantiCompleti.size(); i++) {
            RistoranteDTO r = ristorantiCompleti.get(i);
            Node scheda = creaCardRistorante(r);
            if (scheda != null) {
                restaurantContainer.getChildren().add(scheda);
            }
        }

        resultsCountLabel.setText(ristorantiCompleti.size() + " ristoranti caricati");
        loadMoreButton.setVisible(ciSonoAltri);
        loadMoreButton.setManaged(ciSonoAltri);
        loadMoreButton.setDisable(false);
        if (nuovaRicerca) {
            restaurantScrollPane.setVvalue(0);
        }
    }

    /**
     * Crea la scheda grafica di un ristorante utilizzando la relativa vista FXML.
     *
     * @param r ristorante da rappresentare
     * @return nodo JavaFX della scheda, oppure {@code null} se il caricamento fallisce
     */
    private Node creaCardRistorante(RistoranteDTO r) {
        try {
            FXMLLoader caricatore = new FXMLLoader(getClass().getResource("/org/uninsubria/theknife/client/views/RistoranteItem.fxml"));
            Pane scheda = caricatore.load();

            RistoranteItemController controllore = caricatore.getController();
            controllore.impostaRistorante(r);
            controllore.setOnDettagliAction(this::apriDettagliRistorante);

            return scheda;
        } catch (IOException e) {
            System.err.println("[DEBUG_ERROR] Errore caricamento FXML scheda: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gestisce l'apertura dei dettagli di un ristorante dalla lista.
     *
     * @param r ristorante selezionato
     */
    private void apriDettagliRistorante(RistoranteDTO r) {
        snapshotRicerca = new SearchSnapshot(
                searchField.getText(),
                localitaRicerca,
                latitudineRicerca,
                longitudineRicerca,
                priceFilterCombo.getValue(),
                ratingFilterCombo.getValue(),
                prezzoMinPersonalizzato,
                prezzoMaxPersonalizzato,
                cuisineSelector.ottieniCucineSelezionate(),
                tipologieDisponibili,
                deliveryFilterCheckBox.isSelected(),
                bookingOnlineFilterCheckBox.isSelected(),
                List.copyOf(ristorantiCompleti),
                ciSonoAltri,
                restaurantScrollPane.getVvalue()
        );
    }

    /**
     * Formatta l'etichetta di un intervallo di prezzo personalizzato.
     *
     * @param prezzoMin limite minimo, oppure {@code null} se non definito
     * @param prezzoMax limite massimo, oppure {@code null} se non definito
     * @return descrizione leggibile dell'intervallo
     */
    private String formattaEtichettaPrezzo(Double prezzoMin, Double prezzoMax) {
        String minimo = prezzoMin == null ? "qualsiasi" : "da " + formatoEuro(prezzoMin);
        String massimo = prezzoMax == null ? "350+ €" : "fino a " + formatoEuro(prezzoMax);
        return "Personalizzato: " + minimo + ", " + massimo;
    }

    /**
     * Formatta un valore monetario senza cifre decimali.
     *
     * @param valore importo da formattare
     * @return importo seguito dal simbolo dell'euro
     */
    private String formatoEuro(double valore) {
        return String.format(Locale.ROOT, "%.0f €", valore);
    }

    /**
     * Aggiorna l'etichetta che descrive la posizione e il raggio di ricerca.
     */
    private void aggiornaEtichettaPosizione() {
        locationFilterLabel.setText(localitaRicerca == null || localitaRicerca.isBlank()
                ? "Posizione: non selezionata"
                : "Posizione: " + localitaRicerca + " (raggio "
                + (int) RicercaIniziale.RAGGIO_RICERCA_KM + " km)");
    }
}
