package com.yesset.telegram_bot.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionService {

    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession get(long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSession());
    }

    /** Полный сброс сессии (для /start). Возвращает новую сессию. */
    public UserSession reset(long chatId) {
        UserSession session = new UserSession();
        sessions.put(chatId, session);
        return session;
    }
}
