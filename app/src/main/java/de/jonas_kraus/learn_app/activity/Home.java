package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;


public class Home extends ActionBarActivity {
    Button buttonCatalogue, buttonTodo, buttonSettings, buttonStats;
    private DbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        buttonCatalogue = (Button) findViewById(R.id.buttonCatalogue);
        buttonTodo = (Button) findViewById(R.id.buttonTodos);
        buttonStats = (Button) findViewById(R.id.buttonStats);
        buttonSettings = (Button) findViewById(R.id.buttonSettings);

        dbManager = new DbManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.buttonCatalogue:
                Intent myIntent = new Intent(Home.this, CatalogueActivity.class);
                startActivity(myIntent);
                break;
            case R.id.buttonTodos:
                /* @TODO */
                break;
            case R.id.buttonSettings:
                Intent myIntentSettings = new Intent(Home.this, SettingsActivity.class);
                startActivity(myIntentSettings);
                break;
            case R.id.buttonStats:
                if (dbManager.getCardsCount() > 0) {
                    Intent myIntentStats = new Intent(Home.this, StatisticsActivity.class);
                    startActivity(myIntentStats);
                } else {
                    Toast.makeText(this, "No data to display!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
