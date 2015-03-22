package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class PlayActivity extends ActionBarActivity {
    private LinearLayout linearLayoutHintAnswer, linearLayoutHintKnown, linearLayOutDynamic;
    private SeekBar seekBar;
    private DbManager db;
    private Context context;
    private int currentCategoryParent;
    private ArrayList<Catalogue>checkedCatalogue;
    private List<Card> cards;
    private int cardsPosition = 0;
    private TextView textViewPercent, textViewAnswer, textViewQuestion, textViewQuestionCounter, textViewTimerValue;
    private Button buttonNext, buttonKnown, buttonNotKnown, stopTimerValue;
    private Card.CardType curCardType;
    private List<TextView> listTextView;
    private List<CheckBox> listCheckBox;
    private boolean known;
    private Drawable drawableBorderHighlight;
    private int currentDrawer;
    private int countKnownDrawer;

    private final int WHITE = R.color.white;

    private int countKnown, countNotKnown, countNotViewd, countViewed = 0;
    private List<Integer> uniqueCardIds;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Runnable runnable;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            runnable = this;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hours = mins / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            if (textViewTimerValue != null) {
                textViewTimerValue.setText("" + hours + ":"
                        + String.format("%02d",mins%60) + ":"
                        + String.format("%02d", secs));
                customHandler.postDelayed(this, 0);
            }
        }
    };


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

        drawableBorderHighlight = getResources().getDrawable(R.drawable.border_shape_highlight);

        openDb();

        checkedCatalogue = new ArrayList<Catalogue>();
        cards = new ArrayList<Card>();
        currentDrawer = 0;
        countKnownDrawer = 0;

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
            } else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                //checkedCatalogue = extras.getParcelableArrayList("catalogue");
                //cards = db.getCardDescendantsFromCatalogues(checkedCatalogue);
                //Log.d("list", cards.size() + " "+cards.toString());
            }
        }

        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setSeekBarChangeListener();
        openDb();
        //cards = db.getCardsFromMarked();
        cards = db.getMarkedCards();
        uniqueCardIds = new ArrayList<Integer>();
        for (Card card : cards) {
            uniqueCardIds.add(card.getId());
        }
        countNotViewd = uniqueCardIds.size()-1;
        currentDrawer = cards.get(cardsPosition).getDrawer();
        textViewQuestion.setText(cards.get(cardsPosition).getQuestion());
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        textViewQuestionCounter.setText("1/"+cards.size()+"\t\t\t"+cards.size()/100*1+"%");
        prepareAnswers();
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
                box.setBackgroundColor(WHITE);
                box.setPadding(15, 15, 15, 15);
                listCheckBox.add(box);
                linearLayOutDynamic.addView(box);
                linearLayOutDynamic.setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1f));
                box.setText(ans.getAnswer());
                box.setHint(ans.getAnswer());
                box.setBackgroundColor(WHITE);
                box.setTextColor(Color.parseColor("#999999"));
                box.setPadding(15,15,15,15);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        seekBar.setOnSeekBarChangeListener(null);
        //db.deleteAllMarks();
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
                cards.add(cardsPosition, db.updateRating(cards.get(cardsPosition).getId(), 100, known, cards.get(cardsPosition).getDrawer()));
                if (cards.get(cardsPosition).getDrawer() == 5) {
                    countKnownDrawer++;
                }
                cards.remove(cardsPosition + 1);
                changeToNextCard();
                countKnown++;
                break;
            case R.id.buttonNotKnown:
                countNotKnown++;
                known = false;
                cards.add(cardsPosition, db.updateRating(cards.get(cardsPosition).getId(), 0, known, cards.get(cardsPosition).getDrawer()));
                cards.remove(cardsPosition+1);
                changeToNextCard();
                break;
            case R.id.buttonNext:
                int rating = seekBar.getProgress();
                if (rating == 100) {
                    known = true;
                    countKnown++;
                    if (cards.get(cardsPosition).getDrawer() == 5) {
                        countKnownDrawer++;
                    }
                } else {
                    known = false;
                    countNotKnown++;
                }
                cards.add(cardsPosition, db.updateRating(cards.get(cardsPosition).getId(), rating, known, cards.get(cardsPosition).getDrawer()));
                cards.remove(cardsPosition+1);
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

        int count0 = 0;
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        int count5 = 0;
        for (Card card : cards) {
            switch(card.getDrawer()){
                case 0:
                    count0++;
                    break;
                case 1:
                    count1++;
                    break;
                case 2:
                    count2++;
                    break;
                case 3:
                    count3++;
                    break;
                case 4:
                    count4++;
                    break;
                case 5:
                    count5++;
                    break;
            }
        }
        if (currentDrawer == 5 && countKnownDrawer == count5) {
            currentDrawer = 0;
            countKnownDrawer = 0;
        }
        if (count0>0 && currentDrawer <= 0) {
            currentDrawer = 0;
        } else if (count1 >0 && currentDrawer <= 1) {
            currentDrawer = 1;
        } else if (count2 >0 && currentDrawer <= 2) {
            currentDrawer = 2;
        } else if (count3 >0 && currentDrawer <= 3) {
            currentDrawer = 3;
        } else if (count4 >0 && currentDrawer <= 4) {
            currentDrawer = 4;
        } else if (count5 >0 && currentDrawer <= 5) {
            currentDrawer = 5;
        }
        while(cards.get(cardsPosition).getDrawer() != currentDrawer) {
            cardsPosition++;
            cardsPosition %= cards.size();
        }
        if (uniqueCardIds.size()>0) {
            uniqueCardIds.remove((Object) cards.get(cardsPosition).getId());
        }
        countNotViewd = uniqueCardIds.size();
        countViewed = countNotKnown+countKnown;

        textViewQuestionCounter.setText((cardsPosition+1)+"/"+cards.size()+"\t\t\t"+Math.round((100 / cards.size() * (cardsPosition + 1)))+"%");
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
        if (id == R.id.action_play_close) {
            Intent myIntent = new Intent(PlayActivity.this, CatalogueActivity.class);
            myIntent.putExtra("currentCategoryParent",currentCategoryParent);
            startActivity(myIntent);
            return true;
        } else if (id == R.id.action_play_stats) {

            customHandler.postDelayed(runnable, 0); // refreshes the timer

            /* @TODO Button is in view disabled - make real stop
            stopTimerValue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);

                }
            });
            */
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.prompt_stats, null);

            TextView text0 = (TextView)promptView.findViewById(R.id.textViewDrawer0);
            TextView text1 = (TextView)promptView.findViewById(R.id.textViewDrawer1);
            TextView text2 = (TextView)promptView.findViewById(R.id.textViewDrawer2);
            TextView text3 = (TextView)promptView.findViewById(R.id.textViewDrawer3);
            TextView text4 = (TextView)promptView.findViewById(R.id.textViewDrawer4);
            TextView text5 = (TextView)promptView.findViewById(R.id.textViewDrawer5);

            TextView textViewKnown = (TextView)promptView.findViewById(R.id.textViewCountKnown);
            TextView textViewNotKnown = (TextView)promptView.findViewById(R.id.textViewCountNotKnown);
            TextView textViewViewed = (TextView)promptView.findViewById(R.id.textViewCountViewed);
            TextView textViewNotViewed = (TextView)promptView.findViewById(R.id.textViewCountNotViewed);

            textViewKnown.setText(countKnown+"\tknown");
            textViewNotKnown.setText(countNotKnown+"\tnot known");
            textViewViewed.setText((countViewed+1)+"\tviewed");
            textViewNotViewed.setText((countNotViewd)+"\tnot viewed");

            textViewTimerValue = (TextView) promptView.findViewById(R.id.textViewTimerValue);
            stopTimerValue = (Button) promptView.findViewById(R.id.stopTimerValue);

            int count0 = 0;
            int count1 = 0;
            int count2 = 0;
            int count3 = 0;
            int count4 = 0;
            int count5 = 0;
            TextView curDrawer = (TextView)promptView.findViewWithTag("drawer_"+currentDrawer);
            curDrawer.setBackgroundDrawable(drawableBorderHighlight);
            curDrawer.setTextColor(Color.parseColor("#555555"));
            for (Card card : cards) {
                switch(card.getDrawer()){
                    case 0:
                        count0++;
                        break;
                    case 1:
                        count1++;
                        break;
                    case 2:
                        count2++;
                        break;
                    case 3:
                        count3++;
                        break;
                    case 4:
                        count4++;
                        break;
                    case 5:
                        count5++;
                        break;
                }
            }
            text0.setText(count0+"");
            text1.setText(count1+"");
            text2.setText(count2+"");
            text3.setText(count3+"");
            text4.setText(count4+"");
            text5.setText(count5+"");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(promptView);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setIcon(R.drawable.information_black);
            alertDialog.setTitle("Statistics");
            alertDialog.show();

            return true;
        } else if(id == R.id.action_play_swap_direction) {
            Collections.rotate(cards,cards.size()); /* @TODO rotate the list */
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
