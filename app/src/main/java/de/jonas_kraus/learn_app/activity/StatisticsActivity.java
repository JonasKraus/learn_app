
package de.jonas_kraus.learn_app.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class StatisticsActivity extends ActionBarActivity {

    private TextView textViewDrawer0, textViewDrawer1, textViewDrawer2, textViewDrawer3, textViewDrawer4, textViewDrawer5, textViewCountCards, textViewCountCategories;
    private DbManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        textViewDrawer0 = (TextView)findViewById(R.id.textViewDrawerChart_0);
        textViewDrawer1 = (TextView)findViewById(R.id.textViewDrawerChart_1);
        textViewDrawer2 = (TextView)findViewById(R.id.textViewDrawerChart_2);
        textViewDrawer3 = (TextView)findViewById(R.id.textViewDrawerChart_3);
        textViewDrawer4 = (TextView)findViewById(R.id.textViewDrawerChart_4);
        textViewDrawer5 = (TextView)findViewById(R.id.textViewDrawerChart_5);

        textViewCountCards = (TextView)findViewById(R.id.textViewCountCards);
        textViewCountCategories = (TextView)findViewById(R.id.textViewCountCategories);

        int countCards = db.getCardsCount();
        int countCategories = db.getCategoryCount();

        textViewCountCards.setText(countCards+"\tCards");
        textViewCountCategories.setText(countCategories+"\tCategories");

        int[] distr = db.getDrawerDistribution();

        int maxDrawer = 0;
        for (int i = 0; i < distr.length; i++) {
            if (maxDrawer < distr[i]) {
                maxDrawer = distr[i];
            }
        }

        textViewDrawer0.setText(distr[0]+"");
        textViewDrawer0.setHeight(distr[0]*(200/maxDrawer));
        textViewDrawer1.setText(distr[1]+"");
        textViewDrawer1.setHeight(distr[1]*(200/maxDrawer));
        textViewDrawer2.setText(distr[2]+"");
        textViewDrawer2.setHeight(distr[2]*(200/maxDrawer));
        textViewDrawer3.setText(distr[3]+"");
        textViewDrawer3.setHeight(distr[3]*(200/maxDrawer));
        textViewDrawer4.setText(distr[4]+"");
        textViewDrawer4.setHeight(distr[4]*(200/maxDrawer));
        textViewDrawer5.setText(distr[5]+"");
        textViewDrawer5.setHeight(distr[5]*(200/maxDrawer));

    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
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
