package de.jonas_kraus.learn_app.SoapService;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Util.CustomListFileBrowser;
import de.jonas_kraus.learn_app.activity.FileBrowserActivity;
import de.jonas_kraus.learn_app.activity.ImportActivity;

/**
 * Created by Jonas on 15.10.2015.
 */
public class SoapClientBrowser extends AsyncTask<String,  List<String>, List<String>> {
    String SOAP_ACTION = "http://www.jonas-kraus.de/flashcards/service/";
    String NAMESPACE = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    String METHOD_NAME = "";
    String URL = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    String resultData = "";
    List<String> resultList;
    private Context context;
    private ListActivity listActivity;
    private String path = "";

    public SoapClientBrowser(Context context, ListActivity listActivity) {
        this.context = context;
        this.listActivity = listActivity;
    }

    public List<String> scanForJson(String dir) {
        this.execute("scanForJson",dir);
        return resultList;
    }

    @Override
    protected List<String> doInBackground(String... strings) {

        METHOD_NAME = strings[0];
        SOAP_ACTION += METHOD_NAME;

        List<String> fileList = new ArrayList<String>();
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
            Vector<String> response = (Vector)soapEnv.getResponse();
            Log.d("soap response", results);
            Object[] arr = response.toArray();
            Vector vec2 = (Vector)arr[1];
            Object[] arr2 = vec2.toArray();

            for (Object obj:arr2) {
               // Log.d("Soap response", obj.toString());
                if (!obj.toString().equals(".") && !obj.toString().equals(".."))
                fileList.add(obj.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        if(results != null)
            resultData = results;

        for (String obj:fileList) {
            Log.d("List file:", obj.toString());
        }
        resultList = fileList;
        return fileList;
    }
    private final HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY,URL,60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

    protected void onPostExecute(List<String> result) {

        Collections.sort(result);
        CustomListFileBrowser adapter = new CustomListFileBrowser(context, result);
        listActivity.setListAdapter(adapter);
        //listAdapter.set(adapter);
    }
}
