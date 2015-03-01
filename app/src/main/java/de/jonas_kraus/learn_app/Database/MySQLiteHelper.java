package de.jonas_kraus.learn_app.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Jonas on 26.02.2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper{

    public static final String TABLE_QUESTIONS = "questions";
    public static final String COLUMN_QUESTION_ID = "_id";
    public static final String COLUMN_QUESTION_TYPE = "type";
    public static final String COLUMN_QUESTION_QUESTION = "question";
    public static final String COLUMN_QUESTION_KNOWN = "known";
    public static final String COLUMN_QUESTION_RATING = "rating";
    public static final String COLUMN_QUESTION_HINT = "hint";
    public static final String COLUMN_QUESTION_CATEGORY_ID = "category_id";

    public static final String TABLE_ANSWERS = "answers";
    public static final String COLUMN_ANSWER_ID = "_id";
    public static final String COLUMN_ANSWER_QUESTION_ID = "question_id";
    public static final String COLUMN_ANSWER_IS_CORRECT = "is_correct";
    public static final String COLUMN_ANSWER_ANSWER = "answer";

    public static final String TABLE_CATEGORIES = "categorys";
    public static final String COLUMN_CATEGORY_ID = "_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_PARENT = "parent_id";

    private static final String DATABASE_NAME = "learning_cards_db.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String QUESTIONS_CREATE = "create table "
            + TABLE_QUESTIONS + "("
            + COLUMN_QUESTION_ID
            + " integer primary key autoincrement, "
            + COLUMN_QUESTION_TYPE
            + " enum not null, "
            + COLUMN_QUESTION_QUESTION
            + " text not null, "
            + COLUMN_QUESTION_KNOWN
            + " boolean DEFAULT 0, "
            + COLUMN_QUESTION_RATING
            + " integer DEFAULT 0"
            + COLUMN_QUESTION_HINT
            + " text"
            + COLUMN_QUESTION_CATEGORY_ID
            + " integer not null"
            + ");";

    // Database creation sql statement
    private static final String ANSWERS_CREATE = "create table "
            + TABLE_ANSWERS + "("
            + COLUMN_ANSWER_ID
            + " integer primary key autoincrement, "
            + COLUMN_ANSWER_QUESTION_ID
            + " integer, foreign key("+ COLUMN_ANSWER_QUESTION_ID +") REFERENCES "+TABLE_QUESTIONS+"("+COLUMN_QUESTION_ID+"), "
            + COLUMN_ANSWER_IS_CORRECT
            + " boolean not null, "
            + COLUMN_ANSWER_ANSWER
            + " text not null"
            + ");";

    // Database creation sql statement
    private static final String CATEGORY_CREATE = "create table "
            + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID
            + " integer primary key autoincrement, "
            + COLUMN_CATEGORY_NAME
            + " text not null unique"
            + COLUMN_CATEGORY_PARENT
            + " integer DEFAULT -1, "
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUESTIONS_CREATE);
        db.execSQL(ANSWERS_CREATE);
        db.execSQL(CATEGORY_CREATE);
        Log.d("created", "tabels");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
}

