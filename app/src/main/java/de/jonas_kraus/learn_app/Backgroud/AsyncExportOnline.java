package de.jonas_kraus.learn_app.Backgroud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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

    private void exportCategory() throws IOException, JSONException {
        /*@TODO export to json */
        /*
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Exporting Cards");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        */


        Thread mThread = new Thread() {
            @Override
            public void run() {
                String url = pathOnline;
                File file = new File(pathToExportedFile);
                Log.d("Export Online", "hier "+file.getPath()+" existiert: "+file.exists());

                HttpClient httpclient = new DefaultHttpClient();

                try {

                    HttpPost httppost = new HttpPost("http://www.jonas-kraus.de/flashcards/database/json"+ URLEncoder.encode(url, "UTF-8"));
                    Log.d("export http post",httppost.getURI().toString()+"\n"+httppost.getAllHeaders().toString());

                    MultipartEntity entity = new MultipartEntity();

                    entity.addPart("type", new StringBody("application/json"));
                    entity.addPart("data", new FileBody(file));
                    httppost.setEntity(entity);
                    HttpResponse response = httpclient.execute(httppost);

                    Log.d("server response export", response.getStatusLine().toString());
                } catch (ClientProtocolException e) {
                    Log.d("export error", e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.delete();
                Log.d("export","cache deleted");
            }
        };
        mThread.start();
        //mProgressDialog.dismiss();
    }

}
