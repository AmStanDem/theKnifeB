/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.uninsubria.theknife.client.util.RecensioneDaRispondere;
import org.uninsubria.theknife.client.util.RecensioneSelezionata;
import org.uninsubria.theknife.client.util.RemoteRefreshManager;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.ServerConnection;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.dto.DataChangedEvent;
import org.uninsubria.theknife.common.dto.UtenteDTO;
import org.uninsubria.theknife.common.exceptions.OperazioneNonConsentitaException;
import org.uninsubria.theknife.common.exceptions.RecensioneNonTrovataException;
import org.uninsubria.theknife.common.exceptions.SistemaIndisponibileException;
import org.uninsubria.theknife.common.rmi.SubscriptionTopic;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Controller JavaFX della lista delle recensioni scritte dall'utente.
 * <p>
 * Recupera le recensioni del cliente autenticato e consente di modificarle
 * o eliminarle. Quando si avvia una modifica, conserva questa schermata come
 * origine per tornarvi dopo il salvataggio o l'annullamento.
 * </p>
 * <p>
 * L'elenco viene aggiornato in seguito agli eventi remoti relativi all'utente,
 * ad eccezione di quelli limitati alla modifica dei preferiti.
 * </p>
 */
public class MieRecensioniController {

    @FXML
    private Label resultsCountLabel;

    @FXML
    private ScrollPane restaurantScrollPane;

    @FXML
    private VBox restaurantContainer;

    private final ObservableList<RecensioneDTO> recensioniComplete =
            FXCollections.observableArrayList();

    // =========================================================
    // NAVIGAZIONE
    // =========================================================

