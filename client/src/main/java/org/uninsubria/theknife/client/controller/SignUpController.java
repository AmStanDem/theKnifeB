/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.components.DataNascitaInput;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.UtenteGiaEsistenteException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class SignUpController {
    private static final LocalDate DATA_NASCITA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ITALY)
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private ComboBox<String> ruoloComboBox;

    @FXML
    private DataNascitaInput dataNascitaField;

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
        dataNascitaField.setOnPrimaDiMostrare(this::aggiornaDataMassimaCalendario);

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

        if (email.length() > 254 || !email.matches(EMAIL_REGEX)) {
            labelErrore.setText("Inserisci un indirizzo email valido.");
            emailField.requestFocus();
            return;
        }

        ruolo = ruolo.trim();

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

        String ruoloValidato = ruolo;
        indirizzo.risolviLuogo(
                luogo -> registraUtente(event, nome, cognome, ruoloValidato, email, password, dataNascita, luogo),
                labelErrore::setText
        );
    }

    private void registraUtente(
            ActionEvent event,
            String nome,
            String cognome,
            String ruolo,
            String email,
            String password,
            LocalDate dataNascita,
            GeocodingService.Luogo luogo
    ) {
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

            UtenteDTO utente = new UtenteDTO(
                    null, nome, cognome, email, dataNascita, luogo.etichetta(), ruoloEnum);

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
    private void aggiornaDataMassimaCalendario() {
        LocalDate dataCorrenteDispositivo = LocalDate.now();
        try {
            dataCorrenteDispositivo = ServerConnection.getServer().ottieniDataCorrente();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            labelErrore.setText(
                    "Calendario aperto con la data del dispositivo; il server verificherà la data alla registrazione.");
        }
        dataNascitaField.setDataMassima(dataCorrenteDispositivo);
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