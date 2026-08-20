package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;

/**
 * Controller della schermata di dettaglio di un singolo ristorante
 * (caratteristiche, media stelle, elenco recensioni). Riceve il ristorante
 * da mostrare tramite {@link #setRistoranteDaMostrare(RistoranteDTO)},
 * chiamato dalla schermata di provenienza prima del cambio di scena.
 * <p>
 * Da completare con {@code visualizzaRistorante()} / {@code visualizzaRecensioni()}
 * e, se l'utente è un cliente loggato, con i pulsanti "Aggiungi ai preferiti"
 * e "Scrivi una recensione".
 *
 * @author TheKnife Team
 */
public class DettaglioRistoranteController {

    private static RistoranteDTO ristoranteDaMostrare;

    @FXML
    private Label labelNome;

    /**
     * Imposta il ristorante che la prossima istanza di questo org.uninsubria.menuprova.controller
     * dovrà mostrare, letto in {@code initialize()}.
     *
     * @param ristorante il ristorante selezionato dalla schermata precedente
     */
    public static void setRistoranteDaMostrare(RistoranteDTO ristorante) {
        ristoranteDaMostrare = ristorante;
    }

    @FXML
    private void initialize() {
        if (ristoranteDaMostrare != null && labelNome != null) {
            labelNome.setText(ristoranteDaMostrare.getNome());
        }
    }

    @FXML
    private void onTornaHome() {
        try {
            SceneManager.mostraSchermata("home.fxml", "Home");
        } catch (IOException ignored) {
            // navigazione minima, errore non bloccante per questo stub
        }
    }
}
