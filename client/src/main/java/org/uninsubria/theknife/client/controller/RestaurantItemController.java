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
import org.uninsubria.theknife.client.util.RistoranteSelezionato;
import org.uninsubria.theknife.client.util.SceneManager;
import org.uninsubria.theknife.client.util.CuisineTags;
import org.uninsubria.theknife.common.dto.RistoranteDTO;

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
    @FXML private FlowPane cuisineTags;
    @FXML private Label deliveryTag;
    @FXML private Label bookingTag;
    @FXML private Button detailsButton;
    @FXML private Button editButton;

    private RistoranteDTO ristorante;
    private Consumer<RistoranteDTO> onDettagliAction;
    private Consumer<RistoranteDTO> onModificaAction;

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
        System.out.println(ristorante.toString());
        nameLabel.setText(ristorante.nome());
        addressLabel.setText(ristorante.indirizzo());
        ratingValueLabel.setText(formattaRating(ristorante.mediaStelle()));
        CuisineTags.populate(cuisineTags, ristorante.tipologieCucina());
        metaLabel.setText(formattaMeta(ristorante.prezzoMedio()));

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

    public void setOnModificaAction(Consumer<RistoranteDTO> onModificaAction) {
        this.onModificaAction = onModificaAction;
        editButton.setVisible(onModificaAction != null);
        editButton.setManaged(onModificaAction != null);
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
        RistoranteSelezionato.setRistorante(ristorante);
        SceneManager.switchScene(actionEvent, "/org/uninsubria/theknife/client/views/Ristorante.fxml");
    }

    @FXML
    private void onEditClicked() {
        if (ristorante != null && onModificaAction != null) {
            onModificaAction.accept(ristorante);
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
     * @param prezzoMedio prezzo medio del ristorante
     * @return stringa contenente tipologia di cucina e prezzo medio
     */
    private String formattaMeta(Double prezzoMedio) {
        String prezzo = (prezzoMedio != null) ? String.format("%.0f €", prezzoMedio) : "n.d.";
        return "Prezzo medio " + prezzo;
    }
}