/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;


import org.uninsubria.theknife.client.util.RecensioneSelezionata;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.stars.RatingContainer;
import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.AzioneDuplicataException;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.ModificaObsoletaException;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.RecensioneNonTrovataException;
import org.uninsubria.theknife.common.exceptions.RistoranteNonTrovatoException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

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

    // Cambia il tipo in RatingContainer
    @FXML
    private RatingContainer ratingRecensione;

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
        SessioneUtente.logout();
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    @FXML
    public void handleLogoClick(MouseEvent mouseEvent) {
        SceneManager.switchScene(mouseEvent, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
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
        double stelle = ratingRecensione.getRating();
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
                        stelle,
                        recensioneDaModificare.versione()
                );
                RecensioneSelezionata.pulisci();
                mostraMessaggio(Alert.AlertType.INFORMATION, "Recensione aggiornata",
                        "Le modifiche sono state salvate.");
                tornaAlRistoranteRecensito(actionEvent);
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
                SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
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
            tornaAlRistoranteRecensito(actionEvent);
        } catch (ModificaObsoletaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione modificata altrove",
                    "Un altro dispositivo ha aggiornato questa recensione. Ricarica i dati prima di riprovare.");
            RecensioneSelezionata.pulisci();
            tornaAlRistoranteRecensito(actionEvent);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile salvare la recensione. Riprova più tardi.");
        }
    }

    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        boolean modificaInCorso = recensioneDaModificare != null;
        RecensioneSelezionata.pulisci();
        if (modificaInCorso) {
            tornaAlRistoranteRecensito(actionEvent);
        } else {
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
        }
    }

    private void tornaAlRistoranteRecensito(ActionEvent actionEvent) {
        if (recensioneDaModificare == null || recensioneDaModificare.idRistorante() == null) {
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/MieRecensioni.fxml");
            return;
        }

        try {
            RistoranteDTO ristoranteRecensito = ServerConnection.getServer()
                    .getDettaglioRistorante(recensioneDaModificare.idRistorante());
            RistoranteSelezionato.setRistorante(ristoranteRecensito);
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
        } catch (RemoteException | NotBoundException | RistoranteNonTrovatoException
                 | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Ristorante non disponibile",
                    "La recensione è stata gestita, ma non è stato possibile aprire la scheda del ristorante.");
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/MieRecensioni.fxml");
        }
    }

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}