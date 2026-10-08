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

/**
 * Controller JavaFX del componente riutilizzabile che rappresenta
 * un singolo ristorante all'interno della lista dei risultati.
 * <p>
 * Il controller gestisce la visualizzazione dei principali dati del
 * ristorante, tra cui nome, indirizzo, valutazione, tipologia di cucina,
 * prezzo medio e disponibilità dei servizi di delivery e prenotazione online.
 * </p>
 *
 * <p>
 * I dati vengono forniti tramite un oggetto {@link RistoranteDTO},
 * che viene utilizzato per popolare la card visualizzata nella relativa
 * vista FXML.
 * </p>
 */
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

    /**
     * Popola la card con i dati del ristorante passato.
     * <p>
     * Aggiorna il nome, l'indirizzo, la valutazione, le informazioni
     * relative alla cucina e al prezzo medio e la visibilità dei tag
     * relativi ai servizi disponibili.
     * </p>
     *
     * @param ristorante ristorante di cui visualizzare i dati
     */
    public void setRistorante(RistoranteDTO ristorante) {
        this.ristorante = ristorante;

        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        metaLabel.setText(formattaMeta(ristorante.tipoCucina(), ristorante.prezzoMedio()));

        aggiornaTag(deliveryTag, Boolean.TRUE.equals(ristorante.delivery()));
        aggiornaTag(bookingTag, Boolean.TRUE.equals(ristorante.bookingOnline()));
    }

    /**
     * Imposta l'azione da eseguire quando l'utente seleziona i dettagli
     * del ristorante.
     * <p>
     * Permette al controller della lista dei ristoranti di definire
     * il comportamento associato alla selezione della card.
     * </p>
     *
     * @param onDettagliAction azione da eseguire passando il ristorante
     *                         selezionato
     */
    public void setOnDettagliAction(Consumer<RistoranteDTO> onDettagliAction) {
        this.onDettagliAction = onDettagliAction;
    }

    @FXML
    /**
     * Gestisce il click sul pulsante "Dettagli".
     * <p>
     * Recupera la finestra attualmente visualizzata e sostituisce
     * la scena corrente caricando la vista {@code Ristorante.fxml}.
     * </p>
     *
     * @param actionEvent evento generato dal click dell'utente
     */
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

    /**
     * Mostra o nasconde un tag relativo ai servizi disponibili.
     * <p>
     * Oltre alla visibilità del componente, viene aggiornata anche
     * la proprietà {@code managed} per riservare o liberare lo spazio
     * occupato dal tag all'interno del layout.
     * </p>
     *
     * @param tagLabel etichetta da aggiornare
     * @param visibile indica se il tag deve essere visualizzato
     */
    private void aggiornaTag(Label tagLabel, boolean visibile) {
        tagLabel.setVisible(visibile);
        tagLabel.setManaged(visibile);
    }

    /**
     * Formatta la valutazione media del ristorante.
     * <p>
     * Se la valutazione non è disponibile, viene restituito {@code "N/D"}.
     * </p>
     *
     * @param mediaStelle valutazione media del ristorante
     * @return valutazione formattata con una cifra decimale
     */
    private String formattaRating(Double mediaStelle) {
        if (mediaStelle == null) {
            return "N/D";
        }
        return String.format("%.1f", mediaStelle);
    }

    /**
     * Formatta le informazioni relative alla cucina e al prezzo medio.
     * <p>
     * Se la tipologia di cucina o il prezzo medio non sono disponibili,
     * viene utilizzato un valore indicativo.
     * </p>
     *
     * @param tipoCucina tipologia di cucina del ristorante
     * @param prezzoMedio prezzo medio del ristorante
     * @return stringa contenente tipologia di cucina e prezzo medio
     */
    private String formattaMeta(String tipoCucina, Double prezzoMedio) {
        String cucina = (tipoCucina != null && !tipoCucina.isBlank()) ? tipoCucina : "Cucina non specificata";
        String prezzo = (prezzoMedio != null) ? String.format("%.0f €", prezzoMedio) : "n.d.";
        return cucina + " • Prezzo medio " + prezzo;
    }
}