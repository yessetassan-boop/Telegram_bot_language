package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DecodedWord(
        @JsonPropertyDescription("Слово или короткая смысловая группа на изучаемом языке — ровно как в исходном тексте, в том же порядке")
        String original,

        @JsonPropertyDescription("Дословный, буквальный перевод этого слова/группы на язык-мост (язык, который ученик уже знает). Может звучать коряво — это нормально для метода Биркенбиль")
        String literal
) {
}
