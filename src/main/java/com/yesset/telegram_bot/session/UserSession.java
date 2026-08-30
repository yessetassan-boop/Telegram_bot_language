package com.yesset.telegram_bot.session;

import com.yesset.telegram_bot.claude.Homework;
import com.yesset.telegram_bot.claude.HomeworkTask;

import java.util.ArrayList;
import java.util.List;

public class UserSession {

    private SessionState state = SessionState.CHOOSING_MODE;
    private String kazakhPhrase;

    private Homework homework;
    private int currentTaskIndex;
    private int correctCount;
    private final List<String> missedFocuses = new ArrayList<>();

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

    public void startHomework(Homework homework) {
        this.homework = homework;
        this.currentTaskIndex = 0;
        this.correctCount = 0;
        this.missedFocuses.clear();
    }

    public HomeworkTask currentTask() {
        return homework.tasks().get(currentTaskIndex);
    }

    public int taskNumber() {
        return currentTaskIndex + 1;
    }

    public int totalTasks() {
        return homework.tasks().size();
    }

    public boolean hasMoreTasks() {
        return homework != null && currentTaskIndex < homework.tasks().size();
    }

    public void recordResult(boolean correct, String grammarFocus) {
        if (correct) {
            correctCount++;
        } else if (grammarFocus != null && !grammarFocus.isBlank()) {
            missedFocuses.add(grammarFocus);
        }
        currentTaskIndex++;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public List<String> getMissedFocuses() {
        return missedFocuses;
    }
}
