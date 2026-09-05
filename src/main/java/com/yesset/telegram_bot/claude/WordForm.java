package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record WordForm(
        @JsonPropertyDescription("Сама форма слова на изучаемом языке")
        String form,

        @JsonPropertyDescription("Короткое пояснение на языке-мосте: что означает эта форма и когда она используется")
        String explanationBridge
) {
}
