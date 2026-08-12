package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.CredenzialiErrateException;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Controller della schermata di login. Permette di autenticarsi, di passare
 * alla registrazione, oppure di proseguire come utente ospite (guest).
 *
 * @author TheKnife Team
 */
public class LoginController {

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Label labelErrore;

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
            UtenteDTO utente = ServerConnection.getServer().login(email, password);
            SessioneUtente.login(utente);
            SceneManager.mostraSchermata("home.fxml", "Home");
        } catch (CredenzialiErrateException e) {
            labelErrore.setText("Email o password non corretti.");
        } catch (RemoteException | NotBoundException e) {
            labelErrore.setText("Impossibile contattare il server: " + e.getMessage());
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
     * accesso solo alle funzionalità che non richiedono login.
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
