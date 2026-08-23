package com.yesset.telegram_bot.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.yesset.telegram_bot.session.DialogueExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClaudeDialoguePartner {

    private static final String SYSTEM_PROMPT = """
            Ты — дружелюбный собеседник, который помогает носителю казахского языка практиковать
            разговорный русский. Веди простой, живой диалог на русском языке (уровень A1-A2):
            короткие предложения, повседневные темы, в конце своей реплики обычно задавай
            уточняющий вопрос, чтобы разговор продолжался.

            Если в последнем сообщении пользователя есть грамматические или лексические ошибки —
            мягко и коротко поясни их НА КАЗАХСКОМ ЯЗЫКЕ в correctionKazakh (как лучше было сказать
            и почему), не прерывая при этом естественный ход беседы в replyRussian. Если ошибок нет —
            оставь correctionKazakh пустой строкой.
            """;

    private final AnthropicClient client;

    public ClaudeDialoguePartner(@Value("${anthropic.api-key}") String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    public DialogueTurn respond(List<DialogueExchange> history, String userMessage) {
        MessageCreateParams.Builder baseBuilder = MessageCreateParams.builder()
                .model("claude-opus-5")
                .maxTokens(768L)
                .system(SYSTEM_PROMPT);

        for (DialogueExchange exchange : history) {
            baseBuilder.addUserMessage(exchange.userMessage());
            baseBuilder.addAssistantMessage(exchange.assistantReply());
        }

        StructuredMessageCreateParams<DialogueTurn> params = baseBuilder
                .outputConfig(DialogueTurn.class)
                .addUserMessage(userMessage)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(textBlock -> textBlock.text())
                .orElseThrow(() -> new IllegalStateException("Claude вернул пустой ответ"));
    }
}
