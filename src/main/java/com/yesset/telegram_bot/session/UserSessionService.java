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

    public void reset(long chatId) {
        sessions.put(chatId, new UserSession());
    }
}
