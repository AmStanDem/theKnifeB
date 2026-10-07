package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.enums.RuoloUtente;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.common.exceptions.UtenteGiaEsistenteException;

import java.io.IOException;
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

/*
    @FXML
    private void onAccedi() {
        labelErrore.setText("");
        String email = campoEmail.getText();
        String password = campoPassword.getText();

        if (email.isBlank() || password.isBlank()) {
            labelErrore.setText("Inserisci email e password.");
            return;
        }

        try {
            UtenteDTO utente = ServerConnection.getServer().eseguiLogin(email, password);
            SessioneUtente.login(utente);
            SceneManager.mostraSchermata("MainLayoutLoggato.fxml", "Home");

        } catch (CredenzialiErrateException e) {
            labelErrore.setText("Email o password non corretti.");
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            e.printStackTrace();
            labelErrore.setText("Errore server: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
        } catch (IOException e) {
            labelErrore.setText("Errore nel caricamento della schermata successiva.");
        }
    }
*/
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

        if (nome.isEmpty() ||
                cognome.isEmpty() ||
                ruolo == null ||
                email.isEmpty() ||
                password.isEmpty() ||
                domicilio.isEmpty()) {

            labelErrore.setText("Compila i campi obbligatori!");
            return;
        }

        if (!ruolo.equals("Cliente") &&
                !ruolo.equals("Ristoratore")) {

            labelErrore.setText("Il ruolo selezionato non è valido.");
            ruoloComboBox.requestFocus();
            return;
        }

        if (password.length() < 8) {
            labelErrore.setText(
                    "La password deve contenere almeno 8 caratteri."
            );
            passwordField.requestFocus();
            return;
        }

        if (password.length() > 64) {
            labelErrore.setText(
                    "La password non può superare 64 caratteri."
            );
            passwordField.requestFocus();
            return;
        }

        if (password.contains(" ")) {
            labelErrore.setText(
                    "La password non può contenere spazi."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            labelErrore.setText(
                    "La password deve contenere almeno una lettera minuscola."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            labelErrore.setText(
                    "La password deve contenere almeno una lettera maiuscola."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*\\d.*")) {
            labelErrore.setText(
                    "La password deve contenere almeno un numero."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            labelErrore.setText(
                    "La password deve contenere almeno un carattere speciale."
            );
            passwordField.requestFocus();
            return;
        }

        /* try {

            RuoloUtente ruoloEnum = ruolo.equals("Ristoratore") ? RuoloUtente.GESTORE : RuoloUtente.CLIENTE;
            UtenteDTO utente = new UtenteDTO(null, nome, cognome, email,dataNascita, domicilio, ruoloEnum);
            utente = ServerConnection.getServer().registraCliente(utente,password);
        } catch (UtenteGiaEsistenteException | RemoteException | NotBoundException | DatiMancantiException |
                 SistemaIndisponibileException e) {
            labelErrore.setText(
                    e.getMessage()
            );
        }


        try {
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }

         */
        try {
            // 1. Creazione del DTO
            RuoloUtente ruoloEnum = ruolo.equals("Ristoratore") ? RuoloUtente.GESTORE : RuoloUtente.CLIENTE;
            UtenteDTO utente = new UtenteDTO(null, nome, cognome, email, dataNascita, domicilio, ruoloEnum);

            // 2. Chiamata bloccante al Server
            utente = ServerConnection.getServer().registraCliente(utente, password);

            // 3. SE IL CODICE ARRIVA QUI, LA REGISTRAZIONE HA AVUTO SUCCESSO.
            // Aggiorniamo la sessione (usando la tua classe di utilità)
            SessioneUtente.login(utente);

            // 4. Eseguiamo il cambio scena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(
                    FXMLLoader.load(getClass().getResource("/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml"))
            ));

        } catch (UtenteGiaEsistenteException e) {
            // L'eccezione blocca il try prima del cambio scena, e finisce qui.
            labelErrore.setText("Esiste già un account con questa email. Prova ad accedere.");
            emailField.requestFocus();

        } catch (DatiMancantiException e) {
            labelErrore.setText("Errore nei dati: " + e.getMessage());

        } catch (SistemaIndisponibileException | RemoteException | NotBoundException e) {
            labelErrore.setText("Errore di connessione al server.");
            e.printStackTrace();

        } catch (IOException e) {
            labelErrore.setText("Errore interno: impossibile caricare la schermata successiva.");
            e.printStackTrace();
        }
    }



    public void handleAccedi(MouseEvent mouseEvent) {
        try {
            Stage stage = (Stage) ((Node) mouseEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/LoginView.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogoClick(MouseEvent mouseEvent) {
        try {
            Stage stage = (Stage) ((Node) mouseEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/MainLayout.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}