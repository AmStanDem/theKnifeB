/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.stars;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.SVGPath;

/**
 * Vista interattiva per visualizzare e selezionare una valutazione a stelle.
 * Il puntamento mostra un'anteprima, mentre il clic sinistro conferma la valutazione.
 */
public class InteractiveRatingView extends BaseRatingView {

    private boolean interactive;
    private Runnable onRatingChanged;

    /**
     * Crea una vista di valutazione configurabile come interattiva o di sola lettura.
     *
     * @param maxStarsIgnored parametro non utilizzato; la vista base mostra sempre cinque stelle
     * @param initialRating valutazione iniziale, limitata all'intervallo da 0 a 5
     * @param interactive {@code true} per consentire la selezione della valutazione
     */
    public InteractiveRatingView(int maxStarsIgnored, double initialRating, boolean interactive) {
        super(initialRating);
        this.interactive = interactive;

        for (int i = 0; i < stars.size(); i++) {
            final int starIndex = i + 1;
            final SVGPath star = stars.get(i);

            // Hover: solo anteprima, NON modifica "rating"
            star.setOnMouseMoved(e -> {
                if (!eInterattivo()) return;
                disegnaStelle(valoreIn(star, starIndex, e.getX()));
            });

            // Click sinistro: conferma il voto
            star.setOnMouseClicked(e -> {
                if (!eInterattivo() || e.getButton() != MouseButton.PRIMARY) return;
                rating = valoreIn(star, starIndex, e.getX());
                disegnaStelle(rating);
                if (onRatingChanged != null) {
                    onRatingChanged.run();
                }
            });
        }

        // Uscendo dal componente si torna al voto confermato
        setOnMouseExited(e -> disegnaStelle(rating));

        aggiornaCursore();
        disegnaStelle(rating);
    }

    /** Mezza stella se il mouse è nella metà sinistra, stella intera altrimenti. */
    private double valoreIn(SVGPath star, int starIndex, double mouseX) {
        double starWidth = star.getLayoutBounds().getWidth();
        return (mouseX < starWidth / 2.0) ? (starIndex - 0.5) : starIndex;
    }

    private void aggiornaCursore() {
        setCursor(interactive ? Cursor.HAND : Cursor.DEFAULT);
    }

    /**
     * Imposta se la valutazione può essere modificata dall'utente.
     *
     * @param interactive {@code true} per abilitare l'interazione
     */
    public void impostaInterattivo(boolean interactive) {
        this.interactive = interactive;
        aggiornaCursore();
        disegnaStelle(rating);
    }

    /**
     * Indica se la selezione della valutazione è abilitata.
     *
     * @return {@code true} se la vista è interattiva
     */
    public boolean eInterattivo() {
        return interactive;
    }

    /**
     * Imposta l'azione da eseguire quando l'utente conferma una nuova valutazione.
     *
     * @param onRatingChanged azione da eseguire, oppure {@code null} per non eseguirne alcuna
     */
    public void impostaSuValutazioneCambiata(Runnable onRatingChanged) {
        this.onRatingChanged = onRatingChanged;
    }
}