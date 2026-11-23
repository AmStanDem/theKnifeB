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

import java.util.function.Consumer;

/**
 * Controller JavaFX della card di un ristorante presente nei preferiti.
 * <p>
 * Visualizza nome, indirizzo, descrizione, valutazione, tipologie di cucina,
 * prezzo medio e disponibilità dei servizi. Espone inoltre le azioni per
 * aprire i dettagli e rimuovere il ristorante dai preferiti.
 * </p>
 */
public class PreferitoItemController {

    @FXML private Label nameLabel;
    @FXML private Label ratingValueLabel;
    @FXML private Label addressLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label metaLabel;
    @FXML private FlowPane cuisineTags;
    @FXML private Label deliveryTag;
    @FXML private Label bookingTag;
    @FXML private Button detailsButton;

    private RistoranteDTO ristorante;
    private Consumer<RistoranteDTO> onRemoveFavoriteAction;

    /**
     * Popola la card con i dati del ristorante preferito.
     *
     * @param ristorante ristorante da visualizzare
     */
    public void setRistorante(RistoranteDTO ristorante) {
        this.ristorante = ristorante;

        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        descriptionLabel.setText(ristorante.descrizione() == null || ristorante.descrizione().isBlank()
                ? "Descrizione non disponibile."
                : ristorante.descrizione().trim());
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        CuisineTags.populate(cuisineTags, ristorante.tipologieCucina());
        metaLabel.setText(formattaMeta(ristorante.prezzoMedio(), ristorante.prezzoStimato()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    /**
     * Imposta l'azione da eseguire per rimuovere il ristorante dai preferiti.
     *
     * @param onRemoveFavoriteAction azione chiamata con il ristorante da rimuovere
     */
    public void setOnRemoveFavoriteAction(Consumer<RistoranteDTO> onRemoveFavoriteAction) {
        this.onRemoveFavoriteAction = onRemoveFavoriteAction;
    }

    /**
     * Gestisce il click sul pulsante dei dettagli.
     * <p>
     * Memorizza il ristorante selezionato e apre la vista del ristorante.
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
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
    }

    /**
     * Mostra o nasconde un tag relativo ai servizi disponibili.
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
     * Formatta il prezzo medio e il relativo indicatore di stima.
     *
     * @param prezzoMedio prezzo medio del ristorante
     * @param prezzoStimato indica se il prezzo è stimato
     * @return testo descrittivo del prezzo medio
     */
    private String formattaMeta(Double prezzoMedio, boolean prezzoStimato) {
        String prezzo = (prezzoMedio != null)
                ? (prezzoStimato ? "≈ " : "") + String.format("%.0f €", prezzoMedio)
                : "n.d.";
        return "Prezzo medio " + prezzo;
    }

    /**
     * Gestisce il click sul pulsante di rimozione dai preferiti.
     * <p>
     * Richiama l'azione configurata dal controller padre passando il ristorante
     * corrente.
     * </p>
     */
    @FXML
    private void onRemoveFavoriteClicked() {
        if (onRemoveFavoriteAction != null && ristorante != null) {
            onRemoveFavoriteAction.accept(ristorante);
        }
    }
}