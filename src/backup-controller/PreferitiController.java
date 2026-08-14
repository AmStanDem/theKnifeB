package org.uninsubria.clientTK.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.ServerConnection;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Controller della schermata "I miei preferiti", riservata ai clienti
 * registrati: mostra i ristoranti che l'utente ha salvato come preferiti e
 * permette di rimuoverli o di aprirne il dettaglio.
 *
 * @author TheKnife Team
 */
public class PreferitiController {

    @FXML
    private TableView<RistoranteDTO> tabellaPreferiti;
    @FXML
    private TableColumn<RistoranteDTO, String> colNome;
    @FXML
    private TableColumn<RistoranteDTO, String> colCitta;
    @FXML
    private TableColumn<RistoranteDTO, String> colCucina;
    @FXML
    private TableColumn<RistoranteDTO, String> colPrezzo;
    @FXML
    private TableColumn<RistoranteDTO, String> colMedia;

    @FXML
    private Label labelMessaggio;

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getNome()));
        colCitta.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getCitta()));
        colCucina.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getTipoCucina()));
        colPrezzo.setCellValueFactory(dato ->
                new SimpleStringProperty(String.valueOf(dato.getValue().getFasciaPrezzo())));
        colMedia.setCellValueFactory(dato ->
                new SimpleStringProperty(String.format("%.1f", dato.getValue().getMediaStelle())));

        caricaPreferiti();
    }

    private void caricaPreferiti() {
        try {
            ObservableList<RistoranteDTO> preferiti = FXCollections.observableArrayList(
                    ServerConnection.getServer().visualizzaPreferiti(SessioneUtente.getUtenteCorrente()));
            tabellaPreferiti.setItems(preferiti);
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile contattare il server: " + e.getMessage());
        }
    }

    @FXML
    private void onVisualizzaDettaglio() {
        RistoranteDTO selezionato = tabellaPreferiti.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            labelMessaggio.setText("Seleziona prima un ristorante dall'elenco.");
            return;
        }
        DettaglioRistoranteController.setRistoranteDaMostrare(selezionato);
        try {
            SceneManager.mostraSchermata("dettaglio-ristorante.fxml", selezionato.getNome());
        } catch (IOException e) {
            labelMessaggio.setText("Impossibile aprire il dettaglio del ristorante.");
        }
    }

    @FXML
    private void onRimuoviPreferito() {
        RistoranteDTO selezionato = tabellaPreferiti.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            labelMessaggio.setText("Seleziona prima un ristorante dall'elenco.");
            return;
        }
        try {
            ServerConnection.getServer().rimuoviPreferito(SessioneUtente.getUtenteCorrente(), selezionato.getId());
            labelMessaggio.setText("");
            caricaPreferiti();
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile rimuovere il preferito: " + e.getMessage());
        }
    }

    @FXML
    private void onTornaHome() {
        try {
            SceneManager.mostraSchermata("home.fxml", "Home");
        } catch (IOException e) {
            labelMessaggio.setText("Impossibile tornare alla home.");
        }
    }
}
