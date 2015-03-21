
package de.jonas_kraus.learn_app.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class StatisticsActivity extends ActionBarActivity {

    private TextView textViewDrawer0, textViewDrawer1, textViewDrawer2, textViewDrawer3, textViewDrawer4, textViewDrawer5, textViewCountCards, textViewCountCategories;
    private ImageView imageViewDrawerBar_0, imageViewDrawerBar_1, imageViewDrawerBar_2, imageViewDrawerBar_3, imageViewDrawerBar_4, imageViewDrawerBar_5;
    private DbManager db;
    private LinearLayout llChart;

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

        imageViewDrawerBar_0 = (ImageView)findViewById(R.id.imageViewDrawerBar_0);
        imageViewDrawerBar_1 = (ImageView)findViewById(R.id.imageViewDrawerBar_1);
        imageViewDrawerBar_2 = (ImageView)findViewById(R.id.imageViewDrawerBar_2);
        imageViewDrawerBar_3 = (ImageView)findViewById(R.id.imageViewDrawerBar_3);
        imageViewDrawerBar_4 = (ImageView)findViewById(R.id.imageViewDrawerBar_4);
        imageViewDrawerBar_5 = (ImageView)findViewById(R.id.imageViewDrawerBar_5);

        llChart = (LinearLayout)findViewById(R.id.linearLayoutChart);
        int llHeight = llChart.getLayoutParams().height -100;

        RotateAnimation rotate= (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotate_chart_text);

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

        textViewDrawer0.setText(distr[0]+""); /* @TODO add +"\n"+"Drawer 1" to label */
        //textViewDrawer0.setAnimation(rotate);
        imageViewDrawerBar_0.getLayoutParams().height = distr[0]*(llHeight/maxDrawer);
        textViewDrawer1.setText(distr[1]+"");
        //textViewDrawer1.setAnimation(rotate);
        imageViewDrawerBar_1.getLayoutParams().height = distr[1]*(llHeight/maxDrawer);
        textViewDrawer2.setText(distr[2]+"");
        //textViewDrawer2.setAnimation(rotate);
        imageViewDrawerBar_2.getLayoutParams().height = distr[2]*(llHeight/maxDrawer);
        textViewDrawer3.setText(distr[3]+"");
        //textViewDrawer3.setAnimation(rotate);
        imageViewDrawerBar_3.getLayoutParams().height = distr[3]*(llHeight/maxDrawer);
        textViewDrawer4.setText(distr[4]+"");
        //textViewDrawer4.setAnimation(rotate);
        imageViewDrawerBar_4.getLayoutParams().height = distr[4]*(llHeight/maxDrawer);
        textViewDrawer5.setText(distr[5]+"");
        //textViewDrawer5.setAnimation(rotate);
        imageViewDrawerBar_5.getLayoutParams().height = distr[5]*(llHeight/maxDrawer);

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
