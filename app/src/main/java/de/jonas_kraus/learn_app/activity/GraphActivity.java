package de.jonas_kraus.learn_app.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.sql.SQLException;
import java.util.List;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;
//import de.jonas_kraus.learn_app.Util.GraphView;

public class GraphActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///////////////////////////////////
        DbManager db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Integer> vals = db.getTimeStatisticsList();
        float[] values = new float[vals.size()];
        int max = db.getMaxTimeStatistics();
        String[] verlabels = new String[max/10];
        int count = 0;
        while(max % 10  > 10) {
            max %= 10;
            verlabels[count] = max+"";
            count ++;
        }
        String[] horlabels = new String[vals.size()];
        for (int i = 0; i < vals.size(); i++) {
            values[i] = vals.get(i);
            //verlabels[i] = (vals.get(i)/60)+"min";
            horlabels[i] = i+"";
        }
        /* @TODO Kaputt gemacht!!
        GraphView graphView = new GraphView(this, values, "GraphViewDemo",horlabels, verlabels, GraphView.LINE);
        //////////////////////////////////////////////
        graphView.setPadding(15,15,15,15);
        LinearLayout relativeLayout = new LinearLayout(this);
        relativeLayout.addView(graphView);
        setContentView(relativeLayout);
        Log.d("Test", "" + graphView.getMeasuredWidth());
        Log.d("Test ll", "" + relativeLayout.getWidth());
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
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
