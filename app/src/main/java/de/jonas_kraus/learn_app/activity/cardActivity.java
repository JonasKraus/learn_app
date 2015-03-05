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

import de.jonas_kraus.learn_app.R;

public class cardActivity extends ActionBarActivity {

    EditText editTextQuestion, editTextAnswer;
    RadioGroup radioGroupType;
    Button buttonAddAnswer, buttonCancel, buttonSave;
    RadioButton radioSingle, radioMulti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        editTextQuestion = (EditText)findViewById(R.id.editTextQuestion);
        editTextAnswer = (EditText)findViewById(R.id.editTextAnswer);
        radioGroupType = (RadioGroup)findViewById(R.id.radioGroupType);
        buttonAddAnswer = (Button)findViewById(R.id.buttonAddAnswerField);
        buttonCancel = (Button)findViewById(R.id.buttonCancle);
        buttonSave = (Button)findViewById(R.id.buttonSave);
        radioSingle = (RadioButton)findViewById(R.id.radioSingle);
        radioMulti = (RadioButton)findViewById(R.id.radioMultiplechoice);

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
                } else {
                    buttonAddAnswer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.buttonCardNew:
                break;
            case R.id.buttonCancle:
                Intent myIntent = new Intent(cardActivity.this, CatalogueActivity.class);
                startActivity(myIntent);
                break;
            case R.id.buttonSave:
                Intent myIntent2 = new Intent(cardActivity.this, CatalogueActivity.class);
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
