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
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_QUESTION = "question";

    public static final String TABLE_ANSWERS = "answers";
    public static final String COLUMN_ANSWER_ID = "_id";
    public static final String COLUMN_QUESTION_REFERENCE_ID = "question_id";
    public static final String COLUMN_IS_CORRECT = "is_correct";
    public static final String COLUMN_ANSWER = "answer";

    private static final String DATABASE_NAME = "learning_cards_db.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String QUESTIONS_CREATE = "create table "
            + TABLE_QUESTIONS + "("
            + COLUMN_QUESTION_ID
            + " integer primary key autoincrement, "
            + COLUMN_TYPE
            + " enum not null, "
            +COLUMN_QUESTION
            + " text not null"
            + ");";

    // Database creation sql statement
    private static final String ANSWERS_CREATE = "create table "
            + TABLE_ANSWERS + "("
            + COLUMN_ANSWER_ID
            + " integer primary key autoincrement, "
            + COLUMN_QUESTION_REFERENCE_ID
            + " integer, foreign key("+ COLUMN_QUESTION_REFERENCE_ID+") REFERENCES "+TABLE_QUESTIONS+"("+COLUMN_QUESTION_ID+"), "
            + COLUMN_IS_CORRECT
            + " boolean not null, "
            +COLUMN_ANSWER
            + " text not null"
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUESTIONS_CREATE);
        db.execSQL(ANSWERS_CREATE);
        Log.d("created tables", "tabels");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        onCreate(db);
    }
}

