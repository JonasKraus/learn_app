package de.jonas_kraus.learn_app.SoapService;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.Util.CustomListFileBrowser;

/**
 * Created by Jonas on 15.10.2015.
 */
public class SoapClientBrowserAddCategoryFolder extends AsyncTask<String,  Void, Void> {
    String SOAP_ACTION = "http://www.jonas-kraus.de/flashcards/service/";
    String NAMESPACE = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    String METHOD_NAME = "";
    String URL = "http://www.jonas-kraus.de/flashcards/service/soapServer.php";
    List<String> resultList;
    private Context context;
    private ListActivity listActivity;
    private String path = "";

    public SoapClientBrowserAddCategoryFolder(Context context, ListActivity listActivity) {
        this.context = context;
        this.listActivity = listActivity;
    }

    public List<String> addCategoryFolder(String dir, String folderName) {
        this.execute("addCategoryFolder",dir, folderName);
        return resultList;
    }

    @Override
    protected Void doInBackground(String... strings) {

        METHOD_NAME = strings[0];
        SOAP_ACTION += METHOD_NAME;

        SoapSerializationEnvelope soapEnv = new SoapSerializationEnvelope(SoapEnvelope.VER12);

        HttpTransportSE http = getHttpTransportSE();

        SoapObject soapObject = new SoapObject(null,METHOD_NAME);

        if (strings[1].length()>0) {
            path += "/"+strings[1];
        }
        soapObject.addProperty("subdir", path);
        soapObject.addProperty("dirName", strings[2]);
        soapEnv.setOutputSoapObject(soapObject);
        try {
            http.call(SOAP_ACTION, soapEnv);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }
    private HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY,URL,60000);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

}
