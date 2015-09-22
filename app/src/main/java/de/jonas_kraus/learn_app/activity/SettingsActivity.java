package de.jonas_kraus.learn_app.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.Calendar;

import de.jonas_kraus.learn_app.Broadcast.DailyNotificationReceiver;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class SettingsActivity extends ActionBarActivity {

    private CheckBox boxShowHint, boxShowKnownBar, boxOrderMultiplechoiceAnswers, boxViewRandomCards, boxViewLastDrawer, boxNightMode, boxDailyReminder;
    private RadioButton radioOrderByKnowledge, radioOrderByDrawer, radioOrderByDate;
    private RadioGroup radioGroupOrder;
    private TextView textViewTextSizeQuestions, textViewTextSizeAnswers;
    private SeekBar seekBarTextSizeQuestions, seekBarTextSizeAnswers;
    private DbManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        boxShowHint = (CheckBox) findViewById(R.id.settings_checkbox_showHint);
        boxDailyReminder = (CheckBox) findViewById(R.id.settings_checkbox_dailyReminder);
        boxShowKnownBar = (CheckBox) findViewById(R.id.settings_checkbox_showKnownBar);
        boxOrderMultiplechoiceAnswers = (CheckBox) findViewById(R.id.settings_checkbox_multiplechoiceOrder);
        boxViewRandomCards = (CheckBox) findViewById(R.id.settings_checkbox_viewRandomCards);
        boxViewLastDrawer = (CheckBox) findViewById(R.id.settings_checkbox_viewCardsOfLastDrawer);
        boxNightMode = (CheckBox) findViewById(R.id.settings_checkbox_nightMode);
        radioOrderByKnowledge = (RadioButton) findViewById(R.id.settings_radio_knowledge);
        radioOrderByDrawer = (RadioButton) findViewById(R.id.settings_radio_drawer);
        radioOrderByKnowledge = (RadioButton) findViewById(R.id.settings_radio_date);
        radioGroupOrder = (RadioGroup) findViewById(R.id.settings_radiogroupOrder);
        textViewTextSizeAnswers = (TextView)findViewById(R.id.textViewSettingsTextSizeAnswers);
        textViewTextSizeQuestions = (TextView)findViewById(R.id.textViewSettingsTextSizeQuestions);
        seekBarTextSizeAnswers = (SeekBar)findViewById(R.id.seekBarSettingsTextSizeAnswers);
        seekBarTextSizeQuestions = (SeekBar)findViewById(R.id.seekBarSettingsTextSizeQuestions);

        int id_first = R.id.settings_radio_drawer;
        int id_second = R.id.settings_radio_knowledge;
        int id_third = R.id.settings_radio_date;

        db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int checkedRadioIndex = db.getCardsOrderType();

        boxDailyReminder.setChecked(db.isDailyReminder());
        boxShowHint.setChecked(db.isShowHint());
        boxShowKnownBar.setChecked(db.isShowBar());
        boxOrderMultiplechoiceAnswers.setChecked(db.isOrderMultipleChoiceAnswers());
        boxViewRandomCards.setChecked(db.isViewRandomCards());
        boxViewLastDrawer.setChecked(db.isViewCardsOfLastDrawer());
        boxNightMode.setChecked(db.isNightMode());
        switch (checkedRadioIndex) {
            case 0:
                radioGroupOrder.check(id_first);
                break;
            case 1:
                radioGroupOrder.check(id_second);
                break;
            case 2:
                radioGroupOrder.check(id_third);
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        setOnCheckedChangeListenrOfRadioGroupOrder();
        setSeekBarTextSizeListener();
        int textSizeQuestions = db.getTextSizeQuestions();
        int textSizeAnswers = db.getTextSizeAnswers();
        Log.d("fontsize", textSizeAnswers + " " + textSizeQuestions);
        textViewTextSizeAnswers.setTextSize(textSizeAnswers);
        textViewTextSizeQuestions.setTextSize(textSizeQuestions);
        seekBarTextSizeAnswers.setProgress(textSizeAnswers - 10); /* @TODO  form db*/
        seekBarTextSizeQuestions.setProgress(textSizeQuestions-10); /* @TODO  form db*/
    }

    private void setSeekBarTextSizeListener() {
        seekBarTextSizeQuestions.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekBar.setProgress(progress+10);
                textViewTextSizeQuestions.setText(progress + 10 + "Pt");
                textViewTextSizeQuestions.setTextSize(progress + 10);
                db.setTextSizeQuestions(progress + 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarTextSizeAnswers.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewTextSizeAnswers.setText(progress + 10 + "Pt");
                textViewTextSizeAnswers.setTextSize(progress + 10);
                db.setTextSizeAnswers(progress + 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setOnCheckedChangeListenrOfRadioGroupOrder() {
        radioGroupOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("radio", "checked " + checkedId);
                switch (checkedId) {
                    case R.id.settings_radio_drawer:
                        db.setOrderCards(0);
                        break;
                    case R.id.settings_radio_knowledge:
                        db.setOrderCards(1);
                        break;
                    case R.id.settings_radio_date:
                        db.setOrderCards(2);
                        break;
                }
            }
        });
    }

    public void onCheckedChange(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.settings_checkbox_dailyReminder:
                if (!boxDailyReminder.isChecked()) {
                    disableBroadcastReceivers();
                } else {
                    enableBroadcastReceivers();
                    startDailyNotificationService();
                }
                db.setDailyReminder(boxDailyReminder.isChecked());
            case R.id.settings_checkbox_showHint:
                db.setShowHint(boxShowHint.isChecked());
                break;
            case R.id.settings_checkbox_showKnownBar:
                db.setShowBar(boxShowKnownBar.isChecked());
                break;
            case R.id.settings_checkbox_multiplechoiceOrder:
                db.setOrderMultipleChoiceAnswers(boxOrderMultiplechoiceAnswers.isChecked());
                break;
            case R.id.settings_checkbox_viewRandomCards:
                db.setViewRandomCards(boxViewRandomCards.isChecked());
                break;
            case R.id.settings_checkbox_viewCardsOfLastDrawer:
                db.setViewCardsOfLastDrawer(boxViewLastDrawer.isChecked());
                break;
            case R.id.settings_checkbox_nightMode:
                db.setNightMode(boxNightMode.isChecked());
                break;
        }
    }

    protected void onPause() {
        super.onPause();
        radioGroupOrder.setOnCheckedChangeListener(null);
        seekBarTextSizeAnswers.setOnSeekBarChangeListener(null);
        seekBarTextSizeQuestions.setOnSeekBarChangeListener(null);
        db.close();
    }

    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.settings_back:
                Intent myIntent = new Intent(SettingsActivity.this, Home.class);
                startActivity(myIntent);
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void disableBroadcastReceivers() {

        ComponentName receiverDaily = new ComponentName(this, DailyNotificationReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiverDaily,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void enableBroadcastReceivers() {

        ComponentName receiverDaily = new ComponentName(this, DailyNotificationReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiverDaily,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    protected void startDailyNotificationService() {
        Intent myIntent = new Intent(this , DailyNotificationReceiver.class);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Log.d("daily service", "started");

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 10000, 60000, pendingIntent);
    }
}
