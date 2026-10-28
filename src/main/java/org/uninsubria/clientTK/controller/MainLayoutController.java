package org.uninsubria.clientTK.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.uninsubria.clientTK.util.SceneManager;
import org.uninsubria.clientTK.util.SessioneUtente;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;



public class MainLayoutController {

    @FXML
    private TextField txtLocalita;

    @FXML
    private TextField txtRicerca;

    @FXML
    private Button btnGeolocalizzazione;

    @FXML
    private Button btnAccediRegistrati;

    @FXML
    private ImageView imgUtente;

    @FXML
    public void initialize() {
        boolean loggato =
                SessioneUtente.getUtenteCorrente() != null;

        aggiornaHeader(loggato);
        suggerisciLocalita();
    }

    private void suggerisciLocalita() {
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                // 1. Recupera le coordinate approssimative dell'IP
                HttpRequest requestIp = HttpRequest.newBuilder()
                        .uri(URI.create("https://ipwho.is/"))
                        .GET()
                        .build();

                HttpResponse<String> responseIp = client.send(
                        requestIp,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (responseIp.statusCode() != 200) {
                    return "";
                }

                String jsonIp = responseIp.body();

                System.out.println("Risposta completa ipapi.co:");
                System.out.println(jsonIp);

                double lat = estraiCoordinata(jsonIp, "latitude");
                double lon = estraiCoordinata(jsonIp, "longitude");

                if (Double.isNaN(lat) || Double.isNaN(lon)) {
                    return "";
                }

                System.out.println("Coordinate IP: " + lat + ", " + lon);

                // 2. Cerca la città corrispondente alle coordinate
                String url = String.format(
                        java.util.Locale.US,
                        "https://nominatim.openstreetmap.org/reverse"
                                + "?format=jsonv2&lat=%.6f&lon=%.6f"
                                + "&zoom=10&addressdetails=1"
                                + "&accept-language=it",
                        lat,
                        lon
                );

                HttpRequest requestGeo = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "TheKnifeB/1.0")
                        .GET()
                        .build();

                HttpResponse<String> responseGeo = client.send(
                        requestGeo,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (responseGeo.statusCode() == 200) {
                    String jsonGeo = responseGeo.body();

                    String citta = estraiCampoLocalita(jsonGeo);

                    System.out.println("Località rilevata: " + citta);

                    return citta;
                }

            } catch (Exception e) {
                System.err.println(
                        "Errore nella geolocalizzazione: "
                                + e.getMessage()
                );
            }

            return "";
        }).thenAccept(citta -> Platform.runLater(() -> {
            if (!citta.isBlank() && txtLocalita.getText().isBlank()) {
                txtLocalita.setText(citta);
            }
        }));
    }

    private double estraiCoordinata(String json, String campo) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(
                        "\"" + campo + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)"
                ).matcher(json);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return Double.NaN;
    }

    private String estraiCampoLocalita(String json) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(
                        "\"(?:city|town|municipality|village|hamlet)\""
                                + "\\s*:\\s*\"([^\"]+)\""
                ).matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private void aggiornaHeader(boolean loggato) {
        btnAccediRegistrati.setVisible(!loggato);
        btnAccediRegistrati.setManaged(!loggato);

        imgUtente.setVisible(loggato);
        imgUtente.setManaged(loggato);
    }

    @FXML
    private void onRicercaClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/listaRistoranti.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/LoginView.fxml"
        );
    }

    @FXML
    private void onAreaPersonaleLoggatoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/AreaPersonale.fxml"
        );
    }

    @FXML
    private void onGeolocalizzazioneClick() {
        // Logica geolocalizzazione
    }

    @FXML
    public void onRicercaAvanzataClick(ActionEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/RicercaAvanzataView.fxml"
        );
    }

    @FXML
    public void handleLogoClick(MouseEvent event) {
        SceneManager.switchScene(
                event,
                "/org/uninsubria/clientTK/views/MainLayout.fxml"
        );
    }
}