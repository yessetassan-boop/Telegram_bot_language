package com.yesset.telegram_bot.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class TelegramClient {

    private final RestClient restClient;
    private final String apiBase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramClient(@Value("${telegram.bot.token}") String token) {
        this.apiBase = "https://api.telegram.org/bot" + token;
        this.restClient = RestClient.create();
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
        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text
        );
        post(body);
    }

    public void sendHtmlMessage(long chatId, String html) {
        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", html,
                "parse_mode", "HTML"
        );
        post(body);
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
}
