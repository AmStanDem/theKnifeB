package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;

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
            System.out.println("La password deve contenere almeno 8 caratteri.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() > 64) {
            System.out.println("La password non può superare 64 caratteri.");
            passwordField.requestFocus();
            return;
        }

        if (password.contains(" ")) {
            System.out.println("La password non può contenere spazi.");
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            System.out.println("La password deve contenere almeno una lettera minuscola.");
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            System.out.println("La password deve contenere almeno una lettera maiuscola.");
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*\\d.*")) {
            System.out.println("La password deve contenere almeno un numero.");
            passwordField.requestFocus();
            return;
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            System.out.println("La password deve contenere almeno un carattere speciale.");
            passwordField.requestFocus();
            return;
        }

        System.out.println("Registrazione:");
        System.out.println("Nome: " + nome);
        System.out.println("Cognome: " + cognome);
        System.out.println("Ruolo: " + ruolo);
        System.out.println("Email: " + email);

        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }

    public void handleAccedi(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/LoginView.fxml");
    }

    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }
}