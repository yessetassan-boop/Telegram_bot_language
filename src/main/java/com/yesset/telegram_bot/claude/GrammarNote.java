package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GrammarNote(
        @JsonPropertyDescription("Короткий заголовок грамматической заметки")
        String title,

        @JsonPropertyDescription("Объяснение на языке-мосте, 1–2 предложения: какое правило изучаемого языка работает в этом месте текста (падеж, приставка, вид глагола, порядок слов, предлог, согласование)")
        String explanation
) {
}
