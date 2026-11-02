package org.uninsubria.clientTK.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.Objects;
import java.util.function.Consumer;

public final class LuogoInput extends HBox {

    private final TextField campo = new TextField();
    private final Button mappa = new Button();
    private AutocompleteLuoghi autocomplete;
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
        SVGPath iconaPuntina = new SVGPath();
        iconaPuntina.setContent("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z");
        iconaPuntina.setFill(Color.WHITE);
        iconaPuntina.setScaleX(1.15);
        iconaPuntina.setScaleY(1.15);
        StackPane graphic = new StackPane(iconaPuntina);
        graphic.setMinSize(24, 24);
        graphic.setPrefSize(24, 24);
        graphic.setMaxSize(24, 24);
        mappa.setGraphic(graphic);
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

    public void setOnLuogoSelezionato(Consumer<GeocodingService.Luogo> listener) {
        alSeleziona = Objects.requireNonNull(listener);
    }
}
