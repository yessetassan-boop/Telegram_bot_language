package com.yesset.telegram_bot.session;

public enum SessionState {
    CHOOSING_MODE,
    GRAMMAR_WAITING_RUSSIAN,
    GRAMMAR_WAITING_TRANSLATION,
    WORD_WAITING_WORD,
    EXERCISE_WAITING_ANSWER,
    DIALOGUE_ACTIVE
}