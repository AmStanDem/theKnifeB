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

public final class HomeLogo extends Button {

    private static final String HOME_VIEW =
            "/org/uninsubria/theknife/client/views/MainLayout.fxml";
    private static final String DEFAULT_IMAGE =
            "/org/uninsubria/theknife/client/images/img_1.png";

    private final ImageView imageView = new ImageView();

    public HomeLogo() {
        setImage(loadImage(DEFAULT_IMAGE));
        setFitWidth(441);
        setFitHeight(133);
        imageView.setPreserveRatio(true);
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setPadding(Insets.EMPTY);
        setStyle("-fx-background-color: transparent; -fx-background-insets: 0; "
                + "-fx-border-color: transparent; -fx-border-width: 0; -fx-cursor: hand;");
        setAccessibleText("Torna alla home");
        setOnAction(event -> SceneManager.switchScene(event, HOME_VIEW));
    }

    public Image getImage() {
        return imageView.getImage();
    }

    public void setImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("L'immagine del logo non può essere nulla.");
        }
        imageView.setImage(image);
        setGraphic(imageView);
    }

    public double getFitWidth() {
        return imageView.getFitWidth();
    }

    public void setFitWidth(double fitWidth) {
        imageView.setFitWidth(fitWidth);
        setPrefWidth(fitWidth);
    }

    public double getFitHeight() {
        return imageView.getFitHeight();
    }

    public void setFitHeight(double fitHeight) {
        imageView.setFitHeight(fitHeight);
        setPrefHeight(fitHeight);
    }

    public boolean isPreserveRatio() {
        return imageView.isPreserveRatio();
    }

    public void setPreserveRatio(boolean preserveRatio) {
        imageView.setPreserveRatio(preserveRatio);
    }

    private static Image loadImage(String resourcePath) {
        var resource = HomeLogo.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Risorsa del logo non trovata: " + resourcePath);
        }
        return new Image(resource.toExternalForm());
    }
}
