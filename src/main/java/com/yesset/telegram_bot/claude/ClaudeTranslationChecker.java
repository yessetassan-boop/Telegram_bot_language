package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClaudeTranslationChecker {

    private static final String SYSTEM_PROMPT = """
            Ты — носитель казахского и русского языков и опытный преподаватель казахского языка.
            Пользователь изучает казахский: он присылает фразу на казахском, которую сам придумал,
            а затем свой перевод этой фразы на русский язык.

            При каждом сообщении:
            1. Проверь, верно ли переведена казахская фраза на русский — по смыслу и грамматически.
            2. Разбор перевода (explanationKazakh) пиши НА КАЗАХСКОМ ЯЗЫКЕ — коротко и по-человечески,
               как объяснил бы носитель языка: что не так и почему, при необходимости упомяни падежи,
               окончания, порядок слов.
            3. В nativeRussianVariant ВСЕГДА укажи, как эту фразу перевёл бы на русский язык носитель
               русского языка — даже если перевод пользователя уже верен, предложи естественный,
               живой вариант.
            4. Если в самой казахской фразе пользователя есть грамматические ошибки — кратко объясни
               их НА КАЗАХСКОМ в kazakhPhraseNote. Если ошибок нет — оставь kazakhPhraseNote пустой
               строкой.
            """;

    private final AnthropicClient client;

    public ClaudeTranslationChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public TranslationFeedback check(String kazakhPhrase, String russianTranslation) {
        String userContent = "Казахская фраза: " + kazakhPhrase
                + "\nПеревод пользователя на русский: " + russianTranslation;

        StructuredMessageCreateParams<TranslationFeedback> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(1536L)
                .system(SYSTEM_PROMPT)
                .outputConfig(TranslationFeedback.class)
                .addUserMessage(userContent)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
