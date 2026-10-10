package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.CredenzialiErrateException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;

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
    private void handleSignIn(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayout.fxml");
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

        try {
            UtenteDTO utente = ServerConnection.getServer().eseguiLogin(email, password);
            SessioneUtente.login(utente);
            SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/MainLayoutLoggato.fxml");

        } catch (CredenzialiErrateException e) {
            labelErrore.setText("Email o password non corretti.");
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            e.printStackTrace();
            labelErrore.setText("Errore server: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
        }
    }

    /**
     * Passa alla schermata di registrazione di un nuovo utente.
     */
    @FXML
    private void onVaiRegistrazione(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/registrazione.fxml");
    }

    /**
     * Salta l'autenticazione e prosegue come utente ospite (guest).
     */
    @FXML
    private void onContinuaComeGuest(ActionEvent event) {
        SessioneUtente.logout();
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/home.fxml");
    }
}