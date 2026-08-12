package org.uninsubria.menuprova.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
 * Controller della schermata "Le mie recensioni", riservata ai clienti
 * registrati: elenca le recensioni scritte dall'utente (con il ristorante
 * di riferimento e l'eventuale risposta del gestore) e permette di
 * modificarle o eliminarle.
 *
 * @author TheKnife Team
 */
public class MieRecensioniController {

    @FXML
    private TableView<RecensioneDTO> tabellaRecensioni;
    @FXML
    private TableColumn<RecensioneDTO, String> colRistorante;
    @FXML
    private TableColumn<RecensioneDTO, String> colStelle;
    @FXML
    private TableColumn<RecensioneDTO, String> colData;
    @FXML
    private TableColumn<RecensioneDTO, String> colRisposta;

    @FXML
    private TextArea areaTesto;
    @FXML
    private Spinner<Integer> spinnerStelle;
    @FXML
    private Label labelMessaggio;

    @FXML
    private void initialize() {
        colRistorante.setCellValueFactory(dato ->
                new SimpleStringProperty(dato.getValue().getNomeRistorante()));
        colStelle.setCellValueFactory(dato ->
                new SimpleStringProperty(String.valueOf(dato.getValue().getStelle())));
        colData.setCellValueFactory(dato ->
                new SimpleStringProperty(String.valueOf(dato.getValue().getData())));
        colRisposta.setCellValueFactory(dato -> {
            String risposta = dato.getValue().getRisposta();
            return new SimpleStringProperty(risposta == null || risposta.isBlank() ? "-" : risposta);
        });

        spinnerStelle.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 5));

        tabellaRecensioni.getSelectionModel().selectedItemProperty().addListener((oss, vecchia, nuova) -> {
            if (nuova != null) {
                areaTesto.setText(nuova.getTesto());
                spinnerStelle.getValueFactory().setValue(nuova.getStelle());
            }
        });

        caricaRecensioni();
    }

    private void caricaRecensioni() {
        try {
            ObservableList<RecensioneDTO> recensioni = FXCollections.observableArrayList(
                    ServerConnection.getServer().recensioniUtente(SessioneUtente.getUtenteCorrente()));
            tabellaRecensioni.setItems(recensioni);
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile contattare il server: " + e.getMessage());
        }
    }

    @FXML
    private void onModifica() {
        RecensioneDTO selezionata = tabellaRecensioni.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            labelMessaggio.setText("Seleziona prima una recensione dall'elenco.");
            return;
        }
        String nuovoTesto = areaTesto.getText();
        int nuoveStelle = spinnerStelle.getValue();

        try {
            ServerConnection.getServer().modificaRecensione(
                    SessioneUtente.getUtenteCorrente(), selezionata.getId(), nuoveStelle, nuovoTesto);
            labelMessaggio.setText("Recensione aggiornata.");
            caricaRecensioni();
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile salvare le modifiche: " + e.getMessage());
        }
    }

    @FXML
    private void onElimina() {
        RecensioneDTO selezionata = tabellaRecensioni.getSelectionModel().getSelectedItem();
        if (selezionata == null) {
            labelMessaggio.setText("Seleziona prima una recensione dall'elenco.");
            return;
        }
        try {
            ServerConnection.getServer().eliminaRecensione(SessioneUtente.getUtenteCorrente(), selezionata.getId());
            areaTesto.clear();
            labelMessaggio.setText("Recensione eliminata.");
            caricaRecensioni();
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile eliminare la recensione: " + e.getMessage());
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
