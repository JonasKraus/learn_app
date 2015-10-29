package de.jonas_kraus.learn_app.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;

import de.jonas_kraus.learn_app.Broadcast.DailyNotificationReceiver;
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

        File fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "/flashcards");
        if (! fileNew.exists()){
            Log.d("dir ", "file dosent exist");
            if (! fileNew.mkdirs()){
                Log.e("dir ", "Directory not created");
            }
        }

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

        dbManager.setFirstSettings();
        // Start broadcast alarm for daily reminder
        if (dbManager.getDailyReminderTimeDate() != null) {
            enableBroadcastReceivers();
            startDailyNotificationService();
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
                Intent myIntentTodos = new Intent(Home.this, TodosActivity.class);
                startActivity(myIntentTodos);
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
        //disableBroadcastReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void startDailyNotificationService() {
        Intent myIntent = new Intent(this , DailyNotificationReceiver.class);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        String[] time = dbManager.getDailyReminderTimeString().split(":");

        Calendar cur_cal = Calendar.getInstance();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*60*60, pendingIntent);
    }
    public void enableBroadcastReceivers() {

        ComponentName receiverDaily = new ComponentName(this, DailyNotificationReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiverDaily,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

}
