package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class cardActivity extends ActionBarActivity {

    private EditText editTextQuestion, editTextAnswer, editTextHint;
    private RadioGroup radioGroupType;
    private Button buttonAddAnswer, buttonCancel, buttonSave;
    private RadioButton radioSingle, radioMulti;
    private int currentCategoryParent;
    private Card.CardType cardType = Card.CardType.NOTECARD;
    private DbManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        editTextQuestion = (EditText)findViewById(R.id.editTextQuestion);
        editTextAnswer = (EditText)findViewById(R.id.editTextAnswer);
        editTextHint = (EditText)findViewById(R.id.editTextHint);
        radioGroupType = (RadioGroup)findViewById(R.id.radioGroupType);
        buttonAddAnswer = (Button)findViewById(R.id.buttonAddAnswerField);
        buttonCancel = (Button)findViewById(R.id.buttonCancel);
        buttonSave = (Button)findViewById(R.id.buttonSave);
        radioSingle = (RadioButton)findViewById(R.id.radioSingle);
        radioMulti = (RadioButton)findViewById(R.id.radioMultiplechoice);

        db = new DbManager(this);

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
            } else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
            }
        }

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        radioMulti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(),"checked: "+isChecked, Toast.LENGTH_SHORT).show();
                if (!isChecked) {
                    buttonAddAnswer.setVisibility(View.INVISIBLE);
                    cardType = Card.CardType.NOTECARD;
                } else {
                    buttonAddAnswer.setVisibility(View.VISIBLE);
                    cardType = Card.CardType.MULTIPLECHOICE;
                }
            }
        });
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.buttonCardNew:
                break;
            case R.id.buttonCancel:
                Intent myIntent = new Intent(cardActivity.this, CatalogueActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent);
                break;
            case R.id.buttonSave:
                List<Answer>answers = new ArrayList<Answer>();
                /* @TODO for loop over all edittexts */
                answers.add(new Answer(editTextAnswer.getText().toString()));
                Card card = new Card(cardType,editTextQuestion.getText().toString(), answers,false,0,editTextHint.getText().toString(),currentCategoryParent);
                db.createCard(card);
                Intent myIntent2 = new Intent(cardActivity.this, CatalogueActivity.class);
                myIntent2.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent2);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card, menu);
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
