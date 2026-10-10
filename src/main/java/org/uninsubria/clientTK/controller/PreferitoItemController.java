package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.common.dto.RistoranteDTO;

import java.util.function.Consumer;

public class PreferitoItemController {

    @FXML private Label nameLabel;
    @FXML private Label ratingValueLabel;
    @FXML private Label addressLabel;
    @FXML private Label metaLabel;
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
        metaLabel.setText(formattaMeta(String.valueOf(ristorante.tipologieCucina()), ristorante.prezzoMedio()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    public void setOnDettagliAction(Consumer<RistoranteDTO> onDettagliAction) {
        this.onDettagliAction = onDettagliAction;
    }

    @FXML
    private void onDetailsClicked(ActionEvent actionEvent) {
        SceneManager.switchScene(actionEvent, "/org/uninsubria/clientTK/views/Ristorante.fxml");
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

    private String formattaMeta(String tipoCucina, Double prezzoMedio) {
        String cucina = (tipoCucina != null && !tipoCucina.isBlank()) ? tipoCucina : "Cucina non specificata";
        String prezzo = (prezzoMedio != null) ? String.format("%.0f €", prezzoMedio) : "n.d.";
        return cucina + " • Prezzo medio " + prezzo;
    }

    @FXML
    private void onRemoveFavoriteClicked() {
        if (onRemoveFavoriteAction != null && ristorante != null) {
            onRemoveFavoriteAction.accept(ristorante);
        }
    }
}