/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

/**
 * Utility per visualizzare le tipologie di cucina come etichette in un {@link FlowPane}.
 */
public final class CuisineTags {

    /**
     * Impedisce l'istanziazione della classe di utilità.
     */
    private CuisineTags() {
    }

    /**
     * Popola il contenitore con un'etichetta per ogni tipologia di cucina valida.
     * Rimuove prima i contenuti precedenti e mostra un'etichetta predefinita
     * se non sono disponibili tipologie valide.
     *
     * @param container contenitore da popolare
     * @param cuisines tipologie di cucina da visualizzare, oppure {@code null}
     */
    public static void popola(FlowPane container, List<String> cuisines) {
        container.getChildren().clear();
        if (cuisines != null) {
            cuisines.stream()
                    .filter(cuisine -> cuisine != null && !cuisine.isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(cuisine -> {
                        Label tag = new Label(cuisine);
                        tag.getStyleClass().add("cuisine-chip");
                        container.getChildren().add(tag);
                    });
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("Non specificata");
            empty.getStyleClass().addAll("cuisine-chip", "cuisine-chip-empty");
            container.getChildren().add(empty);
        }
    }
}
