package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.yesset.telegram_bot.session.LanguagePair;
import org.springframework.stereotype.Component;

@Component
public class TextDecoder {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Ты — двуязычный носитель языков «{bridge}» и «{target}» и преподаватель, работающий по
            методу Веры Биркенбиль. Твой ученик — носитель языка «{bridge}», он изучает язык
            «{target}». Ученик присылает текст на языке «{target}». Сделай «декодирование»:

            1. wordByWord — разбери текст СЛОВО ЗА СЛОВОМ, строго в порядке языка «{target}».
               Для каждого слова или короткой смысловой группы:
                 • original — слово/группа на языке «{target}», ровно как в тексте;
                 • literal — дословный, буквальный перевод на язык «{bridge}», даже если звучит коряво.
               Не меняй порядок слов и не «сглаживай». Знаки препинания можно опускать.

            2. fluentTranslation — естественный, живой перевод всего текста на язык «{bridge}».

            3. grammarNotes — от 3 до 5 заметок о грамматике языка «{target}» именно в этом тексте
               (падеж, приставка, вид глагола, порядок слов, предлог, согласование):
                 • title — короткий заголовок;
                 • explanation — объяснение НА ЯЗЫКЕ «{bridge}», 1–2 предложения.

            Все literal и explanation — на языке «{bridge}». Поле original и весь исходный текст —
            на языке «{target}».
            """;

    private final AnthropicClient client;

    public TextDecoder(AnthropicClient client) {
        this.client = client;
    }

    public Decoding decode(LanguagePair pair, String text) {
        StructuredMessageCreateParams<Decoding> params = MessageCreateParams.builder()
                .model(ClaudeSupport.MODEL)
                .maxTokens(3072L)
                .system(ClaudeSupport.fill(SYSTEM_PROMPT_TEMPLATE, pair))
                .outputConfig(Decoding.class)
                .addUserMessage("Текст на языке «" + pair.targetLanguage() + "»:\n" + text)
                .build();

        return ClaudeSupport.firstStructured(client, params);
    }
}
