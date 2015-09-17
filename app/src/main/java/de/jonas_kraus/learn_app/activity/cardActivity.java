package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class cardActivity extends ActionBarActivity implements TextWatcher {

    private EditText editTextQuestion, editTextAnswer, editTextHint, editTextFocus;
    private RadioGroup radioGroupType;
    private Button buttonAddAnswer, buttonDeleteAnswer, buttonCancel, buttonSave;
    private RadioButton radioSingle, radioMulti;
    private int currentCategoryParent;
    private Card.CardType cardType = Card.CardType.NOTECARD;
    private DbManager db;
    private LinearLayout LLEnterText;

    private CheckBox buttonQuestionBullet, checkboxFocus;

    private int _intMyLineCount =0;
    private Card editCard;
    private Boolean editMode = false;

    private List<EditText> editTextList = new ArrayList<EditText>();
    private List<CheckBox> checkBoxList =new ArrayList<CheckBox>();
    private List<LinearLayout> linearlayoutList=new ArrayList<LinearLayout>();
    private boolean checkModeChanged = false;
    private byte checkModeChangedCount = 0;
    private int white;

    private int lineNumber = 0;
    private String indent = "\t";

    private Drawable uncheckmark, checkmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Resources res = getResources();
        uncheckmark = res.getDrawable(R.drawable.uncheckmark);
        checkmark = res.getDrawable(R.drawable.checkmark);

        white = res.getColor(R.color.white);

        editTextQuestion = (EditText)findViewById(R.id.editTextQuestion);
        editTextAnswer = (EditText)findViewById(R.id.editTextAnswer);
        editTextHint = (EditText)findViewById(R.id.editTextHint);
        radioGroupType = (RadioGroup)findViewById(R.id.radioGroupType);
        buttonAddAnswer = (Button)findViewById(R.id.buttonAddAnswerField);
        buttonDeleteAnswer = (Button)findViewById(R.id.buttonDeleteAnswerField);
        buttonCancel = (Button)findViewById(R.id.buttonImportCancel);
        buttonSave = (Button)findViewById(R.id.buttonSave);
        radioSingle = (RadioButton)findViewById(R.id.radioSingle);
        radioMulti = (RadioButton)findViewById(R.id.radioMultiplechoice);
        buttonQuestionBullet = (CheckBox)findViewById(R.id.buttonEditorBullet);
        checkboxFocus = buttonQuestionBullet;
        editTextFocus = editTextQuestion;

        LLEnterText=(LinearLayout) findViewById(R.id.LlTitle);
        LLEnterText.setOrientation(LinearLayout.VERTICAL);

        editTextQuestion.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextFocus = editTextQuestion;
                return false;
            }
        });

        editTextAnswer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextFocus = editTextAnswer;
                return false;
            }
        });

        editTextHint.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextFocus = editTextHint;
                return false;
            }
        });

        db = new DbManager(this);

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent = -1;
                editMode = false;
            } else if (extras.getBoolean("addNewCard")) {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                editMode = false;
            }
            else {
                currentCategoryParent = extras.getInt("currentCategoryParent");
                editCard = db.getCardById(extras.getInt("cardId"));
                editMode = true;
                /*
                editCard = extras.getParcelable("card");
                if (editCard != null) {
                    editMode = true;
                }
                */
            }
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
                checkBoxList.get(i).setChecked(editCard.getAnswers().get(i).isCorrect());
                /*
                if (!checkBoxList.get(i).isChecked()) {
                    checkBoxList.get(i).setButtonDrawable(uncheckmark);
                }
                */
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
                    checkModeChangedCount++;
                    if (checkModeChangedCount > 1) {
                        checkModeChanged = true;
                    }
                    buttonAddAnswer.setVisibility(View.VISIBLE);
                    buttonDeleteAnswer.setVisibility(View.VISIBLE);
                    editTextAnswer.setVisibility(View.GONE);
                    cardType = Card.CardType.MULTIPLECHOICE;

                    if (!editMode) {
                        for (int i = 0; i < 2; i++) {
                            LLEnterText.addView(linearlayout(_intMyLineCount));
                            _intMyLineCount++;
                        }
                    } else if (checkModeChanged || (editCard.getType() == Card.CardType.NOTECARD)) { /* @TODO Mode changed set right checkmark*/
                        checkBoxList.removeAll(checkBoxList);
                        for (int i = 0; i < editCard.getAnswers().size(); i++) {
                            LLEnterText.addView(linearlayout(_intMyLineCount));
                            _intMyLineCount++;
                            checkBoxList.get(i).setButtonDrawable(R.drawable.checkbox_icon);
                            editTextList.get(i).setText(editCard.getAnswers().get(i).getAnswer());
                            checkBoxList.get(i).setChecked(editCard.getAnswers().get(i).isCorrect());


                            Log.d("check list", checkBoxList.get(i).isChecked() + "" + checkBoxList.get(i));
                            /*
                            if (!checkBoxList.get(i).isChecked()) {
                                checkBoxList.get(i).setButtonDrawable(uncheckmark);
                                checkBoxList.get(i).forceLayout();
                                Log.d("uncheck", "hier" + i + " " + checkBoxList.get(i).isChecked() + " " );
                            }
                            */
                        }

                        Log.d("editMode", "geladen" + editCard.getAnswers());
                        if (editCard.getAnswers().size() == 1) {
                            LLEnterText.addView(linearlayout(_intMyLineCount));
                            _intMyLineCount++;
                            checkBoxList.get(0).setChecked(true);
                            //checkBoxList.get(0).setButtonDrawable(checkmark);
                            checkBoxList.get(1).setChecked(false);
                            //checkBoxList.get(1).setButtonDrawable(uncheckmark);
                        }

                    }
                }
            }
        });
        editTextQuestion.addTextChangedListener(this);
        editTextAnswer.addTextChangedListener(this);
        editTextHint.addTextChangedListener(this);
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
        editTextQuestion.addTextChangedListener(null);
        editTextAnswer.addTextChangedListener(null);
        editTextHint.addTextChangedListener(null);
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
                    editTextList.remove(_intMyLineCount);
                    checkBoxList.remove(_intMyLineCount);
                    linearlayoutList.remove(_intMyLineCount);
                } else {
                    Toast.makeText(getApplicationContext(), "At least two answers must be set!", Toast.LENGTH_LONG);
                }
                break;
            case R.id.buttonImportCancel:
                Intent myIntent = new Intent(cardActivity.this, CatalogueActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent);
                break;
            case R.id.buttonSave:
                List<Answer>answers = new ArrayList<Answer>();
                /* @TODO for loop over all edittexts */

                if (editTextList.size() != 0) {
                    for (int i = 0; i < editTextList.size(); i++) {
                        Answer answer = new Answer(checkBoxList.get(i).isChecked(),editTextList.get(i).getText().toString());
                        answers.add(answer);
                    }
                } else {
                    answers.add(new Answer(editTextAnswer.getText().toString()));
                }
                Card card = new Card(cardType,editTextQuestion.getText().toString(), answers,false,0,editTextHint.getText().toString(),currentCategoryParent);
                if (editMode) {
                    card.setId(editCard.getId());
                    card.setMarked(editCard.isMarked());
                    card.getAnswers().get(0).setQuestionId(editCard.getAnswers().get(0).getQuestionId());
                    db.updateEditCard(card);
                } else {
                    db.createCard(card);
                }
                Intent myIntent2 = new Intent(cardActivity.this, CatalogueActivity.class);
                myIntent2.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent2);
                break;
            case R.id.buttonEditorBullet:
                if (!buttonQuestionBullet.isChecked()) {
                    indent = "\t";
                }
                break;
            case R.id.buttonEditorFlag:
                setUnicode("⚑");
                break;
            case R.id.buttonEditorFinger:
                setUnicode("☞");
                break;
            case R.id.buttonEditorCheck:
                setUnicode("✔");
                break;
            case R.id.buttonEditorCross:
                setUnicode("✘");
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_card, menu);
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
        editText.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setMinHeight(150);
        editText.setHint("Answer");
        //editText.setHint("Answer");
        //editText.setWidth(Layout.match_parent);

        editText.setBackgroundColor(white);
        editText.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1f) );
        editTextList.add(editText);
        return editText;
    }
    private CheckBox checkBox(int _intID)
    {
        final CheckBox txtviewAll=new CheckBox(this);
        txtviewAll.setId(_intID);
        txtviewAll.setBackgroundColor(white);
        checkBoxList.add(txtviewAll);
        return txtviewAll;
    }
    private LinearLayout linearlayout(int _intID)
    {
        LinearLayout LLMain=new LinearLayout(this);
        LLMain.setId(_intID);
        final CheckBox checkBox = checkBox(_intID);
        checkBox.setButtonDrawable(R.drawable.checkbox_icon);
        /*
        checkBox.setClickable(true);
        checkBox.setChecked(true);
        checkBox.setButtonDrawable(checkmark);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
                if (checkBox.isChecked()) {
                    checkBox.setButtonDrawable(checkmark);
                } else {
                    checkBox.setButtonDrawable(uncheckmark);
                }
            }
        });
        */
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(0, 0, 0, 15);

        LLMain.setLayoutParams(params);
        LLMain.addView(checkBox);
        LLMain.setBackgroundColor(white);
        LLMain.addView(editText(_intID));
        LLMain.setOrientation(LinearLayout.VERTICAL);
        LLMain.setMinimumHeight(150);
        linearlayoutList.add(LLMain);
        return LLMain;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        if (s.length()>0 && s.subSequence(s.length()-1, s.length()).toString().equalsIgnoreCase("\n")) {
            if (checkboxFocus.isChecked()) {
                editTextFocus.setText(editTextFocus.getText()+indent+"•");
                editTextFocus.setSelection(editTextFocus.getText().length());
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    public void setUnicode(String unicode) {
        int pos = editTextFocus.getSelectionEnd();
        String txt = editTextFocus.getText().toString();
        int length = txt.length();
        if (txt.length()>0 && pos < length) {
            Log.d("sub1", txt.substring(0,pos) +" "+pos+ " " +txt.length() +" " + txt.substring(pos, length));
            editTextFocus.setText(txt.substring(0,pos) + unicode + txt.substring(pos, length));
        } else {
            editTextFocus.setText(txt+unicode);
        }
        editTextFocus.setSelection(pos+1);
    }
}
