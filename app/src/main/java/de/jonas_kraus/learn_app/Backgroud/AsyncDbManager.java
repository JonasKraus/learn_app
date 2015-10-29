package de.jonas_kraus.learn_app.Backgroud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.Database.MySQLiteHelper;

/**
 * Created by Jonas on 29.10.2015.
 */
public class AsyncDbManager extends AsyncTask<String, Integer, Integer> {

    private SQLiteDatabase database;
    private ContentValues values;
    private List<Answer> answers;
    private Context context;

    public AsyncDbManager(SQLiteDatabase database, ContentValues values, List<Answer> answers, Context context) {
        this.database = database;
        this.values = values;
        this.answers = answers;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        Log.d("is database open", "" + database.isOpen());
        int questionId =  (int)database.insert(strings[0], null, values);
        Log.d("is database open", "" + database.isOpen());
        if (answers != null) {
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_ANSWER_QUESTION_ID, questionId);
            for (Answer answer : answers) {
                answer.setQuestionId((int) questionId);
                values.put(MySQLiteHelper.COLUMN_ANSWER_IS_CORRECT, answer.isCorrect());
                values.put(MySQLiteHelper.COLUMN_ANSWER_ANSWER, answer.getAnswer());
                MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(context);
                database = mySQLiteHelper.getWritableDatabase();
                //database.open();
                Log.d("is database open", "" + database.isOpen());
                database.insert(MySQLiteHelper.TABLE_ANSWERS, null, values);
            }
        }
        return questionId;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

}
