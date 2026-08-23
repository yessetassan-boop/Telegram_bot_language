package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TranslationFeedback(
        @JsonPropertyDescription("true, если перевод пользователя на русский верен по смыслу и грамматике")
        boolean translationCorrect,

        @JsonPropertyDescription("Разбор перевода НА КАЗАХСКОМ ЯЗЫКЕ: что не так в переводе пользователя (если есть ошибки) и почему, простыми словами, как объяснил бы носитель языка")
        String explanationKazakh,

        @JsonPropertyDescription("Как эту казахскую фразу перевёл бы на русский язык носитель русского языка — естественный, живой вариант перевода. Заполняется всегда, даже если перевод пользователя уже верен")
        String nativeRussianVariant,

        @JsonPropertyDescription("Если в исходной казахской фразе пользователя есть грамматические ошибки — короткое объяснение НА КАЗАХСКОМ ЯЗЫКЕ. Если ошибок нет — пустая строка")
        String kazakhPhraseNote
) {
}
