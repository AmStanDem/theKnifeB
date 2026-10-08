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
 * Controller del componente riutilizzabile {@code recensione-item}.
 * <p>
 * Il controller gestisce i dati e la visualizzazione di una singola
 * recensione, occupandosi dell'autore, della valutazione, della data,
 * del testo della recensione e dell'eventuale risposta.
 * </p>
 *
 * <p>
 * Il componente viene utilizzato come {@code fx:root} all'interno
 * della relativa vista FXML e può essere incluso in altre viste
 * tramite {@code fx:include}.
 * </p>
 *
 * <p>
 * Dopo l'inclusione, i dati della recensione possono essere impostati
 * tramite il metodo {@link #setDati(String, boolean, String, int, String, String)}.
 * Il componente può inoltre essere creato direttamente via codice e
 * aggiunto a un contenitore JavaFX.
 * </p>
 */
public class RecensioneItemController {

    @FXML private StackPane avatarCircle;
    @FXML private Label avatarInitial;
    @FXML private Label authorLabel;
    @FXML private Label localGuideLabel;
    @FXML private Label timeLabel;
    @FXML private Rating ratingControl;
    @FXML private Label reviewTextField;
    @FXML private Label likeCountLabel;
    @FXML private Label responseTextField;

    /**
     * Imposta tutti i dati della recensione.
     * <p>
     * Il metodo aggiorna l'autore, l'eventuale indicazione di guida locale,
     * la data, la valutazione, il testo della recensione e la risposta.
     * </p>
     *
     * @param nomeAutore nome dell'autore della recensione
     * @param isLocalGuide indica se l'autore è una guida locale
     * @param data data o indicazione temporale della recensione
     * @param valutazione valutazione espressa in stelle
     * @param testoRecensione testo della recensione
     * @param testoRisposta eventuale risposta alla recensione
     */
    public void setDati(String nomeAutore, boolean isLocalGuide, String data,
                        int valutazione, String testoRecensione,
                        String testoRisposta) {

        setAutore(nomeAutore);
        setLocalGuide(isLocalGuide);
        setData(data);
        setValutazione(valutazione);
        setTesto(testoRecensione);
        setRisposta(testoRisposta);
    }

    /**
     * Imposta il nome dell'autore e la relativa iniziale visualizzata
     * nell'avatar.
     *
     * @param nome nome dell'autore
     */
    public void setAutore(String nome) {
        authorLabel.setText(nome);

        if (nome != null && !nome.isBlank()) {
            avatarInitial.setText(
                    String.valueOf(nome.trim().charAt(0)).toUpperCase()
            );
        }
    }

    /**
     * Mostra o nasconde l'indicazione relativa alla guida locale.
     *
     * @param isLocalGuide indica se l'autore è una guida locale
     */
    public void setLocalGuide(boolean isLocalGuide) {
        localGuideLabel.setVisible(isLocalGuide);
        localGuideLabel.setManaged(isLocalGuide);
    }

    /**
     * Imposta la data o l'indicazione temporale della recensione.
     *
     * @param data data o indicazione temporale da visualizzare
     */
    public void setData(String data) {
        timeLabel.setText(data);
    }

    /**
     * Imposta la valutazione della recensione.
     *
     * @param stelle numero di stelle della valutazione
     */
    public void setValutazione(int stelle) {
        ratingControl.setRating(stelle);
    }

    /**
     * Imposta il testo della recensione.
     *
     * @param testo testo della recensione
     */
    public void setTesto(String testo) {
        reviewTextField.setText(testo);
    }

    /**
     * Imposta l'eventuale risposta alla recensione.
     * <p>
     * Se non è presente una risposta, la relativa sezione e il conteggio
     * dei like vengono nascosti.
     * </p>
     *
     * @param risposta testo della risposta
     */
    public void setRisposta(String risposta) {
        boolean presente = risposta != null && !risposta.isBlank();

        responseTextField.setText(presente ? risposta : "");

        responseTextField.getParent().setVisible(presente);
        responseTextField.getParent().setManaged(presente);

        likeCountLabel.setVisible(presente);
        likeCountLabel.setManaged(presente);
    }
}
