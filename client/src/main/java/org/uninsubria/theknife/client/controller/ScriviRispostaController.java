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
import javafx.scene.input.MouseEvent;
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

public class ScriviRispostaController {

    @FXML
    private TextArea txtRecensione;

    @FXML
    private Button btnInviaRisposta;

    @FXML
    private Button btnAnnulla;

    private RecensioneDTO recensione;

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

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/RicercaAvanzataView.fxml");
    }

    @FXML
    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/RecensioneItem.fxml");
    }

    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        RecensioneDaRispondere.pulisci();
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

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
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
        } catch (DatiMancantiException | OperazioneNonConsentitaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Risposta non inviata", e.getMessage());
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile inviare la risposta. Riprova più tardi.");
        }
    }

    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        RecensioneDaRispondere.pulisci();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
    }

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}