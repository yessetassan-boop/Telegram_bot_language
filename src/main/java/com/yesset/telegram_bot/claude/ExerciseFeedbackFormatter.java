package com.yesset.telegram_bot.claude;

public final class ExerciseFeedbackFormatter {

    private ExerciseFeedbackFormatter() {
    }

    public static String toTelegramHtml(ExerciseFeedback feedback) {
        StringBuilder sb = new StringBuilder();

        sb.append(feedback.correct()
                ? "<b>✅ Верно!</b>"
                : "<b>❌ Есть неточности</b>");
        sb.append("\n\n");
        sb.append(escape(feedback.explanationKazakh()));
        sb.append("\n\n");
        sb.append("<b>Правильный вариант:</b>\n");
        sb.append("<code>").append(escape(feedback.correctRussianAnswer())).append("</code>");

        return sb.toString();
    }

    public static String formatTask(String kazakhPhrase) {
        return "<b>🎯 Переведи на русский:</b>\n<code>" + escape(kazakhPhrase) + "</code>";
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
