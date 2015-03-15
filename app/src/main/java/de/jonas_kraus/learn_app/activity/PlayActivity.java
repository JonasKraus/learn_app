package de.jonas_kraus.learn_app.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

import static android.widget.LinearLayout.*;

public class PlayActivity extends ActionBarActivity {
    private LinearLayout linearLayoutHintAnswer, linearLayoutHintKnown, linearLayOutDynamic;
    private SeekBar seekBar;
    private DbManager db;
    private Context context;
    private int currentCategoryParent;
    private ArrayList<Catalogue>checkedCatalogue;
    private List<Card> cards;
    private int cardsPosition = 0;
    private TextView textViewPercent, textViewAnswer, textViewQuestion, textViewQuestionCounter;
    private Button buttonNext, buttonKnown, buttonNotKnown;
    private Card.CardType curCardType;
    private List<TextView> listTextView;
    private List<CheckBox> listCheckBox;
    private boolean known;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        linearLayoutHintAnswer = (LinearLayout)findViewById(R.id.linearLayOutButtonsHintAnswer);
        linearLayoutHintKnown = (LinearLayout)findViewById(R.id.linearLayOutButtonsKnown);
        linearLayOutDynamic = (LinearLayout)findViewById(R.id.linearLayOutDynamic);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
        textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
        textViewQuestionCounter = (TextView) findViewById(R.id.textViewQuestionCounter);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonKnown = (Button) findViewById(R.id.buttonKnown);
        buttonNotKnown = (Button) findViewById(R.id.buttonNotKnown);
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
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        textViewQuestionCounter.setText("1/"+cards.size()+"\t\t\t"+cards.size()/100*1+"%");
        prepareAnswers();
        openDb();
    }

    private void prepareAnswers() {
        textViewAnswer.setVisibility(View.INVISIBLE);
        curCardType = cards.get(cardsPosition).getType();
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        if (curCardType == Card.CardType.NOTECARD) {
            textViewAnswer.setText(cards.get(cardsPosition).getAnswers().get(0).getAnswer());
            textViewAnswer.setVisibility(View.INVISIBLE);
        } else {
            /* @TODO Make multiple answer checkboxes */
            listCheckBox = new ArrayList<CheckBox>();
            textViewAnswer.setText(curCardType.toString());
            textViewAnswer.setVisibility(View.GONE);
            for (Answer ans: cards.get(cardsPosition).getAnswers()) {
                CheckBox box = new CheckBox(context);
                box.setText(ans.getAnswer());
                box.setHint(ans.getAnswer());
                box.setBackgroundColor(Color.parseColor("#fefefe"));
                box.setPadding(15, 15, 15, 15);
                listCheckBox.add(box);
                linearLayOutDynamic.addView(box);
                linearLayOutDynamic.setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1f));
                box.setText(ans.getAnswer());
                box.setHint(ans.getAnswer());
                box.setBackgroundColor(Color.parseColor("#fefefe"));
                box.setTextColor(Color.parseColor("#999999"));
                box.setPadding(15,15,15,15);
            }

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
                buttonNotKnown.setEnabled(true);
                buttonKnown.setEnabled(true);
                if (curCardType == Card.CardType.NOTECARD) {
                    textViewAnswer.setVisibility(View.VISIBLE);
                } else {
                    textViewAnswer.setVisibility(View.GONE);
                    List<Answer>curAns = cards.get(cardsPosition).getAnswers();
                    buttonNext.setEnabled(true);
                    seekBar.setEnabled(false);
                    buttonNotKnown.setEnabled(false);
                    buttonKnown.setEnabled(false);
                    known = true;
                    for (int i = 0; i < curAns.size(); i ++) {
                        listCheckBox.get(i).setEnabled(false);
                        if (listCheckBox.get(i).isChecked() == curAns.get(i).isCorrect()) {
                            listCheckBox.get(i).setTextColor(Color.GREEN);
                        } else {
                            listCheckBox.get(i).setTextColor(Color.RED);
                            listCheckBox.get(i).setChecked(curAns.get(i).isCorrect());
                            known = false;
                        }
                    }
                    if (known) {
                        seekBar.setProgress(100);
                    } else {
                        seekBar.setProgress(0);
                    }

                }
                break;
            case R.id.buttonHint:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(cards.get(cardsPosition).getHint()).setTitle("Hint");
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.buttonKnown:
                known = true;
                db.updateRating(cards.get(cardsPosition).getId(), 100, known);
                changeToNextCard();
                break;
            case R.id.buttonNotKnown:
                known = false;
                db.updateRating(cards.get(cardsPosition).getId(), 0, known);
                changeToNextCard();
                break;
            case R.id.buttonNext:
                known = false;
                int rating = seekBar.getProgress();
                db.updateRating(cards.get(cardsPosition).getId(), rating, known);
                changeToNextCard();
                break;
        }
    }

    private void changeToNextCard() {
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        seekBar.setEnabled(false);
        buttonNext.setEnabled(false);
        cardsPosition ++;
        cardsPosition %= cards.size(); // makes the roundtrip
        textViewQuestionCounter.setText((cardsPosition+1)+"/"+cards.size()+"\t\t\t"+Math.round((100/cards.size()*(cardsPosition+1)))+"%");
        linearLayOutDynamic.removeAllViews();
        if (cardsPosition == cards.size()) {
            Toast.makeText(context, "Learned all Cards", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(PlayActivity.this, CatalogueActivity.class);
            myIntent.putExtra("currentCategoryParent",currentCategoryParent);
            startActivity(myIntent);
        } else {
            linearLayoutHintAnswer.setVisibility(View.VISIBLE);
            linearLayoutHintKnown.setVisibility(View.INVISIBLE);
            textViewQuestion.setText(cards.get(cardsPosition).getQuestion());
            Log.d("On change", "id: " + cards.get(cardsPosition).getId() + " rating: " + cards.get(cardsPosition).getRating() + "  known: " + cards.get(cardsPosition).isKnown());
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
