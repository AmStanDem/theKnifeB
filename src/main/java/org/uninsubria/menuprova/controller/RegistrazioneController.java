package org.uninsubria.menuprova.controller;

import javafx.fxml.FXML;
import org.uninsubria.menuprova.util.SceneManager;

import java.io.IOException;

/**
 * Controller della schermata di registrazione di un nuovo utente
 * (cliente o gestore). Da completare con il form e la chiamata a
 * {@code registrazione()} sul server.
 *
 * @author TheKnife Team
 */
public class RegistrazioneController {

    @FXML
    private void onTornaLogin() {
        try {
            SceneManager.mostraSchermata("login.fxml", "Accedi");
        } catch (IOException ignored) {
            // navigazione minima, errore non bloccante per questo stub
        }
    }
}
