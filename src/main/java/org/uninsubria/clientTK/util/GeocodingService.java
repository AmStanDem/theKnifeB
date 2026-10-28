package org.uninsubria.clientTK.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeocodingService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private GeocodingService() {
    }

    public record Coordinate(double latitudine, double longitudine) {
    }

    public static Coordinate ottieniCoordinate(
            String indirizzo,
            String citta,
            String nazione
    ) throws IOException, InterruptedException {

        String query = indirizzo + ", " + citta + ", " + nazione;

        String url = "https://nominatim.openstreetmap.org/search"
                + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&format=jsonv2"
                + "&limit=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "TheKnifeB/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Servizio di geocodifica non disponibile. Codice HTTP: "
                            + response.statusCode()
            );
        }

        String json = response.body();

        // Nominatim restituisce [] se non trova risultati.
        if (json == null || json.equals("[]") || json.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "\"lat\"\\s*:\\s*\"([^\"]+)\".*?"
                        + "\"lon\"\\s*:\\s*\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IOException(
                    "Impossibile leggere le coordinate della risposta."
            );
        }

        try {
            double latitudine = Double.parseDouble(matcher.group(1));
            double longitudine = Double.parseDouble(matcher.group(2));

            if (!Double.isFinite(latitudine)
                    || !Double.isFinite(longitudine)
                    || latitudine < -90
                    || latitudine > 90
                    || longitudine < -180
                    || longitudine > 180) {

                throw new IOException("Coordinate non valide.");
            }

            return new Coordinate(latitudine, longitudine);

        } catch (NumberFormatException e) {
            throw new IOException("Formato delle coordinate non valido.", e);
        }
    }
}