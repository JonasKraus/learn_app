package de.jonas_kraus.learn_app.activity;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class CatalogueHome extends ListActivity {
    private DbManager db;
    private ListView listViewCatalogue;
    private Button buttonAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue_home);

        openDb();
        listViewCatalogue = getListView();
        buttonAddCategory = (Button) findViewById(R.id.buttonNewCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDb();
    }

    public void onClick(View view) {
        Log.d("CatalogueHome", "click: " + view.getId());
        switch(view.getId()) {
            case R.id.buttonNewCategory:
        }
    }

    private void openDb() {
        try {
            if (db == null) {
                db = new DbManager(this);
            }
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        db.close();
        buttonAddCategory.setOnClickListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalogue_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
