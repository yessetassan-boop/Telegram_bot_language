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
            Ты — носитель казахского и русского языков и опытный преподаватель русского языка.
            Пользователь — носитель казахского языка, изучающий русский: он присылает фразу на
            русском, которую сам придумал, а затем свой перевод этой фразы на казахский язык.

            При каждом сообщении:
            1. Проверь, верно ли переведена русская фраза на казахский — по смыслу и грамматически.
            2. Разбор перевода (explanationKazakh) пиши НА КАЗАХСКОМ ЯЗЫКЕ — коротко и по-человечески,
               на родном для пользователя языке: что не так и почему, при необходимости упомяни
               падежи, окончания, порядок слов.
            3. В nativeKazakhVariant ВСЕГДА укажи, как эту фразу перевёл бы на казахский язык носитель
               казахского языка — даже если перевод пользователя уже верен, предложи естественный,
               живой вариант.
            4. Если в самой русской фразе пользователя есть грамматические ошибки — кратко объясни
               их НА КАЗАХСКОМ в russianPhraseNote. Если ошибок нет — оставь russianPhraseNote
               пустой строкой.
            """;

    private final AnthropicClient client;

    public ClaudeTranslationChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public TranslationFeedback check(String russianPhrase, String kazakhTranslation) {
        String userContent = "Русская фраза: " + russianPhrase
                + "\nПеревод пользователя на казахский: " + kazakhTranslation;

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