package com.yesset.telegram_bot.claude;

public final class TranslationFeedbackFormatter {

    private TranslationFeedbackFormatter() {
    }

    public static String toTelegramHtml(TranslationFeedback feedback) {
        StringBuilder sb = new StringBuilder();

        sb.append(feedback.translationCorrect()
                ? "<b>✅ Перевод верный!</b>"
                : "<b>❌ В переводе есть неточности</b>");
        sb.append("\n\n");

        sb.append("<b>📝 Түсіндірме:</b>\n");
        sb.append("<i>").append(escape(feedback.explanationKazakh())).append("</i>");
        sb.append("\n\n");

        sb.append("<b>🗣 Как сказал бы носитель:</b>\n");
        sb.append("<code>").append(escape(feedback.nativeRussianVariant())).append("</code>");

        String kazakhNote = feedback.kazakhPhraseNote();
        if (kazakhNote != null && !kazakhNote.isBlank()) {
            sb.append("\n\n");
            sb.append("<b>⚠️ Про твою казахскую фразу:</b>\n");
            sb.append(escape(kazakhNote));
        }

        return sb.toString();
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
