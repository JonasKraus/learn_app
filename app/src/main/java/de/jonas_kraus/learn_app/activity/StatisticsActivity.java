
package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.Util.GraphView;

public class StatisticsActivity extends ActionBarActivity {

    private TextView textViewDrawer0, textViewDrawer1, textViewDrawer2, textViewDrawer3, textViewDrawer4, textViewDrawer5, textViewCountCards, textViewCountCategories,
    textViewCountPlayedCards, textViewCountKnown, textViewCountNotKnown, textViewOverallTime;
    private ImageView imageViewDrawerBar_0, imageViewDrawerBar_1, imageViewDrawerBar_2, imageViewDrawerBar_3, imageViewDrawerBar_4, imageViewDrawerBar_5;
    private DbManager db;
    private LinearLayout llChart;
    private Button buttonBack;

    private int currentCategoryParent;
    private boolean returnToCatatlogue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ///////////////////////////////////
        float[] values = new float[] { 2.0f,1.5f, 2.5f, 1.0f , 3.0f };
        String[] verlabels = new String[] { "great", "ok", "bad" };
        String[] horlabels = new String[] { "today", "tomorrow", "next week", "next month" };
        GraphView graphView = new GraphView(this, values, "GraphViewDemo",horlabels, verlabels, GraphView.LINE);
        //////////////////////////////////////////////
        graphView.setPadding(15,15,15,15);
        LinearLayout linearLayoutGraph = (LinearLayout)findViewById(R.id.linearLayoutGraph);
        linearLayoutGraph.addView(graphView);
        db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
                returnToCatatlogue = false;
            } else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                returnToCatatlogue = true;
            }
        }

        textViewDrawer0 = (TextView)findViewById(R.id.textViewDrawerChart_0);
        textViewDrawer1 = (TextView)findViewById(R.id.textViewDrawerChart_1);
        textViewDrawer2 = (TextView)findViewById(R.id.textViewDrawerChart_2);
        textViewDrawer3 = (TextView)findViewById(R.id.textViewDrawerChart_3);
        textViewDrawer4 = (TextView)findViewById(R.id.textViewDrawerChart_4);
        textViewDrawer5 = (TextView)findViewById(R.id.textViewDrawerChart_5);

        textViewCountPlayedCards = (TextView)findViewById(R.id.textViewCountPlayedCards);
        textViewCountKnown = (TextView)findViewById(R.id.textViewStatsCountKnown);
        textViewCountNotKnown = (TextView)findViewById(R.id.textViewStatsCountNotKnown);
        textViewOverallTime = (TextView)findViewById(R.id.textViewStatsCountOverallTime);

        textViewCountPlayedCards.setText(db.getCountCardsStatistics()+" times viewed a card");
        textViewCountKnown.setText(db.getCountKnownStatistics()+" times known");
        textViewCountNotKnown.setText(db.getCountNotKnownStatistics()+" times not known");

        int secs = db.getTimeStatistics();
        int mins = secs / 60;
        int hours = mins / 60;
        int days = hours / 24;
        hours = hours % 24;
        secs = secs % 60;
        textViewOverallTime.setText(
                    days + " Days and "
                    + String.format("%02d",hours) + ":"
                    + String.format("%02d",mins%60) + ":"
                    + String.format("%02d", secs)+" in total time");

        buttonBack = (Button) findViewById(R.id.buttonBackHome);

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

        int countCards = db.getCardsCount(currentCategoryParent);
        int countCategories = db.getCategoryCount(currentCategoryParent);

        textViewCountCards.setText(countCards+"\tCards");
        textViewCountCategories.setText(countCategories+"\tCategories");

        int[] distr = new int[]{0,0,0,0,0,0};
        distr = db.getDrawerDistribution(currentCategoryParent);

        int maxDrawer = 1;
        for (int i = 0; i < distr.length; i++) {
            if (maxDrawer < distr[i]) {
                maxDrawer = distr[i];
            }
        }

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF, 1000, Animation.RELATIVE_TO_SELF);
        translateAnimation.setDuration(1000);

        textViewDrawer0.setText(distr[0]+""); /* @TODO add +"\n"+"Drawer 1" to label */
        //textViewDrawer0.setAnimation(rotate);
        imageViewDrawerBar_0.getLayoutParams().height = distr[0]*(llHeight/maxDrawer);
        imageViewDrawerBar_0.startAnimation(translateAnimation);
        textViewDrawer1.setText(distr[1]+"");
        //textViewDrawer1.setAnimation(rotate);
        imageViewDrawerBar_1.getLayoutParams().height = distr[1]*(llHeight/maxDrawer);
        imageViewDrawerBar_1.startAnimation(translateAnimation);
        textViewDrawer2.setText(distr[2]+"");
        //textViewDrawer2.setAnimation(rotate);
        imageViewDrawerBar_2.getLayoutParams().height = distr[2]*(llHeight/maxDrawer);
        imageViewDrawerBar_2.startAnimation(translateAnimation);
        textViewDrawer3.setText(distr[3]+"");
        //textViewDrawer3.setAnimation(rotate);
        imageViewDrawerBar_3.getLayoutParams().height = distr[3]*(llHeight/maxDrawer);
        imageViewDrawerBar_3.startAnimation(translateAnimation);
        textViewDrawer4.setText(distr[4]+"");
        //textViewDrawer4.setAnimation(rotate);
        imageViewDrawerBar_4.getLayoutParams().height = distr[4]*(llHeight/maxDrawer);
        imageViewDrawerBar_4.startAnimation(translateAnimation);
        textViewDrawer5.setText(distr[5]+"");
        //textViewDrawer5.setAnimation(rotate);
        imageViewDrawerBar_5.getLayoutParams().height = distr[5]*(llHeight/maxDrawer);
        imageViewDrawerBar_5.startAnimation(translateAnimation);
    }

    /**
     * Click on Back button
     * @param view
     */
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonBackHome:

                if (!returnToCatatlogue) {
                    Intent myIntent = new Intent(StatisticsActivity.this, Home.class);
                    startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(StatisticsActivity.this, CatalogueActivity.class);
                    myIntent.putExtra("currentCategoryParent",currentCategoryParent);
                    startActivity(myIntent);
                }
                break;
            case R.id.buttonGraph:
                Intent intent = new Intent(StatisticsActivity.this, GraphActivity.class);
                startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_statistics, menu);
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
