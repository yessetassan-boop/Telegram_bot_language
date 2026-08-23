package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExerciseFeedback(
        @JsonPropertyDescription("true, если перевод пользователя на русский верен по смыслу и грамматике")
        boolean correct,

        @JsonPropertyDescription("Короткое объяснение НА КАЗАХСКОМ ЯЗЫКЕ: что не так в переводе пользователя (если есть ошибки) и почему. Если перевод верен — короткая похвала на казахском")
        String explanationKazakh,

        @JsonPropertyDescription("Правильный (или наиболее естественный) вариант перевода фразы на русский язык")
        String correctRussianAnswer
) {
}
