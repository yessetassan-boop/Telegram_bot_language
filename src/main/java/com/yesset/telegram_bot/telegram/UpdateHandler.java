package com.yesset.telegram_bot.telegram;

import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.yesset.telegram_bot.claude.ClaudeTranslationChecker;
import com.yesset.telegram_bot.claude.TranslationFeedback;
import com.yesset.telegram_bot.claude.TranslationFeedbackFormatter;
import com.yesset.telegram_bot.claude.WordReference;
import com.yesset.telegram_bot.claude.WordReferenceChecker;
import com.yesset.telegram_bot.claude.WordReferenceFormatter;
import com.yesset.telegram_bot.session.SessionState;
import com.yesset.telegram_bot.session.UserSession;
import com.yesset.telegram_bot.session.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateHandler.class);

    private static final String WELCOME_TEXT = """
            Привет! Я помогу учить русский язык.

            Есть два режима:
            📝 Проверка грамматики — пишешь фразу на казахском и её перевод на русский, я разбираю ошибки.
            📖 Разбор слова — присылаешь слово на русском (например, глагол), а я показываю его формы: приставочные однокоренные слова, спряжения, падежи.

            В любой момент вернуться к выбору режима — команда /menu.
            """;

    private static final String MENU_TEXT = "Выбери режим:";

    private static final List<InlineButton> MENU_BUTTONS = List.of(
            new InlineButton("📝 Проверка грамматики", "mode:grammar"),
            new InlineButton("📖 Разбор слова", "mode:word")
    );

    private final TelegramClient telegramClient;
    private final UserSessionService sessionService;
    private final ClaudeTranslationChecker translationChecker;
    private final WordReferenceChecker wordReferenceChecker;

    public UpdateHandler(TelegramClient telegramClient,
                          UserSessionService sessionService,
                          ClaudeTranslationChecker translationChecker,
                          WordReferenceChecker wordReferenceChecker) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.translationChecker = translationChecker;
        this.wordReferenceChecker = wordReferenceChecker;
    }

    public void handle(JsonNode update) {
        JsonNode message = update.get("message");
        if (message != null) {
            handleMessage(message);
            return;
        }
        JsonNode callbackQuery = update.get("callback_query");
        if (callbackQuery != null) {
            handleCallbackQuery(callbackQuery);
        }
    }

    private void handleMessage(JsonNode message) {
        JsonNode chat = message.get("chat");
        JsonNode textNode = message.get("text");
        if (chat == null || textNode == null) {
            return;
        }

        long chatId = chat.get("id").asLong();
        String text = textNode.asText().trim();
        if (text.isEmpty()) {
            return;
        }

        if (text.equals("/start")) {
            sessionService.reset(chatId);
            telegramClient.sendMessage(chatId, WELCOME_TEXT);
            telegramClient.sendMenu(chatId, MENU_TEXT, MENU_BUTTONS);
            return;
        }
        if (text.equals("/menu")) {
            sessionService.reset(chatId);
            telegramClient.sendMenu(chatId, MENU_TEXT, MENU_BUTTONS);
            return;
        }

        UserSession session = sessionService.get(chatId);
        switch (session.getState()) {
            case CHOOSING_MODE -> telegramClient.sendMenu(chatId, MENU_TEXT, MENU_BUTTONS);
            case GRAMMAR_WAITING_KAZAKH -> handleKazakhPhrase(chatId, session, text);
            case GRAMMAR_WAITING_TRANSLATION -> handleTranslation(chatId, session, text);
            case WORD_WAITING_WORD -> handleWordLookup(chatId, text);
        }
    }

    private void handleCallbackQuery(JsonNode callbackQuery) {
        JsonNode dataNode = callbackQuery.get("data");
        JsonNode messageNode = callbackQuery.get("message");
        JsonNode idNode = callbackQuery.get("id");
        if (dataNode == null || messageNode == null || idNode == null) {
            return;
        }

        long chatId = messageNode.get("chat").get("id").asLong();
        String callbackId = idNode.asText();
        UserSession session = sessionService.get(chatId);

        switch (dataNode.asText()) {
            case "mode:grammar" -> {
                session.setState(SessionState.GRAMMAR_WAITING_KAZAKH);
                session.setKazakhPhrase(null);
                telegramClient.sendMessage(chatId,
                        "Режим: проверка грамматики.\n\nНапиши фразу на казахском языке.");
            }
            case "mode:word" -> {
                session.setState(SessionState.WORD_WAITING_WORD);
                telegramClient.sendMessage(chatId,
                        "Режим: разбор слова.\n\nПришли слово на русском (например, глагол) — покажу его формы.");
            }
            default -> {
            }
        }

        telegramClient.answerCallbackQuery(callbackId);
    }

    private void handleKazakhPhrase(long chatId, UserSession session, String text) {
        session.setKazakhPhrase(text);
        session.setState(SessionState.GRAMMAR_WAITING_TRANSLATION);
        telegramClient.sendMessage(chatId, "Хорошо! Теперь переведи эту фразу на русский язык.");
    }

    private void handleTranslation(long chatId, UserSession session, String translation) {
        String kazakhPhrase = session.getKazakhPhrase();

        try {
            TranslationFeedback feedback = translationChecker.check(kazakhPhrase, translation);
            telegramClient.sendHtmlMessage(chatId, TranslationFeedbackFormatter.toTelegramHtml(feedback));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while checking translation for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось проверить перевод — произошла ошибка на стороне ИИ. Попробуй ещё раз чуть позже.");
        } finally {
            session.setState(SessionState.GRAMMAR_WAITING_KAZAKH);
            session.setKazakhPhrase(null);
        }

        telegramClient.sendMessage(chatId,
                "Напиши следующую фразу на казахском, когда будешь готов(а), или /menu, чтобы сменить режим.");
    }

    private void handleWordLookup(long chatId, String word) {
        try {
            WordReference reference = wordReferenceChecker.lookup(word);
            telegramClient.sendHtmlMessage(chatId, WordReferenceFormatter.toTelegramHtml(word, reference));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while looking up word for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось разобрать слово — произошла ошибка на стороне ИИ. Попробуй ещё раз чуть позже.");
        }

        telegramClient.sendMessage(chatId,
                "Пришли следующее слово, когда будешь готов(а), или /menu, чтобы сменить режим.");
    }
}
