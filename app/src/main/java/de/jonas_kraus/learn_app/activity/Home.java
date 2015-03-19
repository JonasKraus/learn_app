package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;


public class Home extends ActionBarActivity {
    Button buttonCatalogue, buttonTodo;
    private DbManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        buttonCatalogue = (Button) findViewById(R.id.buttonCatalogue);
        buttonTodo = (Button) findViewById(R.id.buttonTodos);
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
        dbManager = new DbManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d("dBManager", "error happened");
        }
        //dbManager.deleteAllMarks();
        setCatalogueButtonListener();
        setTodoButtonListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        buttonTodo.setOnClickListener(null);
        buttonCatalogue.setOnClickListener(null);
        dbManager.close();
    }

    private void setTodoButtonListener() {
        buttonTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*@TODO */
            }
        });
    }

    private void setCatalogueButtonListener() {
        buttonCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Home.this, CatalogueActivity.class);
                startActivity(myIntent);
            }
        });
    }
}
