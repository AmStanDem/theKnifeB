package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.common.exceptions.UtenteGiaEsistenteException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;

public class SignUpController {

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private ComboBox<String> ruoloComboBox;

    @FXML
    private DatePicker dataNascitaPicker;

    @FXML
    private TextField indirizzo;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label labelErrore;

    @FXML
    private void handleRegistrati(ActionEvent event) {
        labelErrore.setText("");
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String ruolo = ruoloComboBox.getValue();
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String domicilio = indirizzo.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nome.isEmpty() || cognome.isEmpty() || ruolo == null || email.isEmpty() || password.isEmpty() || domicilio.isEmpty()) {
            labelErrore.setText("Compila tutti i campi obbligatori!");
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
            UtenteDTO utente = new UtenteDTO(null, nome, cognome, email, dataNascita, domicilio, ruoloEnum);

            // Chiamata remota al server
            utente = ServerConnection.getServer().registraCliente(utente, password);
            SessioneUtente.login(utente);

            // Cambio scena pulito tramite SceneManager centralizzato
            SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");

        } catch (UtenteGiaEsistenteException e) {
            labelErrore.setText("Esiste già un account con questa email. Prova ad accedere.");
            emailField.requestFocus();
        } catch (DatiMancantiException e) {
            labelErrore.setText("Errore nei dati: " + e.getMessage());
        } catch (SistemaIndisponibileException | RemoteException | NotBoundException e) {
            labelErrore.setText("Errore di connessione al server.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAccedi(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/LoginView.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}