package de.jonas_kraus.learn_app.Backgroud;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;

/**
 * Created by Jonas on 02.11.2015.
 */
public class AsyncExport extends AsyncTask<String, Integer, Void> {

    private Catalogue catalogue;
    private DbManager db;
    private Context context;

    public boolean doExport(Catalogue catalogue, DbManager db, Context context) {
        this.catalogue = catalogue;
        this.db = db;
        this.context = context.getApplicationContext();
        execute();
        return true;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            exportCategory();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void exportCategory() throws IOException, JSONException {
        /*@TODO export to json */
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Exporting Cards");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();


        Thread mThread = new Thread() {
            @Override
            public void run() {

                catalogue.unsetIcon();
                Category category = catalogue.getCategory();
                List<Card> cards =  db.getCards(category);
                Gson gson = new Gson();
                //Log.d("gson", gson.toJson(curCatalogue)+" category_ "+ gson.toJson(category) + " cards_ " +  gson.toJson(cards)+ " dir "+ context.getFilesDir()+"");

                String filenameCat = "category.json";
                String filenameCards = "cards.json";
                String stringCards = gson.toJson(cards);
                String stringCat = gson.toJson(category);
                FileOutputStream outputStream;

                try {

                    File fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "/flashcards");
                    if (! fileNew.exists()){
                        Log.d("dir ", "file dosent exist");
                        if (! fileNew.mkdirs()){
                            Log.e("dir ", "Directory not created");
                        }
                    }
                    outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +"category.json"));
                    outputStream.write(stringCat.getBytes());
                    outputStream.close();
                    outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +"cards.json"));
                    outputStream.write(stringCards.getBytes());
                    outputStream.close();/*
                    outputStream = context.openFileOutput(filenameCards, Context.MODE_PRIVATE);
                    outputStream.write(stringCards.getBytes());
                    outputStream.close();*/
                    mProgressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
    }

}
