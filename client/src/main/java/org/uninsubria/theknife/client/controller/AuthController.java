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

    // Vincoli di lunghezza dei campi anagrafici (allineati alla colonna VARCHAR(100) del database).
    private static final int NOME_MIN_LENGTH = 2;
    private static final int NOME_MAX_LENGTH = 100;
    private static final int COGNOME_MIN_LENGTH = 2;
    private static final int COGNOME_MAX_LENGTH = 100;

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

    // --- Etichette di errore per singolo campo della registrazione ---
    @FXML
    private Label nomeErrore;

    @FXML
    private Label cognomeErrore;

    @FXML
    private Label dataNascitaErrore;

    @FXML
    private Label domicilioErrore;

    @FXML
    private Label ruoloErrore;

    @FXML
    private Label emailErrore;

    @FXML
    private Label passwordErrore;

    @FXML
    private Label confermaPasswordErrore;

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

        configuraValidazioneDinamica();
    }

    /**
     * Collega la validazione in tempo reale ai campi della registrazione.
     * <p>
     * Un campo viene rivalutato mentre l'utente digita solo se il relativo
     * messaggio di errore è già visibile, così il messaggio scompare appena
     * l'utente corregge il valore senza disturbare la compilazione.
     * </p>
     */
    private void configuraValidazioneDinamica() {
        nomeField.textProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(nomeErrore, validaNome(newValue.trim())));

        cognomeField.textProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(cognomeErrore, validaCognome(newValue.trim())));

        emailField.textProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(emailErrore, validaEmail(newValue.trim())));

        ruoloComboBox.valueProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(ruoloErrore, validaRuolo(newValue)));

        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            aggiornaErroreSeMostrato(passwordErrore, validaPassword(newValue));
            aggiornaErroreSeMostrato(confermaPasswordErrore,
                    validaConfermaPassword(newValue, confermaPasswordField.getText()));
            controllaConfermaPassword();
        });

        confermaPasswordField.textProperty().addListener((obs, oldValue, newValue) -> {
            aggiornaErroreSeMostrato(confermaPasswordErrore,
                    validaConfermaPassword(passwordField.getText(), newValue));
            controllaConfermaPassword();
        });

        dataNascitaField.getCampoTesto().textProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(dataNascitaErrore, validaDataNascita(newValue.trim())));

        indirizzo.getCampoTesto().textProperty().addListener((obs, oldValue, newValue) ->
                aggiornaErroreSeMostrato(domicilioErrore, validaDomicilio(newValue.trim())));
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
        pulisciErroriRegistrazione();
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
        pulisciErroriRegistrazione();
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
            pulisciErroriRegistrazione();
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
     * Verifica tutti i campi del modulo (nome, cognome, data di nascita,
     * domicilio, ruolo, email, password e conferma password) mostrando per
     * ciascuno il relativo messaggio di errore. Quando tutti i controlli sono
     * superati, delega il completamento della registrazione alla risoluzione
     * del luogo.
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
        String dataNascitaTesto = dataNascitaField.getText().trim();

        // Forza il controllo di ogni campo e mostra il relativo messaggio di errore.
        if (!validaCampiRegistrazione(nome, cognome, ruolo, domicilio, email,
                password, confermaPassword, dataNascitaTesto)) {
            focalizzaPrimoCampoNonValido();
            return;
        }

        LocalDate dataNascita = dataNascitaTesto.isEmpty()
                ? null
                : LocalDate.parse(dataNascitaTesto, DATE_FORMATTER);

        String ruoloValidato = ruolo.trim();
        indirizzo.risolviLuogo(
                luogo -> registraUtente(evento, nome, cognome, ruoloValidato, email, password, dataNascita, luogo),
                messaggio -> {
                    mostraErroreCampo(domicilioErrore, messaggio);
                    indirizzo.requestFocus();
                }
        );
    }

    /**
     * Valida tutti i campi della registrazione, aggiornando per ognuno la
     * relativa etichetta di errore quando il valore non è valido.
     *
     * @return {@code true} se tutti i campi sono validi, {@code false} altrimenti
     */
    private boolean validaCampiRegistrazione(
            String nome,
            String cognome,
            String ruolo,
            String domicilio,
            String email,
            String password,
            String confermaPassword,
            String dataNascitaTesto
    ) {
        boolean valido = true;

        String erroreNome = validaNome(nome);
        mostraErroreCampo(nomeErrore, erroreNome);
        valido = valido && erroreNome == null;

        String erroreCognome = validaCognome(cognome);
        mostraErroreCampo(cognomeErrore, erroreCognome);
        valido = valido && erroreCognome == null;

        String erroreData = validaDataNascita(dataNascitaTesto);
        mostraErroreCampo(dataNascitaErrore, erroreData);
        valido = valido && erroreData == null;

        String erroreDomicilio = validaDomicilio(domicilio);
        mostraErroreCampo(domicilioErrore, erroreDomicilio);
        valido = valido && erroreDomicilio == null;

        String erroreRuolo = validaRuolo(ruolo);
        mostraErroreCampo(ruoloErrore, erroreRuolo);
        valido = valido && erroreRuolo == null;

        String erroreEmail = validaEmail(email);
        mostraErroreCampo(emailErrore, erroreEmail);
        valido = valido && erroreEmail == null;

        String errorePassword = validaPassword(password);
        mostraErroreCampo(passwordErrore, errorePassword);
        valido = valido && errorePassword == null;

        String erroreConferma = validaConfermaPassword(password, confermaPassword);
        mostraErroreCampo(confermaPasswordErrore, erroreConferma);
        valido = valido && erroreConferma == null;

        return valido;
    }

    /**
     * Verifica che il nome sia compilato e rispetti i vincoli di lunghezza.
     *
     * @param nome nome da validare, già ripulito dagli spazi
     * @return messaggio di errore, oppure {@code null} se il nome è valido
     */
    private String validaNome(String nome) {
        if (nome.isEmpty()) {
            return "Il nome è obbligatorio.";
        }
        if (nome.length() < NOME_MIN_LENGTH) {
            return "Il nome deve contenere almeno " + NOME_MIN_LENGTH + " caratteri.";
        }
        if (nome.length() > NOME_MAX_LENGTH) {
            return "Il nome non può superare " + NOME_MAX_LENGTH + " caratteri.";
        }
        return null;
    }

    /**
     * Verifica che il cognome sia compilato e rispetti i vincoli di lunghezza.
     *
     * @param cognome cognome da validare, già ripulito dagli spazi
     * @return messaggio di errore, oppure {@code null} se il cognome è valido
     */
    private String validaCognome(String cognome) {
        if (cognome.isEmpty()) {
            return "Il cognome è obbligatorio.";
        }
        if (cognome.length() < COGNOME_MIN_LENGTH) {
            return "Il cognome deve contenere almeno " + COGNOME_MIN_LENGTH + " caratteri.";
        }
        if (cognome.length() > COGNOME_MAX_LENGTH) {
            return "Il cognome non può superare " + COGNOME_MAX_LENGTH + " caratteri.";
        }
        return null;
    }

    /**
     * Verifica che la data di nascita (opzionale) sia nel formato corretto.
     *
     * @param dataNascitaTesto testo della data inserita
     * @return messaggio di errore, oppure {@code null} se la data è valida o assente
     */
    private String validaDataNascita(String dataNascitaTesto) {
        if (dataNascitaTesto == null || dataNascitaTesto.isEmpty()) {
            return null;
        }
        try {
            LocalDate.parse(dataNascitaTesto, DATE_FORMATTER);
            return null;
        } catch (DateTimeParseException e) {
            return "Inserisci una data valida nel formato gg/mm/aaaa.";
        }
    }

    /**
     * Verifica che il domicilio sia stato compilato.
     *
     * @param domicilio testo del domicilio
     * @return messaggio di errore, oppure {@code null} se valido
     */
    private String validaDomicilio(String domicilio) {
        if (domicilio == null || domicilio.isEmpty()) {
            return "Il domicilio è obbligatorio.";
        }
        return null;
    }

    /**
     * Verifica che sia stato selezionato un ruolo valido.
     *
     * @param ruolo ruolo selezionato
     * @return messaggio di errore, oppure {@code null} se valido
     */
    private String validaRuolo(String ruolo) {
        if (ruolo == null || ruolo.isBlank()) {
            return "Seleziona un ruolo.";
        }
        if (!ruolo.equals("Cliente") && !ruolo.equals("Ristoratore")) {
            return "Il ruolo selezionato non è valido.";
        }
        return null;
    }

    /**
     * Verifica che l'email sia compilata e nel formato corretto.
     *
     * @param email indirizzo email
     * @return messaggio di errore, oppure {@code null} se valido
     */
    private String validaEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "L'e-mail è obbligatoria.";
        }
        if (email.length() > 254 || !email.matches(EMAIL_REGEX)) {
            return "Inserisci un indirizzo email valido.";
        }
        return null;
    }

    /**
     * Verifica che la password rispetti i requisiti di sicurezza.
     *
     * @param password password da validare
     * @return messaggio di errore, oppure {@code null} se valida
     */
    private String validaPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La password è obbligatoria.";
        }
        if (password.length() < 8 || password.length() > 64 || password.contains(" ")
                || !password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*")
                || !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*")) {
            return "La password non soddisfa i requisiti di sicurezza.";
        }
        return null;
    }

    /**
     * Verifica che la conferma password coincida con la password.
     *
     * @param password password inserita
     * @param confermaPassword conferma inserita
     * @return messaggio di errore, oppure {@code null} se valida
     */
    private String validaConfermaPassword(String password, String confermaPassword) {
        if (confermaPassword == null || confermaPassword.isEmpty()) {
            return "Conferma la password.";
        }
        if (!password.equals(confermaPassword)) {
            return "Le password non coincidono.";
        }
        return null;
    }

    /**
     * Mostra o nasconde il messaggio di errore di un campo.
     *
     * @param etichetta etichetta associata al campo
     * @param messaggio messaggio di errore, oppure {@code null} per nasconderlo
     */
    private void mostraErroreCampo(Label etichetta, String messaggio) {
        boolean visibile = messaggio != null && !messaggio.isEmpty();
        etichetta.setText(visibile ? messaggio : "");
        etichetta.setManaged(visibile);
        etichetta.setVisible(visibile);
    }

    /**
     * Aggiorna un messaggio di errore durante la digitazione, ma solo se era
     * già visibile, così da non segnalare campi ancora da compilare.
     *
     * @param etichetta etichetta associata al campo
     * @param messaggio nuovo messaggio di errore, oppure {@code null} se valido
     */
    private void aggiornaErroreSeMostrato(Label etichetta, String messaggio) {
        if (etichetta.isVisible()) {
            mostraErroreCampo(etichetta, messaggio);
        }
    }

    /**
     * Porta il focus sul primo campo che presenta un messaggio di errore.
     */
    private void focalizzaPrimoCampoNonValido() {
        Label[] etichette = {
                nomeErrore, cognomeErrore, dataNascitaErrore, domicilioErrore,
                ruoloErrore, emailErrore, passwordErrore, confermaPasswordErrore
        };
        Node[] campi = {
                nomeField, cognomeField, dataNascitaField, indirizzo,
                ruoloComboBox, emailField, passwordField, confermaPasswordField
        };
        for (int i = 0; i < etichette.length; i++) {
            if (etichette[i] != null && etichette[i].isVisible()) {
                campi[i].requestFocus();
                return;
            }
        }
    }

    /**
     * Nasconde tutti i messaggi di errore della registrazione.
     */
    private void pulisciErroriRegistrazione() {
        mostraErroreCampo(nomeErrore, null);
        mostraErroreCampo(cognomeErrore, null);
        mostraErroreCampo(dataNascitaErrore, null);
        mostraErroreCampo(domicilioErrore, null);
        mostraErroreCampo(ruoloErrore, null);
        mostraErroreCampo(emailErrore, null);
        mostraErroreCampo(passwordErrore, null);
        mostraErroreCampo(confermaPasswordErrore, null);
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