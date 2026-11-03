package org.uninsubria.clientTK.util.stars;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.SVGPath;

/**
 * Vista interattiva: l'hover mostra solo un'anteprima,
 * il click (tasto sinistro) fissa il voto in modo definitivo.
 */
public class InteractiveRatingView extends BaseRatingView {

    private boolean interactive;
    private Runnable onRatingChanged;

    public InteractiveRatingView(int maxStarsIgnored, double initialRating, boolean interactive) {
        super(maxStarsIgnored, initialRating);
        this.interactive = interactive;

        for (int i = 0; i < stars.size(); i++) {
            final int starIndex = i + 1;
            final SVGPath star = stars.get(i);

            // Hover: solo anteprima, NON modifica "rating"
            star.setOnMouseMoved(e -> {
                if (!isInteractive()) return;
                paintStars(valueAt(star, starIndex, e.getX()));
            });

            // Click sinistro: conferma il voto
            star.setOnMouseClicked(e -> {
                if (!isInteractive() || e.getButton() != MouseButton.PRIMARY) return;
                rating = valueAt(star, starIndex, e.getX());
                paintStars(rating);
                if (onRatingChanged != null) {
                    onRatingChanged.run();
                }
            });
        }

        // Uscendo dal componente si torna al voto confermato
        setOnMouseExited(e -> paintStars(rating));

        updateCursor();
        paintStars(rating);
    }

    /** Mezza stella se il mouse è nella metà sinistra, stella intera altrimenti. */
    private double valueAt(SVGPath star, int starIndex, double mouseX) {
        double starWidth = star.getLayoutBounds().getWidth();
        return (mouseX < starWidth / 2.0) ? (starIndex - 0.5) : starIndex;
    }

    private void updateCursor() {
        setCursor(interactive ? Cursor.HAND : Cursor.DEFAULT);
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        updateCursor();
        paintStars(rating);
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(maxStars, rating));
        paintStars(this.rating);
    }

    public void setOnRatingChanged(Runnable onRatingChanged) {
        this.onRatingChanged = onRatingChanged;
    }
}