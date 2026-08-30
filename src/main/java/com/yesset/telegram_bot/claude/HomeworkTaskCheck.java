package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record HomeworkTaskCheck(
        @JsonPropertyDescription("true, если перевод пользователя на русский верен по смыслу и грамматике. Мелкие стилистические расхождения с эталоном ошибкой не считаются")
        boolean correct,

        @JsonPropertyDescription("Оценка ответа целым числом от 0 до 100")
        int scorePercent,

        @JsonPropertyDescription("Разбор ответа НА КАЗАХСКОМ ЯЗЫКЕ: что сделано верно, где ошибка в русском переводе и почему (вид глагола, приставка, падеж, предлог, окончание, порядок слов). Коротко, 1-3 предложения, на родном для ученика языке")
        String explanationKazakh,

        @JsonPropertyDescription("Правильный вариант перевода НА РУССКИЙ ЯЗЫК. Если ответ пользователя верен — можно повторить его или привести эталон")
        String correctedAnswerRussian,

        @JsonPropertyDescription("Один короткий практический совет НА КАЗАХСКОМ ЯЗЫКЕ на будущее. Пустая строка, если добавить нечего")
        String tipKazakh
) {
}
