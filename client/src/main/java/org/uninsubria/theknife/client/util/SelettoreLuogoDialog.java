/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Screen;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class SelettoreLuogoDialog {

    private static final ButtonType CONFERMA = new ButtonType(
            "Usa questo luogo", ButtonBar.ButtonData.OK_DONE);

    private SelettoreLuogoDialog() {
    }

    public static Optional<GeocodingService.Luogo> mostra(
            Window owner,
            GeocodingService.Luogo luogoIniziale
    ) {
        Dialog<GeocodingService.Luogo> dialog = new Dialog<>();
        dialog.setTitle("Seleziona una località");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.getDialogPane().getButtonTypes().addAll(CONFERMA, ButtonType.CANCEL);

        TextField ricerca = new TextField();
        ricerca.setPromptText("Cerca una via, città o indirizzo");
        Label stato = new Label("Cerca un luogo oppure trascina la mappa sotto il segnaposto.");
        Label statoMappa = new Label("Caricamento mappa OpenStreetMap...");
        statoMappa.setWrapText(true);
        statoMappa.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 6;");
        AtomicReference<GeocodingService.Luogo> selezionato = new AtomicReference<>();
        Button conferma = (Button) dialog.getDialogPane().lookupButton(CONFERMA);
        conferma.setDisable(true);
        MappaLuoghi mappa = new MappaLuoghi(
                luogoIniziale,
                luogo -> {
                    selezionato.set(luogo);
                    stato.setText(luogo.etichetta());
                    conferma.setDisable(false);
                },
                () -> {
                    selezionato.set(null);
                    stato.setText("Sto verificando il punto selezionato...");
                    conferma.setDisable(true);
                },
                () -> {
                    selezionato.set(null);
                    stato.setText("Posizione spostata. Conferma il punto al centro della mappa.");
                    conferma.setDisable(true);
                },
                messaggio -> stato.setText(messaggio),
                statoMappa::setText
        );

        AutocompleteLuoghi autocomplete = new AutocompleteLuoghi(ricerca, luogo -> {
            selezionato.set(luogo);
            stato.setText(luogo.etichetta());
            conferma.setDisable(false);
            mappa.centraSu(luogo.latitudine(), luogo.longitudine());
        });
        if (luogoIniziale != null) {
            autocomplete.seleziona(luogoIniziale);
        }

        Button zoomOut = new Button("−");
        Button zoomIn = new Button("+");
        Button ricentra = new Button("Torna al luogo");
        ricentra.setAccessibleText("Torna alla posizione iniziale della mappa");
        ricentra.setTooltip(new javafx.scene.control.Tooltip(
                "Torna alla posizione iniziale della mappa"));
        Button selezionaCentro = new Button("Usa il punto al centro");
        selezionaCentro.setDefaultButton(false);
        zoomOut.setOnAction(event -> mappa.cambiaZoom(-1));
        zoomIn.setOnAction(event -> mappa.cambiaZoom(1));
        ricentra.setOnAction(event -> mappa.ricentra());
        selezionaCentro.setOnAction(event -> mappa.selezionaCentro());
        HBox controls = new HBox(8, zoomOut, zoomIn, ricentra, selezionaCentro);
        controls.setPadding(new Insets(8));
        controls.setAlignment(Pos.CENTER);
        controls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        controls.setStyle("-fx-background-color: rgba(255,255,255,0.94);"
                + "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #00000055, 8, 0, 0, 2);");

        StackPane mapPane = new StackPane(mappa, controls);
        StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);
        StackPane.setMargin(controls, new Insets(0, 0, 12, 0));
        StackPane.setAlignment(statoMappa, Pos.TOP_LEFT);
        Label attributionMap = new Label("© OpenStreetMap contributors");
        attributionMap.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 4;");
        StackPane.setAlignment(attributionMap, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(attributionMap, new Insets(0, 8, 8, 0));
        mapPane.getChildren().add(attributionMap);
        VBox.setVgrow(mapPane, Priority.ALWAYS);
        VBox content = new VBox(10, new Label("Località"), ricerca, stato, statoMappa, mapPane);
        content.setPadding(new Insets(8));
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        content.setPrefSize(Math.min(1200, screen.getWidth() * 0.9),
                Math.min(850, screen.getHeight() * 0.88));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(button -> button == CONFERMA ? selezionato.get() : null);
        dialog.setResizable(true);

        return dialog.showAndWait();
    }

    private static final class MappaLuoghi extends Pane {

        private static final int TILE_SIZE = 256;
        private static final int MIN_ZOOM = 2;
        private static final int MAX_ZOOM = 19;
        private static final HttpClient HTTP = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        private static final Map<String, Image> TILE_CACHE = new ConcurrentHashMap<>();
        private static final Semaphore TILE_REQUESTS = new Semaphore(2);

        private final Consumer<GeocodingService.Luogo> selezione;
        private final Runnable inizioSelezione;
        private final Runnable centroSpostato;
        private final Consumer<String> erroreSelezione;
        private final Consumer<String> statoMappa;
        private final Map<String, ImageView> visteTile = new HashMap<>();
        private final Set<String> tileInFlight = ConcurrentHashMap.newKeySet();
        private final StackPane marker = new StackPane();
        private final PauseTransition caricamentoTileDopoPan =
                new PauseTransition(javafx.util.Duration.millis(120));
        private final AtomicLong generazioneSelezione = new AtomicLong();
        private final java.util.concurrent.atomic.AtomicBoolean tileCaricato =
                new java.util.concurrent.atomic.AtomicBoolean();
        private final java.util.concurrent.atomic.AtomicBoolean erroreTileMostrato =
                new java.util.concurrent.atomic.AtomicBoolean();
        private final Rectangle areaVisibile = new Rectangle();
        private double centroLatitudine;
        private double centroLongitudine;
        private double centroX;
        private double centroY;
        private double dragX;
        private double dragY;
        private double dragStartX;
        private double dragStartY;
        private boolean dragging;
        private int zoom;
        private final double latitudineIniziale;
        private final double longitudineIniziale;
        private final int zoomIniziale;

        private MappaLuoghi(
                GeocodingService.Luogo iniziale,
                Consumer<GeocodingService.Luogo> selezione,
                Runnable inizioSelezione,
                Runnable centroSpostato,
                Consumer<String> erroreSelezione,
                Consumer<String> statoMappa
        ) {
            this.selezione = selezione;
            this.inizioSelezione = inizioSelezione;
            this.centroSpostato = centroSpostato;
            this.erroreSelezione = erroreSelezione;
            this.statoMappa = statoMappa;
            zoomIniziale = iniziale == null ? 6 : 14;
            latitudineIniziale = iniziale == null ? 42.5 : iniziale.latitudine();
            longitudineIniziale = iniziale == null ? 12.5 : iniziale.longitudine();
            zoom = zoomIniziale;
            centroLatitudine = latitudineIniziale;
            centroLongitudine = longitudineIniziale;
            setClip(areaVisibile);
            Circle bordoMarker = new Circle(13, javafx.scene.paint.Color.web("#d92d20"));
            bordoMarker.setStroke(javafx.scene.paint.Color.WHITE);
            bordoMarker.setStrokeWidth(3);
            Circle centroMarker = new Circle(4, javafx.scene.paint.Color.WHITE);
            marker.getChildren().addAll(bordoMarker, centroMarker);
            marker.setMinSize(30, 30);
            marker.setPrefSize(30, 30);
            marker.setMaxSize(30, 30);
            marker.setEffect(new javafx.scene.effect.DropShadow(
                    8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.55)));
            marker.setMouseTransparent(true);
            getChildren().add(marker);
            setMinSize(400, 300);
            setPrefSize(760, 420);
            setStyle("-fx-background-color: #e8edf0;");
            caricamentoTileDopoPan.setOnFinished(event -> aggiornaMappa());
            widthProperty().addListener((observable, oldValue, newValue) -> {
                areaVisibile.setWidth(newValue.doubleValue());
                aggiornaMappa();
            });
            heightProperty().addListener((observable, oldValue, newValue) -> {
                areaVisibile.setHeight(newValue.doubleValue());
                aggiornaMappa();
            });
            setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    dragX = event.getX();
                    dragY = event.getY();
                    dragStartX = centroX;
                    dragStartY = centroY;
                    dragging = false;
                }
            });
            setOnMouseDragged(event -> {
                if (event.isPrimaryButtonDown()) {
                    if (Math.abs(event.getX() - dragX) > 4 || Math.abs(event.getY() - dragY) > 4) {
                        dragging = true;
                    }
                    centroX = dragStartX - (event.getX() - dragX);
                    centroY = dragStartY - (event.getY() - dragY);
                    aggiornaCentroCoordinate();
                    aggiornaMappa(false);
                    caricamentoTileDopoPan.playFromStart();
                }
            });
            setOnMouseReleased(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    caricamentoTileDopoPan.stop();
                    aggiornaCentroCoordinate();
                    aggiornaMappa();
                    if (dragging) {
                        generazioneSelezione.incrementAndGet();
                        centroSpostato.run();
                    }
                }
            });
            layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> aggiornaMappa());
            addEventFilter(ScrollEvent.SCROLL, event -> {
                if (Math.abs(event.getDeltaY()) > 0) {
                    cambiaZoom(event.getDeltaY() > 0 ? 1 : -1);
                    event.consume();
                }
            });
            Platform.runLater(this::aggiornaMappa);
        }

        private void centraSu(double latitudine, double longitudine) {
            generazioneSelezione.incrementAndGet();
            zoom = Math.max(zoom, 15);
            centroLatitudine = latitudine;
            centroLongitudine = longitudine;
            aggiornaMappa();
        }

        private void ricentra() {
            generazioneSelezione.incrementAndGet();
            zoom = zoomIniziale;
            centroLatitudine = latitudineIniziale;
            centroLongitudine = longitudineIniziale;
            aggiornaMappa();
        }

        private void cambiaZoom(int delta) {
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom + delta));
            aggiornaMappa();
        }

        private void aggiornaMappa() {
            aggiornaMappa(true);
        }

        private void aggiornaMappa(boolean caricaNuoviTile) {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            centroX = longitudineToX(centroLongitudine, zoom);
            centroY = latitudineToY(centroLatitudine, zoom);
            double left = centroX - getWidth() / 2;
            double top = centroY - getHeight() / 2;
            int minTileX = (int) Math.floor(left / TILE_SIZE);
            int maxTileX = (int) Math.floor((left + getWidth()) / TILE_SIZE);
            int minTileY = (int) Math.floor(top / TILE_SIZE);
            int maxTileY = (int) Math.floor((top + getHeight()) / TILE_SIZE);
            int tileCount = 1 << zoom;
            Set<ImageView> tileVisibili = new HashSet<>();
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                    if (tileY < 0 || tileY >= tileCount) {
                        continue;
                    }
                    int wrappedX = Math.floorMod(tileX, tileCount);
                    String key = zoom + "/" + wrappedX + "/" + tileY;
                    ImageView tile = visteTile.get(key);
                    if (tile == null && caricaNuoviTile) {
                        tile = new ImageView();
                        tile.setFitWidth(TILE_SIZE);
                        tile.setFitHeight(TILE_SIZE);
                        tile.setMouseTransparent(true);
                        visteTile.put(key, tile);
                        getChildren().add(tile);
                    }
                    if (tile == null) {
                        continue;
                    }
                    tileVisibili.add(tile);
                    tile.setLayoutX(tileX * TILE_SIZE - left);
                    tile.setLayoutY(tileY * TILE_SIZE - top);
                    if (caricaNuoviTile && tile.getImage() == null) {
                        caricaTile(key, tile);
                    }
                    tile.toBack();
                }
            }
            visteTile.entrySet().removeIf(entry -> {
                ImageView view = entry.getValue();
                if (!tileVisibili.contains(view)) {
                    getChildren().remove(view);
                    return true;
                }
                return false;
            });
            aggiornaMarker();
        }

        private void caricaTile(String key, ImageView tile) {
            Image cached = TILE_CACHE.get(key);
            if (cached != null) {
                tile.setImage(cached);
                segnalaMappaPronta();
                return;
            }
            if (!tileInFlight.add(key)) {
                return;
            }
            Thread.startVirtualThread(() -> {
                boolean acquired = false;
                try {
                    TILE_REQUESTS.acquire();
                    acquired = true;
                    Image alreadyLoaded = TILE_CACHE.get(key);
                    if (alreadyLoaded != null) {
                        Platform.runLater(() -> {
                            tile.setImage(alreadyLoaded);
                            segnalaMappaPronta();
                        });
                        return;
                    }
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://tile.openstreetmap.org/" + key + ".png"))
                            .timeout(Duration.ofSeconds(10))
                            .header("User-Agent", "TheKnifeB/1.0 (restaurant discovery app)")
                            .GET()
                            .build();
                    HttpResponse<byte[]> response = HTTP.send(
                            request, HttpResponse.BodyHandlers.ofByteArray());
                    if (response.statusCode() != 200) {
                        throw new IOException("HTTP " + response.statusCode());
                    }
                    Image image = new Image(new ByteArrayInputStream(response.body()));
                    TILE_CACHE.put(key, image);
                    if (TILE_CACHE.size() > 400) {
                        TILE_CACHE.keySet().stream().findAny().ifPresent(TILE_CACHE::remove);
                    }
                    Platform.runLater(() -> {
                        ImageView tileAttuale = visteTile.get(key);
                        if (tileAttuale != null && tileAttuale.getImage() != image) {
                            tileAttuale.setImage(image);
                        }
                        segnalaMappaPronta();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    System.err.println("[MAP] Impossibile caricare tile " + key + ": " + e.getMessage());
                    if (!tileCaricato.get() && erroreTileMostrato.compareAndSet(false, true)) {
                        Platform.runLater(() -> statoMappa.accept(
                                "Mappa non disponibile. Puoi comunque cercare un indirizzo."));
                    }
                } finally {
                    tileInFlight.remove(key);
                    if (acquired) {
                        TILE_REQUESTS.release();
                    }
                }
            });
        }

        private void segnalaMappaPronta() {
            if (tileCaricato.compareAndSet(false, true)) {
                statoMappa.accept("Mappa pronta: trascina per spostare, rotella o +/− per zoom.");
            }
        }

        private void selezionaCentro() {
            double latitudine = centroLatitudine;
            double longitudine = centroLongitudine;
            inizioSelezione.run();
            long richiesta = generazioneSelezione.incrementAndGet();
            Thread.startVirtualThread(() -> {
                try {
                    GeocodingService.Luogo luogo =
                            GeocodingService.luogoDaCoordinate(latitudine, longitudine);
                    Platform.runLater(() -> {
                        if (richiesta == generazioneSelezione.get()) {
                            selezione.accept(luogo);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        if (richiesta == generazioneSelezione.get()) {
                            erroreSelezione.accept("Ricerca interrotta. Seleziona di nuovo il punto.");
                        }
                    });
                } catch (IOException e) {
                    System.err.println("[GEOCODING] Reverse geocoding non riuscito: " + e.getMessage());
                    Platform.runLater(() -> {
                        if (richiesta == generazioneSelezione.get()) {
                            erroreSelezione.accept(
                                    "Impossibile identificare il punto. Seleziona un altro luogo.");
                        }
                    });
                }
            });
        }

        private void aggiornaCentroCoordinate() {
            double worldSize = TILE_SIZE * (double) (1 << zoom);
            centroX = (centroX % worldSize + worldSize) % worldSize;
            centroY = Math.max(0, Math.min(worldSize, centroY));
            centroLongitudine = xToLongitudine(centroX, zoom);
            centroLatitudine = yToLatitudine(centroY, zoom);
        }

        private void aggiornaMarker() {
            marker.resize(30, 30);
            marker.setLayoutX((getWidth() - 30) / 2);
            marker.setLayoutY((getHeight() - 30) / 2);
            marker.setVisible(true);
            marker.toFront();
        }

        private static double longitudineToX(double longitudine, int zoom) {
            return (longitudine + 180) / 360 * TILE_SIZE * (1 << zoom);
        }

        private static double latitudineToY(double latitudine, int zoom) {
            double lat = Math.max(-85.05112878, Math.min(85.05112878, latitudine));
            double sin = Math.sin(Math.toRadians(lat));
            return (0.5 - Math.log((1 + sin) / (1 - sin)) / (4 * Math.PI))
                    * TILE_SIZE * (1 << zoom);
        }

        private static double xToLongitudine(double x, int zoom) {
            return x / (TILE_SIZE * (1 << zoom)) * 360 - 180;
        }

        private static double yToLatitudine(double y, int zoom) {
            double n = Math.PI - 2 * Math.PI * y / (TILE_SIZE * (1 << zoom));
            return Math.toDegrees(Math.atan(Math.sinh(n)));
        }
    }
}
