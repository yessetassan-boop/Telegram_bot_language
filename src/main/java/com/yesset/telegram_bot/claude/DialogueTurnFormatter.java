package com.yesset.telegram_bot.claude;

public final class DialogueTurnFormatter {

    private DialogueTurnFormatter() {
    }

    public static String toTelegramHtml(DialogueTurn turn) {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(turn.replyRussian()));

        String correction = turn.correctionKazakh();
        if (correction != null && !correction.isBlank()) {
            sb.append("\n\n<i>💡 ").append(escape(correction)).append("</i>");
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
