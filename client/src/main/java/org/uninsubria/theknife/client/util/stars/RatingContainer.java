/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.stars;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Contenitore grafico per una vista di valutazione a stelle interattiva.
 */
public class RatingContainer extends HBox {

    private final InteractiveRatingView ratingView;

    /**
     * Crea un contenitore con valutazione iniziale pari a zero e selezione abilitata.
     */
    public RatingContainer() {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        // Inizializza le stelle interattive sempre attive (true)
        ratingView = new InteractiveRatingView(5, 0.0, true);

        getChildren().add(ratingView);
    }

    /**
     * Crea un contenitore con valutazione e stato di interazione iniziali specificati.
     *
     * @param initialRating valutazione iniziale, limitata all'intervallo da 0 a 5
     * @param initialLocked {@code true} per disabilitare inizialmente la modifica della valutazione
     */
    public RatingContainer(double initialRating, boolean initialLocked) {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        // Lascia l'interattività attiva basandosi su initialLocked invertito
        ratingView = new InteractiveRatingView(5, initialRating, !initialLocked);

        getChildren().add(ratingView);
    }

    /**
     * Imposta i colori usati per le porzioni piene e vuote delle stelle.
     *
     * @param filled colore delle porzioni piene
     * @param empty colore delle porzioni vuote
     */
    public void impostaColori(Color filled, Color empty) {
        ratingView.impostaColori(filled, empty);
    }

    /**
     * Restituisce la valutazione corrente.
     *
     * @return valutazione compresa tra 0 e 5
     */
    public double ottieniValutazione() {
        return ratingView.ottieniValutazione();
    }

    /**
     * Imposta la valutazione e abilita la modifica da parte dell'utente.
     *
     * @param rating valutazione da visualizzare, limitata all'intervallo da 0 a 5
     */
    public void impostaValutazione(double rating) {
        // Imposta il voto senza bloccare l'interattività, così l'utente può sempre modificarlo
        ratingView.impostaValutazione(rating);
        ratingView.impostaInterattivo(true);
    }

    /**
     * Abilita o disabilita la modifica della valutazione.
     *
     * @param interactive {@code true} per consentire la selezione
     */
    public void impostaInterattivo(boolean interactive) {
        ratingView.impostaInterattivo(interactive);
    }

    /**
     * Imposta l'azione da eseguire quando l'utente conferma una nuova valutazione.
     *
     * @param onRatingChanged azione da eseguire, oppure {@code null} per non eseguirne alcuna
     */
    public void impostaSuValutazioneCambiata(Runnable onRatingChanged) {
        ratingView.impostaSuValutazioneCambiata(onRatingChanged);
    }
}