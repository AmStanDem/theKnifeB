package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.UtenteDTO;

import java.io.IOException;

/**
 * Controller della schermata home: punto di partenza dopo il login (o dopo
 * l'accesso come ospite), da cui si raggiungono la ricerca dei ristoranti e,
 * per gli utenti registrati, le schermate riservate al proprio ruolo.
 *
 * @author TheKnife Team
 */
public class HomeController {

    @FXML
    private Label labelUtente;

    @FXML
    private Button btnPreferiti;
    @FXML
    private Button btnMieRecensioni;
    @FXML
    private Button btnMieiRistoranti;
    @FXML
    private Button btnRecensioniRicevute;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnAccedi;

    /**
     * Inizializza la vista mostrando solo i pulsanti pertinenti al ruolo
     * dell'utente corrente (cliente, gestore, o ospite non autenticato).
     */
    @FXML
    private void initialize() {
        if (SessioneUtente.isLoggato()) {
            UtenteDTO utente = SessioneUtente.getUtenteCorrente();
            labelUtente.setText("Ciao, " + utente.getNome());

            mostra(btnLogout, true);

            if (SessioneUtente.isCliente()) {
                mostra(btnPreferiti, true);
                mostra(btnMieRecensioni, true);
            } else if (SessioneUtente.isGestore()) {
                mostra(btnMieiRistoranti, true);
                mostra(btnRecensioniRicevute, true);
            }
        } else {
            labelUtente.setText("(modalità ospite)");
            mostra(btnAccedi, true);
        }
    }

    private void mostra(Button bottone, boolean visibile) {
        bottone.setVisible(visibile);
        bottone.setManaged(visibile);
    }

    @FXML
    private void onCerca() {
        cambiaSchermata("ricerca-ristoranti.fxml", "Cerca ristoranti");
    }

    @FXML
    private void onVaiPreferiti() {
        cambiaSchermata("preferiti.fxml", "I miei preferiti");
    }

    @FXML
    private void onVaiMieRecensioni() {
        cambiaSchermata("mie-recensioni.fxml", "Le mie recensioni");
    }

    @FXML
    private void onVaiRistorantiGestore() {
        cambiaSchermata("ristoranti-gestore.fxml", "I miei ristoranti");
    }

    @FXML
    private void onVaiRecensioniGestore() {
        cambiaSchermata("recensioni-gestore.fxml", "Recensioni ricevute");
    }

    @FXML
    private void onLogout() {
        SessioneUtente.logout();
        cambiaSchermata("login.fxml", "Accedi");
    }

    @FXML
    private void onAccedi() {
        cambiaSchermata("login.fxml", "Accedi");
    }

    private void cambiaSchermata(String fxml, String titolo) {
        try {
            SceneManager.mostraSchermata(fxml, titolo);
        } catch (IOException e) {
            labelUtente.setText("Errore nell'apertura di \"" + titolo + "\".");
        }
    }
}
