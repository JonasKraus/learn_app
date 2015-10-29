package de.jonas_kraus.learn_app.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.SoapService.SoapClientBrowser;
import de.jonas_kraus.learn_app.SoapService.SoapClientDownloader;
import de.jonas_kraus.learn_app.Util.CustomListFileBrowser;

public class FileBrowserActivity extends ListActivity {

    private Boolean isLocal = true;
    private String pathLocal;
    private String pathOnline = "";
    private int currentCategoryParent;
    private List<String> values = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        // Use the current directory as title
        pathLocal = Environment.getExternalStorageDirectory().getPath();

        if (getIntent().hasExtra("path")) {
            pathLocal = getIntent().getStringExtra("path");
        }
        if (getIntent().hasExtra("currentCategoryParent")) {
            currentCategoryParent = getIntent().getExtras().getInt("currentCategoryParent");
        }
        if (getIntent().hasExtra("isLocal")) {
            isLocal = getIntent().getExtras().getBoolean("isLocal");
        }
        setTitle(pathLocal);

        // Read all files sorted into the values-array

        if (isLocal) {
            File dir = new File(pathLocal);
            if (!dir.canRead()) {
                setTitle(getTitle() + " (inaccessible)");
                Toast.makeText(this, "inaccessible", Toast.LENGTH_SHORT).show();
            }
            String[] list = dir.list();
            if (list != null) {
                for (String file : list) {
                    if ((!file.startsWith(".") && new File(pathLocal + File.separator + file).isDirectory()) || (!file.startsWith(".") && file.endsWith(".json")) ) {
                        values.add(file);
                    }
                }
            } else {
                Toast.makeText(this, "Nothing to display", Toast.LENGTH_SHORT).show();
            }
            Collections.sort(values);
            CustomListFileBrowser adapter = new CustomListFileBrowser(this, values);
            setListAdapter(adapter);
        } else {
            SoapClientBrowser client = new SoapClientBrowser(this, this);
            client.scanForJson("");
        }
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
            if (new File(filename).isDirectory()) {
                Intent intent = new Intent(this, FileBrowserActivity.class);
                intent.putExtra("path", filename);
                intent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(intent);
            } else {
                Intent myIntent = new Intent(FileBrowserActivity.this, ImportActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                myIntent.putExtra("path", filename);
                startActivity(myIntent);
            }
        } else {
            String filename = (String) getListAdapter().getItem(position);
            pathOnline += "/"+filename;
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
                client.scanForJson(pathOnline);
                mProgressDialog.hide();
            }
        }
    }

}

