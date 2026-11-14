/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.UtenteGiaEsistenteException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;

public class SignUpController {
    private static final LocalDate DATA_NASCITA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ITALY)
                    .withResolverStyle(ResolverStyle.STRICT);

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private ComboBox<String> ruoloComboBox;

    @FXML
    private TextField dataNascitaField;

    @FXML
    private LuogoInput indirizzo;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confermaPasswordField;

    @FXML
    private Label labelErrore;

    @FXML
    private void initialize() {
        indirizzo.setPromptText("Inizia a scrivere e seleziona un indirizzo valido");

        passwordField.textProperty().addListener((obs, oldValue, newValue) ->
                controllaConfermaPassword()
        );

        confermaPasswordField.textProperty().addListener((obs, oldValue, newValue) ->
                controllaConfermaPassword()
        );
    }

    @FXML
    private void handleRegistrati(ActionEvent event) {
        labelErrore.setText("");
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String ruolo = ruoloComboBox.getValue();
        String domicilio = indirizzo.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confermaPassword = confermaPasswordField.getText().trim();

        LocalDate dataNascita;
        try {
            String dataNascitaTesto = dataNascitaField.getText().trim();
            dataNascita = dataNascitaTesto.isEmpty()
                    ? null
                    : LocalDate.parse(dataNascitaTesto, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            labelErrore.setText("Inserisci una data valida nel formato gg/mm/aaaa.");
            dataNascitaField.requestFocus();
            return;
        }

        if (nome.isEmpty() || cognome.isEmpty() || ruolo == null || email.isEmpty()
                || password.isEmpty() || confermaPassword.isEmpty() || domicilio.isEmpty()
                || dataNascita == null) {
            labelErrore.setText("Compila tutti i campi obbligatori!");
            return;
        }

        ruolo = ruolo.trim();

        GeocodingService.Luogo luogo = indirizzo.getLuogoSelezionato();
        if (luogo == null || !luogo.etichetta().equals(domicilio.trim())) {
            labelErrore.setText("Seleziona un domicilio valido dai suggerimenti.");
            indirizzo.requestFocus();
            return;
        }

        if (!password.equals(confermaPassword)) {
            labelErrore.setText("Le password non coincidono.");
            confermaPasswordField.requestFocus();
            return;
        }



        if (!ruolo.equals("Cliente") && !ruolo.equals("Ristoratore")) {
            labelErrore.setText("Il ruolo selezionato non è valido.");
            ruoloComboBox.requestFocus();
            return;
        }

        if (password.length() < 8 || password.length() > 64 || password.contains(" ") ||
                !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") ||
                !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*")) {
            labelErrore.setText("La password non soddisfa i requisiti di sicurezza.");
            passwordField.requestFocus();
            return;
        }

        try {
            RuoloUtente ruoloEnum = ruolo.equals("Ristoratore") ? RuoloUtente.GESTORE : RuoloUtente.CLIENTE;
            var server = ServerConnection.getServer();
            LocalDate dataCorrente = server.ottieniDataCorrente();

            if (dataNascita.isBefore(DATA_NASCITA_MINIMA)) {
                labelErrore.setText("La data di nascita non può essere precedente al 01/01/1900.");
                dataNascitaField.requestFocus();
                return;
            }
            if (dataNascita.isAfter(dataCorrente)) {
                labelErrore.setText("La data di nascita non può essere successiva a oggi.");
                dataNascitaField.requestFocus();
                return;
            }

            UtenteDTO utente = new UtenteDTO(null, nome, cognome, email, dataNascita, domicilio, ruoloEnum);

            // Chiamata remota al server
            utente = server.registraCliente(utente, password);
            SessioneUtente.login(utente);

            // Cambio scena pulito tramite SceneManager centralizzato
            SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");

        } catch (UtenteGiaEsistenteException e) {
            labelErrore.setText("Esiste già un account con questa email. Prova ad accedere.");
            emailField.requestFocus();
        } catch (DatiMancantiException e) {
            labelErrore.setText(e.getMessage());
        } catch (SistemaIndisponibileException | RemoteException | NotBoundException e) {
            labelErrore.setText("Il server non è aggiornato o non è raggiungibile. Riavvialo e riprova.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAccedi(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/LoginView.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    @FXML
    private void handleScegliDataNascita() {
        LocalDate dataCorrenteDispositivo = LocalDate.now();
        try {
            dataCorrenteDispositivo = ServerConnection.getServer().ottieniDataCorrente();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            labelErrore.setText(
                    "Calendario aperto con la data del dispositivo; il server verificherà la data alla registrazione.");
        }
        LocalDate dataCorrente = dataCorrenteDispositivo;

        LocalDate dataIniziale = dataCorrente;
        String testoData = dataNascitaField.getText().trim();
        if (!testoData.isEmpty()) {
            try {
                LocalDate dataInserita = LocalDate.parse(testoData, DATE_FORMATTER);
                if (!dataInserita.isBefore(DATA_NASCITA_MINIMA)
                        && !dataInserita.isAfter(dataCorrente)) {
                    dataIniziale = dataInserita;
                }
            } catch (DateTimeParseException ignored) {
                // Keep the calendar usable when replacing an invalid typed date.
            }
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Data di nascita");
        dialog.initOwner(dataNascitaField.getScene().getWindow());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ComboBox<Month> meseComboBox = new ComboBox<>();
        meseComboBox.getItems().setAll(Month.values());
        meseComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Month month) {
                if (month == null) {
                    return "";
                }
                String nome = month.getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                return nome.substring(0, 1).toUpperCase(Locale.ITALIAN) + nome.substring(1);
            }

            @Override
            public Month fromString(String value) {
                return null;
            }
        });
        meseComboBox.setValue(dataIniziale.getMonth());

        ComboBox<Integer> annoComboBox = new ComboBox<>();
        for (int anno = dataCorrente.getYear(); anno >= 1900; anno--) {
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
            Label intestazione = new Label(giorno.getDisplayName(TextStyle.SHORT, Locale.ITALIAN));
            intestazione.setMinWidth(38);
            intestazione.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
            calendario.add(intestazione, giorno.getValue() - 1, 0);
        }

        Runnable aggiornaCalendario = () -> {
            calendario.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null
                    && GridPane.getRowIndex(node) > 0);
            LocalDate primoGiorno = LocalDate.of(annoComboBox.getValue(), meseComboBox.getValue(), 1);
            int offset = primoGiorno.getDayOfWeek().getValue() - 1;
            for (int giorno = 1; giorno <= primoGiorno.lengthOfMonth(); giorno++) {
                LocalDate data = primoGiorno.withDayOfMonth(giorno);
                Button pulsanteGiorno = new Button(Integer.toString(giorno));
                pulsanteGiorno.setMaxWidth(Double.MAX_VALUE);
                pulsanteGiorno.setDisable(data.isBefore(DATA_NASCITA_MINIMA)
                        || data.isAfter(dataCorrente));
                pulsanteGiorno.setOnAction(event -> {
                    dialog.setResult(data);
                    dialog.close();
                });
                calendario.add(pulsanteGiorno, (offset + giorno - 1) % 7,
                        (offset + giorno - 1) / 7 + 1);
            }
        };
        meseComboBox.setOnAction(event -> aggiornaCalendario.run());
        annoComboBox.setOnAction(event -> aggiornaCalendario.run());
        aggiornaCalendario.run();

        VBox contenuto = new VBox(8, new HBox(8, meseComboBox, annoComboBox), calendario);
        contenuto.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(contenuto);
        dialog.showAndWait().ifPresent(data -> dataNascitaField.setText(DATE_FORMATTER.format(data)));
    }

    private void controllaConfermaPassword() {
        String password = passwordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (confermaPassword.isEmpty()) {
            confermaPasswordField.setStyle("");
        } else if (!password.equals(confermaPassword)) {
            confermaPasswordField.setStyle(
                    "-fx-border-color: red; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );
        } else {
            confermaPasswordField.setStyle("");
        }
    }
}