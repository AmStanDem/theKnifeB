package org.uninsubria.clientTK.controller;

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
}