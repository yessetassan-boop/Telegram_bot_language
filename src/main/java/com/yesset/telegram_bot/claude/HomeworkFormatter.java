package com.yesset.telegram_bot.claude;

import java.util.List;

public final class HomeworkFormatter {

    private HomeworkFormatter() {
    }

    public static String renderTask(int number, int total, HomeworkTask task) {
        return "<b>Задание " + number + "/" + total + "</b>\n"
                + "🎯 <i>" + escape(task.grammarFocusKazakh()) + "</i>\n\n"
                + "Переведи на русский:\n"
                + "<b>" + escape(task.promptKazakh()) + "</b>";
    }

    public static String renderFeedback(HomeworkTaskCheck check) {
        StringBuilder sb = new StringBuilder();

        sb.append(check.correct() ? "<b>✅ Верно!</b>" : "<b>❌ Есть ошибка</b>");
        sb.append(" <i>(").append(clampScore(check.scorePercent())).append("%)</i>\n\n");

        sb.append("<b>📝 Түсіндірме:</b>\n");
        sb.append("<i>").append(escape(check.explanationKazakh())).append("</i>\n\n");

        sb.append("<b>🗣 Как сказал бы носитель:</b>\n");
        sb.append("<code>").append(escape(check.correctedAnswerRussian())).append("</code>");

        String tip = check.tipKazakh();
        if (tip != null && !tip.isBlank()) {
            sb.append("\n\n<b>💡 Кеңес:</b>\n").append(escape(tip));
        }
        return sb.toString();
    }

    public static String renderSkip(HomeworkTask task) {
        return "<b>⏭ Задание пропущено.</b>\n\n"
                + "<b>🗣 Эталон:</b>\n<code>" + escape(task.referenceAnswerRussian()) + "</code>\n\n"
                + "<b>💡 Подсказка была:</b>\n" + escape(task.hintKazakh());
    }

    public static String renderSummary(int correct, int total, List<String> missedFocuses) {
        StringBuilder sb = new StringBuilder();

        sb.append("<b>🏁 Домашка завершена!</b>\n\n");
        sb.append("Результат: <b>").append(correct).append(" / ").append(total).append("</b>");

        if (!missedFocuses.isEmpty()) {
            sb.append("\n\n<b>Над чем поработать:</b>");
            for (String focus : missedFocuses) {
                sb.append("\n• ").append(escape(focus));
            }
        } else if (correct == total) {
            sb.append("\n\nВсё верно — отличная работа! 🎉");
        }
        return sb.toString();
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
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
