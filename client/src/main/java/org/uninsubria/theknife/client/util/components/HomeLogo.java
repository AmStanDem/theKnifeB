/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.uninsubria.theknife.client.util.SceneManager;

/**
 * Pulsante con il logo dell'applicazione che riporta alla schermata principale.
 */
public final class HomeLogo extends Button {

    private static final String HOME_VIEW =
            "/org/uninsubria/theknife/client/views/MainLayout.fxml";
    private static final String DEFAULT_IMAGE =
            "/org/uninsubria/theknife/client/images/img_1.png";

    private final ImageView imageView = new ImageView();

    /**
     * Crea il pulsante del logo e ne configura l'azione di ritorno alla home.
     */
    public HomeLogo() {
        setImage(caricaImmagine(DEFAULT_IMAGE));
        setFitWidth(441);
        setFitHeight(133);
        imageView.setPreserveRatio(true);
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setPadding(Insets.EMPTY);
        setStyle("-fx-background-color: transparent; -fx-background-insets: 0; "
                + "-fx-border-color: transparent; -fx-border-width: 0; -fx-cursor: hand;");
        setAccessibleText("Torna alla home");
        setOnAction(evento -> SceneManager.cambiaScena(evento, HOME_VIEW));
    }

    /**
     * Restituisce l'immagine visualizzata nel logo.
     *
     * @return immagine del logo
     */
    public Image getImage() {
        return imageView.getImage();
    }

    /**
     * Imposta l'immagine visualizzata nel logo.
     *
     * @param image immagine da visualizzare
     * @throws IllegalArgumentException se l'immagine è {@code null}
     */
    public void setImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("L'immagine del logo non può essere nulla.");
        }
        imageView.setImage(image);
        setGraphic(imageView);
    }

    /**
     * Restituisce la larghezza di adattamento dell'immagine.
     *
     * @return larghezza di adattamento
     */
    public double getFitWidth() {
        return imageView.getFitWidth();
    }

    /**
     * Imposta la larghezza di adattamento dell'immagine e la larghezza preferita del pulsante.
     *
     * @param fitWidth larghezza di adattamento
     */
    public void setFitWidth(double fitWidth) {
        imageView.setFitWidth(fitWidth);
        setMinWidth(fitWidth);
        setPrefWidth(fitWidth);
        setMaxWidth(fitWidth);
    }

    /**
     * Restituisce l'altezza di adattamento dell'immagine.
     *
     * @return altezza di adattamento
     */
    public double getFitHeight() {
        return imageView.getFitHeight();
    }

    /**
     * Imposta l'altezza di adattamento dell'immagine e l'altezza preferita del pulsante.
     *
     * @param fitHeight altezza di adattamento
     */
    public void setFitHeight(double fitHeight) {
        imageView.setFitHeight(fitHeight);
        setMinHeight(fitHeight);
        setPrefHeight(fitHeight);
        setMaxHeight(fitHeight);
    }

    /**
     * Indica se l'immagine mantiene le proprie proporzioni durante il ridimensionamento.
     *
     * @return {@code true} se le proporzioni vengono mantenute
     */
    public boolean isPreserveRatio() {
        return imageView.isPreserveRatio();
    }

    /**
     * Imposta se mantenere le proporzioni dell'immagine durante il ridimensionamento.
     *
     * @param preserveRatio {@code true} per mantenere le proporzioni
     */
    public void setPreserveRatio(boolean preserveRatio) {
        imageView.setPreserveRatio(preserveRatio);
    }

    private static Image caricaImmagine(String resourcePath) {
        var resource = HomeLogo.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Risorsa del logo non trovata: " + resourcePath);
        }
        return new Image(resource.toExternalForm());
    }
}
