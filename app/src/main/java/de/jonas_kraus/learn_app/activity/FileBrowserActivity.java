package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jonas_kraus.learn_app.Backgroud.AsyncExportOnline;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.SoapService.SoapClientBrowser;
import de.jonas_kraus.learn_app.SoapService.SoapClientBrowserAddCategoryFolder;
import de.jonas_kraus.learn_app.SoapService.SoapClientDownloader;
import de.jonas_kraus.learn_app.Util.CustomListFileBrowser;

public class FileBrowserActivity extends ListActivity {

    private Boolean isLocal = true;
    private String pathLocal;
    private String pathOnline = "";
    private String[] pathBack;
    private int currentCategoryParent;
    private List<String> values = new ArrayList<String>();
    private LinearLayout linearLayoutButtonAdd;
    private Button buttonAdd;
    private View promptView;
    private TextView textViewDir;
    private String pathToExportedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        textViewDir = (TextView)findViewById(R.id.textViewDir);

        // Use the current directory as title
        pathLocal = Environment.getExternalStorageDirectory().getPath();

        if (getIntent().hasExtra("path")) {
            pathLocal = getIntent().getStringExtra("path");
        }
        if (getIntent().hasExtra("pathToExportedFile")) {
            pathToExportedFile = getIntent().getStringExtra("pathToExportedFile");
        }
        if (pathLocal.equals(Environment.getExternalStorageDirectory().getPath())) {
            pathBack = null;
        }
        if (getIntent().hasExtra("currentCategoryParent")) {
            currentCategoryParent = getIntent().getExtras().getInt("currentCategoryParent");
        }
        if (getIntent().hasExtra("isLocal")) {
            isLocal = getIntent().getExtras().getBoolean("isLocal");
            linearLayoutButtonAdd = (LinearLayout)findViewById(R.id.linearLayoutAdd);
            //buttonAdd = (Button)findViewById(R.id.buttonFolderNew);
        }
        //setTitle(pathLocal);
        pathLocal = cutSlashes(pathLocal);
        textViewDir.setText(pathLocal.replace("/", " » "));

        // Read all files sorted into the values-array

