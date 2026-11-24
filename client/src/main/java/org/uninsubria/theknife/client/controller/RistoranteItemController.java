/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import org.uninsubria.theknife.client.util.CuisineTags;
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.common.dto.RistoranteDTO;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Controller JavaFX della card che rappresenta un ristorante in una lista.
 * <p>
 * Visualizza i dati essenziali del ristorante, inclusi nome, indirizzo,
 * valutazione, cucina, prezzo medio e servizi disponibili. Espone callback
 * configurabili per le azioni di apertura dei dettagli, modifica e rimozione
 * dai preferiti.
 * </p>
 */
public class RistoranteItemController {

    @FXML private Label nameLabel;
    @FXML private Label ratingValueLabel;
    @FXML private Label addressLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label metaLabel;
    @FXML private FlowPane cuisineTags;
    @FXML private Label deliveryTag;
    @FXML private Label bookingTag;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button removeButton;

    private RistoranteDTO ristorante;
    private Consumer<RistoranteDTO> onDettagliAction;
    private BiConsumer<ActionEvent, RistoranteDTO> onModificaAction;
    private Consumer<RistoranteDTO> onRemoveFavoriteAction;

    /**
     * Popola la card con i dati del ristorante.
     *
     * @param ristorante ristorante da visualizzare
     */
    public void setRistorante(RistoranteDTO ristorante) {
        this.ristorante = ristorante;
        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        if (descriptionLabel != null) {
            descriptionLabel.setText(ristorante.descrizione() == null || ristorante.descrizione().isBlank()
                    ? "Descrizione non disponibile."
                    : ristorante.descrizione().trim());
        }
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        CuisineTags.populate(cuisineTags, ristorante.tipologieCucina());
        metaLabel.setText(formattaMeta(ristorante.prezzoMedio(), ristorante.prezzoStimato()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    /**
     * Imposta l'azione da eseguire quando viene richiesto di aprire i dettagli.
     *
     * @param onDettagliAction callback invocata con il ristorante selezionato
     */
    public void setOnDettagliAction(Consumer<RistoranteDTO> onDettagliAction) {
        this.onDettagliAction = onDettagliAction;
    }

    /**
     * Imposta l'azione di modifica e aggiorna la visibilità del relativo pulsante.
     *
     * @param onModificaAction callback che riceve l'evento e il ristorante;
     *                         se {@code null}, il pulsante viene nascosto
     */
    public void setOnModificaAction(BiConsumer<ActionEvent, RistoranteDTO> onModificaAction) {
        this.onModificaAction = onModificaAction;
        editButton.setVisible(onModificaAction != null);
        editButton.setManaged(onModificaAction != null);
    }

    /**
     * Imposta l'azione di rimozione dai preferiti e aggiorna la visibilità
     * del relativo pulsante.
     *
     * @param onRemoveFavoriteAction callback invocata con il ristorante da rimuovere;
     *                               se {@code null}, il pulsante viene nascosto
     */
    public void setOnRemoveFavoriteAction(Consumer<RistoranteDTO> onRemoveFavoriteAction) {
        this.onRemoveFavoriteAction = onRemoveFavoriteAction;
        removeButton.setVisible(onRemoveFavoriteAction != null);
        removeButton.setManaged(onRemoveFavoriteAction != null);
    }

    /**
     * Gestisce il click sul pulsante dei dettagli.
     * <p>
     * Memorizza il ristorante selezionato e invoca l'eventuale callback
     * configurata.
     * </p>
     *
     * @param actionEvent evento JavaFX usato per cambiare scena
     */
    @FXML
    private void onDetailsClicked(ActionEvent actionEvent) {
        if (ristorante == null) {
            return;
        }
        RistoranteSelezionato.setRistorante(ristorante);
        if (onDettagliAction != null) {
            onDettagliAction.accept(ristorante);
        }
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
    }

    /**
     * Gestisce il click sul pulsante di modifica.
     *
     * @param actionEvent evento generato dal click sul pulsante
     */
    @FXML
    private void onEditClicked(ActionEvent actionEvent) {
        if (ristorante != null && onModificaAction != null) {
            onModificaAction.accept(actionEvent, ristorante);
        }
    }

    /**
     * Gestisce il click sul pulsante di rimozione dai preferiti.
     */
    @FXML
    private void onRemoveClicked() {
        if (onRemoveFavoriteAction != null && ristorante != null) {
            onRemoveFavoriteAction.accept(ristorante);
        }
    }

    /**
     * Mostra o nasconde un tag relativo ai servizi del ristorante.
     *
     * @param tagLabel etichetta da aggiornare
     * @param visibile {@code true} per mostrare il tag
     */
    private void aggiornaTag(Label tagLabel, boolean visibile) {
        tagLabel.setVisible(visibile);
        tagLabel.setManaged(visibile);
    }

    /**
     * Formatta la valutazione media del ristorante.
     *
     * @param mediaStelle valutazione media, oppure {@code null} se non disponibile
     * @return valutazione con una cifra decimale o {@code "N/D"}
     */
    private String formattaRating(Double mediaStelle) {
        if (mediaStelle == null) {
            return "N/D";
        }
        return String.format("%.1f", mediaStelle);
    }

    /**
     * Formatta il prezzo medio e l'eventuale indicazione che sia stimato.
     *
     * @param prezzoMedio prezzo medio del ristorante
     * @param prezzoStimato indica se il prezzo è una stima
     * @return testo descrittivo del prezzo medio
     */
    private String formattaMeta(Double prezzoMedio, boolean prezzoStimato) {
        String prezzo = (prezzoMedio != null)
                ? (prezzoStimato ? "≈ " : "") + String.format("%.0f €", prezzoMedio)
                : "n.d.";
        return "Prezzo medio " + prezzo;
    }
}