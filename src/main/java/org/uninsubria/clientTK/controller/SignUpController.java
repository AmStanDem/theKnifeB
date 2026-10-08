package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import java.io.IOException;

/**
 * Controller JavaFX dell'interfaccia dedicata alla registrazione
 * di un nuovo utente.
 * <p>
 * Gestisce la raccolta e la validazione dei dati inseriti dall'utente,
 * verificando la correttezza dei campi anagrafici, del ruolo selezionato
 * e dei requisiti di sicurezza della password.
 * </p>
 *
 * <p>
 * Il controller gestisce inoltre la navigazione verso la schermata
 * principale dell'applicazione dopo la registrazione e verso la schermata
 * di accesso o quella principale tramite le relative azioni dell'interfaccia.
 * </p>
 */
public class SignUpController {

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private ComboBox<String> ruoloComboBox;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    /**
     * Gestisce la registrazione di un nuovo utente.
     * <p>
     * Recupera i dati inseriti nei campi della schermata e verifica
     * che siano stati compilati correttamente. Controlla inoltre che
     * il ruolo selezionato sia valido e che la password rispetti
     * i requisiti minimi di lunghezza e complessità.
     * </p>
     *
     * <p>
     * Se uno dei controlli non viene superato, viene mostrato un messaggio
     * nella console e il campo interessato riceve nuovamente il focus.
     * In caso di validazione positiva, viene caricata la schermata
     * {@code MainLayoutLoggato.fxml}.
     * </p>
     *
     * @param event evento generato dal click sul pulsante di registrazione
     */
    @FXML
    private void handleRegistrati(ActionEvent event) {

        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String ruolo = ruoloComboBox.getValue();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nome.isEmpty() ||
                cognome.isEmpty() ||
                ruolo == null ||
                email.isEmpty() ||
                password.isEmpty()) {

            System.out.println("Compila tutti i campi!");
            return;
        }

        if (!ruolo.equals("Cliente") &&
                !ruolo.equals("Ristoratore")) {

            System.out.println("Il ruolo selezionato non è valido.");
            ruoloComboBox.requestFocus();
            return;
        }

        if (password.length() < 8) {
            System.out.println(
                    "La password deve contenere almeno 8 caratteri."
            );
            passwordField.requestFocus();
            return;
        }

        if (password.length() > 64) {
            System.out.println(
                    "La password non può superare 64 caratteri."
            );
            passwordField.requestFocus();
            return;
        }

        if (password.contains(" ")) {
            System.out.println(
                    "La password non può contenere spazi."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            System.out.println(
                    "La password deve contenere almeno una lettera minuscola."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            System.out.println(
                    "La password deve contenere almeno una lettera maiuscola."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*\\d.*")) {
            System.out.println(
                    "La password deve contenere almeno un numero."
            );
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            System.out.println(
                    "La password deve contenere almeno un carattere speciale."
            );
            passwordField.requestFocus();
            return;
        }


        System.out.println("Registrazione:");
        System.out.println("Nome: " + nome);
        System.out.println("Cognome: " + cognome);
        System.out.println("Ruolo: " + ruolo);
        System.out.println("Email: " + email);


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
    }

    /**
     * Gestisce l'accesso alla schermata di login.
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code LoginView.fxml}.
     * </p>
     *
     * @param mouseEvent evento generato dal click dell'utente
     */
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

    /**
     * Gestisce il click sul logo dell'applicazione.
     * <p>
     * Riporta l'utente alla schermata principale caricando
     * la vista {@code MainLayout.fxml}.
     * </p>
     *
     * @param mouseEvent evento generato dal click sul logo
     */
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