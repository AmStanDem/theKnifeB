/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.controlsfx.control.RangeSlider;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.components.CuisineSelector;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.util.FasciaPrezzo;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Controller JavaFX della schermata di ricerca avanzata dei ristoranti.
 * <p>
 * Gestisce la selezione della località, delle tipologie di cucina, della
 * fascia di prezzo, della valutazione minima e dei servizi richiesti.
 * Dopo la validazione dei criteri avvia una ricerca e apre la lista dei
 * risultati.
 * </p>
 */
public class RicercaAvanzataController {

    private static final int FASCIA_MINIMA = 1;
    private static final int FASCIA_MASSIMA = 4;
    private String simboloValuta = "¤";

    @FXML
    private LuogoInput txtLocalita;

    @FXML
    private RangeSlider rangeSliderPrezzo;

    @FXML
    private Label lblFasciaPrezzoMin;

    @FXML
    private Label lblFasciaPrezzoMax;

    @FXML
    private CuisineSelector cuisineSelector;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkPrenotazione;

    @FXML
    private org.uninsubria.theknife.client.util.stars.RatingContainer ratingStelleMinime;

    @FXML
    private Label lblRatingValore;

    /**
     * Inizializza i controlli della ricerca avanzata.
     * <p>
     * Carica le tipologie di cucina, configura i campi e il cursore dei
     * prezzi e mantiene sincronizzati i relativi valori. Aggiorna inoltre
     * l'etichetta della valutazione minima.
     * </p>
     */
    @FXML
    private void initialize() {
        txtLocalita.aggiungiStileCampoTesto("field-input");
        txtLocalita.setOnLuogoSelezionato(luogo -> aggiornaSimboloValuta(luogo.codiceNazione()));
        ratingStelleMinime.impostaSuValutazioneCambiata(this::aggiornaEtichettaRating);

        caricaTipologieCucina();

        rangeSliderPrezzo.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = Math.max(FASCIA_MINIMA,
                    Math.min(Math.round(newValue.doubleValue()), rangeSliderPrezzo.getHighValue()));
            if (valore != newValue.doubleValue()) {
                rangeSliderPrezzo.setLowValue(valore);
                return;
            }
            aggiornaEtichetteFascia();
        });
        rangeSliderPrezzo.highValueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = Math.min(FASCIA_MASSIMA,
                    Math.max(Math.round(newValue.doubleValue()), rangeSliderPrezzo.getLowValue()));
            if (valore != newValue.doubleValue()) {
                rangeSliderPrezzo.setHighValue(valore);
                return;
            }
            aggiornaEtichetteFascia();
        });
        aggiornaEtichetteFascia();
        aggiornaEtichettaRating();
    }

    private void aggiornaSimboloValuta(String codiceNazione) {
        simboloValuta = FasciaPrezzo.simboloValuta(codiceNazione);
        aggiornaEtichetteFascia();
    }

    private void aggiornaEtichetteFascia() {
        int minimo = (int) Math.round(rangeSliderPrezzo.getLowValue());
        int massimo = (int) Math.round(rangeSliderPrezzo.getHighValue());
        lblFasciaPrezzoMin.setText(FasciaPrezzo.simboli(simboloValuta, minimo));
        lblFasciaPrezzoMax.setText(FasciaPrezzo.simboli(simboloValuta, massimo));
    }

    /**
     * Recupera dal server le tipologie di cucina disponibili.
     * <p>
     * La richiesta viene eseguita in un thread virtuale e il selettore viene
     * aggiornato sul thread JavaFX. In caso di errore viene mostrato un avviso.
     * </p>
     */
    private void caricaTipologieCucina() {
        Thread.startVirtualThread(() -> {
            try {
                List<String> tipologie =
                        ServerConnection.ottieniServer().ottieniTutteTipologieCucina();

                Platform.runLater(() -> popolaMenuCucine(tipologie));

            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {

                System.err.println("[SEARCH_WARN] Impossibile caricare le tipologie di cucina: "
                        + e.getMessage());

                Platform.runLater(() -> mostraErrore(
                        "Tipologie non disponibili",
                        "Impossibile caricare le tipologie di cucina dal server."
                ));
            }
        });
    }

    /**
     * Popola il selettore con le tipologie di cucina ricevute dal server.
     *
     * @param tipologie tipologie disponibili
     */
    private void popolaMenuCucine(List<String> tipologie) {
        cuisineSelector.impostaCucine(tipologie);
    }


    /**
     * Aggiorna l'etichetta che descrive la valutazione minima selezionata.
     */
    private void aggiornaEtichettaRating() {
        double rating = ratingStelleMinime.ottieniValutazione();
        lblRatingValore.setText(rating == 0
                ? "Nessun minimo"
                : String.format(java.util.Locale.ITALY, "%.1f+ stelle", rating));
    }

    /**
     * Gestisce l'avvio della ricerca avanzata.
     * <p>
     * Verifica che sia stata inserita una località e la risolve tramite il
     * servizio di geocoding prima di avviare la ricerca.
     * </p>
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void cercaRistorante(ActionEvent evento) {
        if (txtLocalita.getText().isBlank()) {
            Alert avviso = new Alert(Alert.AlertType.WARNING);
            SceneManager.impostaProprietario(avviso);
            avviso.setTitle("Località da selezionare");
            avviso.setHeaderText(null);
            avviso.setContentText("Inserisci una località oppure selezionala sulla mappa.");
            avviso.showAndWait();
            return;
        }

        txtLocalita.risolviLuogo(
                luogo -> avviaRicerca(evento, luogo),
                messaggio -> mostraErroreLocalita(messaggio)
        );
    }

    /**
     * Memorizza i criteri selezionati e apre la lista dei ristoranti.
     *
     * @param evento evento JavaFX usato per cambiare scena
     * @param luogo località risolta dal servizio di geocoding
     */
    private void avviaRicerca(ActionEvent evento, GeocodingService.Luogo luogo) {
        int prezzoMinimo = (int) Math.round(rangeSliderPrezzo.getLowValue());
        int prezzoMassimo = (int) Math.round(rangeSliderPrezzo.getHighValue());
        String fasciaMinima = prezzoMinimo == FASCIA_MINIMA && prezzoMassimo == FASCIA_MASSIMA
                ? FasciaPrezzo.QUALSIASI : FasciaPrezzo.simboli(simboloValuta, prezzoMinimo);
        String fasciaMassima = prezzoMinimo == FASCIA_MINIMA && prezzoMassimo == FASCIA_MASSIMA
                ? FasciaPrezzo.QUALSIASI : FasciaPrezzo.simboli(simboloValuta, prezzoMassimo);
        RicercaIniziale.imposta(
                "",
                luogo,
                cuisineSelector.ottieniCucineSelezionate(),
                fasciaMinima,
                fasciaMassima,
                ratingStelleMinime.ottieniValutazione() > 0 ? ratingStelleMinime.ottieniValutazione() : null,
                chkDelivery.isSelected(),
                chkPrenotazione.isSelected(),
                simboloValuta
        );
        SceneManager.cambiaScena(evento, "/org/uninsubria/theknife/client/views/listaRistoranti.fxml");
    }

    /**
     * Mostra un avviso relativo alla validità della località.
     *
     * @param messaggio descrizione dell'errore da mostrare
     */
    private void mostraErroreLocalita(String messaggio) {
        Alert avviso = new Alert(Alert.AlertType.WARNING);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle("Località non valida");
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }

    /**
     * Ripristina la località e tutti i filtri ai rispettivi valori predefiniti.
     */
    @FXML
    private void reimpostaFiltri() {
        txtLocalita.setText("");
        cuisineSelector.svuotaSelezione();
        rangeSliderPrezzo.setLowValue(FASCIA_MINIMA);
        rangeSliderPrezzo.setHighValue(FASCIA_MASSIMA);
        simboloValuta = "¤";
        aggiornaEtichetteFascia();
        ratingStelleMinime.impostaValutazione(0);
        aggiornaEtichettaRating();
        chkDelivery.setSelected(false);
        chkPrenotazione.setSelected(false);
    }

    /**
     * Mostra un avviso con il titolo e il messaggio specificati.
     *
     * @param titolo titolo dell'avviso
     * @param messaggio testo da mostrare all'utente
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert avviso = new Alert(Alert.AlertType.WARNING);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.show();
    }
}