package de.jonas_kraus.learn_app.Data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import de.jonas_kraus.learn_app.R;

/**
 * Created by Jonas on 02.03.2015.
 */
public class Catalogue implements Parcelable {
    private Card card;
    private Category category;
    private Bitmap icon;

    public static final int CARDS_THRASHOLD = 1;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(card, flags);
        dest.writeParcelable(category, flags);
        dest.writeParcelable(icon, flags);
    }

    public static final Parcelable.Creator<Catalogue> CREATOR = new Parcelable.Creator<Catalogue>() {
        public Catalogue createFromParcel(Parcel parcel) {
            return new Catalogue(parcel);
        }

        public Catalogue[] newArray(int size) {
            return new Catalogue[size];
        }
    };

    public Catalogue(Parcel parcel) {
        this.card = parcel.readParcelable(Card.class.getClassLoader());
        this.category = parcel.readParcelable(Category.class.getClassLoader());
        this.icon = parcel.readParcelable(Bitmap.class.getClassLoader());
    }

    public Catalogue(Card card, Category category) {
        this.card = card;
        this.category = category;
    }
    public Catalogue(Category category, Bitmap icon) {
        this.category = category;
        this.icon = icon;
    }
    public Catalogue(Card card, Bitmap icon) {
        this.card = card;
        this.icon = icon;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        if (card == null) {
            return category.toString();
        } else {
            return card.toString();
        }
    }

    /**
     * unsets the icon
     * used for json output
     */
    public void unsetIcon() {
        this.icon = null;
    }

    public void setMark(Boolean bool) {
        if (this.card != null) {
            this.card.setMarked(bool);
        }
        if (this.category != null) {
            this.category.setMarked(bool);
        }
    }
}
