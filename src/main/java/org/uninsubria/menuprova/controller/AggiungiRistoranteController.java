package org.uninsubria.menuprova.controller;

import javafx.fxml.FXML;
import org.uninsubria.menuprova.util.SceneManager;

import java.io.IOException;

/**
 * Controller del form di inserimento di un nuovo ristorante, riservato ai
 * gestori. Da completare con il form e la chiamata a
 * {@code aggiungiRistorante()} sul server.
 *
 * @author TheKnife Team
 */
public class AggiungiRistoranteController {

    @FXML
    private void onTornaRistoranti() {
        try {
            SceneManager.mostraSchermata("ristoranti-gestore.fxml", "I miei ristoranti");
        } catch (IOException ignored) {
            // navigazione minima, errore non bloccante per questo stub
        }
    }
}
