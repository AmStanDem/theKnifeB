package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.RecensioneDaRispondere;
import org.uninsubria.clientTK.util.SessioneUtente;
import org.uninsubria.common.dto.RecensioneDTO;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class RecensioneItemController {

    @FXML
    private StackPane avatarCircle;

    @FXML
    private Label avatarInitial;

    @FXML
    private Label authorLabel;

    @FXML
    private Label restaurantLabel;

    @FXML
    private Label localGuideLabel;

    @FXML
    private Rating ratingControl;

    @FXML
    private Label timeLabel;

    @FXML
    private Label reviewTextField;

    @FXML
    private Label likeCountLabel;

    @FXML
    private Label responseTextField;

    @FXML
    private Button replyButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private RecensioneDTO recensione;
    private Consumer<RecensioneDTO> onModificaAction;
    private Consumer<RecensioneDTO> onEliminaAction;
    private boolean rispostaAbilitata;
    private boolean rispostaPresente;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN);

    public void setRecensione(RecensioneDTO recensione) {
        if (recensione == null) {
            return;
        }
        this.recensione = recensione;

        setAutore(recensione.nomeAutore());
        setLocalGuide(false);
        boolean mostraRistorante = recensione.nomeRistorante() != null
                && !recensione.nomeRistorante().isBlank();
        restaurantLabel.setText(mostraRistorante ? recensione.nomeRistorante() : "");
        restaurantLabel.setVisible(mostraRistorante);
        restaurantLabel.setManaged(mostraRistorante);

        if (recensione.dataCreazione() != null) {
            setData(recensione.dataCreazione().format(DATE_FORMATTER));
        } else {
            setData("");
        }

        if (recensione.valutazione() != null) {
            setValutazione(recensione.valutazione());
        } else {
            setValutazione(0);
        }

        setTesto(recensione.testo());
        setRisposta(recensione.rispostaGestore());
    }

    public void setAzioniProprie(
            Consumer<RecensioneDTO> onModificaAction,
            Consumer<RecensioneDTO> onEliminaAction
    ) {
        this.onModificaAction = onModificaAction;
        this.onEliminaAction = onEliminaAction;
        editButton.setVisible(true);
        editButton.setManaged(true);
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
        replyButton.setVisible(false);
        replyButton.setManaged(false);
    }

    public void setDati(
            String nomeAutore,
            boolean isLocalGuide,
            String data,
            int valutazione,
            String testoRecensione,
            String testoRisposta) {

        setAutore(nomeAutore);
        setLocalGuide(isLocalGuide);
        setData(data);
        setValutazione(valutazione);
        setTesto(testoRecensione);
        setRisposta(testoRisposta);
    }

    public void setAutore(String nome) {
        if (nome == null || nome.isBlank()) {
            authorLabel.setText("Utente");
            avatarInitial.setText("U");
            return;
        }

        nome = nome.trim();
        authorLabel.setText(nome);
        avatarInitial.setText(String.valueOf(nome.charAt(0)).toUpperCase());
    }

    public void setLocalGuide(boolean isLocalGuide) {
        localGuideLabel.setVisible(isLocalGuide);
        localGuideLabel.setManaged(isLocalGuide);
    }

    public void setData(String data) {
        timeLabel.setText(data != null ? data : "");
    }

    public void setValutazione(int stelle) {
        stelle = Math.max(0, Math.min(5, stelle));
        ratingControl.setRating(stelle);
    }

    public void setTesto(String testo) {
        reviewTextField.setText(testo != null ? testo : "");
    }

    public void setRisposta(String risposta) {
        rispostaPresente = risposta != null && !risposta.isBlank();

        if (rispostaPresente) {
            responseTextField.setText(risposta);
            responseTextField.setVisible(true);
            responseTextField.setManaged(true);
            likeCountLabel.setVisible(true);
            likeCountLabel.setManaged(true);
        } else {
            responseTextField.setVisible(false);
            responseTextField.setManaged(false);
            likeCountLabel.setVisible(false);
            likeCountLabel.setManaged(false);
        }
        aggiornaPulsanteRisposta();
    }

    public void setRispostaAbilitata(boolean abilitata) {
        rispostaAbilitata = abilitata;
        aggiornaPulsanteRisposta();
    }

    private void aggiornaPulsanteRisposta() {
        boolean visibile = rispostaAbilitata && !rispostaPresente;
        replyButton.setVisible(visibile);
        replyButton.setManaged(visibile);
        replyButton.setDisable(!visibile);
    }

    @FXML
    public void onRispondiClicked(ActionEvent actionEvent) {
        if (rispostaAbilitata && SessioneUtente.isGestore()
                && !rispostaPresente && recensione != null
                && recensione.idRecensione() != null) {
            RecensioneDaRispondere.imposta(recensione);
            SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RispondiRecensione.fxml");
        }
    }

    @FXML
    private void onModificaClicked() {
        if (recensione != null && onModificaAction != null) {
            onModificaAction.accept(recensione);
        }
    }

    @FXML
    private void onEliminaClicked() {
        if (recensione != null && onEliminaAction != null) {
            onEliminaAction.accept(recensione);
        }
    }
}