package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClaudeExerciseGenerator {

    private static final String SYSTEM_PROMPT = """
            Ты — опытный преподаватель русского языка для казахоязычной аудитории. Придумай одно
            короткое упражнение: слово или простую фразу НА КАЗАХСКОМ ЯЗЫКЕ, которую пользователь
            должен перевести на русский.

            Каждый раз выбирай новую, разнообразную лексику: бытовые темы, глаголы, существительные,
            прилагательные, повседневные фразы. Не повторяй одни и те же слова из раза в раз.
            """;

    private final AnthropicClient client;

    public ClaudeExerciseGenerator(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public ExercisePrompt generate() {
        StructuredMessageCreateParams<ExercisePrompt> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(512L)
                .system(SYSTEM_PROMPT)
                .outputConfig(ExercisePrompt.class)
                .addUserMessage("Придумай новое упражнение для перевода.")
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
