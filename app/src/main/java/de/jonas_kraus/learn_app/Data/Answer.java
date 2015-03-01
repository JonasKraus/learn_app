package de.jonas_kraus.learn_app.Data;

/**
 * Created by Jonas on 01.03.2015.
 */
public class Answer {
    private boolean correct;
    private String answer;

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Answer(boolean correct, String answer) {

        this.correct = correct;
        this.answer = answer;
    }
}
