package com.yesset.telegram_bot.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TelegramClient {

    /** Лимит Telegram на длину текста одного сообщения. */
    private static final int TELEGRAM_MAX_MESSAGE = 4096;

    private final RestClient restClient;
    private final String apiBase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramClient(@Value("${telegram.bot.token}") String token) {
        this.apiBase = "https://api.telegram.org/bot" + token;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        // read-timeout должен быть больше, чем timeout long-polling в getUpdates (30 с)
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public JsonNode getUpdates(long offset) {
        String raw = restClient.get()
                .uri(apiBase + "/getUpdates?offset={offset}&timeout=30", offset)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Telegram getUpdates response", e);
        }
    }

    public void sendMessage(long chatId, String text) {
        for (String chunk : splitForTelegram(text)) {
            post(Map.of(
                    "chat_id", chatId,
                    "text", chunk
            ));
        }
    }

    public void sendHtmlMessage(long chatId, String html) {
        for (String chunk : splitForTelegram(html)) {
            post(Map.of(
                    "chat_id", chatId,
                    "text", chunk,
                    "parse_mode", "HTML"
            ));
        }
    }

    public void sendMenu(long chatId, String text, List<InlineButton> buttons) {
        List<List<Map<String, String>>> keyboard = buttons.stream()
                .map(button -> List.of(Map.of(
                        "text", button.text(),
                        "callback_data", button.callbackData())))
                .toList();

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "reply_markup", Map.of("inline_keyboard", keyboard)
        );
        post(body);
    }

    public void answerCallbackQuery(String callbackQueryId) {
        Map<String, Object> body = Map.of("callback_query_id", callbackQueryId);
        restClient.post()
                .uri(apiBase + "/answerCallbackQuery")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void post(Map<String, Object> body) {
        restClient.post()
                .uri(apiBase + "/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Режет длинный текст на части по границам строк, чтобы не упереться в лимит Telegram.
     * Теги форматирования в наших сообщениях всегда лежат в пределах одной строки,
     * поэтому разрыв по '\n' их не ломает.
     */
    static List<String> splitForTelegram(String text) {
        if (text == null || text.length() <= TELEGRAM_MAX_MESSAGE) {
            return List.of(text == null ? "" : text);
        }

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : text.split("\n", -1)) {
            if (current.length() > 0 && current.length() + line.length() + 1 > TELEGRAM_MAX_MESSAGE) {
                parts.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(line);
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }
}
