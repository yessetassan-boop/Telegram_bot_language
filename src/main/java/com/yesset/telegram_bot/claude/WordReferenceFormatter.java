package com.yesset.telegram_bot.claude;

public final class WordReferenceFormatter {

    private WordReferenceFormatter() {
    }

    public static String toTelegramHtml(String word, WordReference reference) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>\ud83d\udcd6 ").append(escape(word)).append("</b>");
        String partOfSpeech = reference.partOfSpeech();
        if (partOfSpeech != null && !partOfSpeech.isBlank()) {
            sb.append(" <i>(").append(escape(partOfSpeech)).append(")</i>");
        }
        sb.append("\n\n");

        if (reference.relatedWords() != null && !reference.relatedWords().isEmpty()) {
            sb.append("<b>\ud83d\udd24 \u041e\u0434\u043d\u043e\u043a\u043e\u0440\u0435\u043d\u043d\u044b\u0435 \u0441\u043b\u043e\u0432\u0430:</b>\n");
            for (WordForm related : reference.relatedWords()) {
                sb.append("\u2022 <code>").append(escape(related.form())).append("</code> — ")
                        .append(escape(related.explanationKazakh()))
                        .append('\n');
            }
            sb.append('\n');
        }

        sb.append("<b>\ud83d\udccb \u0424\u043e\u0440\u043c\u044b \u0441\u043b\u043e\u0432\u0430:</b>\n");
        for (WordForm form : reference.forms()) {
            sb.append("\u2022 <code>").append(escape(form.form())).append("</code> — ")
                    .append(escape(form.explanationKazakh()))
                    .append('\n');
        }

        String note = reference.note();
        if (note != null && !note.isBlank()) {
            sb.append("\n<b>\ud83d\udca1 \u041f\u0440\u0438\u043c\u0435\u0447\u0430\u043d\u0438\u0435:</b>\n").append(escape(note));
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