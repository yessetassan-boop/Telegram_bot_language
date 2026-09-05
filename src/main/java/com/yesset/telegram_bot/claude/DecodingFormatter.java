package com.yesset.telegram_bot.claude;

public final class DecodingFormatter {

    private DecodingFormatter() {
    }

    public static String toTelegramHtml(String originalText, Decoding decoding) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>📖 Расшифровка</b>\n\n");

        sb.append("<b>Оригинал:</b>\n").append(escape(originalText)).append("\n\n");

        sb.append("<b>Слово за словом:</b>\n");
        if (decoding.wordByWord() != null) {
            for (DecodedWord word : decoding.wordByWord()) {
                sb.append("• <code>").append(escape(word.original())).append("</code> — ")
                        .append(escape(word.literal()))
                        .append('\n');
            }
        }

        sb.append("\n<b>Живой перевод:</b>\n<i>")
                .append(escape(decoding.fluentTranslation()))
                .append("</i>");

        if (decoding.grammarNotes() != null && !decoding.grammarNotes().isEmpty()) {
            sb.append("\n\n<b>🎯 Грамматика:</b>\n");
            int i = 1;
            for (GrammarNote note : decoding.grammarNotes()) {
                sb.append("\n<b>").append(i++).append(". ").append(escape(note.title())).append("</b>\n")
                        .append(escape(note.explanation()))
                        .append('\n');
            }
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
