package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class PlayActivity extends ActionBarActivity {
    private LinearLayout linearLayoutHintAnswer, linearLayoutHintKnown;
    private SeekBar seekBar;
    private DbManager db;
    private Context context;
    private int currentCategoryParent;
    private ArrayList<Catalogue>checkedCatalogue;
    private List<Card> cards;
    private int cardsPosition = 0;
    private TextView textViewPercent, textViewAnswer, textViewQuestion;
    private Button buttonNext;
    private Card.CardType curCardType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        linearLayoutHintAnswer = (LinearLayout)findViewById(R.id.linearLayOutButtonsHintAnswer);
        linearLayoutHintKnown= (LinearLayout)findViewById(R.id.linearLayOutButtonsKnown);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
        textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setEnabled(false);

        context = getApplicationContext();

        openDb();

        checkedCatalogue = new ArrayList<Catalogue>();
        cards = new ArrayList<Card>();

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
            } else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                checkedCatalogue = extras.getParcelableArrayList("catalogue");
                cards = db.getCardDescendantsFromCatalogues(checkedCatalogue);
                //Log.d("list", cards.size() + " "+cards.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSeekBarChangeListener();
        textViewQuestion.setText(cards.get(cardsPosition).getQuestion());
        prepareAnswers();
        openDb();
    }

    private void prepareAnswers() {
        textViewAnswer.setVisibility(View.INVISIBLE);
        curCardType = cards.get(cardsPosition).getType();
        if (curCardType == Card.CardType.NOTECARD) {
            textViewAnswer.setText(cards.get(cardsPosition).getAnswers().get(0).getAnswer());
            textViewAnswer.setVisibility(View.INVISIBLE);
        } else {
            /* @TODO Make multiple answer checkboxes */
            textViewAnswer.setText(curCardType.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        seekBar.setOnSeekBarChangeListener(null);
        db.close();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAnswer:
                linearLayoutHintAnswer.setVisibility(View.INVISIBLE);
                linearLayoutHintKnown.setVisibility(View.VISIBLE);
                seekBar.setEnabled(true);
                textViewAnswer.setVisibility(View.VISIBLE);
                break;
            case R.id.buttonHint:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(cards.get(cardsPosition).getHint()).setTitle("Hint");
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.buttonKnown:
                changeToNextCard();
                break;
            case R.id.buttonNotKnown:
                changeToNextCard();
                break;
            case R.id.buttonNext:
                changeToNextCard();
                break;
        }
    }

    private void changeToNextCard() {
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        seekBar.setEnabled(false);
        buttonNext.setEnabled(false);
        cardsPosition ++;
        if (cardsPosition == cards.size()) {
            Toast.makeText(context, "Learned all Cards", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(PlayActivity.this, CatalogueActivity.class);
            myIntent.putExtra("currentCategoryParent",currentCategoryParent);
            startActivity(myIntent);
        } else {
            linearLayoutHintAnswer.setVisibility(View.VISIBLE);
            linearLayoutHintKnown.setVisibility(View.INVISIBLE);
            textViewQuestion.setText(cards.get(cardsPosition).getQuestion());

            prepareAnswers();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
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

    private void setSeekBarChangeListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewPercent.setText(progress+"%");
                buttonNext.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
}
