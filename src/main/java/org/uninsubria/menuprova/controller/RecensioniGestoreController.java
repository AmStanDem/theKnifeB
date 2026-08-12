package org.uninsubria.menuprova.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import org.uninsubria.menuprova.util.SceneManager;
import org.uninsubria.menuprova.util.ServerConnection;
import org.uninsubria.menuprova.util.SessioneUtente;
import org.uninsubria.common.dto.RecensioneDTO;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Controller della schermata "Recensioni ricevute", riservata ai gestori
 * registrati: elenca le recensioni relative ai ristoranti dell'utente e
 * permette di rispondere a ciascuna (al massimo una risposta per recensione).
 *
 * @author TheKnife Team
 */
public class RecensioniGestoreController {

    @FXML
    private TableView<RecensioneDTO> tabellaRecensioni;
    @FXML
    private TableColumn<RecensioneDTO, String> colRistorante;
    @FXML
    private TableColumn<RecensioneDTO, String> colCliente;
    @FXML
    private TableColumn<RecensioneDTO, String> colStelle;
    @FXML
    private TableColumn<RecensioneDTO, String> colTesto;
    @FXML
    private TableColumn<RecensioneDTO, String> colRisposta;

    @FXML
    private TextArea areaRisposta;
    @FXML
    private Label labelMessaggio;

    @FXML
    private void initialize() {
        colRistorante.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getNomeRistorante()));
        colCliente.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getAutoreNome()));
        colStelle.setCellValueFactory(dato ->
                new SimpleStringProperty(String.valueOf(dato.getValue().getStelle())));
        colTesto.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getTesto()));
        colRisposta.setCellValueFactory(dato -> {
            String risposta = dato.getValue().getRisposta();
            return new SimpleStringProperty(risposta == null || risposta.isBlank() ? "-" : risposta);
        });

        tabellaRecensioni.getSelectionModel().selectedItemProperty().addListener((oss, vecchia, nuova) -> {
            if (nuova != null) {
                areaRisposta.setText(nuova.getRisposta() == null ? "" : nuova.getRisposta());
            }
        });

        caricaRecensioni();
    }

    private void caricaRecensioni() {
        try {
            ObservableList<RecensioneDTO> recensioni = FXCollections.observableArrayList(
                    ServerConnection.getServer().recensioniRicevute(SessioneUtente.getUtenteCorrente()));
            tabellaRecensioni.setItems(recensioni);
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile contattare il server: " + e.getMessage());
        }
    }

    @FXML
    private void onInviaRisposta() {
        RecensioneDTO selezionata = tabellaRecensioni.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            labelMessaggio.setText("Seleziona prima una recensione dall'elenco.");
            return;
        }
        String testoRisposta = areaRisposta.getText();
        if (testoRisposta.isBlank()) {
            labelMessaggio.setText("La risposta non può essere vuota.");
            return;
        }
        try {
            ServerConnection.getServer().rispostaRecensione(
                    SessioneUtente.getUtenteCorrente(), selezionata.getId(), testoRisposta);
            labelMessaggio.setText("Risposta inviata.");
            caricaRecensioni();
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile inviare la risposta: " + e.getMessage());
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
