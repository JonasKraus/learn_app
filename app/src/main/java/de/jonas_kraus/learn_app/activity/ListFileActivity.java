package de.jonas_kraus.learn_app.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jonas_kraus.learn_app.R;

public class ListFileActivity extends ListActivity {
    private String path;
    private int currentCategoryParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);

        // Use the current directory as title
        path = Environment.getExternalStorageDirectory().getPath();

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }
        if (getIntent().hasExtra("currentCategoryParent")) {
            currentCategoryParent = getIntent().getExtras().getInt("currentCategoryParent");
        }
        setTitle(path);

        // Read all files sorted into the values-array
        List values = new ArrayList();
        File dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
            Toast.makeText(this, "inaccessible", Toast.LENGTH_SHORT).show();
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if ((!file.startsWith(".") && new File(path + File.separator + file).isDirectory()) || (!file.startsWith(".") && file.endsWith(".json")) ) {
                    values.add(file);
                }
            }
        } else {
            Toast.makeText(this, "Nothing to display", Toast.LENGTH_SHORT).show();
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            intent.putExtra("currentCategoryParent", currentCategoryParent);
            startActivity(intent);
        } else {
            // Toast.makeText(this, filename + " is not a directory", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(ListFileActivity.this, ImportActivity.class);
            myIntent.putExtra("currentCategoryParent", currentCategoryParent);
            myIntent.putExtra("path", filename);
            startActivity(myIntent);
        }
    }
}

