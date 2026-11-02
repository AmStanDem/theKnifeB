package org.uninsubria.clientTK.util.stars;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Container grafico che racchiude l'InteractiveRatingView,
 * mantenendo le stelle sempre interattive e modificabili.
 */
public class RatingContainer extends HBox {

    private final InteractiveRatingView ratingView;

    /**
     * Costruttore vuoto obbligatorio per l'istanziazione diretta tramite FXML.
     */
    public RatingContainer() {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        // Inizializza le stelle interattive sempre attive (true)
        ratingView = new InteractiveRatingView(5, 0.0, true);

        getChildren().add(ratingView);
    }

    public RatingContainer(double initialRating, boolean initialLocked) {
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");

        // Lascia l'interattività attiva basandosi su initialLocked invertito
        ratingView = new InteractiveRatingView(5, initialRating, !initialLocked);

        getChildren().add(ratingView);
    }

    public void setColors(Color filled, Color empty) {
        ratingView.setColors(filled, empty);
    }

    public double getRating() {
        return ratingView.getRating();
    }

    public void setRating(double rating) {
        // Imposta il voto senza bloccare l'interattività, così l'utente può sempre modificarlo
        ratingView.setRating(rating);
        ratingView.setInteractive(true);
    }

    public void setInteractive(boolean interactive) {
        ratingView.setInteractive(interactive);
    }

    public void setOnRatingChanged(Runnable onRatingChanged) {
        ratingView.setOnRatingChanged(onRatingChanged);
    }
}