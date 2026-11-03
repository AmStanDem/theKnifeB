package org.uninsubria.clientTK.util;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public final class CuisineTags {

    private CuisineTags() {
    }

    public static void populate(FlowPane container, List<String> cuisines) {
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
