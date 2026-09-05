package com.yesset.telegram_bot.session;

/**
 * Пара «язык, который ученик уже знает» → «язык, который он изучает».
 * bridge — язык-мост (родной / знакомый), target — изучаемый язык.
 */
public enum LanguagePair {

    KK_RU("казахский", "русский", "🇰🇿 каз → 🇷🇺 рус"),
    RU_EN("русский", "английский", "🇷🇺 рус → 🇬🇧 англ"),
    KK_EN("казахский", "английский", "🇰🇿 каз → 🇬🇧 англ");

    private final String bridgeLanguage;
    private final String targetLanguage;
    private final String label;

    LanguagePair(String bridgeLanguage, String targetLanguage, String label) {
        this.bridgeLanguage = bridgeLanguage;
        this.targetLanguage = targetLanguage;
        this.label = label;
    }

    public String bridgeLanguage() {
        return bridgeLanguage;
    }

    public String targetLanguage() {
        return targetLanguage;
    }

    public String label() {
        return label;
    }
}
