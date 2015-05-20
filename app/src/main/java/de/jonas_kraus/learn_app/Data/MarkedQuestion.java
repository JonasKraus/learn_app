package de.jonas_kraus.learn_app.Data;

/**
 * Created by Jonas on 16.03.2015.
 */
public class MarkedQuestion {
    private int id;
    private int question_id;

    public MarkedQuestion(int id, int question_id) {
        this.id = id;
        this.question_id = question_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    @Override
    public String toString() {
        return "MarkedQuestion{" +
                "id=" + id +
                ", question_id=" + question_id +
                '}';
    }
}








