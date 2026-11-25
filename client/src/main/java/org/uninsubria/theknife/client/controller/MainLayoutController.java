/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.RicercaIniziale;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;

/**
 * Controller JavaFX della schermata principale (home) dell'applicazione.
 * <p>
 * Permette di avviare una ricerca per ristoranti tramite località e testo,
 * precompilando la località con il domicilio dell'utente autenticato
 * quando disponibile.
 * </p>
 * <p>
 * Offre inoltre l'accesso alla ricerca avanzata e gestisce la validazione
 * della località prima di cambiare scena.
 * </p>
 */
public class MainLayoutController {

    @FXML
    private LuogoInput txtLocalita;

    @FXML
    private TextField txtRicerca;

    /**
     * Inizializza i componenti della schermata principale.
     * <p>
     * Configura le dimensioni del campo località e, per un utente autenticato,
     * precompila il campo con il domicilio salvato nel suo profilo.
     * </p>
     */
    @FXML
    public void initialize() {
        if (SessioneUtente.ottieniUtenteCorrente() != null) {
            String domicilio = SessioneUtente.ottieniUtenteCorrente().domicilio();
            if (domicilio != null && !domicilio.isBlank()) {
                txtLocalita.setText(domicilio);
            }
        }

        txtLocalita.setTextFieldPrefHeight(65);
        txtLocalita.impostaAmpiezzaPulsanteMappa(65);
    }

    /**
     * Gestisce l'avvio di una ricerca semplice.
     * <p>
     * Verifica la presenza della località, risolve il luogo selezionato tramite
     * geocoding e salva i criteri di ricerca prima di aprire la lista dei
     * ristoranti.
     * </p>
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onRicercaClick(ActionEvent evento) {
        if (txtLocalita.getText().isBlank()) {
            mostraErrore(
                    "Località da selezionare",
                    "Inserisci una località, selezionala dai suggerimenti oppure usa la mappa."
            );
            return;
        }

        txtLocalita.risolviLuogo(luogo -> {
            RicercaIniziale.imposta(txtRicerca.getText(), luogo);
            SceneManager.cambiaScena(
                    evento,
                    "/org/uninsubria/theknife/client/views/listaRistoranti.fxml"
            );
        }, messaggio -> mostraErrore("Località non valida", messaggio));
    }

    /**
     * Apre la schermata di ricerca avanzata.
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onRicercaAvanzataClick(ActionEvent evento) {
        SceneManager.cambiaScena(
                evento,
                "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml"
        );
    }

    /**
     * Visualizza un avviso relativo alla ricerca o alla località.
     *
     * @param titolo titolo della finestra di avviso
     * @param messaggio testo informativo mostrato all'utente
     */
    @FXML
    private void mostraErrore(String titolo, String messaggio) {
        Alert avviso = new Alert(Alert.AlertType.WARNING);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }
}
