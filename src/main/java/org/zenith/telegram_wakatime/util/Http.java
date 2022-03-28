package org.zenith.telegram_wakatime.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Http {
    public static HttpResponse GET (String uri, Map<String, String> headers, String errorMessage)  {
        var client = HttpClient.newHttpClient();

        var requestBuilder = HttpRequest.newBuilder().GET();
        for (var entry : headers.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }
        requestBuilder.uri(URI.create(uri));

        var request = requestBuilder.build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    public static HttpResponse POST (String uri, Map<String, String> headers, String payload, String errorMessage)  {
        var client = HttpClient.newHttpClient();

        var requestBuilder = HttpRequest.newBuilder();
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));

        for (var entry : headers.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }
        requestBuilder.uri(URI.create(uri));

        var request = requestBuilder.build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}
