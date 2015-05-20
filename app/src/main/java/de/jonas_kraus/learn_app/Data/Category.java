package de.jonas_kraus.learn_app.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonas on 01.03.2015.
 */
public class Category implements Parcelable{
    private int id;
    private int parentId;
    private String name;
    private boolean marked;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(parentId);
        dest.writeString(name);
        dest.writeByte((byte) (marked ? 1 : 0));
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel parcel) {
            return new Category(parcel);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public Category(Parcel parcel) {
        this.id = parcel.readInt();
        this.parentId = parcel.readInt();
        this.name = parcel.readString();
        this.marked = parcel.readByte() != 0;
    }

    public Category(int id, int parentId, String name, boolean marked) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.marked = marked;
    }
    public Category(int parent_id, String name) {
        this.parentId = parent_id;
        this.name = name;
        this.marked = false;
    }

    public Category(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public String toString() {
        return "Category: \n\t" + name;
    }
}
