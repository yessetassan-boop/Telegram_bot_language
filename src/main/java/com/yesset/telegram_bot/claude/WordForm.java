package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record WordForm(
        @JsonPropertyDescription("Сама форма слова на казахском языке, например с падежным окончанием, послелогом или спряжением по лицу/времени")
        String form,

        @JsonPropertyDescription("Короткое пояснение на русском языке: что означает эта форма и когда она используется (падеж, время, лицо, послелог и т.д.)")
        String explanationRussian
) {
}
