package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.yesset.telegram_bot.session.LanguagePair;
import org.springframework.stereotype.Component;

@Component
public class ClaudeTranslationChecker {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Ты — двуязычный носитель языков «{bridge}» и «{target}» и опытный преподаватель языка
            «{target}». Твой ученик — носитель языка «{bridge}», он изучает «{target}». Ученику дали
            фразу на языке «{bridge}», и он попытался сказать её на языке «{target}». Иногда есть
            эталонный вариант на «{target}» — тогда ориентируйся на него, но мелкие стилистические
            отличия ошибкой не считай.

            Дай разбор по четырём полям:
            1. translationCorrect — верна ли попытка ученика по смыслу и грамматике языка «{target}».
            2. explanationBridge — разбор НА ЯЗЫКЕ «{bridge}», коротко и по-человечески: что не так и
               почему (порядок слов, окончания, время, предлоги, согласование). Если всё верно —
               коротко похвали.
            3. nativeVariant — как эту же мысль естественно сказал бы носитель языка «{target}».
               ПИШИ ТОЛЬКО НА ЯЗЫКЕ «{target}». Заполняй всегда.
            4. tipBridge — один короткий совет на будущее НА ЯЗЫКЕ «{bridge}», или пустая строка.
            """;

    private final AnthropicClient client;

    public ClaudeTranslationChecker(AnthropicClient client) {
        this.client = client;
    }

    public TranslationFeedback check(LanguagePair pair,
                                    String bridgePhrase,
                                    String targetAttempt,
                                    String targetReference) {
        String userContent = "Фраза на языке «" + pair.bridgeLanguage() + "»: " + bridgePhrase
                + "\nПопытка ученика на языке «" + pair.targetLanguage() + "»: " + targetAttempt;
        if (targetReference != null && !targetReference.isBlank()) {
            userContent += "\nЭталонный вариант на языке «" + pair.targetLanguage() + "»: " + targetReference;
        }

        StructuredMessageCreateParams<TranslationFeedback> params = MessageCreateParams.builder()
                .model(ClaudeSupport.MODEL)
                .maxTokens(1536L)
                .system(ClaudeSupport.fill(SYSTEM_PROMPT_TEMPLATE, pair))
                .outputConfig(TranslationFeedback.class)
                .addUserMessage(userContent)
                .build();

        return ClaudeSupport.firstStructured(client, params);
    }
}
