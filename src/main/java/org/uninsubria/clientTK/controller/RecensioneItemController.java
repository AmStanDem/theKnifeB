package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;

import java.io.IOException;

/**
 * Controller del componente riutilizzabile "recensione-item".
 * Uso come fx:root: <fx:root type="VBox" ...> nel file FXML.
 *
 * Esempio di utilizzo in un'altra view (via fx:include):
 *
 *   <fx:include fx:id="recensione1" source="RecensioneItem.fxml"/>
 *
 * e poi nel controller della view che lo include:
 *
 *   @FXML private RecensioneItemController recensione1Controller;
 *   ...
 *   recensione1Controller.setDati("Mario Rossi", true, "5 mesi fa", 2,
 *       "Testo della recensione...", "Testo della risposta...");
 *
 * Oppure creandolo via codice:
 *
 *   RecensioneItemController item = new RecensioneItemController();
 *   item.setDati(...);
 *   parentContainer.getChildren().add(item);
 */
public class RecensioneItemController extends VBox {

    @FXML private StackPane avatarCircle;
    @FXML private Label avatarInitial;
    @FXML private Label authorLabel;
    @FXML private Label localGuideLabel;
    @FXML private Label timeLabel;
    @FXML private Rating ratingControl;
    @FXML private TextField reviewTextField;
    @FXML private Label likeCountLabel;
    @FXML private TextField responseTextField;

    public RecensioneItemController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RecensioneItem.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Impossibile caricare RecensioneItem.fxml", e);
        }
    }

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
        responseTextField.getParent().setVisible(presente);
        responseTextField.getParent().setManaged(presente);
        likeCountLabel.setVisible(presente);
        likeCountLabel.setManaged(presente);
    }
}
