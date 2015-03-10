package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
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
    private TextView textViewAnswer;
    private RadioGroup radioGroupType;
    private Button buttonAddAnswer, buttonDeleteAnswer, buttonCancel, buttonSave;
    private RadioButton radioSingle, radioMulti;
    private int currentCategoryParent;
    private Card.CardType cardType = Card.CardType.NOTECARD;
    private DbManager db;
    private LinearLayout LLEnterText;

    private int _intMyLineCount =0;
    private Card editCard;
    private Boolean editMode = false;

    private List<EditText> editTextList = new ArrayList<EditText>();
    private List<CheckedTextView> textviewList=new ArrayList<CheckedTextView>();
    private List<LinearLayout> linearlayoutList=new ArrayList<LinearLayout>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        editTextQuestion = (EditText)findViewById(R.id.editTextQuestion);
        editTextAnswer = (EditText)findViewById(R.id.editTextAnswer);
        editTextHint = (EditText)findViewById(R.id.editTextHint);
        radioGroupType = (RadioGroup)findViewById(R.id.radioGroupType);
        buttonAddAnswer = (Button)findViewById(R.id.buttonAddAnswerField);
        buttonDeleteAnswer = (Button)findViewById(R.id.buttonDeleteAnswerField);
        buttonCancel = (Button)findViewById(R.id.buttonCancel);
        buttonSave = (Button)findViewById(R.id.buttonSave);
        radioSingle = (RadioButton)findViewById(R.id.radioSingle);
        radioMulti = (RadioButton)findViewById(R.id.radioMultiplechoice);
        textViewAnswer = (TextView) findViewById(R.id.textViewAnswer);
        
        LLEnterText=(LinearLayout) findViewById(R.id.LlTitle);
        LLEnterText.setOrientation(LinearLayout.VERTICAL);

        db = new DbManager(this);

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
            } else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                editCard = extras.getParcelable("card");
                if (editCard != null) {
                    editMode = true;
                }
            }
        }

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void setEditCard() {
        editTextQuestion.setText(editCard.getQuestion());
        if (editCard.getType() == Card.CardType.MULTIPLECHOICE) {
            radioMulti.setChecked(true);
            for(int i = 0; i< editCard.getAnswers().size(); i++) {
                LLEnterText.addView(linearlayout(_intMyLineCount));
                _intMyLineCount++;
                editTextList.get(i).setText(editCard.getAnswers().get(i).getAnswer());
                textviewList.get(i).setChecked(editCard.getAnswers().get(i).isCorrect());
                if (!textviewList.get(i).isChecked()) {
                    textviewList.get(i).setCheckMarkDrawable(R.drawable.uncheckmark);
                }
            }
        } else {
            editTextAnswer.setText(editCard.getAnswers().get(0).getAnswer());
        }
        editTextHint.setText(editCard.getHint());
    }

    @Override
    public void onResume() {
        super.onResume();
        radioMulti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {
                    buttonAddAnswer.setVisibility(View.GONE);
                    buttonDeleteAnswer.setVisibility(View.GONE);
                    editTextAnswer.setVisibility(View.VISIBLE);
                    cardType = Card.CardType.NOTECARD;
                    editTextList = new ArrayList<EditText>();
                    LLEnterText.removeAllViews();
                    //textViewAnswer.setVisibility(View.VISIBLE);
                } else {
                    buttonAddAnswer.setVisibility(View.VISIBLE);
                    buttonDeleteAnswer.setVisibility(View.VISIBLE);
                    editTextAnswer.setVisibility(View.GONE);
                    cardType = Card.CardType.MULTIPLECHOICE;
                    textViewAnswer.setVisibility(View.GONE);

                    if (!editMode) { /* @TODO Mode changed */
                        for (int i = 0; i < 2; i++) {
                            LLEnterText.addView(linearlayout(_intMyLineCount));
                            _intMyLineCount++;
                        }
                    }
                }
            }
        });
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (editMode) {
            setEditCard();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.buttonAddAnswerField:
                LLEnterText.addView(linearlayout(_intMyLineCount));
                _intMyLineCount++;
                break;
            case R.id.buttonDeleteAnswerField:
                if (_intMyLineCount > 2) {
                    _intMyLineCount--;
                    LLEnterText.removeView(linearlayoutList.get(_intMyLineCount));
                    linearlayoutList.remove(_intMyLineCount);
                } else {
                    Toast.makeText(getApplicationContext(), "At least two answers must be set!", Toast.LENGTH_LONG);
                }
                break;
            case R.id.buttonCancel:
                Intent myIntent = new Intent(cardActivity.this, CatalogueActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent);
                break;
            case R.id.buttonSave:
                List<Answer>answers = new ArrayList<Answer>();
                /* @TODO for loop over all edittexts */

                if (editTextList.size() != 0) {
                    for (int i = 0; i < editTextList.size(); i++) {
                        Answer answer = new Answer(textviewList.get(i).isChecked(),editTextList.get(i).getText().toString());
                        answers.add(answer);
                    }
                } else {
                    answers.add(new Answer(editTextAnswer.getText().toString()));
                }
                Card card = new Card(cardType,editTextQuestion.getText().toString(), answers,false,0,editTextHint.getText().toString(),currentCategoryParent);
                if (editMode) {
                    card.setId(editCard.getId());
                    card.getAnswers().get(0).setQuestionId(editCard.getAnswers().get(0).getQuestionId());
                    db.updateEditCard(card);
                } else {
                    db.createCard(card);
                }
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

    private EditText editText(int _intID) {
        EditText editText = new EditText(this);
        editText.setId(_intID);
        editText.setHeight(100);
        editText.setHint("Answer");
        //editText.setHint("Answer");
        //editText.setWidth(Layout.match_parent);

        editText.setBackgroundColor(Color.parseColor("#fefefe"));
        editText.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1f) );
        editTextList.add(editText);
        return editText;
    }
    private CheckedTextView textView(int _intID)
    {
        final CheckedTextView txtviewAll=new CheckedTextView(this);
        txtviewAll.setId(_intID);
        //txtviewAll.setText("Answer:");

        //txtviewAll.setTextColor(Color.RED);
        //txtviewAll.setTypeface(Typeface.DEFAULT_BOLD);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        txtviewAll.setLayoutParams(params);

        txtviewAll.setBackgroundColor(Color.parseColor("#fefefe"));

        textviewList.add(txtviewAll);
        return txtviewAll;
    }
    private LinearLayout linearlayout(int _intID)
    {
        LinearLayout LLMain=new LinearLayout(this);
        LLMain.setId(_intID);
        final CheckedTextView txtView = textView(_intID);
        txtView.setClickable(true);
        txtView.setChecked(true);
        txtView.setCheckMarkDrawable(R.drawable.checkmark);
        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtView.setChecked(!txtView.isChecked());
                if (txtView.isChecked()) {
                    txtView.setCheckMarkDrawable(R.drawable.checkmark);
                } else {
                    txtView.setCheckMarkDrawable(R.drawable.uncheckmark);
                }
            }
        });
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(0, 0, 0, 15);
        LLMain.setLayoutParams(params);
        LLMain.addView(txtView);
        LLMain.setBackgroundColor(Color.parseColor("#fefefe"));
        LLMain.addView(editText(_intID));
        LLMain.setOrientation(LinearLayout.VERTICAL);
        linearlayoutList.add(LLMain);
        return LLMain;

    }
}
