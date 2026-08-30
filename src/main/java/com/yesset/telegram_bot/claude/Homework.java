package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record Homework(
        @JsonPropertyDescription("Тема домашнего задания на русском языке, как её задал пользователь (например: еда, семья, путешествия). Если пользователь написал «любая» или тему понять нельзя — подставь выбранную тобой бытовую тему")
        String topic,

        @JsonPropertyDescription("Задания по нарастающей сложности, от простого к сложному. Ровно столько заданий, сколько запрошено")
        List<HomeworkTask> tasks
) {
}
