package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TranslationFeedback(
        @JsonPropertyDescription("true, если попытка ученика верна по смыслу и грамматике изучаемого языка. Мелкие стилистические отличия от эталона ошибкой не считаются")
        boolean translationCorrect,

        @JsonPropertyDescription("Короткий разбор на языке-мосте (язык, который ученик уже знает): что не так и почему — порядок слов, окончания, время, предлоги, согласование. Если всё верно — коротко похвали")
        String explanationBridge,

        @JsonPropertyDescription("Как ту же мысль естественно сказал бы носитель изучаемого языка. Пишется ТОЛЬКО на изучаемом языке. Заполняется всегда, даже если ответ ученика уже верен")
        String nativeVariant,

        @JsonPropertyDescription("Один короткий практический совет на будущее на языке-мосте. Пустая строка, если добавить нечего")
        String tipBridge
) {
}
