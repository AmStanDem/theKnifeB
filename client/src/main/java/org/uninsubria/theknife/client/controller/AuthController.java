/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.uninsubria.theknife.client.util.BackHandler;
import org.uninsubria.theknife.client.util.GeocodingService;
import org.uninsubria.theknife.client.util.LuogoInput;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.components.DataNascitaInput;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;
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
import java.util.Locale;

/**
 * Controller unificato per autenticazione (login e registrazione).
 * <p>
 * Alterna tra la modalità login e registrazione tramite un singolo FXML
 * (AuthView.fxml), nascondendo/mostrando i relativi pannelli.
 * </p>
 */
public class AuthController implements BackHandler {

    private static final LocalDate DATA_NASCITA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ITALY)
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // --- Flag di modalità ---
    private boolean modalitaRegistrazione = false;

    // --- Pannelli di alternanza ---
    @FXML
    private javafx.scene.layout.VBox loginFields;

    @FXML
    private javafx.scene.layout.VBox registrazioneFields;

    // --- Campi login ---
    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Label labelErrore;

    @FXML
    private Button signInButton;

    // --- Campi registrazione ---
    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private ComboBox<String> ruoloComboBox;

    @FXML
    private DataNascitaInput dataNascitaField;

    @FXML
    private LuogoInput indirizzo;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confermaPasswordField;

    @FXML
    private Button registratiButton;

    /**
     * Inizializza il controllore.
     * <p>
     * Configura il campo domicilio, aggiorna la data massima del calendario
     * e controlla dinamicamente la corrispondenza tra password e conferma.
     * </p>
     */
    @FXML
    private void initialize() {
        // Imposta la modalità iniziale: login visibile, registrazione nascosta
        modalitaRegistrazione = false;
        loginFields.setVisible(true);
        loginFields.setManaged(true);
        registrazioneFields.setVisible(false);
        registrazioneFields.setManaged(false);

        indirizzo.setPromptText("Inizia a scrivere e seleziona un indirizzo valido");
        dataNascitaField.setOnPrimaDiMostrare(this::aggiornaDataMassimaCalendario);

        passwordField.textProperty().addListener((obs, oldValue, newValue) ->
                controllaConfermaPassword()
        );

        confermaPasswordField.textProperty().addListener((obs, oldValue, newValue) ->
                controllaConfermaPassword()
        );
    }

    /**
     * Alterna alla modalità registrazione.
     * <p>
     * Mostra il pannello di registrazione e nasconde quello di login.
     * </p>
     */
    @FXML
    private void onVaiRegistrazione() {
        modalitaRegistrazione = true;
        loginFields.setVisible(false);
        loginFields.setManaged(false);
        registrazioneFields.setVisible(true);
        registrazioneFields.setManaged(true);
        labelErrore.setText("");
    }

    /**
     * Alterna alla modalità login.
     * <p>
     * Mostra il pannello di login e nasconde quello di registrazione.
     * </p>
     *
     * @param evento evento JavaFX generato dal click sul collegamento
     */
    @FXML
    private void onVaiLogin(MouseEvent evento) {
        modalitaRegistrazione = false;
        registrazioneFields.setVisible(false);
        registrazioneFields.setManaged(false);
        loginFields.setVisible(true);
        loginFields.setManaged(true);
        labelErrore.setText("");
    }

    /**
     * Intercetta il pulsante "indietro" del layout comune.
     * <p>
     * Se è attiva la modalità registrazione, torna al pannello di login
     * restando nella stessa schermata; altrimenti lascia che la navigazione
     * prosegua normalmente.
     * </p>
     *
     * @return {@code true} se la richiesta è stata gestita internamente
     */
    @Override
    public boolean richiestaIndietro() {
        if (modalitaRegistrazione) {
            modalitaRegistrazione = false;
            registrazioneFields.setVisible(false);
            registrazioneFields.setManaged(false);
            loginFields.setVisible(true);
            loginFields.setManaged(true);
            labelErrore.setText("");
            return true;
        }
        return false;
    }

    // =====================================================================
    // LOGIN
    // =====================================================================

    /**
     * Gestisce il click sul pulsante "Accedi": invoca il login remoto e,
     * se le credenziali sono corrette, apre la home dell'utente.
     *
     * @param evento evento JavaFX generato dal pulsante di accesso
     */
    @FXML
    private void onAccedi(ActionEvent evento) {
        labelErrore.setText("");
        String email = campoEmail.getText().trim();
        String password = campoPassword.getText().trim();

        if (email.isBlank() || password.isBlank()) {
            labelErrore.setText("Inserisci email e password.");
            return;
        }

        Stage finestra = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        signInButton.setDisable(true);

        Thread.startVirtualThread(() -> {
            try {
                UtenteDTO utente = ServerConnection.ottieniServer().accedi(email, password);
                Platform.runLater(() -> {
                    SessioneUtente.accedi(utente);
                    SceneManager.pulisciCronologia(finestra);
                    SceneManager.cambiaScena(finestra, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
                });
            } catch (CredenzialiErrateException e) {
                Platform.runLater(() -> mostraErroreLogin("Email o password non corretti."));
            } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
                Platform.runLater(() -> mostraErroreLogin(
                        "Impossibile contattare il server. Verifica la connessione e riprova."));
            }
        });
    }

    /**
     * Mostra un messaggio di errore di autenticazione e riabilita il pulsante.
     *
     * @param messaggio testo dell'errore da visualizzare
     */
    private void mostraErroreLogin(String messaggio) {
        labelErrore.setText(messaggio);
        signInButton.setDisable(false);
    }

    // =====================================================================
    // REGISTRAZIONE
    // =====================================================================

    /**
     * Valida i dati inseriti e avvia la risoluzione del domicilio.
     * <p>
     * Verifica i campi obbligatori, il formato dell'email, il ruolo, la
     * corrispondenza e i requisiti della password. Quando i controlli
     * preliminari sono superati, delega il completamento della registrazione
     * alla risoluzione del luogo.
     * </p>
     *
     * @param evento evento JavaFX associato al pulsante di registrazione
     */
    @FXML
    private void gestisciRegistrazione(ActionEvent evento) {
        labelErrore.setText("");
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String ruolo = ruoloComboBox.getValue();
        String domicilio = indirizzo.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confermaPassword = confermaPasswordField.getText().trim();

        LocalDate dataNascita;
        try {
            String dataNascitaTesto = dataNascitaField.getText().trim();
            dataNascita = dataNascitaTesto.isEmpty()
                    ? null
                    : LocalDate.parse(dataNascitaTesto, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            labelErrore.setText("Inserisci una data valida nel formato gg/mm/aaaa.");
            dataNascitaField.requestFocus();
            return;
        }

        if (nome.isEmpty() || cognome.isEmpty() || ruolo == null || email.isEmpty()
                || password.isEmpty() || confermaPassword.isEmpty() || domicilio.isEmpty()) {
            labelErrore.setText("Compila tutti i campi obbligatori!");
            return;
        }

        if (email.length() > 254 || !email.matches(EMAIL_REGEX)) {
            labelErrore.setText("Inserisci un indirizzo email valido.");
            emailField.requestFocus();
            return;
        }

        ruolo = ruolo.trim();

        if (!password.equals(confermaPassword)) {
            labelErrore.setText("Le password non coincidono.");
            confermaPasswordField.requestFocus();
            return;
        }

        if (!ruolo.equals("Cliente") && !ruolo.equals("Ristoratore")) {
            labelErrore.setText("Il ruolo selezionato non è valido.");
            ruoloComboBox.requestFocus();
            return;
        }

        if (password.length() < 8 || password.length() > 64 || password.contains(" ") ||
                !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") ||
                !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*")) {
            labelErrore.setText("La password non soddisfa i requisiti di sicurezza.");
            passwordField.requestFocus();
            return;
        }

        String ruoloValidato = ruolo;
        indirizzo.risolviLuogo(
                luogo -> registraUtente(evento, nome, cognome, ruoloValidato, email, password, dataNascita, luogo),
                labelErrore::setText
        );
    }

    /**
     * Registra l'utente sul server e avvia la sessione dopo il successo.
     * <p>
     * Converte il ruolo selezionato, verifica la data di nascita rispetto
     * alla data corrente del server e invia i dati tramite il servizio remoto.
     * </p>
     *
     * @param evento evento JavaFX usato per cambiare scena dopo la registrazione
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param ruolo ruolo selezionato nel modulo
     * @param email indirizzo email dell'utente
     * @param password password dell'account
     * @param dataNascita data di nascita, oppure {@code null} se non specificata
     * @param luogo domicilio risolto dal servizio di geocoding
     */
    private void registraUtente(
            ActionEvent evento,
            String nome,
            String cognome,
            String ruolo,
            String email,
            String password,
            LocalDate dataNascita,
            GeocodingService.Luogo luogo
    ) {
        try {
            RuoloUtente ruoloEnum = ruolo.equals("Ristoratore") ? RuoloUtente.GESTORE : RuoloUtente.CLIENTE;
            var server = ServerConnection.ottieniServer();
            LocalDate dataCorrente = server.ottieniDataCorrente();

            if (dataNascita != null && dataNascita.isBefore(DATA_NASCITA_MINIMA)) {
                labelErrore.setText("La data di nascita non può essere precedente al 01/01/1900.");
                dataNascitaField.requestFocus();
                return;
            }
            if (dataNascita != null && dataNascita.isAfter(dataCorrente)) {
                labelErrore.setText("La data di nascita non può essere successiva a oggi.");
                dataNascitaField.requestFocus();
                return;
            }

            UtenteDTO utente = new UtenteDTO(
                    null, nome, cognome, email, dataNascita, luogo.etichetta(), ruoloEnum);

            // Chiamata remota al server
            utente = server.registrazione(utente, password);
            SessioneUtente.accedi(utente);

            // Dopo la registrazione, la schermata di autenticazione non deve restare nello storico.
            SceneManager.pulisciCronologia(SceneManager.ottieniStage(evento));
            Stage finestra = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            SceneManager.pulisciCronologia(finestra);
            SceneManager.cambiaScena(finestra, "/org/uninsubria/theknife/client/views/MainLayout.fxml");

        } catch (UtenteGiaEsistenteException e) {
            labelErrore.setText("Esiste già un account con questa email. Prova ad accedere.");
            emailField.requestFocus();
        } catch (DatiMancantiException e) {
            labelErrore.setText(e.getMessage());
        } catch (SistemaIndisponibileException | RemoteException | NotBoundException e) {
            labelErrore.setText("Il server non è aggiornato o non è raggiungibile. Riavvialo e riprova.");
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna la data massima selezionabile nel calendario.
     * <p>
     * Usa la data corrente fornita dal server; se il server non è disponibile,
     * imposta temporaneamente la data del dispositivo e informa l'utente.
     * </p>
     */
    @FXML
    private void aggiornaDataMassimaCalendario() {
        LocalDate dataCorrenteDispositivo = LocalDate.now();
        try {
            dataCorrenteDispositivo = ServerConnection.ottieniServer().ottieniDataCorrente();
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            labelErrore.setText(
                    "Calendario aperto con la data del dispositivo; il server verificherà la data alla registrazione.");
        }
        dataNascitaField.setDataMassima(dataCorrenteDispositivo);
    }

    /**
     * Aggiorna lo stile del campo di conferma in base alla corrispondenza
     * con la password.
     */
    private void controllaConfermaPassword() {
        String password = passwordField.getText();
        String confermaPassword = confermaPasswordField.getText();

        if (confermaPassword.isEmpty()) {
            confermaPasswordField.setStyle("");
        } else if (!password.equals(confermaPassword)) {
            confermaPasswordField.setStyle(
                    "-fx-border-color: red; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );
        } else {
            confermaPasswordField.setStyle("");
        }
    }
}