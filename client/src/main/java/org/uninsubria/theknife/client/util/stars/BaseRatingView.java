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
 * Vista base JavaFX per visualizzare una valutazione tramite cinque stelle.
 * Le stelle vengono create una sola volta e aggiornate cambiandone il riempimento.
 */
public class BaseRatingView extends HBox {

    protected static final String STAR_SVG = "M12 .587l3.668 7.431 8.2 1.192-5.934 5.787 1.399 8.168-7.333-3.854-7.333 3.854 1.399-8.168-5.934-5.787 8.2-1.192z";

    protected final int maxStars = 5;
    protected double rating = 0.0;

    protected Color filledColor = Color.web("#66CC33");
    protected Color emptyColor = Color.web("#334d40");

    protected final List<SVGPath> stars = new ArrayList<>();

    /**
     * Crea la vista delle stelle con la valutazione iniziale specificata.
     *
     * @param initialRating valutazione iniziale, limitata all'intervallo da 0 a 5
     */
    public BaseRatingView(double initialRating) {
        this.rating = initialRating;
        setSpacing(6);
        setPadding(new Insets(5));
        setStyle("-fx-background-color: transparent;");
        creaStelle();
        disegnaStelle(rating);
    }

    private void creaStelle() {
        for (int i = 0; i < maxStars; i++) {
            SVGPath star = new SVGPath();
            star.setContent(STAR_SVG);
            star.setScaleX(1.2);
            star.setScaleY(1.2);
            stars.add(star);
            getChildren().add(star);
        }
    }

    /**
     * Aggiorna il riempimento delle stelle esistenti in base alla valutazione.
     *
     * @param targetRating valutazione da rappresentare
     */
    protected void disegnaStelle(double targetRating) {
        for (int i = 1; i <= maxStars; i++) {
            SVGPath star = stars.get(i - 1);
            if (i <= targetRating) {
                star.setFill(filledColor);
            } else if (i - 1 < targetRating) {
                star.setFill(creaGradienteParziale(targetRating - (i - 1)));
            } else {
                star.setFill(emptyColor);
            }
        }
    }

    /**
     * Crea un gradiente per rappresentare una stella parzialmente riempita.
     *
     * @param filledFraction frazione della stella da riempire
     * @return gradiente che combina il colore pieno e quello vuoto
     */
    protected LinearGradient creaGradienteParziale(double filledFraction) {
        return new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, filledColor),
                new Stop(filledFraction, filledColor),
                new Stop(Math.min(1.0, filledFraction + 0.001), emptyColor),
                new Stop(1.0, emptyColor)
        );
    }

    /**
     * Imposta la valutazione visualizzata.
     *
     * @param rating valutazione da visualizzare, limitata all'intervallo da 0 a 5
     */
    public void impostaValutazione(double rating) {
        this.rating = Math.clamp(rating, 0.0, maxStars);
        disegnaStelle(this.rating);
    }

    /**
     * Imposta i colori usati per le porzioni piene e vuote delle stelle.
     *
     * @param filled colore delle porzioni piene
     * @param empty colore delle porzioni vuote
     */
    public void impostaColori(Color filled, Color empty) {
        this.filledColor = filled;
        this.emptyColor = empty;
        disegnaStelle(rating);
    }

    /**
     * Restituisce la valutazione attualmente visualizzata.
     *
     * @return valutazione compresa tra 0 e 5
     */
    public double ottieniValutazione() {
        return rating;
    }
}