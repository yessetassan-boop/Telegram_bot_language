package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WordReferenceChecker {

    private static final String SYSTEM_PROMPT = """
            Ты — носитель казахского языка и преподаватель. Пользователь присылает одно слово на
            казахском языке (чаще всего глагол в базовой форме, но может быть и существительное или
            прилагательное).

            Покажи его основные формы:
            - для глагола: спряжения по лицам и временам (настоящее, прошедшее, будущее), повелительное
              наклонение, формы с наиболее употребимыми послелогами (кейін, бойынша, арқылы, туралы,
              үшін и т.п.);
            - для существительного: падежные формы, формы с послелогами, форму множественного числа;
            - для прилагательного: сравнительную степень и типичные сочетания.

            К каждой форме дай короткое пояснение НА РУССКОМ ЯЗЫКЕ: что она означает и когда
            используется. Если в слове опечатка — всё равно постарайся понять, что имелось в виду,
            и упомяни это в note.
            """;

    private final AnthropicClient client;

    public WordReferenceChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public WordReference lookup(String word) {
        StructuredMessageCreateParams<WordReference> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(2048L)
                .system(SYSTEM_PROMPT)
                .outputConfig(WordReference.class)
                .addUserMessage("Слово на казахском: " + word)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
