package com.yesset.telegram_bot.telegram;

import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.yesset.telegram_bot.claude.ClaudeTranslationChecker;
import com.yesset.telegram_bot.claude.Decoding;
import com.yesset.telegram_bot.claude.DecodingFormatter;
import com.yesset.telegram_bot.claude.TextDecoder;
import com.yesset.telegram_bot.claude.TranslationFeedback;
import com.yesset.telegram_bot.claude.TranslationFeedbackFormatter;
import com.yesset.telegram_bot.claude.WordReference;
import com.yesset.telegram_bot.claude.WordReferenceChecker;
import com.yesset.telegram_bot.claude.WordReferenceFormatter;
import com.yesset.telegram_bot.session.LanguagePair;
import com.yesset.telegram_bot.session.SessionState;
import com.yesset.telegram_bot.session.UserSession;
import com.yesset.telegram_bot.session.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateHandler.class);

    // callback_data
    private static final String MODE_DECODE = "mode:decode";
    private static final String MODE_SAY = "mode:say";
    private static final String MODE_LANG = "mode:lang";
    private static final String ACT_REREAD = "act:reread";
    private static final String ACT_WORD = "act:word";
    private static final String ACT_SAY = "act:say";
    private static final String ACT_NEW = "act:new";
    private static final String ACT_MENU = "act:menu";
    private static final String LANG_PREFIX = "lang:";

    private static final String WELCOME_TEXT = """
            Привет! Я помогаю «расшифровывать» изучаемый язык через тот, который ты уже знаешь —
            по методу Биркенбиль.

            Ты присылаешь любой текст на изучаемом языке (фразу из песни, заголовок, сообщение),
            а я показываю:
            • дословный разбор слово за словом
            • живой перевод
            • 3–5 заметок по грамматике

            Потом можно потренироваться — сказать ту же мысль самому.

            Сменить язык или режим — команда /menu.
            """;

    private final TelegramClient telegramClient;
    private final UserSessionService sessionService;
    private final TextDecoder textDecoder;
    private final WordReferenceChecker wordReferenceChecker;
    private final ClaudeTranslationChecker translationChecker;

    public UpdateHandler(TelegramClient telegramClient,
                         UserSessionService sessionService,
                         TextDecoder textDecoder,
                         WordReferenceChecker wordReferenceChecker,
                         ClaudeTranslationChecker translationChecker) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.textDecoder = textDecoder;
        this.wordReferenceChecker = wordReferenceChecker;
        this.translationChecker = translationChecker;
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

    // ---------- обычные сообщения ----------

    private void handleMessage(JsonNode message) {
        JsonNode chat = message.get("chat");
        if (chat == null) {
            return;
        }
        long chatId = chat.get("id").asLong();

        JsonNode textNode = message.get("text");
        if (textNode == null) {
            telegramClient.sendMessage(chatId, "Пришли, пожалуйста, обычным текстом 🙂");
            return;
        }
        String text = textNode.asText().trim();
        if (text.isEmpty()) {
            return;
        }

        if (text.equals("/start")) {
            UserSession session = sessionService.reset(chatId);
            telegramClient.sendMessage(chatId, WELCOME_TEXT);
            sendMenu(chatId, session);
            return;
        }
        if (text.equals("/menu")) {
            UserSession session = sessionService.get(chatId);
            session.resetConversation();
            sendMenu(chatId, session);
            return;
        }

        UserSession session = sessionService.get(chatId);
        switch (session.getState()) {
            case CHOOSING_MODE, CHOOSING_LANGUAGE -> sendMenu(chatId, session);
            case DECODE_WAITING_TEXT, AFTER_DECODE -> doDecode(chatId, session, text);
            case WORD_WAITING_WORD -> doWordLookup(chatId, session, text);
            case PRODUCTION_WAITING_SOURCE -> {
                session.setProductionSource(text);
                session.setProductionReference(null);
                session.setState(SessionState.PRODUCTION_WAITING_ATTEMPT);
                telegramClient.sendMessage(chatId,
                        "Теперь скажи это на языке «" + session.getLanguagePair().targetLanguage() + "».");
            }
            case PRODUCTION_WAITING_ATTEMPT -> doProductionCheck(chatId, session, text);
        }
    }

    // ---------- нажатия кнопок ----------

    private void handleCallbackQuery(JsonNode callbackQuery) {
        JsonNode dataNode = callbackQuery.get("data");
        JsonNode messageNode = callbackQuery.get("message");
        JsonNode idNode = callbackQuery.get("id");
        if (dataNode == null || messageNode == null || idNode == null) {
            return;
        }

        long chatId = messageNode.get("chat").get("id").asLong();
        String data = dataNode.asText();
        UserSession session = sessionService.get(chatId);
        LanguagePair pair = session.getLanguagePair();

        switch (data) {
            case MODE_DECODE, ACT_NEW -> {
                session.setState(SessionState.DECODE_WAITING_TEXT);
                telegramClient.sendMessage(chatId,
                        "Пришли текст на языке «" + pair.targetLanguage() + "» — одно-два предложения. "
                                + "Лучше то, что тебе реально интересно: строчка из песни, цитата, мем.");
            }
            case MODE_SAY -> {
                session.setState(SessionState.PRODUCTION_WAITING_SOURCE);
                telegramClient.sendMessage(chatId,
                        "Напиши фразу на языке «" + pair.bridgeLanguage() + "» — я попрошу сказать её "
                                + "на «" + pair.targetLanguage() + "» и разберу ошибки.");
            }
            case MODE_LANG -> {
                session.setState(SessionState.CHOOSING_LANGUAGE);
                sendLanguageButtons(chatId);
            }
            case ACT_REREAD -> {
                if (session.getLastDecoding() != null && session.getLastText() != null) {
                    telegramClient.sendHtmlMessage(chatId, DecodingFormatter.toTelegramHtml(
                            session.getLastText(), session.getLastDecoding()));
                    sendAfterDecodeButtons(chatId);
                } else {
                    sendMenu(chatId, session);
                }
            }
            case ACT_WORD -> {
                session.setState(SessionState.WORD_WAITING_WORD);
                telegramClient.sendMessage(chatId, "Какое слово разобрать? Напиши его.");
            }
            case ACT_SAY -> {
                if (session.getLastDecoding() != null) {
                    session.setProductionSource(session.getLastDecoding().fluentTranslation());
                    session.setProductionReference(session.getLastText());
                    session.setState(SessionState.PRODUCTION_WAITING_ATTEMPT);
                    telegramClient.sendMessage(chatId,
                            "Скажи это на языке «" + pair.targetLanguage() + "»:\n\n«"
                                    + session.getLastDecoding().fluentTranslation() + "»");
                } else {
                    session.setState(SessionState.PRODUCTION_WAITING_SOURCE);
                    telegramClient.sendMessage(chatId,
                            "Напиши фразу на языке «" + pair.bridgeLanguage() + "».");
                }
            }
            case ACT_MENU -> {
                session.resetConversation();
                sendMenu(chatId, session);
            }
            default -> {
                if (data.startsWith(LANG_PREFIX)) {
                    applyLanguage(chatId, session, data.substring(LANG_PREFIX.length()));
                }
            }
        }

        telegramClient.answerCallbackQuery(idNode.asText());
    }

    // ---------- сценарии ----------

    private void doDecode(long chatId, UserSession session, String text) {
        telegramClient.sendMessage(chatId, "⏳ Расшифровываю…");
        try {
            Decoding decoding = textDecoder.decode(session.getLanguagePair(), text);
            session.setLastText(text);
            session.setLastDecoding(decoding);
            session.setState(SessionState.AFTER_DECODE);
            telegramClient.sendHtmlMessage(chatId, DecodingFormatter.toTelegramHtml(text, decoding));
            sendAfterDecodeButtons(chatId);
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while decoding text for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось расшифровать — ошибка на стороне ИИ. Попробуй ещё раз чуть позже.");
        }
    }

    private void doWordLookup(long chatId, UserSession session, String word) {
        try {
            WordReference reference = wordReferenceChecker.lookup(session.getLanguagePair(), word);
            telegramClient.sendHtmlMessage(chatId, WordReferenceFormatter.toTelegramHtml(word, reference));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while looking up word for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось разобрать слово — ошибка на стороне ИИ. Попробуй ещё раз чуть позже.");
        }

        if (session.getLastDecoding() != null) {
            session.setState(SessionState.AFTER_DECODE);
            sendAfterDecodeButtons(chatId);
        } else {
            sendMenu(chatId, session);
        }
    }

    private void doProductionCheck(long chatId, UserSession session, String attempt) {
        try {
            TranslationFeedback feedback = translationChecker.check(
                    session.getLanguagePair(),
                    session.getProductionSource(),
                    attempt,
                    session.getProductionReference());
            telegramClient.sendHtmlMessage(chatId, TranslationFeedbackFormatter.toTelegramHtml(feedback));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while checking production for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось проверить — ошибка на стороне ИИ. Пришли ответ ещё раз.");
            return;
        }

        if (session.getLastDecoding() != null) {
            session.setState(SessionState.AFTER_DECODE);
            sendAfterDecodeButtons(chatId);
        } else {
            session.setState(SessionState.CHOOSING_MODE);
            telegramClient.sendMenu(chatId, "Ещё?", List.of(
                    new InlineButton("✍️ Ещё фраза", MODE_SAY),
                    new InlineButton("📥 Расшифровать текст", MODE_DECODE),
                    new InlineButton("☰ Меню", ACT_MENU)
            ));
        }
    }

    private void applyLanguage(long chatId, UserSession session, String name) {
        try {
            LanguagePair pair = LanguagePair.valueOf(name);
            session.setLanguagePair(pair);
            telegramClient.sendMessage(chatId, "Готово: " + pair.label());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown language pair callback: {}", name);
        }
        session.resetConversation();
        sendMenu(chatId, session);
    }

    // ---------- меню и кнопки ----------

    private void sendMenu(long chatId, UserSession session) {
        session.setState(SessionState.CHOOSING_MODE);
        telegramClient.sendMenu(chatId, "Выбери, что делаем:", List.of(
                new InlineButton("📥 Расшифровать текст", MODE_DECODE),
                new InlineButton("✍️ Тренировка: скажи сам", MODE_SAY),
                new InlineButton("⚙️ Язык: " + session.getLanguagePair().label(), MODE_LANG)
        ));
    }

    private void sendAfterDecodeButtons(long chatId) {
        telegramClient.sendMenu(chatId, "Что дальше?", List.of(
                new InlineButton("🔁 Прочитать ещё раз", ACT_REREAD),
                new InlineButton("🔍 Разобрать слово", ACT_WORD),
                new InlineButton("✍️ Теперь скажи сам", ACT_SAY),
                new InlineButton("📥 Новый текст", ACT_NEW),
                new InlineButton("☰ Меню", ACT_MENU)
        ));
    }

    private void sendLanguageButtons(long chatId) {
        List<InlineButton> buttons = Arrays.stream(LanguagePair.values())
                .map(pair -> new InlineButton(pair.label(), LANG_PREFIX + pair.name()))
                .toList();
        telegramClient.sendMenu(chatId, "Выбери пару «знаю → учу»:", buttons);
    }
}
