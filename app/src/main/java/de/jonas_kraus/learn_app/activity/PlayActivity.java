package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    private LinearLayout linearLayoutHintAnswer;
    private SeekBar seekBar;
    private DbManager db;
    private Context context;
    private int currentCategoryParent;
    private ArrayList<Catalogue>checkedCatalogue;
    private List<Card> cards;
    private int cardsPosition = 0;
    private TextView textViewPercent, textViewAnswer, textViewQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        linearLayoutHintAnswer = (LinearLayout)findViewById(R.id.linearLayOutButtonsHintAnswer);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
        textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);

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
                Log.d("list", cards.size() + " "+cards.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSeekBarChangeListener();
        openDb();
        textViewQuestion.setText(cards.get(cardsPosition).getQuestion());
        textViewAnswer.setText(null);
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
                //linearLayoutHintAnswer.setVisibility(View.GONE);
                textViewAnswer.setText(cards.get(cardsPosition).getAnswers().get(0).getAnswer());
                break;
            case R.id.buttonHint:
                /*
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                break;
                        }
                    }
                };

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Hint").setTitle("Hint").setPositiveButton("Ok", dialogClickListener).show(); /* @TODO Set message to hint */

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete the card:").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                break;
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
