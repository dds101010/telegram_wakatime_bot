package org.zenith.telegram_wakatime.wakatime.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zenith.telegram_wakatime.wakatime.exceptions.DurationsNotAvailableException;
import org.zenith.telegram_wakatime.wakatime.model.Duration;

import java.net.http.HttpResponse;

public class WakatimeAPIHelper {
    private static final String WAKATIME_URI = "https://wakatime.com/api/v1/users/current";

    public static String getDurationsURL (String date) {
        return String.format("%s/durations?date=%s", WAKATIME_URI, date);
    }

    public static Duration[] extractDurationsFromResponse (ObjectMapper mapper, HttpResponse response,
                                                           String errorMessage) throws DurationsNotAvailableException {

        JsonNode json = null;
        try {
            json = mapper.readValue(response.body().toString(), JsonNode.class);
            if (json.isNull() || json.get("data").isNull()) {
                throw new DurationsNotAvailableException();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(errorMessage, e);
        }

        var durationsString = json.get("data");
        try {
            var durations =  mapper.readValue(durationsString.toString(), Duration[].class);
            if (durations.length == 0) {
                throw new DurationsNotAvailableException();
            }
            return durations;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}