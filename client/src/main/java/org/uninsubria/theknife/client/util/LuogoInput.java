/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LuogoInput extends HBox {

    private final TextField campo = new TextField();
    private final Button mappa = new Button();
    private final AutocompleteLuoghi autocomplete;
    private Consumer<GeocodingService.Luogo> alSeleziona = luogo -> { };

    public LuogoInput() {
        setAlignment(Pos.CENTER);
        setSpacing(6);
        getStyleClass().add("location-container");
        campo.setPromptText("Inizia a scrivere e seleziona un luogo");
        campo.getStyleClass().addAll(
                "location-input-field",
                "location-field",
                "field-input",
                "custom-text-field"
        );
        campo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(campo, Priority.ALWAYS);

        mappa.setMnemonicParsing(false);
        mappa.setAccessibleText("Seleziona un luogo sulla mappa");
        mappa.setTooltip(new Tooltip("Cerca un luogo o selezionalo sulla mappa"));
        var pinResource = LuogoInput.class.getResource("/org/uninsubria/theknife/client/images/pin-geolocalizzazione-rosso.png");
        if (pinResource == null) {
            throw new IllegalStateException("Immagine del pin di geolocalizzazione non trovata.");
        }
        ImageView pin = new ImageView(new Image(pinResource.toExternalForm()));
        pin.setFitWidth(28);
        pin.setFitHeight(28);
        pin.setPreserveRatio(true);
        pin.setMouseTransparent(true);
        mappa.setGraphic(pin);
        mappa.getStyleClass().add("location-input-map-button");
        mappa.setOnAction(event -> SelettoreLuogoDialog
                .mostra(getScene() == null ? null : getScene().getWindow(), getLuogoSelezionato())
                .ifPresent(this::setLuogoSelezionato));

        getChildren().addAll(campo, mappa);
        autocomplete = new AutocompleteLuoghi(campo, luogo -> alSeleziona.accept(luogo));
        heightProperty().addListener((observable, oldHeight, newHeight) -> {
            double height = newHeight.doubleValue();
            if (height > 0) {
                campo.setPrefHeight(height);
                mappa.setPrefHeight(height);
            }
        });
    }

    public String getText() {
        return campo.getText();
    }

    public void setText(String value) {
        campo.setText(value);
    }

    public void setPromptText(String promptText) {
        campo.setPromptText(promptText);
    }

    public String getPromptText() {
        return campo.getPromptText();
    }

    public StringProperty promptTextProperty() {
        return campo.promptTextProperty();
    }

    public void setTextFieldPrefWidth(double width) {
        campo.setPrefWidth(width);
    }

    public void addTextFieldStyleClass(String styleClass) {
        if (!campo.getStyleClass().contains(styleClass)) {
            campo.getStyleClass().add(styleClass);
        }
    }

    public void setTextFieldPrefHeight(double height) {
        campo.setPrefHeight(height);
        mappa.setPrefHeight(height);
    }

    public void setMapButtonPrefWidth(double width) {
        mappa.setPrefWidth(width);
        mappa.setMinWidth(width);
        mappa.setMaxWidth(width);
    }

    public void setTextFieldEditable(boolean editable) {
        campo.setEditable(editable);
    }

    public void setMapButtonVisible(boolean visible) {
        mappa.setVisible(visible);
        mappa.setManaged(visible);
    }

    public void requestFocus() {
        campo.requestFocus();
    }

    public GeocodingService.Luogo getLuogoSelezionato() {
        return autocomplete.getLuogoSelezionato();
    }

    public void setLuogoSelezionato(GeocodingService.Luogo luogo) {
        autocomplete.seleziona(Objects.requireNonNull(luogo));
    }

    public void risolviLuogo(
            Consumer<GeocodingService.Luogo> alSuccesso,
            Consumer<String> alErrore
    ) {
        Objects.requireNonNull(alSuccesso);
        Objects.requireNonNull(alErrore);
        String query = campo.getText() == null ? "" : campo.getText().trim();
        GeocodingService.Luogo selezionato = getLuogoSelezionato();
        if (selezionato != null && selezionato.etichetta().equals(query)) {
            alSuccesso.accept(selezionato);
            return;
        }
        if (query.length() < 3) {
            alErrore.accept("Inserisci almeno tre caratteri per cercare una località.");
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                List<GeocodingService.Luogo> suggerimenti = GeocodingService.suggerisciLuoghi(query);
                Platform.runLater(() -> {
                    if (!query.equals(campo.getText().trim())) {
                        alErrore.accept("La località è cambiata. Verifica il testo e riprova.");
                    } else if (suggerimenti.isEmpty()) {
                        alErrore.accept("Nessuna località trovata. Verifica il testo inserito.");
                    } else {
                        GeocodingService.Luogo primoSuggerimento = suggerimenti.getFirst();
                        autocomplete.seleziona(primoSuggerimento);
                        alSuccesso.accept(primoSuggerimento);
                    }
                });
            } catch (IOException e) {
                System.err.println("[GEOCODING] Impossibile risolvere la località: " + e.getMessage());
                Platform.runLater(() ->
                        alErrore.accept("Ricerca località temporaneamente non disponibile."));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[GEOCODING] Ricerca località interrotta.");
                Platform.runLater(() ->
                        alErrore.accept("Ricerca località temporaneamente non disponibile."));
            }
        });
    }

    public void setOnLuogoSelezionato(Consumer<GeocodingService.Luogo> listener) {
        alSeleziona = Objects.requireNonNull(listener);
    }
}
