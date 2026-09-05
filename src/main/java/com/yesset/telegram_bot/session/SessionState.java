package com.yesset.telegram_bot.session;

public enum SessionState {
    CHOOSING_MODE,
    CHOOSING_LANGUAGE,
    DECODE_WAITING_TEXT,
    AFTER_DECODE,
    WORD_WAITING_WORD,
    PRODUCTION_WAITING_SOURCE,
    PRODUCTION_WAITING_ATTEMPT
}
