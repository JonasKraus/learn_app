package de.jonas_kraus.learn_app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class TodosActivity extends ActionBarActivity {

    CheckBox checkBoxNewestCards, checkBoxOldestCards, checkBoxDrawers, checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;
    Button buttonNewestMinus, buttonNewestPlus, buttonOldestMinus, buttonOldestPlus, buttonBack, buttonStart;
    EditText editTextNewest, editTextOldest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todos);

        checkBoxNewestCards = (CheckBox)findViewById(R.id.checkBoxTodosNewestCards);
        checkBoxOldestCards = (CheckBox)findViewById(R.id.checkBoxTodosOldestCards);
        checkBoxDrawers = (CheckBox)findViewById(R.id.checkBoxTodosDrawers);
        checkBox1 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer1);
        checkBox2 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer2);
        checkBox3 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer3);
        checkBox4 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer4);
        checkBox5 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer5);
        checkBox6 = (CheckBox)findViewById(R.id.checkBoxTodosDrawer6);
        buttonNewestMinus = (Button)findViewById(R.id.buttonTodosNewestMinus);
        buttonNewestPlus = (Button)findViewById(R.id.buttonTodosNewestPlus);
        buttonOldestMinus = (Button)findViewById(R.id.buttonTodosOldestMinus);
        buttonOldestPlus = (Button)findViewById(R.id.buttonTodosOldestPlus);
        editTextNewest = (EditText)findViewById(R.id.editTextTodosNewestNumberCards);
        editTextOldest = (EditText)findViewById(R.id.editTextTodosOldestNumberCards);
        buttonStart = (Button)findViewById(R.id.buttonTodosStart);
    }

    protected void onResume() {
        super.onResume();
        checkBoxDrawers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonStart.setEnabled(true);
                } else if (!checkBoxNewestCards.isChecked()&&!checkBoxOldestCards.isChecked()) {
                    buttonStart.setEnabled(false);
                }
            }
        });
        checkBoxOldestCards.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonStart.setEnabled(true);
                    checkBoxNewestCards.setChecked(false);
                } else if (!checkBoxNewestCards.isChecked()&&!checkBoxDrawers.isChecked()) {
                    buttonStart.setEnabled(false);
                }
            }
        });
        checkBoxNewestCards.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonStart.setEnabled(true);
                    checkBoxOldestCards.setChecked(false);
                } else if (!checkBoxDrawers.isChecked()&&!checkBoxOldestCards.isChecked()) {
                    buttonStart.setEnabled(false);
                }
            }
        });
    }

    protected void onPause() {
        super.onPause();
        checkBoxDrawers.setOnCheckedChangeListener(null);
        checkBoxNewestCards.setOnCheckedChangeListener(null);
        checkBoxOldestCards.setOnCheckedChangeListener(null);
    }

    public void onMinus(View view) {
        int id = view.getId();
        int num;
        switch (id) {
            case R.id.buttonTodosNewestMinus:
                num = Integer.parseInt(editTextNewest.getText().toString());
                if (num > 5) {
                    num--;
                    editTextNewest.setText(num+"");
                }
                break;
            case R.id.buttonTodosOldestMinus:
                num = Integer.parseInt(editTextOldest.getText().toString());
                if (num > 5) {
                    num--;
                    editTextOldest.setText(num+"");
                }
                break;
        }
    }
    public void onPlus(View view) {
        int id = view.getId();
        int num;
        switch (id) {
            case R.id.buttonTodosNewestPlus:
                num = Integer.parseInt(editTextNewest.getText().toString());
                num++;
                editTextNewest.setText(num+"");
                break;
            case R.id.buttonTodosOldestPlus:
                num = Integer.parseInt(editTextOldest.getText().toString());
                num++;
                editTextOldest.setText(num+"");
                break;
        }
    }

    public void onStartCards(View view) {
        String num = null;
        if (checkBoxNewestCards.isChecked()) {
            num = editTextNewest.getText().toString();
        } else if (checkBoxOldestCards.isChecked()) {
            num = editTextOldest.getText().toString();
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        if (checkBox1.isChecked()) {
            list.add(0);
        }
        if (checkBox2.isChecked()) {
            list.add(1);
        }
        if (checkBox3.isChecked()) {
            list.add(2);
        }
        if (checkBox4.isChecked()) {
            list.add(3);
        }
        if (checkBox5.isChecked()) {
            list.add(4);
        }
        if (checkBox6.isChecked()) {
            list.add(5);
        }
        DbManager db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Card> cardsTest = db.getTodosCards(checkBoxNewestCards.isChecked(),checkBoxOldestCards.isChecked(),num,checkBoxDrawers.isChecked(),list);
        if (cardsTest.size() > 0) {
            Intent intent = new Intent(TodosActivity.this, PlayActivity.class);
            intent.putExtra("isTodos", true);
            intent.putExtra("newest", checkBoxNewestCards.isChecked());
            intent.putExtra("oldest", checkBoxOldestCards.isChecked());
            intent.putExtra("limit", num);
            intent.putExtra("drawers", checkBoxDrawers.isChecked());
            intent.putIntegerArrayListExtra("drawersList", list);
            startActivity(intent);
        } else {
            Toast.makeText(this, "To less Cards!", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackClick(View view) {
        Intent myIntent = new Intent(TodosActivity.this, Home.class);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todos, menu);
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
