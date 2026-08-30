package com.yesset.telegram_bot.session;

public enum SessionState {
    CHOOSING_MODE,
    GRAMMAR_WAITING_KAZAKH,
    GRAMMAR_WAITING_TRANSLATION,
    WORD_WAITING_WORD,
    HOMEWORK_WAITING_TOPIC,
    HOMEWORK_IN_PROGRESS
}
