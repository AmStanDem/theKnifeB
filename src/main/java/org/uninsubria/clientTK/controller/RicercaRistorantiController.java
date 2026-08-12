package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import org.uninsubria.clientTK.util.SceneManager;

import java.io.IOException;

/**
 * Controller della schermata di ricerca dei ristoranti, accessibile anche
 * senza login. Da completare con i filtri e la chiamata a
 * {@code cercaRistorante()} sul server, e con l'elenco risultati che apre
 * {@link DettaglioRistoranteController}.
 *
 * @author TheKnife Team
 */
public class RicercaRistorantiController {

    @FXML
    private void onTornaHome() {
        try {
            SceneManager.mostraSchermata("home.fxml", "Home");
        } catch (IOException ignored) {
            // navigazione minima, errore non bloccante per questo stub
        }
    }
}
