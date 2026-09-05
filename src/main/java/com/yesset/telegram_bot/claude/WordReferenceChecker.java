package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.yesset.telegram_bot.session.LanguagePair;
import org.springframework.stereotype.Component;

@Component
public class WordReferenceChecker {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Ты — носитель языка «{target}» и опытный преподаватель языка «{target}» для тех, кто
            говорит на языке «{bridge}». Ученик присылает одно слово на языке «{target}» (чаще
            всего глагол в начальной форме, но может быть существительное или прилагательное).

            Особое внимание удели словообразовательной семье слова (relatedWords): если в языке
            «{target}» есть приставки или аффиксы, меняющие вид или смысл слова, обязательно покажи
            4–10 самых употребимых форм с кратким пояснением значения на языке «{bridge}» для
            каждой. Если для этой части речи это неприменимо — пустой список.

            Затем покажи основные грамматические формы (forms):
            - для глагола: спряжение по лицам и временам, повелительное наклонение, вид;
            - для существительного: падежные формы и множественное число;
            - для прилагательного: степени сравнения и типичные сочетания.

            Все пояснения к формам пиши КОРОТКО НА ЯЗЫКЕ «{bridge}». Если в слове опечатка — всё
            равно постарайся понять, что имелось в виду, и отметь это в note.
            """;

    private final AnthropicClient client;

    public WordReferenceChecker(AnthropicClient client) {
        this.client = client;
    }

    public WordReference lookup(LanguagePair pair, String word) {
        StructuredMessageCreateParams<WordReference> params = MessageCreateParams.builder()
                .model(ClaudeSupport.MODEL)
                .maxTokens(2048L)
                .system(ClaudeSupport.fill(SYSTEM_PROMPT_TEMPLATE, pair))
                .outputConfig(WordReference.class)
                .addUserMessage("Слово на языке «" + pair.targetLanguage() + "»: " + word)
                .build();

        return ClaudeSupport.firstStructured(client, params);
    }
}
