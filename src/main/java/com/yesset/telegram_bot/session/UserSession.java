package com.yesset.telegram_bot.session;

import java.util.ArrayList;
import java.util.List;

public class UserSession {

    private static final int MAX_DIALOGUE_HISTORY = 6;

    private SessionState state = SessionState.CHOOSING_MODE;
    private String russianPhrase;
    private String exercisePhrase;
    private final List<DialogueExchange> dialogueHistory = new ArrayList<>();

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public String getRussianPhrase() {
        return russianPhrase;
    }

    public void setRussianPhrase(String russianPhrase) {
        this.russianPhrase = russianPhrase;
    }

    public String getExercisePhrase() {
        return exercisePhrase;
    }

    public void setExercisePhrase(String exercisePhrase) {
        this.exercisePhrase = exercisePhrase;
    }

    public List<DialogueExchange> getDialogueHistory() {
        return dialogueHistory;
    }

    public void addDialogueExchange(String userMessage, String assistantReply) {
        dialogueHistory.add(new DialogueExchange(userMessage, assistantReply));
        while (dialogueHistory.size() > MAX_DIALOGUE_HISTORY) {
            dialogueHistory.remove(0);
        }
    }

    public void clearDialogueHistory() {
        dialogueHistory.clear();
    }
}
