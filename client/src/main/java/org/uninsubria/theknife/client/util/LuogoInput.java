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

/**
 * Campo per inserire una località con suggerimenti automatici e selezione tramite mappa.
 */
public final class LuogoInput extends HBox {

    private final TextField campo = new TextField();
    private final Button mappa = new Button();
    private final AutocompleteLuoghi autocomplete;
    private Consumer<GeocodingService.Luogo> alSeleziona = luogo -> { };

    /**
     * Crea il campo di località con completamento automatico e pulsante per aprire la mappa.
     */
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
        mappa.setOnAction(evento -> SelettoreLuogoDialog
                .mostra(getScene() == null ? null : getScene().getWindow(), ottieniLuogoSelezionato())
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

    /**
     * Restituisce il testo inserito nel campo.
     *
     * @return testo del campo
     */
    public String getText() {
        return campo.getText();
    }

    /**
     * Imposta il testo del campo.
     *
     * @param value testo da visualizzare
     */
    public void setText(String value) {
        campo.setText(value);
    }

    /**
     * Imposta il testo segnaposto del campo.
     *
     * @param promptText testo segnaposto
     */
    public void setPromptText(String promptText) {
        campo.setPromptText(promptText);
    }

    /**
     * Restituisce il testo segnaposto del campo.
     *
     * @return testo segnaposto
     */
    public String getPromptText() {
        return campo.getPromptText();
    }

    /**
     * Restituisce la proprietà JavaFX del testo segnaposto.
     *
     * @return proprietà del testo segnaposto
     */
    public StringProperty promptTextProperty() {
        return campo.promptTextProperty();
    }

    /**
     * Imposta la larghezza preferita del campo di testo.
     *
     * @param width larghezza preferita
     */
    public void setTextFieldPrefWidth(double width) {
        campo.setPrefWidth(width);
    }

    /**
     * Aggiunge una classe CSS al campo di testo, se non è già presente.
     *
     * @param styleClass classe CSS da aggiungere
     */
    public void aggiungiStileCampoTesto(String styleClass) {
        if (!campo.getStyleClass().contains(styleClass)) {
            campo.getStyleClass().add(styleClass);
        }
    }

    /**
     * Imposta l'altezza preferita del campo e del pulsante della mappa.
     *
     * @param height altezza preferita
     */
    public void setTextFieldPrefHeight(double height) {
        campo.setPrefHeight(height);
        mappa.setPrefHeight(height);
    }

    /**
     * Imposta la larghezza fissa del pulsante della mappa.
     *
     * @param width larghezza del pulsante
     */
    public void impostaAmpiezzaPulsanteMappa(double width) {
        mappa.setPrefWidth(width);
        mappa.setMinWidth(width);
        mappa.setMaxWidth(width);
    }

    /**
     * Imposta se il testo del campo può essere modificato.
     *
     * @param editable {@code true} per consentire la modifica
     */
    public void setTextFieldEditable(boolean editable) {
        campo.setEditable(editable);
    }

    /**
     * Mostra o nasconde il pulsante per selezionare una località sulla mappa.
     *
     * @param visible {@code true} per mostrare il pulsante
     */
    public void impostaPulsanteMappaVisibile(boolean visible) {
        mappa.setVisible(visible);
        mappa.setManaged(visible);
    }

    /**
     * Richiede il focus per il campo di testo.
     */
    public void requestFocus() {
        campo.requestFocus();
    }

    /**
     * Restituisce la località attualmente selezionata.
     *
     * @return località selezionata oppure {@code null} se non è stata selezionata
     */
    public GeocodingService.Luogo ottieniLuogoSelezionato() {
        return autocomplete.ottieniLuogoSelezionato();
    }

    /**
     * Imposta la località selezionata, aggiornando il testo e notificando il listener.
     *
     * @param luogo località da selezionare
     * @throws NullPointerException se {@code luogo} è {@code null}
     */
    public void setLuogoSelezionato(GeocodingService.Luogo luogo) {
        autocomplete.seleziona(Objects.requireNonNull(luogo));
    }

    /**
     * Risolve il testo del campo in una località. Se non è già selezionata una località
     * corrispondente al testo, cerca i suggerimenti e usa il primo risultato disponibile.
     *
     * @param alSuccesso azione da eseguire con la località risolta
     * @param alErrore azione da eseguire con il messaggio se non è possibile risolverla
     * @throws NullPointerException se uno dei callback è {@code null}
     */
    public void risolviLuogo(
            Consumer<GeocodingService.Luogo> alSuccesso,
            Consumer<String> alErrore
    ) {
        Objects.requireNonNull(alSuccesso);
        Objects.requireNonNull(alErrore);
        String query = campo.getText() == null ? "" : campo.getText().trim();
        GeocodingService.Luogo selezionato = ottieniLuogoSelezionato();
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

    /**
     * Imposta il listener invocato quando viene selezionata una località.
     *
     * @param listener callback da invocare con la località selezionata
     * @throws NullPointerException se {@code listener} è {@code null}
     */
    public void setOnLuogoSelezionato(Consumer<GeocodingService.Luogo> listener) {
        alSeleziona = Objects.requireNonNull(listener);
    }
}
