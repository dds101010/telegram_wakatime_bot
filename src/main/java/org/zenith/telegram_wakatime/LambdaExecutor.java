package org.zenith.telegram_wakatime;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zenith.telegram_wakatime.telegram.util.TelegramAPIHelper;
import org.zenith.telegram_wakatime.util.Http;
import org.zenith.telegram_wakatime.wakatime.exceptions.DurationsNotAvailableException;
import org.zenith.telegram_wakatime.wakatime.model.Duration;
import org.zenith.telegram_wakatime.wakatime.util.WakatimeAPIHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class LambdaExecutor implements RequestHandler<Object, String> {

    @Override
    public String handleRequest (Object input, Context context) {

        // creating properties file for reading secrets
        Properties properties = getProperties();

        // onetime wakatime API headers
        final var WAKATIME_HEADERS = Map.of("Authorization", String.format("Basic %s", properties.getProperty(
                "wakatime.token")));
        final var TELEGRAM_HEADERS = Map.of("Content-Type", "application/json");
        final var TELEGRAM_TOKEN = properties.getProperty("telegram.token");
        final var TELEGRAM_CHATID = properties.getProperty("telegram.chat_id");

        // create and configure object mapper for reading and writing JSON
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * START::: business logic starts here
         */
        var dateForDurations = getDate();
        var durationsResponse = Http.GET(WakatimeAPIHelper.getDurationsURL(dateForDurations), WAKATIME_HEADERS,
                "ERR-0001: Wakatime durations API");

        // get all durations
        Duration[] durations = null;
        try {
            durations = WakatimeAPIHelper.extractDurationsFromResponse(mapper, durationsResponse,
                    "ERR-0002: Wakatime durations API response parsing");
        } catch (DurationsNotAvailableException e) {
            return "INFO-0001: No Durations logged today!";
        }

        // reduce all durations to projects
        var map = Arrays.stream(durations).collect(
                Collectors.toMap(Duration::getProject, Duration::getDuration, Double::sum)
        );

        var message = TelegramAPIHelper.prepareMessage(map);
        // System.out.println(message);
        var payload = TelegramAPIHelper.prepareJSONPayload(mapper, TELEGRAM_CHATID, message);

        var telegramResponse = Http.POST(TelegramAPIHelper.getSendMessageURL(TELEGRAM_TOKEN), TELEGRAM_HEADERS,
                payload, "ERR-0004: Error while sending message on Telegram");

        return String.valueOf(telegramResponse.statusCode());
    }


    public static void main (String[] args) {
        System.out.println(new LambdaExecutor().handleRequest(null, null));
    }

    private static Properties getProperties () {
        var properties = new Properties();
        try {
            properties.load(LambdaExecutor.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("ERR: Reading properties file. ", e);
        }
        return properties;
    }

    private static String getDate () {
        var today = LocalDate.now();
        var yesterday = today.minusDays(1);
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return formatter.format(yesterday);
    }
}
