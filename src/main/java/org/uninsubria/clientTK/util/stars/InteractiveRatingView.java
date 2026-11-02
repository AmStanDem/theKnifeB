package org.uninsubria.clientTK.util.stars;

import javafx.scene.shape.SVGPath;

/**
 * Versione base della vista interattiva per il rendering e l'evento di click/hover.
 */
public class InteractiveRatingView extends BaseRatingView {

    private boolean interactive;
    private Runnable onRatingChanged;

    public InteractiveRatingView(int maxStarsIgnored, double initialRating, boolean interactive) {
        super(maxStarsIgnored, initialRating);
        this.interactive = interactive;
        drawStars(rating);
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        drawStars(rating);
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(maxStars, rating));
        drawStars(this.rating);
    }

    public void setOnRatingChanged(Runnable onRatingChanged) {
        this.onRatingChanged = onRatingChanged;
    }

    @Override
    protected void drawStars(double targetRating) {
        getChildren().clear();

        for (int i = 1; i <= maxStars; i++) {
            SVGPath star = new SVGPath();
            star.setContent(STAR_SVG);
            star.setScaleX(1.2);
            star.setScaleY(1.2);

            if (i <= targetRating) {
                star.setFill(filledColor);
            } else if (i - 0.5 == targetRating) {
                star.setFill(createHalfGradient());
            } else {
                star.setFill(emptyColor);
            }

            final int starIndex = i;

            if (interactive) {
                star.setOnMouseMoved(e -> {
                    double mouseXInStar = e.getX();
                    double starWidth = star.getLayoutBounds().getWidth();
                    double currentHover = (mouseXInStar < starWidth / 2.0) ? (starIndex - 0.5) : starIndex;
                    drawStars(currentHover);
                });
            } else {
                star.setOnMouseMoved(null);
            }

            star.setOnMouseClicked(e -> {
                if (!interactive) return;

                double mouseXInStar = e.getX();
                double starWidth = star.getLayoutBounds().getWidth();
                rating = (mouseXInStar < starWidth / 2.0) ? (starIndex - 0.5) : starIndex;
                drawStars(rating);

                if (onRatingChanged != null) {
                    onRatingChanged.run();
                }
            });

            getChildren().add(star);
        }

        if (interactive) {
            setOnMouseExited(e -> drawStars(rating));
        } else {
            setOnMouseExited(null);
        }
    }
}