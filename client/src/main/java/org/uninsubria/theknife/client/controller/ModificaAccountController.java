/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.components.DataNascitaInput;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.CredenzialiErrateException;
import org.uninsubria.theknife.common.exceptions.DatiMancantiException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.exceptions.UtenteGiaEsistenteException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Controller JavaFX della schermata di modifica dell'account.
 * <p>
 * Popola il modulo con i dati dell'utente autenticato e consente di modificarne
 * nome, cognome, email, data di nascita, domicilio e password. Prima del
 * salvataggio valida i campi, risolve il domicilio tramite geocoding e invia
 * l'aggiornamento al server tramite RMI.
 * </p>
 * <p>
 * Dopo un aggiornamento riuscito rigenera la sessione con i dati restituiti dal
 * server e torna alla schermata personale appropriata al ruolo dell'utente.
 * </p>
 */
public class ModificaAccountController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @FXML private Label titoloLabel;
    @FXML private Label messaggioLabel;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private DataNascitaInput dataNascitaField;
    @FXML private LuogoInput domicilioField;
    @FXML private PasswordField passwordCorrenteField;
    @FXML private PasswordField nuovaPasswordField;
    @FXML private PasswordField confermaPasswordField;
    @FXML private Button salvaButton;

    /**
     * Inizializza il modulo con i dati dell'utente autenticato.
     * <p>
     * Verifica che la sessione sia valida, configura il campo data con una
     * data massima pari alla data odierna e popola i campi con le informazioni
     * correnti dell'account.
     * </p>
     */
    @FXML
    private void initialize() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || utente.ruolo() == UtenteDTO.RuoloUtente.GUEST) {
            messaggioLabel.setText("Accedi con un account cliente o ristoratore per modificare il profilo.");
            salvaButton.setDisable(true);
            return;
        }
        titoloLabel.setText(utente.ruolo() == UtenteDTO.RuoloUtente.GESTORE
                ? "Modifica account ristoratore"
                : "Modifica account cliente");
        domicilioField.setPromptText("Inizia a scrivere e seleziona un luogo");
        domicilioField.addTextFieldStyleClass("custom-text-field");
        dataNascitaField.setDataMassima(LocalDate.now());
        nomeField.setText(utente.nome());
        cognomeField.setText(utente.cognome());
        emailField.setText(utente.email());
        if (utente.dataNascita() != null) {
            dataNascitaField.setText(DATE_FORMAT.format(utente.dataNascita()));
        }
        domicilioField.setText(utente.domicilio());
    }

    /**
     * Gestisce il salvataggio delle modifiche all'account.
     * <p>
     * Controlla la presenza dell'utente nella sessione, il formato della data,
     * i campi obbligatori e la corrispondenza tra nuova password e conferma.
     * Successivamente risolve il domicilio prima di inviare i dati al server.
     * </p>
     *
     * @param event evento JavaFX usato per identificare la finestra e la scena
     */
    @FXML
    private void onSalva(ActionEvent event) {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || utente.ruolo() == UtenteDTO.RuoloUtente.GUEST) {
            messaggioLabel.setText("La sessione non è più attiva. Accedi nuovamente.");
            return;
        }

        LocalDate dataNascita;
        try {
            String testoData = dataNascitaField.getText().trim();
            dataNascita = testoData.isEmpty() ? null : LocalDate.parse(testoData, DATE_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            messaggioLabel.setText("Inserisci una data valida nel formato gg/mm/aaaa.");
            return;
        }

        String nome = nomeField.getText() == null ? "" : nomeField.getText().trim();
        String cognome = cognomeField.getText() == null ? "" : cognomeField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String domicilio = domicilioField.getText() == null ? "" : domicilioField.getText().trim();
        String passwordCorrente = passwordCorrenteField.getText();
        String nuovaPassword = nuovaPasswordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || domicilio.isEmpty()
                || passwordCorrente == null || passwordCorrente.isBlank()) {
            messaggioLabel.setText("Compila tutti i campi obbligatori e inserisci la password attuale.");
            return;
        }
        if (!nuovaPassword.isEmpty() && !nuovaPassword.equals(confermaPassword)) {
            messaggioLabel.setText("La nuova password e la conferma non coincidono.");
            return;
        }
        if (nuovaPassword.isEmpty() && !confermaPassword.isEmpty()) {
            messaggioLabel.setText("Inserisci la nuova password oppure svuota il campo di conferma.");
            return;
        }

        salvaButton.setDisable(true);
        messaggioLabel.setText("Verifica della località...");
        domicilioField.risolviLuogo(
                luogo -> aggiornaAccount(event, utente, nome, cognome, email, dataNascita,
                        passwordCorrente, nuovaPassword, luogo.etichetta()),
                messaggio -> {
                    messaggioLabel.setText(messaggio);
                    salvaButton.setDisable(false);
                }
        );
    }

    /**
     * Invia al server i dati aggiornati dell'account.
     * <p>
     * Crea il DTO dell'utente aggiornato, conferma la password attuale tramite
     * il servizio remoto e aggiorna la sessione con il risultato restituito dal
     * server.
     * </p>
     *
     * @param event evento JavaFX associato al salvataggio
     * @param utente dati correnti dell'utente autenticato
     * @param nome nuovo nome dell'utente
     * @param cognome nuovo cognome dell'utente
     * @param email nuovo indirizzo email
     * @param dataNascita nuova data di nascita, oppure {@code null}
     * @param passwordCorrente password attuale necessaria per confermare il cambio
     * @param nuovaPassword nuova password, oppure stringa vuota per mantenerla invariata
     * @param domicilio domicilio risolto dal servizio di geocoding
     */
    private void aggiornaAccount(
            ActionEvent event,
            UtenteDTO utente,
            String nome,
            String cognome,
            String email,
            LocalDate dataNascita,
            String passwordCorrente,
            String nuovaPassword,
            String domicilio
    ) {
        UtenteDTO aggiornato = new UtenteDTO(
                utente.idUtente(), nome, cognome, email, dataNascita, domicilio, utente.ruolo());
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        messaggioLabel.setText("Salvataggio in corso...");
        Thread.startVirtualThread(() -> {
            try {
                UtenteDTO risultato = ServerConnection.getServer()
                        .aggiornaAccount(aggiornato, passwordCorrente, nuovaPassword);
                Platform.runLater(() -> {
                    SessioneUtente.login(risultato);
                    SceneManager.switchScene(stage, vistaProfilo(risultato));
                });
            } catch (CredenzialiErrateException e) {
                mostraErrore("La password attuale non è corretta.");
            } catch (UtenteGiaEsistenteException e) {
                mostraErrore("Esiste già un account con questa e-mail.");
            } catch (DatiMancantiException e) {
                mostraErrore(e.getMessage());
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                mostraErrore("Impossibile aggiornare l'account. Verifica la connessione e riprova.");
            }
        });
    }

    /**
     * Annulla la modifica e torna alla schermata precedente.
     *
     * @param event evento JavaFX usato per la navigazione
     */
    @FXML
    private void onAnnulla(ActionEvent event) {
        SceneManager.goBack(event);
    }

    /**
     * Restituisce la vista personale associata al ruolo dell'utente.
     *
     * @param utente utente del quale determinare la destinazione
     * @return percorso FXML dell'area ristoratore o dell'area personale
     */
    private String vistaProfilo(UtenteDTO utente) {
        return utente.ruolo() == UtenteDTO.RuoloUtente.GESTORE
                ? "/org/uninsubria/theknife/client/views/AreaRistoratore.fxml"
                : "/org/uninsubria/theknife/client/views/AreaPersonale.fxml";
    }

    /**
     * Visualizza un errore e riabilita il pulsante di salvataggio.
     *
     * @param messaggio testo dell'errore da mostrare
     */
    private void mostraErrore(String messaggio) {
        Platform.runLater(() -> {
            messaggioLabel.setText(messaggio);
            salvaButton.setDisable(false);
        });
    }
}
