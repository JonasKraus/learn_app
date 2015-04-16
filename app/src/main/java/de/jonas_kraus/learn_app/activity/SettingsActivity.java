package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
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

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class SettingsActivity extends ActionBarActivity {

    private CheckBox boxShowHint, boxShowKnownBar, boxOrderMultiplechoiceAnswers, boxViewRandomCards, boxViewLastDrawer, boxNightMode;
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
                db.setTextSizeQuestions(progress+10);
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
                db.setTextSizeAnswers(progress+10);
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
                Log.d("radio", "checked " +checkedId);
                switch(checkedId) {
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
}
