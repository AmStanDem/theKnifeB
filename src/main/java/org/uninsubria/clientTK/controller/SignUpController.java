package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    private void handleRegistrati() {

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

        System.out.println("Registrazione:");
        System.out.println("Nome: " + nome);
        System.out.println("Cognome: " + cognome);
        System.out.println("Ruolo: " + ruolo);
        System.out.println("Email: " + email);
    }
}