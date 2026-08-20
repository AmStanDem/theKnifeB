package org.uninsubria.clientTK.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.uninsubria.common.dto.RistoranteDTO;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.function.Consumer;

public class RestaurantItemController {

    @FXML private Label nameLabel;
    @FXML private Label ratingValueLabel;
    @FXML private Label addressLabel;
    @FXML private Label metaLabel;
    @FXML private Label deliveryTag;
    @FXML private Label bookingTag;
    @FXML private Button detailsButton;

    private RistoranteDTO ristorante;
    private Consumer<RistoranteDTO> onDettagliAction;

    /** Popola la card con i dati del ristorante passato. */
    public void setRistorante(RistoranteDTO ristorante) {
        this.ristorante = ristorante;

        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        metaLabel.setText(formattaMeta(ristorante.tipoCucina(), ristorante.prezzoMedio()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    /** Permette al chiamante (controller lista) di reagire al click su "Dettagli". */
    public void setOnDettagliAction(Consumer<RistoranteDTO> onDettagliAction) {
        this.onDettagliAction = onDettagliAction;
    }

    @FXML
    private void onDetailsClicked(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) ((Node) actionEvent.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(
                    FXMLLoader.load(
                            getClass().getResource("/org/uninsubria/clientTK/views/Ristorante.fxml")
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Mostra o nasconde il tag, riservando/liberando lo spazio nel layout. */
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
}