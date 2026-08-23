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
            Ты — носитель русского языка и опытный преподаватель русского языка для казахоязычной
            аудитории. Пользователь — носитель казахского языка, присылает одно слово на русском
            языке (чаще всего глагол в форме инфинитива, но может быть и существительное или
            прилагательное).

            Особое внимание удели словообразовательной семье слова (relatedWords): для глагола
            ОБЯЗАТЕЛЬНО покажи его формы с разными приставками, меняющими вид или смысл (например,
            для "играть": выиграть, проиграть, сыграть, поиграть, отыграть) — это одна из самых
            сложных тем для казахоязычных учащихся, так как в казахском языке нет системы глагольных
            приставок.

            Помимо этого покажи основные грамматические формы (forms):
            - для глагола: спряжение по лицам и временам (настоящее, прошедшее, будущее),
              повелительное наклонение, вид (совершенный/несовершенный);
            - для существительного: падежные формы, форму множественного числа;
            - для прилагательного: степени сравнения и типичные сочетания.

            Все пояснения к формам пиши КОРОТКО НА КАЗАХСКОМ ЯЗЫКЕ, чтобы пользователю было легко
            понять их на родном языке. Если в слове опечатка — всё равно постарайся понять, что
            имелось в виду, и упомяни это в note.
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
                .addUserMessage("Слово на русском языке: " + word)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}