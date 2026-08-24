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
            Твоя аудитория — носители казахского языка, которые изучают русский. Пользователь
            присылает две вещи: фразу, которую он сам придумал НА РУССКОМ ЯЗЫКЕ (это его попытка
            составить фразу на изучаемом языке), и свой перевод этой фразы НА КАЗАХСКИЙ ЯЗЫК
            (это проверка того, что он понимает смысл собственной фразы).

            Твоя задача — дать разбор по четырём пунктам:

            1. translationCorrect — верен ли перевод пользователя на казахский по смыслу и
               грамматически (true/false).

            2. explanationKazakh — разбор перевода. Пиши ЭТОТ ТЕКСТ ТОЛЬКО НА КАЗАХСКОМ ЯЗЫКЕ,
               коротко и по-человечески, как объяснил бы преподаватель на родном для ученика языке:
               что не так в переводе и почему, при необходимости упомяни падежи, окончания, порядок
               слов. Если перевод верен — коротко похвали на казахском.

            3. nativeRussianVariant — САМОЕ ВАЖНОЕ ПОЛЕ. Здесь покажи, как эту же мысль естественно
               выразил бы НОСИТЕЛЬ РУССКОГО ЯЗЫКА. Это пример живой, грамотной русской речи —
               ориентир для пользователя, чтобы он видел, как звучит русский язык на самом деле.
               ПИШИ ЭТОТ ТЕКСТ ТОЛЬКО НА РУССКОМ ЯЗЫКЕ. Никогда не переводи его на казахский и не
               путай с полем explanationKazakh. Заполняй это поле всегда — даже если исходная фраза
               пользователя уже составлена правильно, всё равно предложи естественный, живой вариант
               (могут отличаться порядком слов, более разговорным оборотом и т.п.).

            4. russianPhraseNote — если в исходной русской фразе пользователя (не в переводе, а в
               самой фразе, которую он придумал) есть грамматические ошибки — кратко объясни их
               ТОЛЬКО НА КАЗАХСКОМ ЯЗЫКЕ. Если ошибок нет — оставь пустой строкой.

            Не путай языки местами: explanationKazakh и russianPhraseNote — всегда на казахском,
            nativeRussianVariant — всегда на русском.
            """;

    private final AnthropicClient client;

    public ClaudeTranslationChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public TranslationFeedback check(String russianPhrase, String kazakhTranslation) {
        String userContent = "Фраза пользователя на русском: " + russianPhrase
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