    /**
     * Apre l'area personale dell'utente.
     *
     * @param evento evento generato dal click sull'area personale
     */
    @FXML
    private void onAreaPersonaleClick(MouseEvent evento) {
        SceneManager.cambiaScena(evento, "/org/uninsubria/theknife/client/views/ProfiloView.fxml");
    }
    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    /**
     * Inizializza la vista caricando le recensioni dell'utente.
     * <p>
     * Attiva inoltre la sottoscrizione agli aggiornamenti remoti dell'utente
     * per ricaricare l'elenco quando cambia uno dei dati rilevanti.
     * </p>
     */
    @FXML
    public void initialize() {
        caricaRecensioni();
        aggiornaLista(recensioniComplete);
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente != null) {
            RemoteRefreshManager.sottoscrivi(this, SubscriptionTopic.UTENTE, utente.idUtente(), evento -> {
                if (evento.tipo() != DataChangedEvent.ChangeType.PREFERITI_MODIFICATI) {
                    caricaRecensioni();
                    aggiornaLista(recensioniComplete);
                }
            }, () -> {
                caricaRecensioni();
                aggiornaLista(recensioniComplete);
            });
        }
    }

    /**
     * Recupera dal server le recensioni scritte dall'utente autenticato.
     * <p>
     * L'operazione richiede un utente cliente. In caso di accesso mancante o
     * errore di comunicazione svuota l'elenco e mostra un avviso.
     * </p>
     */
    private void caricaRecensioni() {
        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null || !SessioneUtente.eCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per visualizzare le tue recensioni.");
            recensioniComplete.clear();
            return;
        }

        try {
            recensioniComplete.setAll(ServerConnection.ottieniServer().visualizzaRecensioniUtente(utente.idUtente()));
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile caricare le tue recensioni. Riprova più tardi.");
            recensioniComplete.clear();
        }
    }

    // =========================================================
    // RICERCA E LISTA
    // =========================================================

    /**
     * Ricrea la lista grafica delle recensioni fornite.
     *
     * @param nuovaLista recensioni da visualizzare
     */
    private void aggiornaLista(List<RecensioneDTO> nuovaLista) {
        restaurantContainer.getChildren().clear();

        for (RecensioneDTO recensione : nuovaLista) {
            Node scheda = creaCardRecensione(recensione);
            if (scheda != null) {
                restaurantContainer.getChildren().add(scheda);
            }
        }

        resultsCountLabel.setText(nuovaLista.size() + " recensioni trovate");
        restaurantScrollPane.setVvalue(0);
    }

    /**
     * Crea la scheda grafica di una recensione caricando la relativa vista FXML.
     * <p>
     * Configura le azioni di modifica ed eliminazione disponibili per
     * l'utente proprietario della recensione.
     * </p>
     *
     * @param recensione recensione da rappresentare
     * @return nodo JavaFX della scheda, oppure {@code null} se il caricamento fallisce
     */
    private Node creaCardRecensione(RecensioneDTO recensione) {
        try {
            FXMLLoader caricatore = new FXMLLoader(getClass().getResource("/org/uninsubria/theknife/client/views/RecensioneItem.fxml"));
            Node scheda = caricatore.load();
            RecensioneItemController controllore = caricatore.getController();
            controllore.setRecensione(recensione);
            controllore.setAzioniProprie(this::modificaRecensione, this::confermaEliminazione);
            return scheda;
        } catch (Exception e) {
            System.err.println("ERRORE caricando RecensioneItem.fxml: " + e.getMessage());
            e.printStackTrace();
            mostraMessaggio(Alert.AlertType.ERROR, "Errore",
                    "Impossibile visualizzare una delle tue recensioni.");
            return null;
        }
    }

    /**
     * Seleziona una recensione e apre il modulo per modificarla.
     *
     * @param recensione recensione da modificare
     */
    private void modificaRecensione(RecensioneDTO recensione) {
        RecensioneDaRispondere.pulisci();
        RecensioneSelezionata.imposta(
                recensione,
                RecensioneSelezionata.OrigineModifica.MIE_RECENSIONI
        );
        Stage finestra = (Stage) restaurantContainer.getScene().getWindow();
        SceneManager.cambiaScena(finestra, "/org/uninsubria/theknife/client/views/ScriviRecensione.fxml");
    }

    /**
     * Richiede conferma e elimina una recensione dal server.
     * <p>
     * Dopo l'eliminazione aggiorna l'elenco locale. Se la recensione non è
     * più disponibile o il server restituisce un errore, gestisce l'esito
     * appropriato con un messaggio all'utente.
     * </p>
     *
     * @param recensione recensione da eliminare
     */
    private void confermaEliminazione(RecensioneDTO recensione) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        SceneManager.impostaProprietario(conferma);
        conferma.setTitle("Elimina recensione");
        conferma.setHeaderText("Vuoi eliminare la recensione per " + recensione.nomeRistorante() + "?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        if (conferma.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) {
            return;
        }

        UtenteDTO utente = SessioneUtente.ottieniUtenteCorrente();
        if (utente == null || !SessioneUtente.eCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per eliminare una recensione.");
            return;
        }

        try {
            ServerConnection.ottieniServer().eliminaRecensione(recensione.idRecensione(), utente.idUtente());
            recensioniComplete.removeIf(elemento ->
                    java.util.Objects.equals(elemento.idRecensione(), recensione.idRecensione()));
            aggiornaLista(recensioniComplete);
        } catch (RecensioneNonTrovataException | OperazioneNonConsentitaException e) {
            mostraMessaggio(Alert.AlertType.WARNING, "Recensione non trovata",
                    "La recensione è già stata eliminata o non è più disponibile.");
            caricaRecensioni();
            aggiornaLista(recensioniComplete);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile eliminare la recensione. Riprova più tardi.");
        }
    }

    /**
     * Visualizza una finestra di dialogo con messaggio configurabile.
     *
     * @param tipo tipo dell'avviso
     * @param titolo titolo della finestra
     * @param messaggio testo da mostrare all'utente
     */
    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert avviso = new Alert(tipo);
        SceneManager.impostaProprietario(avviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }
}