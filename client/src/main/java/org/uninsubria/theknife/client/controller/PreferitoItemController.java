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

public class PreferitoItemController {

    @FXML private Label nameLabel;
    @FXML private Label ratingValueLabel;
    @FXML private Label addressLabel;
    @FXML private Label metaLabel;
    @FXML private FlowPane cuisineTags;
    @FXML private Label deliveryTag;
    @FXML private Label bookingTag;
    @FXML private Button detailsButton;

    private RistoranteDTO ristorante;
    private Consumer<RistoranteDTO> onDettagliAction;
    private Consumer<RistoranteDTO> onRemoveFavoriteAction;

    public void setRistorante(RistoranteDTO ristorante) {
        this.ristorante = ristorante;

        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        CuisineTags.populate(cuisineTags, ristorante.tipologieCucina());
        metaLabel.setText(formattaMeta(ristorante.prezzoMedio()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    public void setOnDettagliAction(Consumer<RistoranteDTO> onDettagliAction) {
        this.onDettagliAction = onDettagliAction;
    }

    public void setOnRemoveFavoriteAction(Consumer<RistoranteDTO> onRemoveFavoriteAction) {
        this.onRemoveFavoriteAction = onRemoveFavoriteAction;
    }

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

    private void aggiornaTag(Label tagLabel, boolean visibile) {
        tagLabel.setVisible(visibile);
        tagLabel.setManaged(visibile);
    }

    private String formattaRating(Double mediaStelle) {
        if (mediaStelle == null) {
            return "N/D";
        }
        return String.format("%.1f", mediaStelle);
    }

    private String formattaMeta(Double prezzoMedio) {
        String prezzo = (prezzoMedio != null) ? String.format("%.0f €", prezzoMedio) : "n.d.";
        return "Prezzo medio " + prezzo;
    }

    @FXML
    private void onRemoveFavoriteClicked() {
        if (onRemoveFavoriteAction != null && ristorante != null) {
            onRemoveFavoriteAction.accept(ristorante);
        }
    }
}