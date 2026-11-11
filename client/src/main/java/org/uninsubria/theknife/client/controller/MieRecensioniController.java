/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import java.util.ArrayList;
import java.util.List;

public class MieRecensioniController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

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

    @FXML
    private void onAreaPersonaleClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/AreaPersonale.fxml");
    }


    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(event, "/org/uninsubria/theknife/client/views/MainLayout.fxml");
    }

    // =========================================================
    // INIZIALIZZAZIONE
    // =========================================================

    @FXML
    public void initialize() {
        caricaRecensioni();
        aggiornaLista(recensioniComplete);
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente != null) {
            RemoteRefreshManager.subscribe(this, SubscriptionTopic.USER, utente.idUtente(), event -> {
                if (event.type() != DataChangedEvent.ChangeType.FAVORITES_CHANGED) {
                    caricaRecensioni();
                    aggiornaLista(recensioniComplete);
                }
            }, () -> {
                caricaRecensioni();
                aggiornaLista(recensioniComplete);
            });
        }
    }

    private void caricaRecensioni() {
        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per visualizzare le tue recensioni.");
            recensioniComplete.clear();
            return;
        }

        try {
            recensioniComplete.setAll(ServerConnection.getServer().getRecensioniUtente(utente.idUtente()));
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore di connessione",
                    "Impossibile caricare le tue recensioni. Riprova più tardi.");
            recensioniComplete.clear();
        }
    }

    // =========================================================
    // RICERCA E LISTA
    // =========================================================

    @FXML
    private void onSearch() {
        String query = txtRicerca.getText() == null ? "" : txtRicerca.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            aggiornaLista(recensioniComplete);
            return;
        }

        List<RecensioneDTO> filtrate = new ArrayList<>();
        for (RecensioneDTO recensione : recensioniComplete) {
            boolean matchTesto = recensione.testo() != null && recensione.testo().toLowerCase().contains(query);
            boolean matchAutore = recensione.nomeAutore() != null && recensione.nomeAutore().toLowerCase().contains(query);
            boolean matchRistorante = recensione.nomeRistorante() != null
                    && recensione.nomeRistorante().toLowerCase().contains(query);

            if (matchTesto || matchAutore || matchRistorante) {
                filtrate.add(recensione);
            }
        }
        aggiornaLista(filtrate);
    }

    private void aggiornaLista(List<RecensioneDTO> nuovaLista) {
        restaurantContainer.getChildren().clear();

        for (RecensioneDTO recensione : nuovaLista) {
            Node card = creaCardRecensione(recensione);
            if (card != null) {
                restaurantContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(nuovaLista.size() + " recensioni trovate");
        restaurantScrollPane.setVvalue(0);
    }

    private Node creaCardRecensione(RecensioneDTO recensione) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/uninsubria/theknife/client/views/RecensioneItem.fxml"));
            Node card = loader.load();
            RecensioneItemController controller = loader.getController();
            controller.setRecensione(recensione);
            controller.setAzioniProprie(this::modificaRecensione, this::confermaEliminazione);
            return card;
        } catch (IOException e) {
            mostraMessaggio(Alert.AlertType.ERROR, "Errore",
                    "Impossibile visualizzare una delle tue recensioni.");
            return null;
        }
    }

    private void modificaRecensione(RecensioneDTO recensione) {
        RecensioneSelezionata.imposta(recensione);
        Stage stage = (Stage) restaurantContainer.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/uninsubria/theknife/client/views/ScriviRecensione.fxml");
    }

    private void confermaEliminazione(RecensioneDTO recensione) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        SceneManager.initOwner(conferma);
        conferma.setTitle("Elimina recensione");
        conferma.setHeaderText("Vuoi eliminare la recensione per " + recensione.nomeRistorante() + "?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        if (conferma.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) {
            return;
        }

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraMessaggio(Alert.AlertType.WARNING, "Accesso richiesto",
                    "Accedi come cliente per eliminare una recensione.");
            return;
        }

        try {
            ServerConnection.getServer().eliminaRecensione(recensione.idRecensione(), utente.idUtente());
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

    private void mostraMessaggio(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        SceneManager.initOwner(alert);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}