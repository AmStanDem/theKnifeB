/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.common.dto.RistoranteDTO;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Controller JavaFX della dashboard del gestore.
 * <p>
 * Mostra in un'unica schermata il profilo del gestore (nome, cognome, email, ruolo)
 * nella parte superiore e la lista dei ristoranti gestiti nella parte inferiore.
 * Consente di aggiungere, modificare e visualizzare i dettagli dei ristoranti,
 * nonche' di accedere al profilo utente e di effettuare il logout.
 * </p>
 */
public class DashboardGestoreController {

    private static final String VISTA_PROFILO =
            "/org/uninsubria/theknife/client/views/ProfiloView.fxml";

    @FXML
    private Label resultsCountLabel;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private VBox restaurantContainer;

    @FXML
    private Label lblNome;

    @FXML
    private Label lblCognome;

    @FXML
    private Label lblRuolo;

    @FXML
    private Label lblEmail;

    /**
     * Inizializza la dashboard del gestore.
     * <p>
     * Verifica il ruolo dell'utente corrente, popola i dati del profilo,
     * carica i ristoranti associati e attiva l'aggiornamento automatico
     * del catalogo.
     * </p>
     */
    @FXML
    private void initialize() {
        UtenteDTO gestore = SessioneUtente.ottieniUtenteCorrente();
        if (gestore == null || !SessioneUtente.eGestore()) {
            emptyStateLabel.setText("Accedi come ristoratore per visualizzare i tuoi ristoranti.");
            mostraErrore("Accesso non consentito", "Questa pagina e' riservata ai ristoratori.");
            return;
        }

        lblNome.setText(gestore.nome());
        lblCognome.setText(gestore.cognome());
        lblRuolo.setText(gestore.ruolo().name());
        lblEmail.setText(gestore.email());

        caricaRistoranti(gestore);
        RemoteRefreshManager.sottoscrivi(
                this, SubscriptionTopic.CATALOGO, null, evento -> caricaRistoranti(gestore),
                () -> caricaRistoranti(gestore));
    }

    /**
     * Recupera dal server i ristoranti associati al gestore e aggiorna la vista.
     * <p>
     * Le scheda vengono configurate per aprire i dettagli o la schermata di
     * modifica. Se non sono presenti ristoranti, viene mostrato lo stato vuoto.
     * </p>
     *
     * @param gestore gestore autenticato proprietario dei ristoranti
     */
    private void caricaRistoranti(UtenteDTO gestore) {
        try {
            List<RistoranteDTO> ristoranti =
                    ServerConnection.ottieniServer().ottieniRistorantiGestore(gestore.idUtente());
            restaurantContainer.getChildren().clear();
            resultsCountLabel.setText(ristoranti.size()
                    + (ristoranti.size() == 1 ? " ristorante" : " ristoranti"));

            if (ristoranti.isEmpty()) {
                emptyStateLabel.setText("Non hai ancora aggiunto ristoranti.");
                emptyStateLabel.setVisible(true);
                emptyStateLabel.setManaged(true);
                return;
            }

            emptyStateLabel.setVisible(false);
            emptyStateLabel.setManaged(false);
            for (RistoranteDTO ristorante : ristoranti) {
                FXMLLoader caricatore = new FXMLLoader(getClass().getResource(
                        "/org/uninsubria/theknife/client/views/RistoranteItem.fxml"));
                Node scheda = caricatore.load();
                RistoranteItemController controllore = caricatore.getController();
                controllore.impostaRistorante(ristorante);
                controllore.setOnDettagliAction(ristoranteSelezionato -> {
                    RistoranteSelezionato.impostaRitornoAiMieiRistoranti(true);
                });
                controllore.setOnModificaAction((evento, ristoranteSelezionato) -> {
                    FormRistoranteController formController = SceneManager.cambiaScenaEOttieniController(
                            SceneManager.ottieniStage(evento),
                            "/org/uninsubria/theknife/client/views/FormRistorante.fxml");
                    formController.apriModifica(ristoranteSelezionato);
                });
                restaurantContainer.getChildren().add(scheda);
            }
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            resultsCountLabel.setText("Impossibile caricare i ristoranti.");
            mostraErrore("Errore di connessione",
                    "Non e' stato possibile recuperare i ristoranti associati al tuo account.");
        } catch (IOException e) {
            resultsCountLabel.setText("Impossibile visualizzare i ristoranti.");
            mostraErrore("Errore di visualizzazione",
                    "Non e' stato possibile caricare la scheda di un ristorante.");
        }
    }

    /**
     * Apre l'area personale dell'utente.
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onProfiloClick(ActionEvent evento) {
        SceneManager.cambiaScena(evento, VISTA_PROFILO);
    }

    /**
     * Apre la schermata di inserimento di un nuovo ristorante.
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onAggiungiClick(ActionEvent evento) {
        FormRistoranteController formController = SceneManager.cambiaScenaEOttieniController(
                SceneManager.ottieniStage(evento),
                "/org/uninsubria/theknife/client/views/FormRistorante.fxml");
        formController.apriCreazione();
    }

    /**
     * Effettua il logout dell'utente corrente e torna alla schermata principale.
     *
     * @param evento evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onEsciClick(ActionEvent evento) {
        SceneManager.pulisciCronologia(SceneManager.ottieniStage(evento));
        SessioneUtente.esci();
        ListaRistorantiController.pulisciRicercaSalvata();
        SceneManager.cambiaScena(evento, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    /**
     * Visualizza una finestra di errore configurabile.
     *
     * @param titolo    titolo della finestra
     * @param messaggio descrizione dell'errore da mostrare
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert avviso = new Alert(Alert.AlertType.ERROR);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }
}