package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HomeworkChecker {

    private static final String SYSTEM_PROMPT = """
            Ты — носитель казахского и русского языков и опытный преподаватель русского языка.
            Твоя аудитория — носители казахского языка, которые изучают русский. Пользователь
            выполняет домашнее задание: ему дали предложение на казахском, он прислал свой перевод
            на русский. У тебя есть эталонный перевод.

            Проверь ответ пользователя:
            1. Верен ли он по смыслу и грамматике русского языка. Мелкие стилистические расхождения
               с эталоном ошибкой не считаются.
            2. Поставь оценку от 0 до 100.
            3. Разбор (explanationKazakh) пиши НА КАЗАХСКОМ ЯЗЫКЕ, коротко: что верно, где ошибка в
               русском переводе и почему (вид глагола, приставка, падеж, предлог, окончание,
               порядок слов).
            4. В correctedAnswerRussian дай правильный вариант НА РУССКОМ ЯЗЫКЕ.
            5. tipKazakh — один короткий совет на будущее НА КАЗАХСКОМ ЯЗЫКЕ, или пустая строка,
               если добавить нечего.
            """;

    private final AnthropicClient client;

    public HomeworkChecker(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public HomeworkTaskCheck check(HomeworkTask task, String userAnswer) {
        String userContent = "Предложение на казахском: " + task.promptKazakh()
                + "\nГрамматический фокус: " + task.grammarFocusKazakh()
                + "\nЭталонный перевод на русский: " + task.referenceAnswerRussian()
                + "\nПеревод пользователя на русский: " + userAnswer;

        StructuredMessageCreateParams<HomeworkTaskCheck> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(1024L)
                .system(SYSTEM_PROMPT)
                .outputConfig(HomeworkTaskCheck.class)
                .addUserMessage(userContent)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
