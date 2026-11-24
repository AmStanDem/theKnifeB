/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Menu a selezione multipla delle tipologie di cucina, con filtro testuale.
 */
public final class CuisineSelector extends MenuButton {

    private static final String PLACEHOLDER = "Tipo di cucina";

    private final Set<String> selectedCuisines = new LinkedHashSet<>();
    private final List<CheckBox> cuisineCheckBoxes = new ArrayList<>();

    /**
     * Crea un selettore di cucine inizialmente vuoto.
     */
    public CuisineSelector() {
        setText(PLACEHOLDER);
        getStyleClass().add("cuisine-selector");
    }

    /**
     * Imposta le tipologie di cucina disponibili nel menu.
     * Le selezioni vengono mantenute solo se la tipologia è ancora disponibile.
     * I valori {@code null} e vuoti vengono ignorati.
     *
     * @param cuisines tipologie di cucina da mostrare, oppure {@code null} per non mostrarne
     */
    public void setCuisines(Collection<String> cuisines) {
        cuisineCheckBoxes.clear();
        VBox menuContent = new VBox(8);
        menuContent.setPadding(new Insets(8));
        menuContent.setPrefWidth(260);
        Set<String> availableCuisines = new LinkedHashSet<>();
        if (cuisines != null) {
            cuisines.stream()
                    .filter(cuisine -> cuisine != null && !cuisine.isBlank())
                    .map(String::trim)
                    .forEach(availableCuisines::add);
        }
        selectedCuisines.retainAll(availableCuisines);

        TextField searchInput = new TextField();
        searchInput.setPromptText("Filtra cucine...");
        searchInput.getStyleClass().add("search-cuisine-input");
        searchInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                searchInput.insertText(searchInput.getCaretPosition(), " ");
                event.consume();
            }
        });

        VBox checkBoxes = new VBox(6);
        for (String cuisine : availableCuisines) {
            CheckBox checkBox = new CheckBox(cuisine);
            checkBox.setStyle("-fx-text-fill: #1e293b;");
            checkBox.setSelected(selectedCuisines.contains(checkBox.getText()));
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    selectedCuisines.add(checkBox.getText());
                } else {
                    selectedCuisines.remove(checkBox.getText());
                }
                aggiornaEtichetta();
            });
            cuisineCheckBoxes.add(checkBox);
            checkBoxes.getChildren().add(checkBox);
        }

        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);
            for (CheckBox checkBox : cuisineCheckBoxes) {
                boolean visible = query.isEmpty()
                        || checkBox.getText().toLowerCase(Locale.ROOT).contains(query);
                checkBox.setVisible(visible);
                checkBox.setManaged(visible);
            }
        });

        ScrollPane scrollPane = new ScrollPane(checkBoxes);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(180);
        scrollPane.setMaxHeight(220);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button clearButton = new Button("Deseleziona tutte");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand;"
        );
        clearButton.setOnAction(event -> clearSelection());

        menuContent.getChildren().addAll(searchInput, scrollPane, clearButton);
        CustomMenuItem menuItem = new CustomMenuItem(menuContent, false);
        menuItem.setHideOnClick(false);
        getItems().setAll(menuItem);
        setOnShowing(event -> Platform.runLater(searchInput::requestFocus));
        aggiornaEtichetta();
    }

    /**
     * Imposta le tipologie selezionate e aggiorna le caselle visibili.
     * I valori {@code null} e vuoti vengono ignorati.
     *
     * @param cuisines tipologie da selezionare, oppure {@code null} per azzerare la selezione
     */
    public void setSelectedCuisines(Collection<String> cuisines) {
        selectedCuisines.clear();
        if (cuisines != null) {
            cuisines.stream()
                    .filter(cuisine -> cuisine != null && !cuisine.isBlank())
                    .map(String::trim)
                    .forEach(selectedCuisines::add);
        }
        cuisineCheckBoxes.forEach(checkBox ->
                checkBox.setSelected(selectedCuisines.contains(checkBox.getText())));
        aggiornaEtichetta();
    }

    /**
     * Restituisce le tipologie selezionate nell'ordine in cui sono state selezionate.
     *
     * @return copia non modificabile delle tipologie selezionate
     */
    public List<String> getSelectedCuisines() {
        return List.copyOf(selectedCuisines);
    }

    /**
     * Deseleziona tutte le tipologie.
     */
    public void clearSelection() {
        selectedCuisines.clear();
        cuisineCheckBoxes.forEach(checkBox -> checkBox.setSelected(false));
        aggiornaEtichetta();
    }

    private void aggiornaEtichetta() {
        if (selectedCuisines.isEmpty()) {
            setText(PLACEHOLDER);
        } else if (selectedCuisines.size() == 1) {
            setText(selectedCuisines.iterator().next());
        } else {
            setText("Cucine (" + selectedCuisines.size() + ")");
        }
    }
}
