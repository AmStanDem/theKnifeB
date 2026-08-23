package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import org.uninsubria.common.dto.RecensioneDTO;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller del singolo item "RecensioneItem".
 */
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

    /**
     * Formatta la data nel formato:
     *
     * 30 luglio 2026
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "d MMMM yyyy",
                    Locale.ITALIAN
            );

    /**
     * Imposta direttamente i dati provenienti dal RecensioneDTO.
     */
    public void setRecensione(RecensioneDTO recensione) {

        if (recensione == null) {
            return;
        }

        // Autore
        setAutore(recensione.nomeAutore());

        // Non abbiamo questa informazione nel DTO,
        // quindi non mostriamo "Local Guide".
        setLocalGuide(false);

        // Data
        if (recensione.dataCreazione() != null) {
            setData(
                    recensione.dataCreazione()
                            .format(DATE_FORMATTER)
            );
        } else {
            setData("");
        }

        // Valutazione
        if (recensione.valutazione() != null) {
            setValutazione(recensione.valutazione());
        } else {
            setValutazione(0);
        }

        // Testo recensione
        setTesto(recensione.testo());

        // Risposta del gestore
        setRisposta(recensione.rispostaGestore());
    }

    /**
     * Metodo generico per impostare tutti i dati.
     */
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

        avatarInitial.setText(
                String.valueOf(nome.charAt(0))
                        .toUpperCase()
        );
    }

    public void setLocalGuide(boolean isLocalGuide) {

        localGuideLabel.setVisible(isLocalGuide);
        localGuideLabel.setManaged(isLocalGuide);
    }

    public void setData(String data) {

        timeLabel.setText(
                data != null ? data : ""
        );
    }

    /**
     * Valutazione da 0 a 5 stelle.
     */
    public void setValutazione(int stelle) {

        // Sicurezza: mantiene il valore tra 0 e 5
        stelle = Math.max(0, Math.min(5, stelle));

        ratingControl.setRating(stelle);
    }

    public void setTesto(String testo) {

        reviewTextField.setText(
                testo != null ? testo : ""
        );
    }

    /**
     * Gestisce la risposta del gestore.
     *
     * Se esiste:
     * - mostra la risposta
     * - nasconde "Rispondi"
     *
     * Se NON esiste:
     * - nasconde la risposta
     * - mostra "Rispondi"
     */
    public void setRisposta(String risposta) {

        boolean presente =
                risposta != null &&
                        !risposta.isBlank();

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

    /**
     * Apertura della schermata per rispondere alla recensione.
     */
    @FXML
    public void onRispondiClicked(ActionEvent actionEvent) {

        try {

            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/uninsubria/clientTK/views/RispondiRecensione.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}