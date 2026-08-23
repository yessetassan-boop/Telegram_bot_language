package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClaudeExerciseChecker {

    private static final String SYSTEM_PROMPT = """
            Ты — опытный преподаватель русского языка для казахоязычной аудитории. Пользователю
            было дано слово или фраза на казахском языке, которую нужно перевести на русский.
            Оцени его перевод: верен ли он по смыслу и грамматически. Объяснение (explanationKazakh)
            пиши КОРОТКО НА КАЗАХСКОМ ЯЗЫКЕ, понятно и по-доброму, как объяснил бы преподаватель.
            В correctRussianAnswer всегда укажи правильный/наиболее естественный вариант перевода.
            """;

    private final AnthropicClient client;

    public ClaudeExerciseChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public ExerciseFeedback check(String kazakhPhrase, String userAnswer) {
        String userContent = "Фраза на казахском: " + kazakhPhrase
                + "\nПеревод пользователя на русский: " + userAnswer;

        StructuredMessageCreateParams<ExerciseFeedback> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(768L)
                .system(SYSTEM_PROMPT)
                .outputConfig(ExerciseFeedback.class)
                .addUserMessage(userContent)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
