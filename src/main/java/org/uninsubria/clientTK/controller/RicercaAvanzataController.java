package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.RangeSlider;
import org.uninsubria.clientTK.util.LuogoInput;
import org.uninsubria.clientTK.util.GeocodingService;
import org.uninsubria.clientTK.util.RicercaIniziale;
import org.uninsubria.clientTK.util.SceneManager;

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
    private void initialize() {
        txtLocalita.addTextFieldStyleClass("field-input");
        txtLocalita.setOnLuogoSelezionato(luogo -> { });

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
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
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
        RicercaIniziale.imposta("", luogo);
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/listaRistoranti.fxml");
    }

    @FXML
    private void reimpostaFiltri() {
        rangeSliderPrezzo.setLowValue(PREZZO_MINIMO);
        rangeSliderPrezzo.setHighValue(PREZZO_MASSIMO);
    }
}