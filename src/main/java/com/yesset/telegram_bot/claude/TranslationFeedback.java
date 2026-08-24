package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TranslationFeedback(
        @JsonPropertyDescription("true, если перевод пользователя на русский верен по смыслу и грамматике")
        boolean translationCorrect,

        @JsonPropertyDescription("Разбор перевода НА КАЗАХСКОМ ЯЗЫКЕ: что не так в переводе пользователя на русский (если есть ошибки) и почему, простыми словами, понятными на родном для пользователя языке")
        String explanationKazakh,

        @JsonPropertyDescription("Пример того, как ЭТУ ЖЕ МЫСЛЬ естественно выразил бы носитель русского языка. Пишется ТОЛЬКО НА РУССКОМ ЯЗЫКЕ (это пример живой русской речи для практики, а не перевод на казахский). Заполняется всегда, даже если перевод пользователя уже звучит естественно")
        String nativeRussianVariant,

        @JsonPropertyDescription("Если в исходной казахской фразе пользователя есть грамматические ошибки — короткое объяснение НА КАЗАХСКОМ ЯЗЫКЕ. Если ошибок нет — пустая строка")
        String kazakhPhraseNote
) {
}
