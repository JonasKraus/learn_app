package de.jonas_kraus.learn_app.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

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
    public static final String COLUMN_QUESTION_DRAWER = "drawer";
    public static final String COLUMN_QUESTION_DATE = "date";
    public static final String COLUMN_QUESTION_MARKED = "marked";


    public static final String TABLE_ANSWERS = "answers";
    public static final String COLUMN_ANSWER_ID = "_id";
    public static final String COLUMN_ANSWER_QUESTION_ID = "question_id";
    public static final String COLUMN_ANSWER_IS_CORRECT = "is_correct";
    public static final String COLUMN_ANSWER_ANSWER = "answer";

    public static final String TABLE_CATEGORIES = "categorys";
    public static final String COLUMN_CATEGORY_ID = "_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_PARENT = "parent_id";
    public static final String COLUMN_CATEGORY_MARKED = "marked";


    public static final String TABLE_MARKED = "marked";
    public static final String COLUMN_MARKED_ID = "_id";
    public static final String COLUMN_MARKED_QUESTION_ID = "question_id";

    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_SETTINGS_SHOW_HINT = "show_hint";
    public static final String COLUMN_SETTINGS_SHOW_BAR = "show_known_bar";
    public static final String COLUMN_SETTINGS_MULTIPLECHOICE_CHANGE_ANSWER_ORDER = "multiple_choice_change_answer_order";
    public static final String COLUMN_SETTINGS_VIEW_RANDOM_CARDS = "view_random_cards";
    public static final String COLUMN_SETTINGS_VIEW_LAST_DRAWER_CARDS = "view_last_drawer_cards";
    public static final String COLUMN_SETTINGS_NIGHT_MODE = "night_mode";
    public static final String COLUMN_SETTINGS_CARDS_ORDER = "cards_order";

    private static final String DATABASE_NAME = "learning_cards_db.db";
    private static final int DATABASE_VERSION = 9;

    // Database creation sql statement
    private static final String QUESTIONS_CREATE = "create table "
            + TABLE_QUESTIONS + "("
            + COLUMN_QUESTION_ID
            + " integer primary key autoincrement, "
            + COLUMN_QUESTION_TYPE
            + " text not null, "
            + COLUMN_QUESTION_QUESTION
            + " text not null, "
            + COLUMN_QUESTION_KNOWN
            + " integer DEFAULT 0, "
            + COLUMN_QUESTION_RATING
            + " integer DEFAULT 0, "
            + COLUMN_QUESTION_HINT
            + " text, "
            + COLUMN_QUESTION_CATEGORY_ID
            + " integer not null, "
            + COLUMN_QUESTION_DRAWER
            + " integer DEFAULT 0, "
            + COLUMN_QUESTION_DATE
            + " datetime DEFAULT now, "
            + COLUMN_QUESTION_MARKED
            + " ineteger DEFAULT 0 "
            + ");";

    // Database creation sql statement
    private static final String ANSWERS_CREATE = "create table "
            + TABLE_ANSWERS + "("
            + COLUMN_ANSWER_ID
            + " integer primary key autoincrement, "
            + COLUMN_ANSWER_QUESTION_ID
            + " integer, "
            + COLUMN_ANSWER_IS_CORRECT
            + " integer not null, "
            + COLUMN_ANSWER_ANSWER
            + " text not null, FOREIGN  KEY("+ COLUMN_ANSWER_QUESTION_ID +") REFERENCES "+TABLE_QUESTIONS+"("+COLUMN_QUESTION_ID+") ON DELETE CASCADE "
            + ");";

    // Database creation sql statement
    private static final String CATEGORY_CREATE = "create table "
            + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID
            + " integer primary key autoincrement, "
            + COLUMN_CATEGORY_PARENT
            + " integer DEFAULT "+(-1)+", "
            + COLUMN_CATEGORY_NAME
            + " text not null, " /* @TODO Check if the category has the same name and parent id, then UNIQUE?! */
            + COLUMN_CATEGORY_MARKED
            + " ineteger DEFAULT 0 "
            + ");";

    // Database creation sql statement
    private static final String MARKED_CREATE = "create table "
            + TABLE_MARKED + "("
            + COLUMN_MARKED_ID
            + " integer primary key autoincrement, "
            + COLUMN_MARKED_QUESTION_ID
            + " integer "
            //+ "FOREIGN  KEY("+ COLUMN_MARKED_QUESTION_ID +") REFERENCES "+TABLE_QUESTIONS+"("+COLUMN_QUESTION_ID+") ON DELETE CASCADE "
            + ");";

    // Database creation sql statement
    private static final String SETTINGS_CREATE = "create table "
            + TABLE_SETTINGS + "("
            + COLUMN_SETTINGS_SHOW_HINT
            + " ineteger DEFAULT 1, "
            + COLUMN_SETTINGS_SHOW_BAR
            + " ineteger DEFAULT 1, "
            + COLUMN_SETTINGS_MULTIPLECHOICE_CHANGE_ANSWER_ORDER
            + " ineteger DEFAULT 0, "
            + COLUMN_SETTINGS_VIEW_RANDOM_CARDS
            + " ineteger DEFAULT 0, "
            + COLUMN_SETTINGS_VIEW_LAST_DRAWER_CARDS
            + " ineteger DEFAULT 1, "
            + COLUMN_SETTINGS_NIGHT_MODE
            + " ineteger DEFAULT 0, "
            + COLUMN_SETTINGS_CARDS_ORDER
            + " ineteger DEFAULT 0"
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUESTIONS_CREATE);
        db.execSQL(ANSWERS_CREATE);
        db.execSQL(CATEGORY_CREATE);
        db.execSQL(SETTINGS_CREATE);
        //db.execSQL(MARKED_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }
}

