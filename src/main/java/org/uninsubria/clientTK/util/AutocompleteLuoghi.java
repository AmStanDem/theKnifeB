package org.uninsubria.clientTK.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class AutocompleteLuoghi {

    private final TextField campo;
    private final Consumer<GeocodingService.Luogo> alSeleziona;
    private final ContextMenu suggerimenti = new ContextMenu();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(350));
    private final AtomicLong generazione = new AtomicLong();
    private GeocodingService.Luogo luogoSelezionato;
    private boolean aggiornamentoInterno;

    public AutocompleteLuoghi(
            TextField campo,
            Consumer<GeocodingService.Luogo> alSeleziona
    ) {
        this.campo = campo;
        this.alSeleziona = alSeleziona;
        campo.textProperty().addListener((observable, precedente, nuovo) -> {
            if (aggiornamentoInterno) {
                return;
            }
            luogoSelezionato = null;
            long richiesta = generazione.incrementAndGet();
            suggerimenti.hide();
            if (nuovo == null || nuovo.trim().length() < 3) {
                debounce.stop();
                return;
            }
            debounce.setOnFinished(event -> caricaSuggerimenti(nuovo.trim(), richiesta));
            debounce.playFromStart();
        });
        campo.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                debounce.stop();
                suggerimenti.hide();
            }
        });
        campo.focusedProperty().addListener((observable, eraAttivo, attivo) -> {
            if (attivo && luogoSelezionato == null && campo.getText().trim().length() >= 3) {
                debounce.setOnFinished(event ->
                        caricaSuggerimenti(campo.getText().trim(), generazione.get()));
                debounce.playFromStart();
            } else if (!attivo) {
                suggerimenti.hide();
            }
        });
    }

    public GeocodingService.Luogo getLuogoSelezionato() {
        return luogoSelezionato;
    }

    public void seleziona(GeocodingService.Luogo luogo) {
        luogoSelezionato = luogo;
        aggiornamentoInterno = true;
        campo.setText(luogo.etichetta());
        aggiornamentoInterno = false;
        suggerimenti.hide();
        alSeleziona.accept(luogo);
    }

    private void caricaSuggerimenti(String query, long richiesta) {
        Thread.startVirtualThread(() -> {
            try {
                List<GeocodingService.Luogo> luoghi = GeocodingService.suggerisciLuoghi(query);
                Platform.runLater(() -> {
                    if (richiesta != generazione.get()
                            || !campo.isFocused()
                            || !query.equals(campo.getText().trim())) {
                        return;
                    }
                    mostraSuggerimenti(luoghi);
                });
            } catch (Exception e) {
                System.err.println("[GEOCODING] Impossibile caricare i suggerimenti: " + e.getMessage());
                Platform.runLater(() -> {
                    if (richiesta == generazione.get()
                            && campo.isFocused()
                            && query.equals(campo.getText().trim())) {
                        mostraMessaggio("Ricerca località temporaneamente non disponibile.");
                    }
                });
            }
        });
    }

    private void mostraSuggerimenti(List<GeocodingService.Luogo> luoghi) {
        suggerimenti.getItems().clear();
        for (GeocodingService.Luogo luogo : luoghi) {
            MenuItem item = new MenuItem(luogo.etichetta());
            item.setOnAction(event -> seleziona(luogo));
            suggerimenti.getItems().add(item);
        }
        if (luoghi.isEmpty()) {
            mostraMessaggio("Nessuna località trovata.");
            return;
        }
        if (!suggerimenti.getItems().isEmpty() && campo.getScene() != null
                && campo.getScene().getWindow() != null
                && campo.getScene().getWindow().isShowing()) {
            suggerimenti.show(campo, Side.BOTTOM, 0, 0);
        } else {
            suggerimenti.hide();
        }
    }

    private void mostraMessaggio(String messaggio) {
        MenuItem item = new MenuItem(messaggio);
        item.setDisable(true);
        suggerimenti.getItems().setAll(item);
        if (campo.getScene() != null && campo.getScene().getWindow() != null
                && campo.getScene().getWindow().isShowing()) {
            suggerimenti.show(campo, Side.BOTTOM, 0, 0);
        }
    }
}
