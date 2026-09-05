package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.yesset.telegram_bot.session.LanguagePair;

/**
 * Общие мелочи для всех клиентов к Claude: имя модели, подстановка языковой пары
 * в шаблон промпта и извлечение первого структурированного ответа.
 */
final class ClaudeSupport {

    static final String MODEL = "claude-opus-5";

    private ClaudeSupport() {
    }

    static String fill(String template, LanguagePair pair) {
        return template
                .replace("{bridge}", pair.bridgeLanguage())
                .replace("{target}", pair.targetLanguage());
    }

    static <T> T firstStructured(AnthropicClient client, StructuredMessageCreateParams<T> params) {
        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(block -> block.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
