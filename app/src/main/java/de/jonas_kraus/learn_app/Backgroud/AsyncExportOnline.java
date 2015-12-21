package de.jonas_kraus.learn_app.Backgroud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
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
