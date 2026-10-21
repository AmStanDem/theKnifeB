package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.RecensioneSelezionata;
import org.uninsubria.clientTK.util.RistoranteSelezionato;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.RecensioneDTO;
import org.uninsubria.common.dto.RistoranteDTO;
import org.uninsubria.common.dto.UtenteDTO;
import org.uninsubria.common.exceptions.AzioneDuplicataException;
import org.uninsubria.common.exceptions.DatiMancantiException;
import org.uninsubria.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.common.exceptions.RecensioneNonTrovataException;
import org.uninsubria.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;

public class ScriviRecensioneController {

    @FXML
    private Label titoloLabel;

    @FXML
    private TextField txtLocalita;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private TextArea txtRecensione;

    @FXML
    private Rating ratingRecensione;

    @FXML
    private Button btnInviaRecensione;

    @FXML
    private Button btnAnnulla;

    private RecensioneDTO recensioneDaModificare;
    private RistoranteDTO ristorante;

    @FXML
    public void initialize() {
        recensioneDaModificare = RecensioneSelezionata.get();
        ristorante = RistoranteSelezionato.getRistorante();

        boolean clienteLoggato = SessioneUtente.isCliente();
        txtRecensione.setDisable(!clienteLoggato);
        ratingRecensione.setDisable(!clienteLoggato);
        btnInviaRecensione.setDisable(!clienteLoggato);

        if (!clienteLoggato) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per scrivere o modificare una recensione.");
            return;
        }

        if (recensioneDaModificare != null) {
            titoloLabel.setText("Modifica la tua recensione");
            btnInviaRecensione.setText("Salva modifiche");
            txtRecensione.setText(recensioneDaModificare.testo());
            ratingRecensione.setRating(recensioneDaModificare.valutazione());
        } else if (ristorante == null || ristorante.idRistorante() == null) {
            btnInviaRecensione.setDisable(true);
            mostraMessaggio(Alert.AlertType.ERROR, "Ristorante non selezionato",
                    "Apri la scheda di un ristorante prima di scrivere una recensione.");
        }
    }

    @FXML
    private void onRicercaClick(ActionEvent event) {
        System.out.println("Ricerca cliccata");
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/clientTK/views/SignUp.fxml");
    }

    @FXML
    private void onGeolocalizzazioneClick() {
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml");
    }

    @FXML
    public void onPreferitiClick(ActionEvent actionEvent) {
    }

    @FXML
    public void onRecensioniClick(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RecensioneItem.fxml");
    }

    @FXML
    public void onLogOutClick(ActionEvent actionEvent) {
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/clientTK/views/MainLayout.fxml");
    }

    @FXML
    public void onInviaRecensioneClick(ActionEvent actionEvent) {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per scrivere una recensione.");
            return;
        }

        String testo = txtRecensione.getText();
        int stelle = (int) Math.round(ratingRecensione.getRating());
        if (testo == null || testo.isBlank() || stelle < 1 || stelle > 5) {
            mostraMessaggio(Alert.AlertType.WARNING, "Dati incompleti",
                    "Inserisci il testo e assegna da 1 a 5 stelle.");
            return;
        }

        try {
            if (recensioneDaModificare != null) {
                ServerConnection.getServer().modificaRecensione(
                        recensioneDaModificare.idRecensione(),
                        utente.idUtente(),
                        testo.trim(),
                        stelle
                );
                RecensioneSelezionata.pulisci();
                mostraMessaggio(Alert.AlertType.INFORMATION, "Recensione aggiornata",
                        "Le modifiche sono state salvate.");
                SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MieRecensioni.fxml");
            } else {
                RecensioneDTO recensione = new RecensioneDTO(
                        null,
                        stelle,
                        testo.trim(),
                        utente.nome(),
                        LocalDateTime.now(),
                        null,
                        ristorante.idRistorante(),
                        ristorante.nome()
                );
                ServerConnection.getServer().inserisciRecensione(
                        recensione,
                        ristorante.idRistorante(),
                        utente.idUtente()
                );
                mostraMessaggio(Alert.AlertType.INFORMATION, "Recensione inviata",
                        "La tua recensione è stata pubblicata.");
                SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/Ristorante.fxml");
            }
        } catch (AzioneDuplicataException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione già presente",
                    "Hai già recensito questo ristorante. Puoi modificare la recensione dalla sezione “Le mie recensioni”.");
        } catch (DatiMancantiException | OperazioneNonConsentitaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Operazione non riuscita", e.getMessage());
        } catch (RecensioneNonTrovataException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione non disponibile",
                    "La recensione è stata eliminata o non è più modificabile.");
            RecensioneSelezionata.pulisci();
            SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/MieRecensioni.fxml");
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile salvare la recensione. Riprova più tardi.");
        }
    }

    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        boolean modificaInCorso = recensioneDaModificare != null;
        RecensioneSelezionata.pulisci();
        String destinazione = modificaInCorso
                ? "/org/uninsubria/clientTK/views/MieRecensioni.fxml"
                : "/org/uninsubria/clientTK/views/Ristorante.fxml";
        SceneManager.switchScene(actionEvent, destinazione);
    }

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}