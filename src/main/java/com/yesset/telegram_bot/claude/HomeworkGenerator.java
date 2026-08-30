package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HomeworkGenerator {

    private static final String SYSTEM_PROMPT = """
            Ты — носитель казахского и русского языков и опытный преподаватель русского языка.
            Твоя аудитория — носители казахского языка, которые изучают русский. Пользователь
            просит домашнее задание на заданную тему.

            Составь набор коротких заданий на перевод С КАЗАХСКОГО НА РУССКИЙ:
            - каждое задание — одно естественное казахское предложение из 4-10 слов по теме;
            - задания идут по нарастающей сложности, от простого к сложному;
            - у каждого задания свой грамматический фокус именно по русскому языку (вид глагола,
              приставки глагола, падеж, предлог, род и число, согласование) — старайся не повторять
              фокус между заданиями;
            - для каждого задания дай эталонный перевод на русский (живой вариант носителя русского
              языка) и короткую подсказку.

            Формулировку грамматического фокуса (grammarFocusKazakh) и подсказку (hintKazakh) пиши
            НА КАЗАХСКОМ ЯЗЫКЕ — на родном для ученика языке; русский грамматический термин можно
            дать в скобках. Само задание (promptKazakh) — на казахском, эталон
            (referenceAnswerRussian) — на русском.

            Тему бери из сообщения пользователя. Если он написал «любая» или тему понять нельзя —
            выбери бытовую тему сам и укажи её в поле topic.
            """;

    private final AnthropicClient client;

    public HomeworkGenerator(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public Homework generate(String topic, int taskCount) {
        String userContent = "Тема: " + topic + "\nКоличество заданий: " + taskCount;

        StructuredMessageCreateParams<Homework> params = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(2560L)
                .system(SYSTEM_PROMPT)
                .outputConfig(Homework.class)
                .addUserMessage(userContent)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
