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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.uninsubria.theknife.client.util.BackHandler;
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

/**
 * Controller JavaFX unificato per la gestione del profilo utente.
 * <p>
 * Alterna tra una vista di riepilogo (sezione profilo) e un modulo di modifica
 * (sezione modifica). Supporta sia clienti che gestori, mostrando pulsanti
 * e informazioni differenziati in base al ruolo.
 * </p>
 */
public class ProfiloController implements BackHandler {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // ===================== SEZIONE PROFILO =====================
    @FXML
    private VBox sezioneProfilo;

    @FXML
    private Label lblNome;

    @FXML
    private Label lblCognome;

    @FXML
    private Label lblData;

    @FXML
    private Label lblDomicilio;

    @FXML
    private Label lblRuolo;

    @FXML
    private Label lblEmail;

    @FXML
    private Button btnPreferiti;

    @FXML
    private Button btnRecensioni;

    @FXML
    private Button btnRistoranti;

    @FXML
    private Button btnAggiungi;

    @FXML
    private Button btnModificaCredenziali;

    // ===================== SEZIONE MODIFICA =====================
    @FXML
    private VBox sezioneModifica;

    @FXML
    private TextField nomeField;

    @FXML
    private TextField cognomeField;

    @FXML
    private TextField emailField;

    @FXML
    private DataNascitaInput dataNascitaField;

    @FXML
    private LuogoInput domicilioField;

    @FXML
    private PasswordField passwordCorrenteField;

    @FXML
    private PasswordField nuovaPasswordField;

    @FXML
    private PasswordField confermaPasswordField;

    @FXML
    private Label messaggioLabel;

    @FXML
    private Button btnSalva;

    @FXML
    private Button btnAnnulla;

    // ===================== NAVIGAZIONE VISTE =====================

    /**
     * Mostra la sezione profilo e nasconde quella di modifica.
     */
    private void mostraProfilo() {
        sezioneProfilo.setVisible(true);
        sezioneProfilo.setManaged(true);
        sezioneModifica.setVisible(false);
        sezioneModifica.setManaged(false);
    }

    /**
     * Mostra la sezione modifica e nasconde quella del profilo,
     * popolando i campi con i dati correnti dell'utente.
     */
    private void mostraModifica() {
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null) {
            return;
        }
        sezioneProfilo.setVisible(false);
        sezioneProfilo.setManaged(false);
        sezioneModifica.setVisible(true);
        sezioneModifica.setManaged(true);

        messaggioLabel.setText("");
        btnSalva.setDisable(false);

        domicilioField.setPromptText("Inizia a scrivere e seleziona un luogo");
        domicilioField.aggiungiStileCampoTesto("custom-text-field");
        dataNascitaField.setDataMassima(LocalDate.now());

