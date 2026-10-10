package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Rating;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.common.dto.RecensioneDTO;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RecensioneItemController {

    @FXML
    private StackPane avatarCircle;

    @FXML
    private Label avatarInitial;

    @FXML
    private Label authorLabel;

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

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN);

    public void setRecensione(RecensioneDTO recensione) {
        if (recensione == null) {
            return;
        }

        setAutore(recensione.nomeAutore());
        setLocalGuide(false);

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
        boolean presente = risposta != null && !risposta.isBlank();

        if (presente) {
            responseTextField.setText(risposta);
            responseTextField.setVisible(true);
            responseTextField.setManaged(true);
            likeCountLabel.setVisible(true);
            likeCountLabel.setManaged(true);
            replyButton.setVisible(false);
            replyButton.setManaged(false);
        } else {
            responseTextField.setVisible(false);
            responseTextField.setManaged(false);
            likeCountLabel.setVisible(false);
            likeCountLabel.setManaged(false);
            replyButton.setVisible(true);
            replyButton.setManaged(true);
        }
    }

    @FXML
    public void onRispondiClicked(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/RispondiRecensione.fxml");
    }
}