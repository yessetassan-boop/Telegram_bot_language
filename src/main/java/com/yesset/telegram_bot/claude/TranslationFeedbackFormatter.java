package com.yesset.telegram_bot.claude;

public final class TranslationFeedbackFormatter {

    private TranslationFeedbackFormatter() {
    }

    public static String toTelegramHtml(TranslationFeedback feedback) {
        StringBuilder sb = new StringBuilder();

        sb.append(feedback.translationCorrect()
                ? "<b>\u2705 \u041f\u0435\u0440\u0435\u0432\u043e\u0434 \u0432\u0435\u0440\u043d\u044b\u0439!</b>"
                : "<b>\u274c \u0412 \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0435 \u0435\u0441\u0442\u044c \u043d\u0435\u0442\u043e\u0447\u043d\u043e\u0441\u0442\u0438</b>");
        sb.append("\n\n");

        sb.append("<b>\ud83d\udcdd \u0422\u04af\u0441\u0456\u043d\u0434\u0456\u0440\u043c\u0435:</b>\n");
        sb.append("<i>").append(escape(feedback.explanationKazakh())).append("</i>");
        sb.append("\n\n");

        sb.append("<b>\ud83d\udde3 \u041a\u0430\u043a \u0441\u043a\u0430\u0437\u0430\u043b \u0431\u044b \u043d\u043e\u0441\u0438\u0442\u0435\u043b\u044c:</b>\n");
        sb.append("<code>").append(escape(feedback.nativeRussianVariant())).append("</code>");

        String russianNote = feedback.russianPhraseNote();
        if (russianNote != null && !russianNote.isBlank()) {
            sb.append("\n\n");
            sb.append("<b>\u26a0\ufe0f \u041f\u0440\u043e \u0442\u0432\u043e\u044e \u0440\u0443\u0441\u0441\u043a\u0443\u044e \u0444\u0440\u0430\u0437\u0443:</b>\n");
            sb.append(escape(russianNote));
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