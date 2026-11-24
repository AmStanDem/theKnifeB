/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO.RuoloUtente;

/**
 * Controller JavaFX della schermata dell'area personale.
 * <p>
 * Visualizza i dati dell'utente autenticato e configura le informazioni
 * e le azioni disponibili in base al ruolo, distinguendo tra cliente
 * e gestore.
 * </p>
 * <p>
 * L'area personale consente di accedere alle sezioni preferiti,
 * recensioni, ristoranti gestiti, aggiunta di un ristorante e modifica
 * delle credenziali.
 * </p>
 */
public class AreaPersonaleController {

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

    /**
     * Inizializza la vista dell'area personale.
     * <p>
     * Recupera l'utente dalla sessione corrente, quindi mostra i dati
     * comuni e configura i campi e i pulsanti disponibili in base al ruolo.
     * Se nessun utente è autenticato, il metodo termina senza modificare
     * i componenti della vista.
     * </p>
     */
    @FXML
    private void initialize() {

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();

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


    /**
     * Gestisce il click sul pulsante dei ristoranti preferiti.
     * <p>
     * Carica la schermata contenente l'elenco dei preferiti del cliente.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    private void onPreferitiClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/Preferiti.fxml"
        );
    }


    /**
     * Gestisce il click sul pulsante delle recensioni personali.
     * <p>
     * Carica la schermata con le recensioni scritte dal cliente
     * autenticato.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    private void onRecensioniClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MieRecensioni.fxml"
        );
    }


    /**
     * Gestisce il click sul pulsante di aggiunta di un ristorante.
     * <p>
     * Carica la schermata utilizzata da un gestore per inserire un nuovo
     * ristorante.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    private void onAggiungiClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/AggiungiRistorante.fxml"
        );
    }


    /**
     * Gestisce il click sul pulsante dei ristoranti gestiti.
     * <p>
     * Carica la lista dei ristoranti associati al gestore autenticato.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    private void onRistorantiClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/ListaRistorantiGestore.fxml"
        );
    }

    /**
     * Gestisce il click sul pulsante di modifica delle credenziali.
     * <p>
     * Carica la schermata per modificare i dati dell'account dell'utente
     * autenticato.
     * </p>
     *
     * @param event evento generato dal click sull'elemento JavaFX
     */
    @FXML
    private void onModificaCredenzialiClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/ModificaAccount.fxml"
        );
    }

    /**
     * Effettua il logout dell'utente corrente e torna alla schermata principale.
     *
     * @param event evento generato dal pulsante di logout
     */
    @FXML
    private void onLogOutClick(ActionEvent event) {
        SceneManager.clearHistory(SceneManager.getStage(event));
        SessioneUtente.logout();
        SceneManager.switchScene(
                event,
                "/org/uninsubria/theknife/client/views/MainLayout.fxml"
        );
    }
}