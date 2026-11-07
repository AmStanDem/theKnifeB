/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.RangeSlider;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

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
    private ComboBox<String> cmbTipologiaCucina;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkPrenotazione;

    @FXML
    private org.uninsubria.theknife.client.util.stars.RatingContainer ratingStelleMinime;

    @FXML
    private Label lblRatingValore;

    @FXML
    private void initialize() {
        txtLocalita.addTextFieldStyleClass("field-input");
        ratingStelleMinime.setOnRatingChanged(this::aggiornaEtichettaRating);
        cmbTipologiaCucina.setDisable(true);
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

    private void caricaTipologieCucina() {
        Thread.startVirtualThread(() -> {
            try {
                List<String> tipologie = ServerConnection.getServer().ottieniTutteTipologieCucina();
                Platform.runLater(() -> {
                    cmbTipologiaCucina.getItems().setAll(tipologie);
                    cmbTipologiaCucina.setDisable(false);
                });
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                System.err.println("[SEARCH_WARN] Impossibile caricare le tipologie di cucina: " + e.getMessage());
                Platform.runLater(() -> {
                    cmbTipologiaCucina.setDisable(false);
                    mostraErrore(
                            "Tipologie non disponibili",
                            "Impossibile caricare le tipologie di cucina dal server."
                    );
                });
            }
        });
    }

    private void aggiornaEtichettaRating() {
        double rating = ratingStelleMinime.getRating();
        lblRatingValore.setText(rating == 0
                ? "Nessun minimo"
                : String.format(java.util.Locale.ITALY, "%.1f+ stelle", rating));
    }

    private void configuraCampoPrezzo(TextField campo, boolean accettaPiu) {
        String pattern = accettaPiu ? "\\d{0,3}\\+?" : "\\d{0,3}";
        campo.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change ->
                change.getControlNewText().matches(pattern) ? change : null));
    }

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

    private String formattaPrezzoMassimo(int prezzo) {
        return prezzo == PREZZO_MASSIMO ? PREZZO_MASSIMO + "+" : Integer.toString(prezzo);
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    @FXML
    private void cercaRistorante(ActionEvent event) {
        GeocodingService.Luogo luogo = txtLocalita.getLuogoSelezionato();
        if (luogo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Località da selezionare");
            alert.setHeaderText(null);
            alert.setContentText("Seleziona una località dai suggerimenti prima di cercare.");
            alert.showAndWait();
            return;
        }
        double prezzoMinimo = rangeSliderPrezzo.getLowValue();
        double prezzoMassimo = rangeSliderPrezzo.getHighValue();
        RicercaIniziale.imposta(
                "",
                luogo,
                cmbTipologiaCucina.getValue() == null ? List.of() : List.of(cmbTipologiaCucina.getValue()),
                prezzoMinimo,
                prezzoMassimo < PREZZO_MASSIMO ? prezzoMassimo : null,
                ratingStelleMinime.getRating() > 0 ? ratingStelleMinime.getRating() : null,
                chkDelivery.isSelected(),
                chkPrenotazione.isSelected()
        );
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/listaRistoranti.fxml");
    }

    @FXML
    private void reimpostaFiltri() {
        txtLocalita.setText("");
        cmbTipologiaCucina.setValue(null);
        rangeSliderPrezzo.setLowValue(PREZZO_MINIMO);
        rangeSliderPrezzo.setHighValue(PREZZO_MASSIMO);
        ratingStelleMinime.setRating(0);
        aggiornaEtichettaRating();
        chkDelivery.setSelected(false);
        chkPrenotazione.setSelected(false);
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.show();
    }
}