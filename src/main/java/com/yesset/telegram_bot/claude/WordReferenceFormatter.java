package com.yesset.telegram_bot.claude;

public final class WordReferenceFormatter {

    private WordReferenceFormatter() {
    }

    public static String toTelegramHtml(String word, WordReference reference) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>📖 ").append(escape(word)).append("</b>");
        String partOfSpeech = reference.partOfSpeech();
        if (partOfSpeech != null && !partOfSpeech.isBlank()) {
            sb.append(" <i>(").append(escape(partOfSpeech)).append(")</i>");
        }
        sb.append("\n\n");

        for (WordForm form : reference.forms()) {
            sb.append("• <code>").append(escape(form.form())).append("</code> — ")
                    .append(escape(form.explanationRussian()))
                    .append('\n');
        }

        String note = reference.note();
        if (note != null && !note.isBlank()) {
            sb.append("\n<b>💡 Примечание:</b>\n").append(escape(note));
        }

        return sb.toString().stripTrailing();
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
