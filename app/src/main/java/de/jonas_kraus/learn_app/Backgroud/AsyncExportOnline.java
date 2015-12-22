package de.jonas_kraus.learn_app.Backgroud;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import de.jonas_kraus.learn_app.activity.FileBrowserActivity;

/**
 * Created by Jonas on 02.11.2015.
 */
public class AsyncExportOnline extends AsyncTask<String, Integer, Void> {

    private Context context;
    private String pathToExportedFile, pathOnline;

    public boolean doExport(String pathToExportedFile, String pathOnline, Context context) {
        this.pathToExportedFile = pathToExportedFile;
        this.pathOnline = pathOnline;
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

    private String exportCategory() throws IOException, JSONException {
        /*@TODO export to json */
        String url = pathOnline;
        File file = new File(pathToExportedFile);
        return FileBrowserActivity.postJSON(url, file);
    }

}