        nomeField.setText(utente.nome());
        cognomeField.setText(utente.cognome());
        emailField.setText(utente.email());
        if (utente.dataNascita() != null) {
            dataNascitaField.setText(DATE_FORMAT.format(utente.dataNascita()));
        } else {
            dataNascitaField.setText("");
        }
        domicilioField.setText(utente.domicilio());
        passwordCorrenteField.setText("");
        nuovaPasswordField.setText("");
        confermaPasswordField.setText("");
    }

    /**
     * Intercetta il pulsante "indietro" del layout comune.
     * <p>
     * Se è aperta la sezione di modifica delle credenziali, la freccia riporta
     * alla vista del profilo invece di uscire dalla pagina; altrimenti lascia
     * che la navigazione prosegua normalmente.
     * </p>
     *
     * @return {@code true} se la richiesta è stata gestita internamente
     */
    @Override
    public boolean richiestaIndietro() {
        if (sezioneModifica.isVisible()) {
            mostraProfilo();
            return true;
        }
        return false;
    }

    // ===================== INIZIALIZZAZIONE =====================

    /**
     * Inizializza la vista del profilo.
     * <p>
     * Mostra la sezione profilo, recupera l'utente dalla sessione corrente,
     * visualizza i dati comuni e configura la visibilità dei pulsanti in
     * base al ruolo.
     * </p>
     */
    @FXML
    private void initialize() {
        mostraProfilo();

        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();

        if (utente == null) {
            return;
        }

        // Dati comuni
        lblNome.setText(utente.nome());
        lblCognome.setText(utente.cognome());
        lblEmail.setText(utente.email());

        // =========================
        // CLIENTE
        // =========================

        if (utente.ruolo() == RuoloUtente.CLIENTE) {

            lblRuolo.setText("Cliente");

            // Data di nascita
            if (utente.dataNascita() != null) {
                lblData.setText(utente.dataNascita().toString());
            } else {
                lblData.setText("-");
            }

            // Domicilio
            if (utente.domicilio() != null) {
                lblDomicilio.setText(utente.domicilio());
            } else {
                lblDomicilio.setText("-");
            }

            // Mostra data e domicilio
            lblData.getParent().setVisible(true);
            lblData.getParent().setManaged(true);

            lblDomicilio.getParent().setVisible(true);
            lblDomicilio.getParent().setManaged(true);

            // Mostra pulsanti cliente
            btnPreferiti.setVisible(true);
            btnPreferiti.setManaged(true);

            btnRecensioni.setVisible(true);
            btnRecensioni.setManaged(true);
            btnModificaCredenziali.setVisible(true);
            btnModificaCredenziali.setManaged(true);

            // Nasconde pulsanti ristoratore
            btnRistoranti.setVisible(false);
            btnRistoranti.setManaged(false);

            btnAggiungi.setVisible(false);
            btnAggiungi.setManaged(false);
        }

        // =========================
        // RISTORATORE
        // =========================

        else if (utente.ruolo() == RuoloUtente.GESTORE) {

            lblRuolo.setText("Ristoratore");

            // Data di nascita
            if (utente.dataNascita() != null) {
                lblData.setText(utente.dataNascita().toString());
            } else {
                lblData.setText("-");
            }

            // Domicilio
            if (utente.domicilio() != null) {
                lblDomicilio.setText(utente.domicilio());
            } else {
                lblDomicilio.setText("-");
            }

            // Mostra data e domicilio
            lblData.getParent().setVisible(true);
            lblData.getParent().setManaged(true);

            lblDomicilio.getParent().setVisible(true);
            lblDomicilio.getParent().setManaged(true);

            // Nasconde pulsanti cliente
            btnPreferiti.setVisible(false);
            btnPreferiti.setManaged(false);

            btnRecensioni.setVisible(false);
            btnRecensioni.setManaged(false);

            // Mostra pulsanti ristoratore
            btnRistoranti.setVisible(true);
            btnRistoranti.setManaged(true);

            btnAggiungi.setVisible(true);
            btnAggiungi.setManaged(true);

            btnModificaCredenziali.setVisible(true);
            btnModificaCredenziali.setManaged(true);
        }
    }

    // ===================== PROFILO: PULSANTI NAVIGAZIONE =====================

    /**
     * Gestisce il click sul pulsante dei ristoranti preferiti.
     */
    @FXML
    private void onPreferitiClick(ActionEvent evento) {
        SceneManager.cambiaScena(
                evento,
                "/org/uninsubria/theknife/client/views/Preferiti.fxml"
        );
    }

    /**
     * Gestisce il click sul pulsante delle recensioni personali.
     */
    @FXML
    private void onRecensioniClick(ActionEvent evento) {
        SceneManager.cambiaScena(
                evento,
                "/org/uninsubria/theknife/client/views/MieRecensioni.fxml"
        );
    }

    /**
     * Gestisce il click sul pulsante di aggiunta di un ristorante.
     */
    @FXML
    private void onAggiungiClick(ActionEvent evento) {
        FormRistoranteController formController = SceneManager.cambiaScenaEOttieniController(
                SceneManager.ottieniStage(evento),
                "/org/uninsubria/theknife/client/views/FormRistorante.fxml"
        );
        formController.apriCreazione();
    }

    /**
     * Gestisce il click sul pulsante dei ristoranti gestiti.
     */
    @FXML
    private void onRistorantiClick(ActionEvent evento) {
        SceneManager.cambiaScena(
                evento,
                "/org/uninsubria/theknife/client/views/DashboardGestore.fxml"
        );
    }

    /**
     * Gestisce il click sul pulsante di modifica delle credenziali.
     * <p>
     * Mostra la sezione di modifica del profilo.
     * </p>
     */
    @FXML
    private void onModificaCredenzialiClick(ActionEvent evento) {
        mostraModifica();
    }

    /**
     * Effettua il logout dell'utente corrente e torna alla schermata principale.
     */
    @FXML
    private void onEsciClick(ActionEvent evento) {
        SceneManager.pulisciCronologia(SceneManager.ottieniStage(evento));
        SessioneUtente.esci();
        ListaRistorantiController.pulisciRicercaSalvata();
        SceneManager.cambiaScena(
                evento,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }

    // ===================== MODIFICA: PULSANTI =====================

    /**
     * Annulla la modifica e torna alla sezione profilo.
     */
    @FXML
    private void onAnnullaModifica(ActionEvent evento) {
        mostraProfilo();
    }

    /**
     * Gestisce il salvataggio delle modifiche all'account.
     * <p>
     * Valida i campi (data, obbligatori, corrispondenza password),
     * risolve il domicilio tramite geocoding e invia l'aggiornamento
     * al server tramite RMI.
     * </p>
     */
    @FXML
    private void onSalvaModifica(ActionEvent evento) {
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null || utente.ruolo() == RuoloUtente.GUEST) {
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

        btnSalva.setDisable(true);
        messaggioLabel.setText("Verifica della località...");
        domicilioField.risolviLuogo(
                luogo -> aggiornaAccount(evento, utente, nome, cognome, email, dataNascita,
                        passwordCorrente, nuovaPassword, luogo.etichetta()),
                msg -> {
                    messaggioLabel.setText(msg);
                    btnSalva.setDisable(false);
                }
        );
    }

    /**
     * Invia al server i dati aggiornati dell'account.
     */
    private void aggiornaAccount(
            ActionEvent evento,
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
        Stage finestra = (Stage) ((javafx.scene.Node) evento.getSource()).getScene().getWindow();
        messaggioLabel.setText("Salvataggio in corso...");
        Thread.startVirtualThread(() -> {
            try {
                UtenteDTO risultato = ServerConnection.ottieniServer()
                        .aggiornaAccount(aggiornato, passwordCorrente, nuovaPassword);
                Platform.runLater(() -> {
                    SessioneUtente.accedi(risultato);
                    SceneManager.sostituisciScenaCorrente(
                            finestra,
                            "/org/uninsubria/theknife/client/views/ProfiloView.fxml"
                    );
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
     * Visualizza un errore e riabilita il pulsante di salvataggio.
     */
    private void mostraErrore(String messaggio) {
        Platform.runLater(() -> {
            messaggioLabel.setText(messaggio);
            btnSalva.setDisable(false);
        });
    }
}