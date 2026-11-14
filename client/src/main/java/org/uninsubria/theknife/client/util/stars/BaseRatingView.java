/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.stars;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclasse base per il rendering delle stelle di valutazione.
 * Le stelle vengono create UNA SOLA VOLTA; per aggiornare la grafica
 * si cambia soltanto il riempimento (paintStars), senza ricreare i nodi.
 */
public class BaseRatingView extends HBox {

    protected static final String STAR_SVG = "M12 .587l3.668 7.431 8.2 1.192-5.934 5.787 1.399 8.168-7.333-3.854-7.333 3.854 1.399-8.168-5.934-5.787 8.2-1.192z";

    protected final int maxStars = 5;
    protected double rating = 0.0;

    protected Color filledColor = Color.web("#66CC33");
    protected Color emptyColor = Color.web("#334d40");

    protected final List<SVGPath> stars = new ArrayList<>();

    public BaseRatingView(int maxStarsIgnored, double initialRating) {
        this.rating = initialRating;
        setSpacing(6);
        setPadding(new Insets(5));
        setStyle("-fx-background-color: transparent;");
        buildStars();
        paintStars(rating);
    }

    private void buildStars() {
        for (int i = 0; i < maxStars; i++) {
            SVGPath star = new SVGPath();
            star.setContent(STAR_SVG);
            star.setScaleX(1.2);
            star.setScaleY(1.2);
            stars.add(star);
            getChildren().add(star);
        }
    }

    /** Aggiorna solo il colore delle stelle esistenti. */
    protected void paintStars(double targetRating) {
        for (int i = 1; i <= maxStars; i++) {
            SVGPath star = stars.get(i - 1);
            if (i <= targetRating) {
                star.setFill(filledColor);
            } else if (i - 1 < targetRating) {
                star.setFill(createPartialGradient(targetRating - (i - 1)));
            } else {
                star.setFill(emptyColor);
            }
        }
    }

    protected LinearGradient createPartialGradient(double filledFraction) {
        return new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, filledColor),
                new Stop(filledFraction, filledColor),
                new Stop(Math.min(1.0, filledFraction + 0.001), emptyColor),
                new Stop(1.0, emptyColor)
        );
    }

    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(maxStars, rating));
        paintStars(this.rating);
    }

    public void setColors(Color filled, Color empty) {
        this.filledColor = filled;
        this.emptyColor = empty;
        paintStars(rating);
    }

    public double getRating() {
        return rating;
    }
}