        if (isLocal) {
            makeLocalListView(pathLocal);
        } else {
            linearLayoutButtonAdd.setVisibility(View.VISIBLE);
            pathBack = pathOnline.split("/");
            SoapClientBrowser client = new SoapClientBrowser(this, this);
            pathOnline = cutSlashes(pathOnline);
            textViewDir.setText(pathOnline.replace("/"," » "));
            client.scanForJson(pathOnline);
        }
    }

    private void makeLocalListView(String pathLocal) {
        pathBack = pathLocal.split("/");
        for (String str:pathBack) {
            Log.d("path back", ""+str);
        }
        Log.d("path back local", pathLocal);
        File dir = new File(pathLocal);
        if (!dir.canRead()) {
            //setTitle(getTitle() + " (inaccessible)");
            //Toast.makeText(this, "inaccessible", Toast.LENGTH_SHORT).show();
            goToCatalogueActivity();
        }
        String[] list = dir.list();
        if (list != null) {
            values = new ArrayList<String>();
            for (String file : list) {
                if ((!file.startsWith(".") && new File(pathLocal + File.separator + file).isDirectory()) || (!file.startsWith(".") && file.endsWith(".json")) ) {
                    values.add(file);
                }
            }
        } else {
            //Toast.makeText(this, "Nothing to import!", Toast.LENGTH_SHORT).show();
        }
        pathLocal = cutSlashes(pathLocal);
        textViewDir.setText(pathLocal.replace("/", " » "));
        Collections.sort(values);
        CustomListFileBrowser adapter = new CustomListFileBrowser(this, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (isLocal) {
            String filename = (String) getListAdapter().getItem(position);
            if (pathLocal.endsWith(File.separator)) {
                filename = pathLocal + filename;
            } else {
                filename = pathLocal + File.separator + filename;
            }
            pathLocal = filename;
            if (new File(filename).isDirectory()) {
                makeLocalListView(filename);
                /*
                Intent intent = new Intent(this, FileBrowserActivity.class);
                intent.putExtra("path", filename);
                intent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(intent);
                */
            } else {
                Intent myIntent = new Intent(FileBrowserActivity.this, ImportActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                Log.d("filename an import", filename);
                myIntent.putExtra("path", filename);
                startActivity(myIntent);
            }
            pathBack = pathLocal.split("/");
        } else {
            String filename = (String) getListAdapter().getItem(position);
            pathOnline += "/"+filename;
            pathBack = pathOnline.split("/");
            if (filename.endsWith(".json")) {

                final SoapClientDownloader client = new SoapClientDownloader(FileBrowserActivity.this);
                client.downloadJson(pathOnline, currentCategoryParent);

            } else {
                ProgressDialog mProgressDialog;

                mProgressDialog = new ProgressDialog(FileBrowserActivity.this);
                mProgressDialog.setMessage("Scanning Server...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                SoapClientBrowser client = new SoapClientBrowser(this, this);
                textViewDir.setText(pathOnline.replace("/", " » "));
                client.scanForJson(pathOnline);
                mProgressDialog.hide();
            }
        }
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonFolderNew:
                makeCategory();
                break;
            case R.id.buttonExportCancel:
                goToCatalogueActivity();
                break;
            case R.id.buttonExportBack:
                goBackInDir();
                break;
            case R.id.buttonExportHere:
                exportFile();
        }
    }

    private void exportFile() {

        AsyncExportOnline asyncExportOnline = new AsyncExportOnline();
        asyncExportOnline.doExport(pathToExportedFile, pathOnline, this);

    }

    private void goBackInDir() {
        String[] pathNewBack = {""};
        if (pathBack != null && pathBack.length >0) {
            pathNewBack = new String[pathBack.length - 1];
        } else {
            goToCatalogueActivity();
        }
        if (isLocal) {
            pathLocal = "";
            for (int i = 0; i < pathBack.length-1; i++) {
                pathLocal += pathBack[i]+"/";
                pathNewBack[i] = pathBack[i];
            }
            Log.d("path local back befor",""+pathLocal);
            makeLocalListView(pathLocal);
            pathLocal = cutSlashes(pathLocal);
            textViewDir.setText(pathLocal.replace("/", " » "));
        } else {
            pathOnline = "";
            for (int i = 0; i < pathBack.length-1; i++) {
                pathOnline += pathBack[i]+"/";
                pathNewBack[i] = pathBack[i];
            }
            SoapClientBrowser soapClientBrowser = new SoapClientBrowser(FileBrowserActivity.this, FileBrowserActivity.this);
            soapClientBrowser.scanForJson(pathOnline);
            pathOnline = cutSlashes(pathOnline);
            textViewDir.setText(pathOnline.replace("/"," » "));
        }
        pathBack = pathNewBack;
    }

    private void goToCatalogueActivity() {
        Intent myIntent = new Intent(FileBrowserActivity.this, CatalogueActivity.class);
        myIntent.putExtra("currentCategoryParent", currentCategoryParent);
        startActivity(myIntent);
    }

    private void makeCategory() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        promptView = layoutInflater.inflate(R.layout.prompt_add_category, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                SoapClientBrowserAddCategoryFolder client = new SoapClientBrowserAddCategoryFolder(FileBrowserActivity.this, FileBrowserActivity.this);
                EditText name = (EditText) promptView.findViewById(R.id.promptAddCatalogueInput);
                client.addCategoryFolder(pathOnline,name.getText().toString());
                SoapClientBrowser clientScan = new SoapClientBrowser(FileBrowserActivity.this, FileBrowserActivity.this);
                clientScan.scanForJson(pathOnline);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setIcon(R.drawable.categoryicon);
        alertDialog.setTitle("Enter category name:");
        alertDialog.show();
    }

    private String cutSlashes(String path) {
        while (path.length() > 0 && path.charAt(path.length()-1)=='/') {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }

}

