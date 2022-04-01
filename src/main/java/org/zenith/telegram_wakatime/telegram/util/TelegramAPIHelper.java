package org.zenith.telegram_wakatime.telegram.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TelegramAPIHelper {
    private static final String TELEGRAM_URL = "https://api.telegram.org/bot";

    @AllArgsConstructor
    @Getter
    private static class Project {
        String name;
        Long time;
    }

    public static String prepareMessage (Map<String, Double> wakatimeData) {
        // sorting of projects is based on duration
        var queue = new PriorityQueue<Project>(Comparator.comparingLong(Project::getTime).reversed());
        wakatimeData.entrySet().forEach(entry -> queue.offer(new Project(entry.getKey(),
                convertToMinutes(entry.getValue()))));

        var builder = new StringBuilder();
        while (!queue.isEmpty()) {
            var project = queue.poll();
            builder.append("- ").append(project.getName()).append(" - ").append(project.getTime()).append(" min\n");
        }
        return builder.toString();
    }

    private static long convertToMinutes (double seconds) {
        return Math.round(seconds / 60);
    }

    public static String prepareJSONPayload (ObjectMapper mapper, String chatId, String text) {
        var map = Map.of("chat_id", chatId, "text", text);
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ERR-0003: Error while preparing Telegram payload", e);
        }
    }

    public static String getSendMessageURL (String botToken) {
        return String.format("%s%s/sendMessage", TELEGRAM_URL, botToken);
    }
}
