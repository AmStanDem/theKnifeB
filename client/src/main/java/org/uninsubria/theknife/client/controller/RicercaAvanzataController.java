/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

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
    private MenuButton cuisineMenuButton;


    private final Set<String> selectedCuisines = new HashSet<>();
    private final List<CheckBox> cuisineCheckBoxes = new ArrayList<>();

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

    private void popolaMenuCucine(List<String> tipologie) {

        VBox menuContent = new VBox(8);
        menuContent.setPadding(new Insets(8));
        menuContent.setPrefWidth(260);

        TextField searchCuisineInput = new TextField();
        searchCuisineInput.setPromptText("Filtra cucine...");

        searchCuisineInput.getStyleClass().add("search-cuisine-input");

        searchCuisineInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                searchCuisineInput.insertText(
                        searchCuisineInput.getCaretPosition(), " ");
                event.consume();
            }
        });

        VBox checkBoxesBox = new VBox(6);
        cuisineCheckBoxes.clear();

        for (String tipo : tipologie) {

            if (tipo == null || tipo.isBlank()) {
                continue;
            }

            CheckBox cb = new CheckBox(tipo.trim());
            cb.setStyle("-fx-text-fill: #1e293b;");

            cb.setSelected(selectedCuisines.contains(cb.getText()));

            cb.setOnAction(e -> {

                if (cb.isSelected()) {
                    selectedCuisines.add(cb.getText());
                } else {
                    selectedCuisines.remove(cb.getText());
                }

                aggiornaEtichettaMenuCucine();
            });

            cuisineCheckBoxes.add(cb);
            checkBoxesBox.getChildren().add(cb);
        }

        searchCuisineInput.textProperty().addListener((obs, oldVal, newVal) -> {

            String q = newVal == null ? "" : newVal.trim().toLowerCase();

            for (CheckBox cb : cuisineCheckBoxes) {

                boolean visibile = q.isEmpty()
                        || cb.getText().toLowerCase().contains(q);

                cb.setVisible(visibile);
                cb.setManaged(visibile);
            }
        });

        ScrollPane scroll = new ScrollPane(checkBoxesBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(180);
        scroll.setMaxHeight(220);

        scroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent;"
        );

        Button clearBtn = new Button("Deseleziona tutte");
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        clearBtn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-text-fill: #475569; -fx-cursor: hand;"
        );

        clearBtn.setOnAction(e -> {

            for (CheckBox cb : cuisineCheckBoxes) {
                cb.setSelected(false);
            }

            selectedCuisines.clear();
            aggiornaEtichettaMenuCucine();
        });

        menuContent.getChildren().addAll(
                searchCuisineInput,
                scroll,
                clearBtn
        );

        CustomMenuItem item = new CustomMenuItem(menuContent, false);
        item.setHideOnClick(false);

        cuisineMenuButton.getItems().setAll(item);

        cuisineMenuButton.setOnShowing(
                e -> Platform.runLater(searchCuisineInput::requestFocus)
        );

        aggiornaEtichettaMenuCucine();
    }

    private void aggiornaEtichettaMenuCucine() {

        if (selectedCuisines.isEmpty()) {

            cuisineMenuButton.setText("Tipo di cucina");

        } else if (selectedCuisines.size() == 1) {

            cuisineMenuButton.setText(selectedCuisines.iterator().next());

        } else {

            cuisineMenuButton.setText(
                    "Cucine (" + selectedCuisines.size() + ")"
            );
        }
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
            SceneManager.initOwner(alert);
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
                new ArrayList<>(selectedCuisines),
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
        for (CheckBox cb : cuisineCheckBoxes) {
            cb.setSelected(false);
        }

        selectedCuisines.clear();
        aggiornaEtichettaMenuCucine();
        rangeSliderPrezzo.setLowValue(PREZZO_MINIMO);
        rangeSliderPrezzo.setHighValue(PREZZO_MASSIMO);
        ratingStelleMinime.setRating(0);
        aggiornaEtichettaRating();
        chkDelivery.setSelected(false);
        chkPrenotazione.setSelected(false);
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.show();
    }
}