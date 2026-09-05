package com.yesset.telegram_bot.claude;

public final class TranslationFeedbackFormatter {

    private TranslationFeedbackFormatter() {
    }

    public static String toTelegramHtml(TranslationFeedback feedback) {
        StringBuilder sb = new StringBuilder();

        sb.append(feedback.translationCorrect()
                ? "<b>✅ Верно!</b>"
                : "<b>❌ Есть ошибки</b>");
        sb.append("\n\n");

        sb.append("<b>📝 Разбор:</b>\n<i>")
                .append(escape(feedback.explanationBridge()))
                .append("</i>\n\n");

        sb.append("<b>🗣 Как сказал бы носитель:</b>\n<code>")
                .append(escape(feedback.nativeVariant()))
                .append("</code>");

        String tip = feedback.tipBridge();
        if (tip != null && !tip.isBlank()) {
            sb.append("\n\n<b>💡 Совет:</b>\n").append(escape(tip));
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
