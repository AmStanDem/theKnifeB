package org.uninsubria.menuprova.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.uninsubria.menuprova.util.SceneManager;
import org.uninsubria.menuprova.util.ServerConnection;
import org.uninsubria.menuprova.util.SessioneUtente;
import org.uninsubria.common.dto.RistoranteDTO;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Controller della schermata "I miei ristoranti", riservata ai gestori
 * registrati: elenca i ristoranti inseriti dall'utente con la relativa
 * valutazione media e il numero di recensioni ricevute, e permette di
 * aggiungerne di nuovi.
 *
 * @author TheKnife Team
 */
public class RistorantiGestoreController {

    @FXML
    private TableView<RistoranteDTO> tabellaRistoranti;
    @FXML
    private TableColumn<RistoranteDTO, String> colNome;
    @FXML
    private TableColumn<RistoranteDTO, String> colCitta;
    @FXML
    private TableColumn<RistoranteDTO, String> colCucina;
    @FXML
    private TableColumn<RistoranteDTO, String> colMedia;
    @FXML
    private TableColumn<RistoranteDTO, String> colNumRecensioni;

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
        colMedia.setCellValueFactory(dato ->
                new SimpleStringProperty(String.format("%.1f", dato.getValue().getMediaStelle())));
        colNumRecensioni.setCellValueFactory(dato ->
                new SimpleStringProperty(String.valueOf(dato.getValue().getNumeroRecensioni())));

        caricaRistoranti();
    }

    private void caricaRistoranti() {
        try {
            ObservableList<RistoranteDTO> ristoranti = FXCollections.observableArrayList(
                    ServerConnection.getServer().ristorantiDelGestore(SessioneUtente.getUtenteCorrente()));
            tabellaRistoranti.setItems(ristoranti);
        } catch (RemoteException | NotBoundException e) {
            labelMessaggio.setText("Impossibile contattare il server: " + e.getMessage());
        }
    }

    @FXML
    private void onAggiungiRistorante() {
        try {
            SceneManager.mostraSchermata("aggiungi-ristorante.fxml", "Nuovo ristorante");
        } catch (IOException e) {
            labelMessaggio.setText("Impossibile aprire il modulo di inserimento.");
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
