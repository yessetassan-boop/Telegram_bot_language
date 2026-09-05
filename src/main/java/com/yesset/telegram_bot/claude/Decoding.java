package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record Decoding(
        @JsonPropertyDescription("Естественный, живой перевод всего текста на язык-мост")
        String fluentTranslation,

        @JsonPropertyDescription("Разбор текста слово за словом, строго в порядке изучаемого языка")
        List<DecodedWord> wordByWord,

        @JsonPropertyDescription("От 3 до 5 заметок о грамматике изучаемого языка именно в этом тексте")
        List<GrammarNote> grammarNotes
) {
}
