package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record WordReference(
        @JsonPropertyDescription("Часть речи на изучаемом языке, например: глагол, существительное, прилагательное")
        String partOfSpeech,

        @JsonPropertyDescription("Словообразовательная семья: однокоренные формы с приставками/аффиксами, меняющими вид или значение слова. 4–10 самых употребимых форм, для каждой — краткое пояснение на языке-мосте. Пустой список, если для этой части речи это неприменимо")
        List<WordForm> relatedWords,

        @JsonPropertyDescription("6–12 самых полезных грамматических форм этого слова (спряжение, времена, наклонение, вид; падежи и множественное число; степени сравнения). Для каждой — краткое пояснение на языке-мосте")
        List<WordForm> forms,

        @JsonPropertyDescription("Короткое дополнительное примечание на языке-мосте, если уместно (устойчивое выражение, частая ошибка, опечатка во вводе). Пустая строка, если добавить нечего")
        String note
) {
}
