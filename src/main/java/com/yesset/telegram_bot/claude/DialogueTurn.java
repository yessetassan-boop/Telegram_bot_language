package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DialogueTurn(
        @JsonPropertyDescription("Естественный ответ НА РУССКОМ ЯЗЫКЕ, продолжающий разговор — простыми предложениями (уровень A1-A2), с уточняющим вопросом в конце, чтобы поддержать диалог")
        String replyRussian,

        @JsonPropertyDescription("Если в последнем сообщении пользователя на русском есть ошибки — короткая мягкая поправка НА КАЗАХСКОМ ЯЗЫКЕ (что и как лучше сказать). Если ошибок нет — пустая строка")
        String correctionKazakh
) {
}
