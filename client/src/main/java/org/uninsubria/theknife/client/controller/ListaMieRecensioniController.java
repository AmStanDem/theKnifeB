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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

public class ListaMieRecensioniController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ComboBox<String> ratingFilterCombo;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private Label resultsCountLabel;

    @FXML
    private ScrollPane reviewScrollPane;

    @FXML
    private VBox reviewContainer;

    private final ObservableList<RecensioneDTO> recensioniComplete =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        configuraFiltri();
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
            recensioniComplete.clear();
            return;
        }
        try {
            recensioniComplete.setAll(ServerConnection.getServer().getRecensioniUtente(utente.idUtente()));
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraErrore("Impossibile caricare le tue recensioni. Riprova più tardi.");
            recensioniComplete.clear();
        }
    }

    /**
     * Configura il filtro per la valutazione.
     */
    private void configuraFiltri() {

        ratingFilterCombo.setItems(
                FXCollections.observableArrayList(
                        "3+",
                        "3.5+",
                        "4+",
                        "4.5+"
                )
        );
    }

    /**
     * Ricerca nelle proprie recensioni.
     *
     * Cerca nel testo della recensione
     * oppure nel nome dell'autore.
     */
    @FXML
    private void onSearch() {

        applicaFiltri();
    }

    /**
     * Richiamato quando cambia il filtro.
     */
    @FXML
    private void onFilterChanged() {

        applicaFiltri();
    }

    /**
     * Reset ricerca e filtri.
     */
    @FXML
    private void onResetFilters() {

        searchField.clear();

        ratingFilterCombo
                .getSelectionModel()
                .clearSelection();

        aggiornaLista(recensioniComplete);
    }

    /**
     * Applica ricerca + filtro valutazione.
     */
    private void applicaFiltri() {

        String query = searchField.getText() == null
                ? ""
                : searchField.getText()
                .trim()
                .toLowerCase();

        String ratingSelezionato =
                ratingFilterCombo.getValue();

        Double ratingMinimo = ratingSelezionato == null
                ? null
                : parseRatingMinimo(ratingSelezionato);

        List<RecensioneDTO> filtrate = new ArrayList<>();

        for (RecensioneDTO recensione : recensioniComplete) {

            // -------------------------
            // RICERCA TESTO
            // -------------------------

            boolean matchTesto =
                    recensione.testo() != null
                            && recensione.testo()
                            .toLowerCase()
                            .contains(query);

            // -------------------------
            // RICERCA AUTORE
            // -------------------------

            boolean matchAutore =
                    recensione.nomeAutore() != null
                            && recensione.nomeAutore()
                            .toLowerCase()
                            .contains(query);

            // -------------------------
            // FILTRO RATING
            // -------------------------

            boolean matchRating =
                    ratingMinimo == null
                            || (
                            recensione.valutazione() != null
                                    && recensione.valutazione()
                                    >= ratingMinimo
                    );

            // -------------------------
            // RISULTATO
            // -------------------------

            boolean matchRicerca =
                    query.isEmpty()
                            || matchTesto
                            || matchAutore;

            if (matchRicerca && matchRating) {
                filtrate.add(recensione);
            }
        }

        aggiornaLista(filtrate);
    }

    /**
     * Converte:
     *
     * "3+"   -> 3.0
     * "3.5+" -> 3.5
     * "4+"   -> 4.0
     * "4.5+" -> 4.5
     */
    private double parseRatingMinimo(String etichetta) {

        return Double.parseDouble(
                etichetta.replace("+", "")
        );
    }

    /**
     * Aggiorna la lista delle recensioni.
     */
    private void aggiornaLista(
            List<RecensioneDTO> nuovaLista) {

        reviewContainer.getChildren().clear();

        for (RecensioneDTO recensione : nuovaLista) {

            Node card = creaCardRecensione(recensione);

            if (card != null) {
                reviewContainer.getChildren().add(card);
            }
        }

        resultsCountLabel.setText(
                nuovaLista.size() + " recensioni trovate"
        );

        reviewScrollPane.setVvalue(0);
    }



    private void modificaRecensione(RecensioneDTO recensione) {
        RecensioneSelezionata.imposta(recensione);
        Stage stage = (Stage) reviewContainer.getScene().getWindow();
        SceneManager.switchScene(stage, "/org/uninsubria/theknife/client/views/ScriviRecensione.fxml");
    }

    private void eliminaRecensione(RecensioneDTO recensione) {
        javafx.scene.control.Alert conferma = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        SceneManager.initOwner(conferma);
        conferma.setTitle("Elimina recensione");
        conferma.setHeaderText("Vuoi eliminare la recensione?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        if (conferma.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) {
            return;
        }

        UtenteDTO utente = SessioneUtente.getUtenteCorrente();
        if (utente == null || !SessioneUtente.isCliente()) {
            mostraErrore("Accedi come cliente per eliminare una recensione.");
            return;
        }
        try {
            ServerConnection.getServer().eliminaRecensione(recensione.idRecensione(), utente.idUtente());
            recensioniComplete.removeIf(elemento ->
                    java.util.Objects.equals(elemento.idRecensione(), recensione.idRecensione()));
            aggiornaLista(recensioniComplete);
        } catch (RecensioneNonTrovataException | OperazioneNonConsentitaException e) {
            mostraErrore("La recensione è già stata eliminata o non è più disponibile.");
            caricaRecensioni();
            aggiornaLista(recensioniComplete);
        } catch (RemoteException | NotBoundException | SistemaIndisponibileException e) {
            mostraErrore("Impossibile eliminare la recensione. Riprova più tardi.");
        }
    }

    private void mostraErrore(String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        SceneManager.initOwner(alert);
        alert.setTitle("Recensioni");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private Node creaCardRecensione(RecensioneDTO recensione) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/uninsubria/theknife/client/views/RecensioneItem.fxml"
                    )
            );

            Node card = loader.load();

            RecensioneItemController controller =
                    loader.getController();

            controller.setRecensione(recensione);
            controller.setAzioniProprie(this::modificaRecensione, this::eliminaRecensione);

            return card;

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}