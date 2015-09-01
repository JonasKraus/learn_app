package de.jonas_kraus.learn_app.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Jonas on 26.02.2015.
 */
public class Card implements Parcelable{



    public enum CardType {MULTIPLECHOICE, NOTECARD};
    private int id;
    private CardType type;
    private String question;
    private List<Answer> answers;
    private boolean known;
    private int rating;
    private String hint;
    private int categoryId;
    private int drawer;
    private boolean marked;
    private Timestamp viewed;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type.toString());
        dest.writeString(question);
        dest.writeList(answers);
        dest.writeByte((byte) (known ? 1 : 0));
        dest.writeInt(rating);
        dest.writeString(hint);
        dest.writeInt(categoryId);
        dest.writeInt(drawer);
        dest.writeByte((byte) (marked ? 1 : 0));
        dest.writeString(viewed.toString());
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        public Card createFromParcel(Parcel parcel) {
            return new Card(parcel);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    private Card(Parcel parcel) {
        this.id = parcel.readInt();
        this.type = CardType.valueOf(parcel.readString());
        this.question = parcel.readString();
        //List<Answer>answers = new ArrayList<Answer>();
        this.answers = parcel.readArrayList(Answer.class.getClassLoader());
        this.known = parcel.readByte() != 0;
        this.rating = parcel.readInt();
        this.hint = parcel.readString();
        this.categoryId = parcel.readInt();
        this.drawer = parcel.readInt();
        this.marked = parcel.readByte() != 0;
        this.viewed = Timestamp.valueOf(parcel.readString());
    }

    public Card(int id, CardType type, String question, List<Answer> answers, boolean known, int rating, String hint, int categoryId, int drawer, boolean marked, Timestamp timestamp) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.answers = answers;
        this.known = known;
        this.rating = rating;
        this.hint = hint;
        this.categoryId = categoryId;
        this.drawer = drawer;
        this.marked = marked;
        this.viewed = timestamp;
    }

    public Card(CardType type, String question, List<Answer> answers, boolean known, int rating, String hint, int categoryId) {
        this.type = type;
        this.question = question;
        this.answers = answers;
        this.known = known;
        this.rating = rating;
        this.hint = hint;
        this.categoryId = categoryId;
        this.drawer = 0;
        this.marked = false;
        this.viewed = null;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDrawer() {
        return drawer;
    }

    public void setDrawer(int drawer) {
        this.drawer = drawer;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public Timestamp getViewed() {
        return viewed;
    }

    public void setViewed(Timestamp viewed) {
        this.viewed = viewed;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", type=" + type +
                ", question='" + question + '\'' +
                ", answers=" + answers +
                ", known=" + known +
                ", rating=" + rating +
                ", hint='" + hint + '\'' +
                ", categoryId=" + categoryId +
                ", drawer=" + drawer +
                ", marked=" + marked +
                '}';
    }
}
