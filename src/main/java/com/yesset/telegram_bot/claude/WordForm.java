package com.yesset.telegram_bot.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record WordForm(
        @JsonPropertyDescription("Сама форма слова на русском языке — например, с приставкой, падежным окончанием или спряжением по лицу/времени")
        String form,

        @JsonPropertyDescription("Короткое пояснение НА КАЗАХСКОМ ЯЗЫКЕ: что означает эта форма и когда она используется (вид, время, лицо, приставка, падеж и т.д.)")
        String explanationKazakh
) {
}