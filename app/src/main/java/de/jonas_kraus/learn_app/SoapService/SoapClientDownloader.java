package de.jonas_kraus.learn_app.SoapService;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Util.CustomListFileBrowser;
import de.jonas_kraus.learn_app.activity.FileBrowserActivity;
import de.jonas_kraus.learn_app.activity.ImportActivity;

/**
 * Created by Jonas on 28.10.2015.
 */
public class SoapClientDownloader extends AsyncTask<String, String, String> {

    String SOAP_ACTION = "http://www.jonas-kraus.de/flashcards/service/";
    String NAMESPACE = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    String METHOD_NAME = "";
    String URL = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    String resultData = "";
    private String path = "";
    private FileBrowserActivity fileBrowserActivity;
    private int currentCategoryParent = -1;

    public SoapClientDownloader(FileBrowserActivity fileBrowserActivity) {
        this.fileBrowserActivity = fileBrowserActivity;
    }

    public void downloadJson(String file, int currentCategoryParent) {
        this.currentCategoryParent = currentCategoryParent;
        this.execute("downloadJson",file);
    }

    @Override
    protected String doInBackground(String... strings) {

        METHOD_NAME = strings[0];
        SOAP_ACTION += METHOD_NAME;

        SoapSerializationEnvelope soapEnv = new SoapSerializationEnvelope(SoapEnvelope.VER12);

        HttpTransportSE http = getHttpTransportSE();

        String results = null;

        SoapObject soapObject = new SoapObject(null,METHOD_NAME);

        path += "/"+strings[1];

        Log.d("soap path", path);
        soapObject.addProperty("subdir", path);
        soapEnv.setOutputSoapObject(soapObject);

        try {
            http.call(SOAP_ACTION, soapEnv);
            //SoapPrimitive response = (SoapPrimitive) soapEnv.getResponse();
            results = soapEnv.bodyIn.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        if(results != null)
            resultData = results;

        return results;
    }
    private final HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY,URL,60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

    protected void onPostExecute(String result) {
        result = result.replace("downloadJsonResponse{result=return; return=", "");
        result = result.substring(0,result.length()-3);
        if (result.startsWith("[")) {
            //result = result.substring(1,result.length()-1);
        }
        Log.d("soap Download card", result);
        FileOutputStream outputStream;
        try {

            File fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "/flashcards/.cache");
            if (! fileNew.exists()){
                Log.d("dir ", "file dosent exist");
                if (! fileNew.mkdirs()){
                    Log.e("dir ", "Directory not created");
                }
            }
            outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +"Download.json"));
            outputStream.write(result.getBytes());
            outputStream.close();
            //outputStream = fileBrowserActivity.openFileOutput(result, Context.MODE_PRIVATE);
            //outputStream.write(result.getBytes());
            //outputStream.close();
            Intent myIntent = new Intent(fileBrowserActivity, ImportActivity.class);
            myIntent.putExtra("currentCategoryParent", currentCategoryParent);
            myIntent.putExtra("path", fileNew.getPath() + File.separator+"Download.json");
            fileBrowserActivity.startActivity(myIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(fileBrowserActivity.getApplicationContext(),"Something went wrong while Downloading Cards",Toast.LENGTH_LONG).show();
        }
        // save file
        //listAdapter.set(adapter);
    }
}
