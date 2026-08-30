package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record HomeworkTask(
        @JsonPropertyDescription("Короткое естественное предложение НА КАЗАХСКОМ ЯЗЫКЕ (4-10 слов) по теме задания. Пользователь должен будет перевести его на русский язык")
        String promptKazakh,

        @JsonPropertyDescription("Грамматический фокус этого задания — на чём тренируется навык русского языка (вид глагола, приставки глагола, падеж, предлог, род и число, согласование). Одна короткая фраза НА КАЗАХСКОМ ЯЗЫКЕ; русский грамматический термин можно дать в скобках")
        String grammarFocusKazakh,

        @JsonPropertyDescription("Эталонный перевод предложения НА РУССКИЙ ЯЗЫК — естественный, живой вариант носителя русского языка")
        String referenceAnswerRussian,

        @JsonPropertyDescription("Короткая подсказка НА КАЗАХСКОМ ЯЗЫКЕ: какое правило русского языка или какую форму нужно применить. Направляет, но не содержит готового перевода")
        String hintKazakh
) {
}
