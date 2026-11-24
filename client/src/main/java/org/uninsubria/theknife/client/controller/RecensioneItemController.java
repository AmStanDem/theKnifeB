/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.RecensioneDaRispondere;
import org.uninsubria.theknife.client.util.SessioneUtente;
import org.uninsubria.theknife.client.util.stars.BaseRatingView;
import org.uninsubria.theknife.common.dto.RecensioneDTO;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Controller JavaFX della scheda di una recensione.
 * <p>
 * Visualizza autore, data, valutazione, testo e risposta del gestore.
 * In base al contesto della vista abilita le azioni per rispondere,
 * modificare o eliminare la recensione.
 * </p>
 */
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
    private HBox ratingStarsContainer;

    private BaseRatingView ratingControl;

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

    /**
     * Inizializza il componente grafico della valutazione.
     */
    @FXML
    private void initialize() {
        ratingControl = new BaseRatingView(0);
        ratingControl.setSpacing(1);
        ratingControl.setScaleX(0.5);
        ratingControl.setScaleY(0.5);
        ratingControl.setColors(Color.web("#4CAF50"), Color.web("#dadce0"));
        if (ratingStarsContainer != null) {
            ratingStarsContainer.getChildren().add(ratingControl);
        }
    }

    /**
     * Popola la scheda con i dati della recensione.
     *
     * @param recensione recensione da visualizzare; se {@code null}, la scheda
     *                   non viene modificata
     */
    public void setRecensione(RecensioneDTO recensione) {
        if (recensione == null) {
            return;
        }
        this.recensione = recensione;

        setAutore(recensione.nomeAutore());
        setLocalGuide(false);
        boolean mostraRistorante = recensione.nomeRistorante() != null
                && !recensione.nomeRistorante().isBlank();
        if (restaurantLabel != null) {
            restaurantLabel.setText(mostraRistorante ? recensione.nomeRistorante() : "");
            restaurantLabel.setVisible(mostraRistorante);
            restaurantLabel.setManaged(mostraRistorante);
        }

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

    /**
     * Configura le azioni di modifica ed eliminazione della recensione.
     * <p>
     * Mostra i relativi pulsanti e nasconde quello per rispondere.
     * </p>
     *
     * @param onModificaAction azione da invocare per la modifica
     * @param onEliminaAction azione da invocare per l'eliminazione
     */
    public void setAzioniProprie(
            Consumer<RecensioneDTO> onModificaAction,
            Consumer<RecensioneDTO> onEliminaAction
    ) {
        this.onModificaAction = onModificaAction;
        this.onEliminaAction = onEliminaAction;
        if (editButton != null) {
            editButton.setVisible(true);
            editButton.setManaged(true);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
        if (replyButton != null) {
            replyButton.setVisible(false);
            replyButton.setManaged(false);
        }
    }

    /**
     * Imposta il nome dell'autore e l'iniziale mostrata nell'avatar.
     *
     * @param nome nome dell'autore; se vuoto o {@code null}, viene mostrato
     *             il nome generico "Utente"
     */
    public void setAutore(String nome) {
        if (nome == null || nome.isBlank()) {
            if (authorLabel != null) authorLabel.setText("Utente");
            if (avatarInitial != null) avatarInitial.setText("U");
            return;
        }

        nome = nome.trim();
        if (authorLabel != null) authorLabel.setText(nome);
        if (avatarInitial != null) avatarInitial.setText(String.valueOf(nome.charAt(0)).toUpperCase());
    }

    /**
     * Imposta la visibilità dell'indicatore di guida locale.
     *
     * @param isLocalGuide {@code true} per mostrare l'indicatore
     */
    public void setLocalGuide(boolean isLocalGuide) {
        if (localGuideLabel != null) {
            localGuideLabel.setVisible(isLocalGuide);
            localGuideLabel.setManaged(isLocalGuide);
        }
    }

    /**
     * Imposta il testo della data della recensione.
     *
     * @param data data già formattata; se {@code null}, il testo viene svuotato
     */
    public void setData(String data) {
        if (timeLabel != null) timeLabel.setText(data != null ? data : "");
    }

    /**
     * Imposta la valutazione mostrata dalle stelle, limitandola all'intervallo
     * da 0 a 5.
     *
     * @param stelle valutazione da visualizzare
     */
    public void setValutazione(double stelle) {
        stelle = Math.clamp(stelle, 0, 5);
        if (ratingControl != null) ratingControl.setRating(stelle);
    }

    /**
     * Imposta il testo della recensione.
     *
     * @param testo contenuto della recensione; se {@code null}, il testo viene
     *              svuotato
     */
    public void setTesto(String testo) {
        if (reviewTextField != null) reviewTextField.setText(testo != null ? testo : "");
    }

    /**
     * Imposta la risposta del gestore e aggiorna la visibilità della relativa
     * sezione.
     *
     * @param risposta risposta del gestore; se vuota o {@code null}, la sezione
     *                 della risposta viene nascosta
     */
    public void setRisposta(String risposta) {
        rispostaPresente = risposta != null && !risposta.isBlank();

        if (rispostaPresente) {
            if (responseTextField != null) {
                responseTextField.setText(risposta);
                responseTextField.setVisible(true);
                responseTextField.setManaged(true);
            }
            if (likeCountLabel != null) {
                likeCountLabel.setVisible(true);
                likeCountLabel.setManaged(true);
            }
        } else {
            if (responseTextField != null) {
                responseTextField.setVisible(false);
                responseTextField.setManaged(false);
            }
            if (likeCountLabel != null) {
                likeCountLabel.setVisible(false);
                likeCountLabel.setManaged(false);
            }
        }
        aggiornaPulsanteRisposta();
    }

    /**
     * Abilita o disabilita la possibilità di rispondere alla recensione.
     *
     * @param abilitata {@code true} se il gestore può rispondere
     */
    public void setRispostaAbilitata(boolean abilitata) {
        rispostaAbilitata = abilitata;
        aggiornaPulsanteRisposta();
    }

    /**
     * Aggiorna la visibilità e lo stato del pulsante di risposta in base alle
     * autorizzazioni e alla presenza di una risposta.
     */
    private void aggiornaPulsanteRisposta() {
        boolean visibile = rispostaAbilitata && !rispostaPresente;
        if (replyButton != null) {
            replyButton.setVisible(visibile);
            replyButton.setManaged(visibile);
            replyButton.setDisable(!visibile);
        }
    }

    /**
     * Apre il modulo per rispondere alla recensione, se l'azione è consentita.
     *
     * @param actionEvent evento JavaFX usato per cambiare scena
     */
    @FXML
    public void onRispondiClicked(ActionEvent actionEvent) {
        if (rispostaAbilitata && SessioneUtente.isGestore()
                && !rispostaPresente && recensione != null
                && recensione.idRecensione() != null) {
            RecensioneDaRispondere.imposta(recensione);
            SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/RispondiRecensione.fxml");
        }
    }

    /**
     * Invoca l'azione di modifica configurata, se recensione e callback sono
     * disponibili.
     */
    @FXML
    private void onModificaClicked() {
        if (recensione != null && onModificaAction != null) {
            onModificaAction.accept(recensione);
        }
    }

    /**
     * Invoca l'azione di eliminazione configurata, se recensione e callback
     * sono disponibili.
     */
    @FXML
    private void onEliminaClicked() {
        if (recensione != null && onEliminaAction != null) {
            onEliminaAction.accept(recensione);
        }
    }
}