package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
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

import java.io.IOException;
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




    /**
     * Gestisce il click sul pulsante "Accedi": invoca il login remoto e,
     * se le credenziali sono corrette, apre la home dell'utente.
     */
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

    /**
     * Passa alla schermata di registrazione di un nuovo utente.
     */
    @FXML
    private void onVaiRegistrazione() {
        try {
            SceneManager.mostraSchermata("registrazione.fxml", "Registrati");
        } catch (IOException e) {
            labelErrore.setText("Impossibile aprire la schermata di registrazione.");
        }
    }

    /**
     * Salta l'autenticazione e prosegue come utente ospite (guest), con
     * accesso solo alle funzionalitÃ che non richiedono login.
     */
    @FXML
    private void onContinuaComeGuest() {
        SessioneUtente.logout();
        try {
            SceneManager.mostraSchermata("home.fxml", "Home (ospite)");
        } catch (IOException e) {
            labelErrore.setText("Impossibile aprire la home.");
        }
    }
}