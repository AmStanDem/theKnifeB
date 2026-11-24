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
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;

/**
 * Controller JavaFX del modulo per scrivere o modificare una recensione.
 * <p>
 * Gestisce la compilazione e la validazione del testo e della valutazione,
 * invia le recensioni al server e permette di aggiornare o annullare una
 * recensione esistente.
 * </p>
 * <p>
 * L'accesso alle operazioni è riservato agli utenti autenticati con ruolo
 * cliente.
 * </p>
 */
public class ScriviRecensioneController {

    @FXML
    private Label titoloLabel;

    @FXML
    private TextArea txtRecensione;

    // Cambia il tipo in RatingContainer
    @FXML
    private RatingContainer ratingRecensione;

    @FXML
    private Button btnInviaRecensione;

    private RecensioneDTO recensioneDaModificare;
    private RistoranteDTO ristorante;

    /**
     * Inizializza il modulo in base al contesto di apertura.
     * <p>
     * Recupera il ristorante selezionato e l'eventuale recensione da
     * modificare. Se l'utente è un cliente, popola i campi per la modifica;
     * in caso contrario disabilita l'invio e mostra un avviso.
     * </p>
     */
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

    /**
     * Gestisce l'invio di una nuova recensione o il salvataggio delle modifiche.
     * <p>
     * Verifica che l'utente sia un cliente e che siano presenti testo e
     * valutazione validi, quindi esegue l'operazione appropriata sul server.
     * Gestisce gli errori di validazione, concorrenza e comunicazione.
     * </p>
     *
     * @param actionEvent evento JavaFX usato per la navigazione al termine
     *                    dell'operazione
     */
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
                tornaAlRistoranteRecensito(actionEvent, false);
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
                SceneManager.replaceCurrentScene(
                        actionEvent,
                        "/org/uninsubria/theknife/client/views/Ristorante.fxml"
                );
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
            tornaAlRistoranteRecensito(actionEvent, true);
        } catch (ModificaObsoletaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione modificata altrove",
                    "Un altro dispositivo ha aggiornato questa recensione. Ricarica i dati prima di riprovare.");
            RecensioneSelezionata.pulisci();
            tornaAlRistoranteRecensito(actionEvent, true);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile salvare la recensione. Riprova più tardi.");
        }
    }

    /**
     * Annulla la scrittura o modifica della recensione e torna alla schermata
     * appropriata.
     *
     * @param actionEvent evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onAnnullaClick(ActionEvent actionEvent) {
        boolean modificaInCorso = recensioneDaModificare != null;
        RecensioneSelezionata.pulisci();
        if (modificaInCorso) {
            tornaAlRistoranteRecensito(actionEvent, true);
        } else {
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
        }
    }

    /**
     * Torna alla lista delle recensioni personali dopo la modifica di una
     * recensione esistente e pulisce la selezione corrente.
     *
     * @param actionEvent evento JavaFX usato per cambiare scena
     */
    private void tornaAlRistoranteRecensito(ActionEvent actionEvent, boolean scartaVistaCorrente) {
        RecensioneSelezionata.pulisci();
        if (scartaVistaCorrente) {
            SceneManager.replaceCurrentScene(
                    actionEvent,
                    "/org/uninsubria/theknife/client/views/MieRecensioni.fxml"
            );
        } else {
            SceneManager.switchScene(
                    actionEvent,
                    "/org/uninsubria/theknife/client/views/MieRecensioni.fxml"
            );
        }
    }

    /**
     * Visualizza una finestra di dialogo con il messaggio indicato.
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