/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RecensioneDaRispondere;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Controller JavaFX del modulo per rispondere a una recensione.
 * <p>
 * Recupera la recensione selezionata e permette al gestore autenticato di
 * inviare una risposta, purché la recensione sia valida e non disponga già
 * di una risposta.
 * </p>
 */
public class ScriviRispostaController {

    @FXML
    private TextArea txtRecensione;

    @FXML
    private Button btnInviaRisposta;

    private RecensioneDTO recensione;

    /**
     * Inizializza il modulo verificando che sia possibile rispondere alla
     * recensione selezionata.
     * <p>
     * Se l'utente non è un gestore, la recensione non è valida o è già stata
     * risposta, disabilita i controlli di compilazione e invio e mostra un
     * avviso.
     * </p>
     */
    @FXML
    public void initialize() {
        recensione = RecensioneDaRispondere.get();
        boolean abilitato = SessioneUtente.isGestore()
                && recensione != null
                && recensione.idRecensione() != null
                && (recensione.rispostaGestore() == null
                || recensione.rispostaGestore().isBlank());
        txtRecensione.setDisable(!abilitato);
        btnInviaRisposta.setDisable(!abilitato);

        if (!abilitato) {
            mostraMessaggio(Alert.AlertType.WARNING, "Risposta non disponibile",
                    "Seleziona una recensione senza risposta e accedi come gestore.");
        }
    }

    /**
     * Valida e invia al server la risposta alla recensione.
     * <p>
     * Richiede un gestore autenticato, una recensione identificabile e un
     * testo non vuoto. Se l'operazione riesce, pulisce la selezione corrente
     * e torna alla pagina del ristorante.
     * </p>
     *
     * @param actionEvent evento JavaFX usato per cambiare scena dopo l'invio
     */
    @FXML
    public void onInviaRispostaClick(ActionEvent actionEvent) {
        UtenteDTO gestore = SessioneUtente.getUtenteCorrente();
        if (!SessioneUtente.isGestore() || gestore == null
                || recensione == null || recensione.idRecensione() == null) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come gestore e seleziona una recensione valida.");
            return;
        }

        String testo = txtRecensione.getText();
        if (testo == null || testo.isBlank()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Testo obbligatorio",
                    "Inserisci il testo della risposta.");
            return;
        }

        try {
            ServerConnection.getServer().rispondiARecensione(
                    recensione.idRecensione(), testo.trim(), gestore.idUtente());
            RecensioneDaRispondere.pulisci();
            mostraMessaggio(Alert.AlertType.INFORMATION, "Risposta inviata",
                    "La risposta è stata pubblicata.");
            SceneManager.replaceCurrentScene(
                    actionEvent,
                    "/org/uninsubria/theknife/client/views/Ristorante.fxml"
            );
        } catch (DatiMancantiException | OperazioneNonConsentitaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Risposta non inviata", e.getMessage());
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile inviare la risposta. Riprova più tardi.");
        }
    }

    /**
     * Annulla la compilazione della risposta e torna alla pagina del ristorante.
     *
     * @param actionEvent evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        RecensioneDaRispondere.pulisci();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
    }

    /**
     * Visualizza una finestra di dialogo con il messaggio specificato.
     *
     * @param tipo tipo dell'avviso
     * @param titolo titolo della finestra
     * @param messaggio testo da mostrare all'utente
     */
    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}