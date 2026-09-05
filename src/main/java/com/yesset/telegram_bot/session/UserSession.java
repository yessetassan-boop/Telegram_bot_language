package com.yesset.telegram_bot.session;

import com.yesset.telegram_bot.claude.Decoding;

public class UserSession {

    private SessionState state = SessionState.CHOOSING_MODE;
    private LanguagePair languagePair = LanguagePair.KK_RU;

    /** Последний расшифрованный текст на изучаемом языке. */
    private String lastText;
    /** Результат последней расшифровки — для повторного показа и как эталон в тренировке. */
    private Decoding lastDecoding;

    /** Фраза на языке-мосте, которую ученик должен сказать на изучаемом языке. */
    private String productionSource;
    /** Необязательный эталон на изучаемом языке (например, исходный текст расшифровки). */
    private String productionReference;

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public LanguagePair getLanguagePair() {
        return languagePair;
    }

    public void setLanguagePair(LanguagePair languagePair) {
        this.languagePair = languagePair;
    }

    public String getLastText() {
        return lastText;
    }

    public void setLastText(String lastText) {
        this.lastText = lastText;
    }

    public Decoding getLastDecoding() {
        return lastDecoding;
    }

    public void setLastDecoding(Decoding lastDecoding) {
        this.lastDecoding = lastDecoding;
    }

    public String getProductionSource() {
        return productionSource;
    }

    public void setProductionSource(String productionSource) {
        this.productionSource = productionSource;
    }

    public String getProductionReference() {
        return productionReference;
    }

    public void setProductionReference(String productionReference) {
        this.productionReference = productionReference;
    }

    /** Сбрасывает ход диалога, но сохраняет выбранную языковую пару. */
    public void resetConversation() {
        this.state = SessionState.CHOOSING_MODE;
        this.lastText = null;
        this.lastDecoding = null;
        this.productionSource = null;
        this.productionReference = null;
    }
}
