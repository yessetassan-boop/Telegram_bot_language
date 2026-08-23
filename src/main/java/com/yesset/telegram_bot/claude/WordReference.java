package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record WordReference(
        @JsonPropertyDescription("Часть речи на русском языке, например: глагол, существительное, прилагательное")
        String partOfSpeech,

        @JsonPropertyDescription("8-12 самых полезных форм этого слова: спряжения по лицам и временам (для глагола), падежные формы, формы с послелогами (кейін, бойынша, арқылы, туралы и т.п.) — с кратким пояснением на русском для каждой")
        List<WordForm> forms,

        @JsonPropertyDescription("Короткое дополнительное примечание на русском языке, если уместно (например, устойчивое выражение с этим словом). Пустая строка, если добавить нечего")
        String note
) {
}
