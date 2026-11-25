/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Campo per inserire una data di nascita o selezionarla da un calendario.
 * Accetta date comprese tra il 1 gennaio 1900 e la data massima configurata.
 */
public final class DataNascitaInput extends HBox {

    private static final LocalDate DATA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ITALY)
                    .withResolverStyle(ResolverStyle.STRICT);

    private final TextField campo = new TextField();
    private final Button seleziona = new Button("Scegli");
    private LocalDate dataMassima = LocalDate.now();
    private Runnable primaDiMostrare = () -> { };

    /**
     * Crea il campo di inserimento della data con il pulsante per aprire il calendario.
     */
    public DataNascitaInput() {
        setAlignment(Pos.CENTER);
        setSpacing(8);
        campo.setPromptText("gg/mm/aaaa");
        campo.setPrefHeight(40);
        campo.getStyleClass().add("custom-text-field");
        HBox.setHgrow(campo, Priority.ALWAYS);

        seleziona.setMnemonicParsing(false);
        seleziona.setPrefHeight(40);
        seleziona.getStyleClass().add("date-picker-button");
        seleziona.setOnAction(evento -> {
            primaDiMostrare.run();
            mostraCalendario(getScene() == null ? null : getScene().getWindow());
        });

        getChildren().addAll(campo, seleziona);
    }

    /**
     * Restituisce il testo attualmente inserito.
     *
     * @return testo del campo data
     */
    public String getText() {
        return campo.getText();
    }

    /**
     * Imposta il testo del campo data.
     *
     * @param text testo da visualizzare
     */
    public void setText(String text) {
        campo.setText(text);
    }

    /**
     * Richiede il focus per il campo di inserimento.
     */
    public void requestFocus() {
        campo.requestFocus();
    }

    /**
     * Imposta la data più recente selezionabile nel calendario.
     *
     * @param dataMassima data limite inclusiva, non precedente al 1 gennaio 1900
     * @throws IllegalArgumentException se la data è {@code null} o precedente al limite minimo
     */
    public void setDataMassima(LocalDate dataMassima) {
        if (dataMassima == null || dataMassima.isBefore(DATA_MINIMA)) {
            throw new IllegalArgumentException("La data massima deve essere successiva al 01/01/1900.");
        }
        this.dataMassima = dataMassima;
    }

    /**
     * Imposta un'azione da eseguire prima di mostrare il calendario.
     * Se l'azione è {@code null}, non viene eseguita alcuna operazione.
     *
     * @param azione azione preliminare
     */
    public void setOnPrimaDiMostrare(Runnable azione) {
        primaDiMostrare = azione == null ? () -> { } : azione;
    }

    /**
     * Mostra un calendario e, se viene selezionata una data, la scrive nel campo.
     *
     * @param proprietario finestra proprietaria del dialogo, oppure {@code null}
     */
    public void mostraCalendario(Window proprietario) {
        LocalDate dataIniziale = dataMassima;
        try {
            LocalDate dataInserita = LocalDate.parse(campo.getText().trim(), FORMATTER);
            if (!dataInserita.isBefore(DATA_MINIMA) && !dataInserita.isAfter(dataMassima)) {
                dataIniziale = dataInserita;
            }
        } catch (DateTimeParseException | NullPointerException ignored) {
            // Keep the calendar usable when replacing an invalid typed date.
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Data di nascita");
        if (proprietario != null) {
            dialog.initOwner(proprietario);
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.setResultConverter(button -> null);

        ComboBox<Month> meseComboBox = new ComboBox<>();
        meseComboBox.getItems().setAll(Month.values());
        meseComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Month mese) {
                if (mese == null) {
                    return "";
                }
                String nome = mese.getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                return nome.substring(0, 1).toUpperCase(Locale.ITALIAN) + nome.substring(1);
            }

            @Override
            public Month fromString(String valore) {
                return null;
            }
        });
        meseComboBox.setValue(dataIniziale.getMonth());

        ComboBox<Integer> annoComboBox = new ComboBox<>();
        for (int anno = dataMassima.getYear(); anno >= DATA_MINIMA.getYear(); anno--) {
            annoComboBox.getItems().add(anno);
        }
        annoComboBox.setValue(dataIniziale.getYear());
        annoComboBox.setVisibleRowCount(12);
        annoComboBox.setPrefWidth(110);

        GridPane calendario = new GridPane();
        calendario.setHgap(4);
        calendario.setVgap(4);
        calendario.setPadding(new Insets(12, 0, 0, 0));

        for (DayOfWeek giorno : DayOfWeek.values()) {
            Label intestazione = new Label(
                    giorno.getDisplayName(TextStyle.SHORT, Locale.ITALIAN));
            intestazione.setMinWidth(38);
            intestazione.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
            calendario.add(intestazione, giorno.getValue() - 1, 0);
        }

        Runnable aggiornaCalendario = () -> {
            calendario.getChildren().removeIf(node ->
                    GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
            LocalDate primoGiorno = LocalDate.of(
                    annoComboBox.getValue(), meseComboBox.getValue(), 1);
            int offset = primoGiorno.getDayOfWeek().getValue() - 1;
            for (int giorno = 1; giorno <= primoGiorno.lengthOfMonth(); giorno++) {
                LocalDate data = primoGiorno.withDayOfMonth(giorno);
                Button pulsanteGiorno = new Button(Integer.toString(giorno));
                pulsanteGiorno.setMaxWidth(Double.MAX_VALUE);
                pulsanteGiorno.setDisable(data.isBefore(DATA_MINIMA) || data.isAfter(dataMassima));
                pulsanteGiorno.setOnAction(evento -> {
                    dialog.setResult(data);
                    dialog.close();
                });
                calendario.add(pulsanteGiorno, (offset + giorno - 1) % 7,
                        (offset + giorno - 1) / 7 + 1);
            }
        };
        meseComboBox.setOnAction(evento -> aggiornaCalendario.run());
        annoComboBox.setOnAction(evento -> aggiornaCalendario.run());
        aggiornaCalendario.run();

        VBox contenuto = new VBox(8, new HBox(8, meseComboBox, annoComboBox), calendario);
        contenuto.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(contenuto);
        dialog.showAndWait().ifPresent(data -> campo.setText(FORMATTER.format(data)));
    }
}
