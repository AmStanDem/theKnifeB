package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Rating;

/**
 * Controller del singolo item "recensione-item".
 * Non estende più VBox: viene istanziato tramite FXMLLoader classico
 * (fx:controller nel FXML), quindi va caricato così:
 *
 *   FXMLLoader loader = new FXMLLoader(getClass().getResource("/.../RecensioneItem.fxml"));
 *   Parent root = loader.load();
 *   RecensioneItemController controller = loader.getController();
 *   controller.setDati(...);
 *   contenitore.getChildren().add(root);
 *
 * Questo pattern è quello usato da ListaRecensioniController per popolare
 * dinamicamente la lista scorrevole.
 */
public class RecensioneItemController {

    @FXML private StackPane avatarCircle;
    @FXML private Label avatarInitial;
    @FXML private Label authorLabel;
    @FXML private Label localGuideLabel;
    @FXML private Rating ratingControl;
    @FXML private Label timeLabel;
    @FXML private Label reviewTextField;
    @FXML private Label likeCountLabel;
    @FXML private Label responseTextField;

    /** Imposta tutti i dati della recensione in un colpo solo. */
    public void setDati(String nomeAutore, boolean isLocalGuide, String data,
                         int valutazione, String testoRecensione, String testoRisposta) {
        setAutore(nomeAutore);
        setLocalGuide(isLocalGuide);
        setData(data);
        setValutazione(valutazione);
        setTesto(testoRecensione);
        setRisposta(testoRisposta);
    }

    public void setAutore(String nome) {
        authorLabel.setText(nome);
        if (nome != null && !nome.isBlank()) {
            avatarInitial.setText(String.valueOf(nome.trim().charAt(0)).toUpperCase());
        }
    }

    public void setLocalGuide(boolean isLocalGuide) {
        localGuideLabel.setVisible(isLocalGuide);
        localGuideLabel.setManaged(isLocalGuide);
    }

    public void setData(String data) {
        timeLabel.setText(data);
    }

    /** Valutazione da 0 a 5 stelle. */
    public void setValutazione(int stelle) {
        ratingControl.setRating(stelle);
    }

    public void setTesto(String testo) {
        reviewTextField.setText(testo);
    }

    /** Se non c'è ancora una risposta, nasconde la riga corrispondente. */
    public void setRisposta(String risposta) {
        boolean presente = risposta != null && !risposta.isBlank();
        responseTextField.setText(presente ? risposta : "");
        responseTextField.setVisible(presente);
        responseTextField.setManaged(presente);
        likeCountLabel.setVisible(presente);
        likeCountLabel.setManaged(presente);
    }
}
