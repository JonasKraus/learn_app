package de.jonas_kraus.learn_app.activity;

import android.app.ActionBar;
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
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    private Button buttonNext, buttonKnown, buttonNotKnown, stopTimerValue, buttonHint;
    private Card.CardType curCardType;
    private List<TextView> listTextView;
    private List<CheckBox> listCheckBox;
    private boolean known;
    private Drawable drawableBorderHighlight;
    private int currentDrawer;
    private int countKnownDrawer;

    private int WHITE, BLACK, RED, GREEN;

    private int countKnown, countNotKnown, countNotViewd, countViewed = 0;
    private List<Integer> uniqueCardIds;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Runnable runnable;

    private Boolean isTodos, isNewest, isOldest, isDrawers = false;
    private String limit;
    private ArrayList<Integer> list;
    private int milliseconds = 0;

    // Settings
    private boolean isShowHint, isShowKnownBar, isChangeMultipleChoiceAnswers, isViewRandomCards, isViewLastDrawer, isNightMode;
    private int orderType, knownBarVisibility;

    private final AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
    private final TranslateAnimation translateAnimation= new TranslateAnimation(1000,Animation.RELATIVE_TO_SELF,Animation.RELATIVE_TO_SELF,Animation.RELATIVE_TO_SELF);

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            runnable = this;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hours = mins / 60;
            secs = secs % 60;
            milliseconds = secs;
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

        openDb();

        linearLayoutHintAnswer = (LinearLayout)findViewById(R.id.linearLayOutButtonsHintAnswer);
        linearLayoutHintKnown = (LinearLayout)findViewById(R.id.linearLayOutButtonsKnown);
        linearLayOutDynamic = (LinearLayout)findViewById(R.id.linearLayOutDynamic);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
        textViewQuestion.setTextSize(db.getTextSizeQuestions());
        textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
        textViewAnswer.setTextSize(db.getTextSizeAnswers());
        textViewQuestionCounter = (TextView) findViewById(R.id.textViewQuestionCounter);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonKnown = (Button) findViewById(R.id.buttonKnown);
        buttonNotKnown = (Button) findViewById(R.id.buttonNotKnown);
        buttonHint = (Button) findViewById(R.id.buttonHint);
        buttonNext.setEnabled(false);

        alphaAnimation.setDuration(300);
        translateAnimation.setDuration(600);

        context = getApplicationContext();

        drawableBorderHighlight = getResources().getDrawable(R.drawable.border_shape_highlight);

        //Settings
        isShowHint = db.isShowHint();
        if (!isShowHint) {
            buttonHint.setVisibility(View.GONE);
        } else {
            buttonHint.setVisibility(View.VISIBLE);
        }
        isShowKnownBar = db.isShowBar();
        setKnownBarVisibility();
        isChangeMultipleChoiceAnswers = db.isOrderMultipleChoiceAnswers();
        isViewRandomCards = db.isViewRandomCards();
        isViewLastDrawer = db.isViewCardsOfLastDrawer();
        orderType = db.getCardsOrderType();
        isNightMode = db.isNightMode(); /* @TODO */

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
                isTodos = extras.getBoolean("isTodos");
                isNewest = extras.getBoolean("newest");
                isOldest = extras.getBoolean("oldest");
                limit = extras.getString("limit");
                isDrawers = extras.getBoolean("drawers");
                list = extras.getIntegerArrayList("drawersList");
            }
        }

        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        if (!isNightMode) {
            WHITE = getResources().getColor(R.color.white);
            BLACK = getResources().getColor(R.color.black);
        } else {

            RelativeLayout relativeLayoutPlayActivity = (RelativeLayout)findViewById(R.id.relativeLayoutPlayActivity);
            relativeLayoutPlayActivity.setBackgroundColor(Color.BLACK);
            LinearLayout linearLayoutKnownBarHeader = (LinearLayout)findViewById(R.id.linearLayoutKnownBarHeader);
            LinearLayout linearLayoutSeekbar = (LinearLayout)findViewById(R.id.linearLayoutSeekbar);
            TextView textViewKnown = (TextView)findViewById(R.id.textViewKnown);
            textViewKnown.setTextColor(getResources().getColor(R.color.white));
            textViewPercent.setTextColor(getResources().getColor(R.color.white));
            textViewAnswer.setTextColor(getResources().getColor(R.color.white));
            linearLayoutKnownBarHeader.setBackgroundColor(Color.BLACK);
            linearLayoutSeekbar.setBackgroundColor(Color.BLACK);
            textViewQuestion.setBackgroundColor(Color.BLACK);
            textViewAnswer.setBackgroundColor(Color.BLACK);

            WHITE = Color.BLACK;
            BLACK = getResources().getColor(R.color.white);

        }
        GREEN = getResources().getColor(R.color.green_3);
        RED = getResources().getColor(R.color.red_3);

    }

    private void setKnownBarVisibility() {
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutKnownBarHeader);
        if (!isShowKnownBar) {
            knownBarVisibility = View.GONE;
        } else {
            knownBarVisibility = View.VISIBLE;
        }
        seekBar.setVisibility(knownBarVisibility);
        linearLayout.setVisibility(knownBarVisibility);
        buttonNext.setEnabled(isShowKnownBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSeekBarChangeListener();
        openDb();
        //cards = db.getCardsFromMarked();
        if (isTodos) {
            cards = db.getTodosCards(isNewest, isOldest, limit, isDrawers, list);
            Log.d("cards", cards.size() + " -> " +  cards.toString());
        } else if (!isViewRandomCards && !isTodos) {
            cards = db.getMarkedCards();
        } else if (isViewRandomCards && !isTodos){
            cards = db.getAllCardsRandomized();
        }
        Log.d("cards", cards.size() + " -> " +  cards.toString());
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
            List<Answer>answers = cards.get(cardsPosition).getAnswers();
            if(isChangeMultipleChoiceAnswers) {
                long seed = System.nanoTime();
                Collections.shuffle(answers, new Random(seed));
            }
            for (Answer ans: answers) {
                /*
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
                box.setTextColor(BLACK);
                box.setPadding(15,15,15,15);
                Log.d("Color", box.getDrawingCacheBackgroundColor() +", " + WHITE + ", " +box.getBackground() );
                */
                CheckBox box = new CheckBox(context);
                box.setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_icon));
                box.setText(ans.getAnswer());
                box.setTextSize(db.getTextSizeAnswers());
                box.setPadding(15, 15, 15, 15);
                box.setTextColor(BLACK);
                listCheckBox.add(box);
                linearLayOutDynamic.addView(box);
                linearLayOutDynamic.setBackgroundColor(WHITE);
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
                    textViewAnswer.startAnimation(alphaAnimation);
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
                            listCheckBox.get(i).setTextColor(GREEN);
                            if (listCheckBox.get(i).isChecked()) {
                                listCheckBox.get(i).setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_checked_right));
                            } else {
                                listCheckBox.get(i).setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked_right));
                            }
                            listCheckBox.get(i).startAnimation(alphaAnimation);
                        } else {
                            listCheckBox.get(i).setTextColor(RED);
                            listCheckBox.get(i).setChecked(curAns.get(i).isCorrect());
                            if (listCheckBox.get(i).isChecked()) {
                                listCheckBox.get(i).setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_checked_wrong));
                            } else {
                                listCheckBox.get(i).setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked_wrong));
                            }
                            listCheckBox.get(i).startAnimation(alphaAnimation);
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
                TextView title = new TextView(context);
                title.setText("Hint");
                title.setTextColor(getResources().getColor(R.color.white));
                title.setTextSize(db.getTextSizeQuestions());
                title.setPadding(45, 45, 45, 45);
                title.setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_selector_blue_actionbar));
                AlertDialog builder = new AlertDialog.Builder(this).setMessage(cards.get(cardsPosition).getHint()).setCustomTitle(title).show();
                /* @TODO Nightmode */
                TextView textView = (TextView) builder.findViewById(android.R.id.message);
                if (isNightMode) {
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setBackgroundColor(getResources().getColor(R.color.black));
                }
                textView.setTextSize(db.getTextSizeAnswers());
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
                //Card cardToRemove = cards.get(cardsPosition); // Alternative zum entfernen
                cards.add(cardsPosition, db.updateRating(cards.get(cardsPosition).getId(), 0, known, cards.get(cardsPosition).getDrawer()));
                //cards.remove(cardToRemove); Alternative
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

        //textViewQuestion.startAnimation(rotateAnimationFull);
        textViewQuestion.startAnimation(translateAnimation);
        //linearLayOutDynamic.startAnimation(alphaAnimationInv);
        //linearLayOutDynamic.startAnimation(rotateAnimationFull);
        linearLayOutDynamic.startAnimation(translateAnimation);

        cardsPosition ++;
        if (orderType == 1 && cardsPosition == cards.size()) { // loads the same cards again with the changed values
            cards = db.getMarkedCards();
        }
        cardsPosition %= cards.size(); // makes the roundtrip
        seekBar.setProgress(cards.get(cardsPosition).getRating());
        seekBar.setEnabled(false);
        buttonNext.setEnabled(false);

        switch(orderType) {
            case 0:
                int count0 = 0;
                int count1 = 0;
                int count2 = 0;
                int count3 = 0;
                int count4 = 0;
                int count5 = 0;
                List<Integer> drawerFillings = new ArrayList<Integer>(Collections.nCopies(6, 0));
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
                    drawerFillings = new ArrayList<>();
                    drawerFillings.add(count0);
                    drawerFillings.add(count1);
                    drawerFillings.add(count2);
                    drawerFillings.add(count3);
                    drawerFillings.add(count4);
                    drawerFillings.add(count5);
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
                    if (drawerFillings.get(currentDrawer) == 0) {
                        currentDrawer++;
                        currentDrawer %= 6;
                    }
                }
                break;
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
            return showStatisticsPrompt(true);
        } else if (id == R.id.action_play_stats) {
            return showStatisticsPrompt(false);
        } else if(id == R.id.action_play_swap_direction) {
            Collections.rotate(cards, cards.size()); /* @TODO rotate the list */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean showStatisticsPrompt(boolean isClose) {
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


        final Button buttonCloseCards = (Button)promptView.findViewById(R.id.buttonCloseCards);
        if (isClose) {
            buttonCloseCards.setVisibility(View.VISIBLE);
            buttonCloseCards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTodos) {
                        Intent myIntent = new Intent(PlayActivity.this, CatalogueActivity.class);
                        myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                        buttonCloseCards.setOnClickListener(null);
                        db.createStatistic(cards.size(), countKnown, countNotKnown, countViewed, countNotViewd, milliseconds);
                        startActivity(myIntent);
                    } else {
                        Intent intent = new Intent(PlayActivity.this, TodosActivity.class);
                        buttonCloseCards.setOnClickListener(null);
                        db.createStatistic(cards.size(), countKnown, countNotKnown, countViewed, countNotViewd, milliseconds);
                        startActivity(intent);
                    }
                }
            });
        }

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

        TextView textViewOverallRating = (TextView)promptView.findViewById(R.id.textViewOverallRating);

        LinearLayout linearLayoutDrawers = (LinearLayout)promptView.findViewById(R.id.linearLayoutDrawers);

        textViewOverallRating.setVisibility(View.VISIBLE);
        float overallRating = 0.0f;
        for (Card card : cards) {
            overallRating += card.getRating();
        }
        overallRating /= cards.size();
        textViewOverallRating.setText(overallRating+"%");

        if (orderType > 0) {
            linearLayoutDrawers.setVisibility(View.GONE);
        } else {
            LinearLayout linearLayoutOverallKnowledge = (LinearLayout)promptView.findViewById(R.id.linearLayoutOverallKnowledge);
            linearLayoutOverallKnowledge.setVisibility(View.GONE);
        }

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
        //curDrawer.setBackgroundDrawable(drawableBorderHighlight);
        //curDrawer.setPadding(30,30,30,30);
        //curDrawer.setTextColor(getResources().getColor(R.color.white));
        //curDrawer.setTextSize(30);
        //curDrawer.setTextColor(Color.parseColor("#555555"));
        curDrawer.setAlpha(1);
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
        TextView title = new TextView(context);
        title.setText("Statistics");
        title.setTextColor(getResources().getColor(R.color.white));
        title.setTextSize(db.getTextSizeQuestions());
        title.setPadding(45, 45, 45, 45);
        title.setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_selector_blue_actionbar));
        alertDialog.setCustomTitle(title);
        alertDialog.show();

        return true;
    }

    /**
     * Sets the listener for the known bar
     */
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

    /**
     * To set the right buttons enabled from the settings
     * @param button
     * @param isEnabled
     */
    private void enableButton(Button button, boolean isEnabled) {
        button.setEnabled(isEnabled);
    }
}
