package com.yesset.telegram_bot.telegram;

import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.yesset.telegram_bot.claude.ClaudeDialoguePartner;
import com.yesset.telegram_bot.claude.ClaudeExerciseChecker;
import com.yesset.telegram_bot.claude.ClaudeExerciseGenerator;
import com.yesset.telegram_bot.claude.ClaudeTranslationChecker;
import com.yesset.telegram_bot.claude.DialogueTurn;
import com.yesset.telegram_bot.claude.DialogueTurnFormatter;
import com.yesset.telegram_bot.claude.ExerciseFeedback;
import com.yesset.telegram_bot.claude.ExerciseFeedbackFormatter;
import com.yesset.telegram_bot.claude.ExercisePrompt;
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

            Есть четыре режима:
            📝 Проверка грамматики — пишешь фразу на русском и её перевод на казахский, я разбираю ошибки.
            📖 Разбор слова — присылаешь слово на русском (например, глагол), а я показываю его формы: приставочные однокоренные слова, спряжения, падежи.
            🎯 Тренировка — я даю слово или фразу на казахском, ты переводишь на русский, я проверяю.
            💬 Диалог — свободное общение на русском, я поддерживаю разговор и подсказываю на казахском, если есть ошибки.

            В любой момент вернуться к выбору режима — команда /menu.
            """;

    private static final String MENU_TEXT = "Выбери режим:";

    private static final List<InlineButton> MENU_BUTTONS = List.of(
            new InlineButton("📝 Проверка грамматики", "mode:grammar"),
            new InlineButton("📖 Разбор слова", "mode:word"),
            new InlineButton("🎯 Тренировка", "mode:exercise"),
            new InlineButton("💬 Диалог", "mode:dialogue")
    );

    private final TelegramClient telegramClient;
    private final UserSessionService sessionService;
    private final ClaudeTranslationChecker translationChecker;
    private final WordReferenceChecker wordReferenceChecker;
    private final ClaudeExerciseGenerator exerciseGenerator;
    private final ClaudeExerciseChecker exerciseChecker;
    private final ClaudeDialoguePartner dialoguePartner;

    public UpdateHandler(TelegramClient telegramClient,
                          UserSessionService sessionService,
                          ClaudeTranslationChecker translationChecker,
                          WordReferenceChecker wordReferenceChecker,
                          ClaudeExerciseGenerator exerciseGenerator,
                          ClaudeExerciseChecker exerciseChecker,
                          ClaudeDialoguePartner dialoguePartner) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.translationChecker = translationChecker;
        this.wordReferenceChecker = wordReferenceChecker;
        this.exerciseGenerator = exerciseGenerator;
        this.exerciseChecker = exerciseChecker;
        this.dialoguePartner = dialoguePartner;
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
            case GRAMMAR_WAITING_RUSSIAN -> handleRussianPhrase(chatId, session, text);
            case GRAMMAR_WAITING_TRANSLATION -> handleTranslation(chatId, session, text);
            case WORD_WAITING_WORD -> handleWordLookup(chatId, text);
            case EXERCISE_WAITING_ANSWER -> handleExerciseAnswer(chatId, session, text);
            case DIALOGUE_ACTIVE -> handleDialogueMessage(chatId, session, text);
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
                session.setState(SessionState.GRAMMAR_WAITING_RUSSIAN);
                session.setRussianPhrase(null);
                telegramClient.sendMessage(chatId,
                        "Режим: проверка грамматики.\n\nНапиши фразу на русском языке.");
            }
            case "mode:word" -> {
                session.setState(SessionState.WORD_WAITING_WORD);
                telegramClient.sendMessage(chatId,
                        "Режим: разбор слова.\n\nПришли слово на русском (например, глагол) — покажу его формы.");
            }
            case "mode:exercise" -> {
                session.setState(SessionState.EXERCISE_WAITING_ANSWER);
                telegramClient.sendMessage(chatId,
                        "Режим: тренировка.\n\nЯ буду давать слово или фразу на казахском — переводи на русский.");
                sendNextExercise(chatId, session);
            }
            case "mode:dialogue" -> {
                session.setState(SessionState.DIALOGUE_ACTIVE);
                session.clearDialogueHistory();
                telegramClient.sendMessage(chatId,
                        "Режим: диалог.\n\nНапиши что-нибудь на русском, а я подхвачу разговор. Если будут ошибки — подскажу на казахском.");
            }
            default -> {
            }
        }

        telegramClient.answerCallbackQuery(callbackId);
    }

    private void handleRussianPhrase(long chatId, UserSession session, String text) {
        session.setRussianPhrase(text);
        session.setState(SessionState.GRAMMAR_WAITING_TRANSLATION);
        telegramClient.sendMessage(chatId, "Хорошо! Теперь переведи эту фразу на казахский язык.");
    }

    private void handleTranslation(long chatId, UserSession session, String translation) {
        String russianPhrase = session.getRussianPhrase();

        try {
            TranslationFeedback feedback = translationChecker.check(russianPhrase, translation);
            telegramClient.sendHtmlMessage(chatId, TranslationFeedbackFormatter.toTelegramHtml(feedback));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while checking translation for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось проверить перевод — произошла ошибка на стороне ИИ. Попробуй ещё раз чуть позже.");
        } finally {
            session.setState(SessionState.GRAMMAR_WAITING_RUSSIAN);
            session.setRussianPhrase(null);
        }

        telegramClient.sendMessage(chatId,
                "Напиши следующую фразу на русском, когда будешь готов(а), или /menu, чтобы сменить режим.");
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

    private void handleExerciseAnswer(long chatId, UserSession session, String answer) {
        String kazakhPhrase = session.getExercisePhrase();

        if (kazakhPhrase != null) {
            try {
                ExerciseFeedback feedback = exerciseChecker.check(kazakhPhrase, answer);
                telegramClient.sendHtmlMessage(chatId, ExerciseFeedbackFormatter.toTelegramHtml(feedback));
            } catch (AnthropicServiceException e) {
                log.error("Claude API error while checking exercise answer for chat {}", chatId, e);
                telegramClient.sendMessage(chatId,
                        "Не получилось проверить ответ — произошла ошибка на стороне ИИ.");
            }
        }

        sendNextExercise(chatId, session);
    }

    private void sendNextExercise(long chatId, UserSession session) {
        try {
            ExercisePrompt prompt = exerciseGenerator.generate();
            session.setExercisePhrase(prompt.kazakhPhrase());
            telegramClient.sendHtmlMessage(chatId, ExerciseFeedbackFormatter.formatTask(prompt.kazakhPhrase()));
        } catch (AnthropicServiceException e) {
            log.error("Claude API error while generating exercise for chat {}", chatId, e);
            session.setExercisePhrase(null);
            telegramClient.sendMessage(chatId,
                    "Не получилось создать задание — произошла ошибка на стороне ИИ. Попробуй ещё раз чуть позже или напиши /menu.");
        }
    }

    private void handleDialogueMessage(long chatId, UserSession session, String text) {
        try {
            DialogueTurn turn = dialoguePartner.respond(session.getDialogueHistory(), text);
            telegramClient.sendHtmlMessage(chatId, DialogueTurnFormatter.toTelegramHtml(turn));
            session.addDialogueExchange(text, turn.replyRussian());
        } catch (AnthropicServiceException e) {
            log.error("Claude API error during dialogue for chat {}", chatId, e);
            telegramClient.sendMessage(chatId,
                    "Не получилось ответить — произошла ошибка на стороне ИИ. Попробуй написать ещё раз.");
        }
    }
}
