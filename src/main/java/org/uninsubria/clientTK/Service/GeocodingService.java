package org.uninsubria.clientTK.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeocodingService {

    private static final String URL_OPENSTREETMAP =
            "https://nominatim.openstreetmap.org/search";

    public void geocode(String address) throws IOException, InterruptedException {

        String url = URL_OPENSTREETMAP
                + "?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                + "&format=json";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "TheKnifeB")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println(response.body());
    }

}