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

    private static final int PREZZO_MINIMO = 10;
    private static final int PREZZO_MASSIMO = 350;

    @FXML
    private LuogoInput txtLocalita;

    @FXML
    private RangeSlider rangeSliderPrezzo;

    @FXML
    private TextField txtPrezzoMin;

    @FXML
    private TextField txtPrezzoMax;

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
        txtLocalita.addTextFieldStyleClass("field-input");
        ratingStelleMinime.setOnRatingChanged(this::aggiornaEtichettaRating);

        caricaTipologieCucina();

        configuraCampoPrezzo(txtPrezzoMin, false);
        configuraCampoPrezzo(txtPrezzoMax, true);

        rangeSliderPrezzo.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = Math.max(PREZZO_MINIMO,
                    Math.min(Math.round(newValue.doubleValue()), rangeSliderPrezzo.getHighValue()));
            if (valore != newValue.doubleValue()) {
                rangeSliderPrezzo.setLowValue(valore);
                return;
            }
            txtPrezzoMin.setText(Integer.toString((int) valore));
        });
        rangeSliderPrezzo.highValueProperty().addListener((observable, oldValue, newValue) -> {
            double valore = Math.min(PREZZO_MASSIMO,
                    Math.max(Math.round(newValue.doubleValue()), rangeSliderPrezzo.getLowValue()));
            if (valore != newValue.doubleValue()) {
                rangeSliderPrezzo.setHighValue(valore);
                return;
            }
            txtPrezzoMax.setText(formattaPrezzoMassimo((int) valore));
        });

        txtPrezzoMin.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                aggiornaPrezzoMin();
            }
        });
        txtPrezzoMax.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                aggiornaPrezzoMax();
            }
        });
        aggiornaPrezzoMin();
        aggiornaPrezzoMax();
        aggiornaEtichettaRating();
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
                        ServerConnection.getServer().ottieniTutteTipologieCucina();

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
        cuisineSelector.setCuisines(tipologie);
    }


    /**
     * Aggiorna l'etichetta che descrive la valutazione minima selezionata.
     */
    private void aggiornaEtichettaRating() {
        double rating = ratingStelleMinime.getRating();
        lblRatingValore.setText(rating == 0
                ? "Nessun minimo"
                : String.format(java.util.Locale.ITALY, "%.1f+ stelle", rating));
    }

    /**
     * Limita il testo inseribile in un campo di prezzo al formato previsto.
     *
     * @param campo campo di testo da configurare
     * @param accettaPiu {@code true} se è ammesso il suffisso {@code +}
     */
    private void configuraCampoPrezzo(TextField campo, boolean accettaPiu) {
        String pattern = accettaPiu ? "\\d{0,3}\\+?" : "\\d{0,3}";
        campo.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change ->
                change.getControlNewText().matches(pattern) ? change : null));
    }

    /**
     * Aggiorna il limite minimo di prezzo a partire dal campo di testo,
     * mantenendolo entro i limiti e coerente con quello massimo.
     */
    @FXML
    private void aggiornaPrezzoMin() {
        Integer prezzo = parsePrezzo(txtPrezzoMin.getText(), false);
        if (prezzo == null) {
            txtPrezzoMin.setText(Integer.toString((int) Math.round(rangeSliderPrezzo.getLowValue())));
            return;
        }
        double valore = Math.max(PREZZO_MINIMO,
                Math.min(prezzo, (int) Math.round(rangeSliderPrezzo.getHighValue())));
        rangeSliderPrezzo.setLowValue(valore);
        txtPrezzoMin.setText(Integer.toString((int) valore));
    }

    /**
     * Aggiorna il limite massimo di prezzo a partire dal campo di testo,
     * mantenendolo entro i limiti e coerente con quello minimo.
     */
    @FXML
    private void aggiornaPrezzoMax() {
        Integer prezzo = parsePrezzo(txtPrezzoMax.getText(), true);
        if (prezzo == null) {
            txtPrezzoMax.setText(formattaPrezzoMassimo(
                    (int) Math.round(rangeSliderPrezzo.getHighValue())));
            return;
        }
        double valore = Math.min(PREZZO_MASSIMO,
                Math.max(prezzo, (int) Math.round(rangeSliderPrezzo.getLowValue())));
        rangeSliderPrezzo.setHighValue(valore);
        txtPrezzoMax.setText(formattaPrezzoMassimo((int) valore));
    }

    /**
     * Converte il testo del prezzo in un intero e ne verifica il formato e
     * l'intervallo ammesso.
     *
     * @param testo valore inserito
     * @param accettaPiu {@code true} se il suffisso {@code +} è consentito
     * @return prezzo valido, oppure {@code null} se il testo non è valido
     */
    private Integer parsePrezzo(String testo, boolean accettaPiu) {
        if (testo == null || !testo.matches(accettaPiu ? "\\d{1,3}\\+?" : "\\d{1,3}")) {
            return null;
        }
        String valore = testo.endsWith("+") ? testo.substring(0, testo.length() - 1) : testo;
        try {
            int prezzo = Integer.parseInt(valore);
            return prezzo >= PREZZO_MINIMO && prezzo <= PREZZO_MASSIMO ? prezzo : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Formatta il limite massimo di prezzo, indicando con {@code +} il valore
     * massimo della fascia.
     *
     * @param prezzo limite da formattare
     * @return testo del prezzo formattato
     */
    private String formattaPrezzoMassimo(int prezzo) {
        return prezzo == PREZZO_MASSIMO ? PREZZO_MASSIMO + "+" : Integer.toString(prezzo);
    }

    /**
     * Gestisce l'avvio della ricerca avanzata.
     * <p>
     * Verifica che sia stata inserita una località e la risolve tramite il
     * servizio di geocoding prima di avviare la ricerca.
     * </p>
     *
     * @param event evento JavaFX usato per cambiare scena
     */
    @FXML
    private void cercaRistorante(ActionEvent event) {
        if (txtLocalita.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            SceneManager.initOwner(alert);
            alert.setTitle("Località da selezionare");
            alert.setHeaderText(null);
            alert.setContentText("Inserisci una località oppure selezionala sulla mappa.");
            alert.showAndWait();
            return;
        }

        txtLocalita.risolviLuogo(
                luogo -> avviaRicerca(event, luogo),
                messaggio -> mostraErroreLocalita(messaggio)
        );
    }

    /**
     * Memorizza i criteri selezionati e apre la lista dei ristoranti.
     *
     * @param event evento JavaFX usato per cambiare scena
     * @param luogo località risolta dal servizio di geocoding
     */
    private void avviaRicerca(ActionEvent event, GeocodingService.Luogo luogo) {
        double prezzoMinimo = rangeSliderPrezzo.getLowValue();
        double prezzoMassimo = rangeSliderPrezzo.getHighValue();
        RicercaIniziale.imposta(
                "",
                luogo,
                cuisineSelector.getSelectedCuisines(),
                prezzoMinimo,
                prezzoMassimo < PREZZO_MASSIMO ? prezzoMassimo : null,
                ratingStelleMinime.getRating() > 0 ? ratingStelleMinime.getRating() : null,
                chkDelivery.isSelected(),
                chkPrenotazione.isSelected()
        );
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/listaRistoranti.fxml");
    }

    /**
     * Mostra un avviso relativo alla validità della località.
     *
     * @param messaggio descrizione dell'errore da mostrare
     */
    private void mostraErroreLocalita(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        SceneManager.initOwner(alert);
        alert.setTitle("Località non valida");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    /**
     * Ripristina la località e tutti i filtri ai rispettivi valori predefiniti.
     */
    @FXML
    private void reimpostaFiltri() {
        txtLocalita.setText("");
        cuisineSelector.clearSelection();
        rangeSliderPrezzo.setLowValue(PREZZO_MINIMO);
        rangeSliderPrezzo.setHighValue(PREZZO_MASSIMO);
        ratingStelleMinime.setRating(0);
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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.show();
    }
}