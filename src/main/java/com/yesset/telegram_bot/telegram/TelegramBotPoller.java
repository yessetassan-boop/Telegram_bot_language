package com.yesset.telegram_bot.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotPoller {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotPoller.class);

    private final TelegramClient telegramClient;
    private final UpdateHandler updateHandler;

    private volatile boolean running = true;
    private Thread pollingThread;

    public TelegramBotPoller(TelegramClient telegramClient, UpdateHandler updateHandler) {
        this.telegramClient = telegramClient;
        this.updateHandler = updateHandler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        pollingThread = new Thread(this::pollLoop, "telegram-poller");
        pollingThread.setDaemon(true);
        pollingThread.start();
        log.info("Telegram long-polling started");
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private void pollLoop() {
        long offset = 0;
        while (running) {
            try {
                JsonNode response = telegramClient.getUpdates(offset);
                JsonNode results = response.get("result");
                if (results != null) {
                    for (JsonNode update : results) {
                        offset = update.get("update_id").asLong() + 1;
                        try {
                            updateHandler.handle(update);
                        } catch (Exception e) {
                            log.error("Error while handling Telegram update", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error while polling Telegram getUpdates, retrying in 5s", e);
                sleep(5000);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
