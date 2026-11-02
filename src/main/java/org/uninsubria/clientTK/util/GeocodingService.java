package org.uninsubria.clientTK.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class GeocodingService {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final Pattern JSON_STRING = Pattern.compile(
            "\"%s\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );
    private static long prossimaRichiestaNominatim;

    private GeocodingService() {
    }

    public record Coordinate(double latitudine, double longitudine) {
    }

    public record Luogo(
            String etichetta,
            String indirizzo,
            String localita,
            String nazione,
            double latitudine,
            double longitudine
    ) {
        public Luogo {
            if (etichetta == null || etichetta.isBlank()
                    || indirizzo == null || indirizzo.isBlank()
                    || localita == null || localita.isBlank()
                    || nazione == null || nazione.isBlank()
                    || !Double.isFinite(latitudine) || latitudine < -90 || latitudine > 90
                    || !Double.isFinite(longitudine) || longitudine < -180 || longitudine > 180) {
                throw new IllegalArgumentException("Dati del luogo non validi.");
            }
        }
    }

    public static List<Luogo> suggerisciLuoghi(String query) throws IOException, InterruptedException {
        if (query == null || query.trim().length() < 3) {
            return List.of();
        }
        String url = BASE_URL + "/search?q=" + encode(query.trim())
                + "&format=jsonv2&addressdetails=1&limit=5&accept-language=it";
        String json = inviaRichiesta(url);
        List<Luogo> luoghi = new ArrayList<>();
        for (String object : oggettiJsonPrincipali(json)) {
            String lat = stringa(object, "lat");
            String lon = stringa(object, "lon");
            String displayName = stringa(object, "display_name");
            String locality = primaStringa(object,
                    "city", "town", "municipality", "village", "hamlet", "county");
            String country = stringa(object, "country");
            if (locality == null) {
                locality = displayName;
            }
            String address = indirizzoDaJson(object, locality);
            try {
                if (displayName != null && lat != null && lon != null
                        && locality != null && country != null && address != null) {
                    luoghi.add(new Luogo(displayName, address, locality, country,
                            Double.parseDouble(lat), Double.parseDouble(lon)));
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed entries returned by the external geocoder.
            }
        }
        return List.copyOf(luoghi);
    }

    public static Luogo luogoDaCoordinate(double latitudine, double longitudine)
            throws IOException, InterruptedException {
        if (!Double.isFinite(latitudine) || latitudine < -90 || latitudine > 90
                || !Double.isFinite(longitudine) || longitudine < -180 || longitudine > 180) {
            throw new IllegalArgumentException("Coordinate non valide.");
        }
        String url = String.format(Locale.US,
                BASE_URL + "/reverse?format=jsonv2&addressdetails=1&lat=%.7f&lon=%.7f&accept-language=it",
                latitudine, longitudine);
        String json = inviaRichiesta(url);
        String displayName = stringa(json, "display_name");
        String locality = primaStringa(json,
                "city", "town", "municipality", "village", "hamlet", "county");
        String country = stringa(json, "country");
        if (locality == null) {
            locality = displayName;
        }
        String address = indirizzoDaJson(json, locality);
        if (displayName == null || locality == null || country == null || address == null) {
            throw new IOException("Non è stato possibile identificare il luogo selezionato.");
        }
        return new Luogo(displayName, address, locality, country, latitudine, longitudine);
    }

    public static Coordinate ottieniCoordinate(String indirizzo, String citta, String nazione)
            throws IOException, InterruptedException {
        String query = String.join(", ", Stream.of(indirizzo, citta, nazione)
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .toList());
        List<Luogo> risultati = suggerisciLuoghi(query);
        return risultati.isEmpty() ? null
                : new Coordinate(risultati.getFirst().latitudine(), risultati.getFirst().longitudine());
    }

    private static String inviaRichiesta(String url) throws IOException, InterruptedException {
        attendiLimiteNominatim();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "TheKnifeB/1.0 (restaurant discovery app)")
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Servizio di geocodifica non disponibile (HTTP "
                    + response.statusCode() + ").");
        }
        return response.body();
    }

    private static synchronized void attendiLimiteNominatim() throws InterruptedException {
        long adesso = System.currentTimeMillis();
        long attesa = prossimaRichiestaNominatim - adesso;
        prossimaRichiestaNominatim = Math.max(adesso, prossimaRichiestaNominatim) + 1_000;
        if (attesa > 0) {
            Thread.sleep(attesa);
        }
    }

    private static List<String> oggettiJsonPrincipali(String json) {
        List<String> oggetti = new ArrayList<>();
        int inizio = -1;
        int profondita = 0;
        boolean inStringa = false;
        boolean escape = false;
        for (int i = 0; i < json.length(); i++) {
            char carattere = json.charAt(i);
            if (inStringa) {
                if (escape) {
                    escape = false;
                } else if (carattere == '\\') {
                    escape = true;
                } else if (carattere == '"') {
                    inStringa = false;
                }
                continue;
            }
            if (carattere == '"') {
                inStringa = true;
            } else if (carattere == '{') {
                if (profondita++ == 0) {
                    inizio = i;
                }
            } else if (carattere == '}' && profondita > 0 && --profondita == 0) {
                oggetti.add(json.substring(inizio, i + 1));
            }
        }
        return oggetti;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String primaStringa(String json, String... fields) {
        for (String field : fields) {
            String value = stringa(json, field);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String indirizzoDaJson(String json, String localita) {
        String strada = primaStringa(json,
                "road", "pedestrian", "residential", "street", "path", "footway");
        String numero = stringa(json, "house_number");
        if (strada != null) {
            return numero == null ? strada : strada + ", " + numero;
        }
        return localita;
    }

    private static String stringa(String json, String field) {
        Pattern pattern = Pattern.compile(String.format(JSON_STRING.pattern(), Pattern.quote(field)));
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? decodificaStringaJson(matcher.group(1)) : null;
    }

    private static String decodificaStringaJson(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current != '\\' || i + 1 >= value.length()) {
                decoded.append(current);
                continue;
            }
            char escaped = value.charAt(++i);
            switch (escaped) {
                case '"', '\\', '/' -> decoded.append(escaped);
                case 'n', 'r', 't' -> decoded.append(' ');
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'u' -> {
                    if (i + 4 < value.length()) {
                        decoded.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                }
                default -> decoded.append(escaped);
            }
        }
        return decoded.toString();
    }
}
