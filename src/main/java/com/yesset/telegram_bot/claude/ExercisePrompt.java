package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExercisePrompt(
        @JsonPropertyDescription("Слово или короткая фраза (1-6 слов) НА КАЗАХСКОМ ЯЗЫКЕ, которую пользователь должен перевести на русский. Подбирай разнообразную лексику разного уровня сложности из бытовых тем: глаголы, существительные, прилагательные, короткие фразы")
        String kazakhPhrase
) {
}
