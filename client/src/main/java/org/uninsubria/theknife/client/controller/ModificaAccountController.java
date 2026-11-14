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

public class ModificaAccountController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @FXML private Label titoloLabel;
    @FXML private Label messaggioLabel;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private TextField dataNascitaField;
    @FXML private TextField domicilioField;
    @FXML private PasswordField passwordCorrenteField;
    @FXML private PasswordField nuovaPasswordField;
    @FXML private PasswordField confermaPasswordField;
    @FXML private Button salvaButton;

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
        nomeField.setText(utente.nome());
        cognomeField.setText(utente.cognome());
        emailField.setText(utente.email());
        if (utente.dataNascita() != null) {
            dataNascitaField.setText(DATE_FORMAT.format(utente.dataNascita()));
        }
        domicilioField.setText(utente.domicilio());
    }

    @FXML
    private void onSalva(ActionEvent event) {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || utente.ruolo() == UtenteDTO.RuoloUtente.GUEST) {
            messaggioLabel.setText("La sessione non è più attiva. Accedi nuovamente.");
            return;
        }

        LocalDate dataNascita;
        try {
            dataNascita = LocalDate.parse(dataNascitaField.getText().trim(), DATE_FORMAT);
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

        UtenteDTO aggiornato = new UtenteDTO(
                utente.idUtente(), nome, cognome, email, dataNascita, domicilio, utente.ruolo());
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        salvaButton.setDisable(true);
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

    @FXML
    private void onAnnulla(ActionEvent event) {
        SceneManager.goBack(event);
    }

    private String vistaProfilo(UtenteDTO utente) {
        return utente.ruolo() == UtenteDTO.RuoloUtente.GESTORE
                ? "/org/uninsubria/theknife/client/views/AreaRistoratore.fxml"
                : "/org/uninsubria/theknife/client/views/AreaPersonale.fxml";
    }

    private void mostraErrore(String messaggio) {
        Platform.runLater(() -> {
            messaggioLabel.setText(messaggio);
            salvaButton.setDisable(false);
        });
    }
}
