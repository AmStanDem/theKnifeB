/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.CredenzialiErrateException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Label labelErrore;

    @FXML
    private Button signInButton;

    @FXML
    private void handleSignIn(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    /**
     * Gestisce il click sul pulsante "Accedi": invoca il login remoto e,
     * se le credenziali sono corrette, apre la home dell'utente.
     */
    @FXML
    private void onAccedi(ActionEvent event) {
        labelErrore.setText("");
        String email = campoEmail.getText();
        String password = campoPassword.getText();

        if (email.isBlank() || password.isBlank()) {
            labelErrore.setText("Inserisci email e password.");
            return;
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        signInButton.setDisable(true);

        Thread.startVirtualThread(() -> {
            try {
                UtenteDTO utente = ServerConnection.getServer().eseguiLogin(email, password);
                Platform.runLater(() -> {
                    SessioneUtente.login(utente);
                    SceneManager.switchScene(stage, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
                });
            } catch (CredenzialiErrateException e) {
                Platform.runLater(() -> mostraErroreLogin("Email o password non corretti."));
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                Platform.runLater(() -> mostraErroreLogin(
                        "Errore server: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            }
        });
    }

    private void mostraErroreLogin(String messaggio) {
        labelErrore.setText(messaggio);
        signInButton.setDisable(false);
    }

    /**
     * Passa alla schermata di registrazione di un nuovo utente.
     */
    @FXML
    private void onVaiRegistrazione(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/SignUp.fxml");
    }

    /**
     * Salta l'autenticazione e prosegue come utente ospite (guest).
     */
    @FXML
    private void onContinuaComeGuest(ActionEvent event) {
        SessioneUtente.logout();
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/home.fxml");
    }
}