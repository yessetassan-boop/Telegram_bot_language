package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TranslationFeedback(
        @JsonPropertyDescription("true, если перевод пользователя на казахский верен по смыслу и грамматике")
        boolean translationCorrect,

        @JsonPropertyDescription("Разбор перевода НА КАЗАХСКОМ ЯЗЫКЕ: что не так в переводе пользователя (если есть ошибки) и почему, простыми словами, понятными на родном для пользователя языке")
        String explanationKazakh,

        @JsonPropertyDescription("Как эту русскую фразу перевёл бы на казахский язык носитель казахского языка — естественный, живой вариант перевода. Заполняется всегда, даже если перевод пользователя уже верен")
        String nativeKazakhVariant,

        @JsonPropertyDescription("Если в исходной русской фразе пользователя есть грамматические ошибки — короткое объяснение НА КАЗАХСКОМ ЯЗЫКЕ. Если ошибок нет — пустая строка")
        String russianPhraseNote
) {
}