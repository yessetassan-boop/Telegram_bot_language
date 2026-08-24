package com.yesset.telegram_bot.session;

public class UserSession {

    private SessionState state = SessionState.CHOOSING_MODE;
    private String kazakhPhrase;

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public String getKazakhPhrase() {
        return kazakhPhrase;
    }

    public void setKazakhPhrase(String kazakhPhrase) {
        this.kazakhPhrase = kazakhPhrase;
    }
}
