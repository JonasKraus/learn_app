package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class CatalogueActivity extends ListActivity {
    private DbManager db;
    private ListView listViewCatalogue;
    private TextView textViewPrompt;
    private View promptView;
    private Button buttonAddCategory;
    private int currentCategoryParent = -1;
    private Category curCategory;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);

        openDb();
        List<Category> categories = db.getCategoriesByLevel(currentCategoryParent);
        List<Card> cards = db.getCardsByLevel(currentCategoryParent);
        List<de.jonas_kraus.learn_app.Data.Catalogue> catalogue = new ArrayList<de.jonas_kraus.learn_app.Data.Catalogue>();
        for (Category cat : categories) {
            catalogue.add(new de.jonas_kraus.learn_app.Data.Catalogue(cat));
        }
        for (Card card : cards) {
            catalogue.add(new de.jonas_kraus.learn_app.Data.Catalogue(card));
        }
        ArrayAdapter<de.jonas_kraus.learn_app.Data.Catalogue> adapter = new ArrayAdapter<de.jonas_kraus.learn_app.Data.Catalogue>(this, android.R.layout.simple_list_item_1, catalogue);
        setListAdapter(adapter);
        listViewCatalogue = getListView();
        buttonAddCategory = (Button) findViewById(R.id.buttonCategoryNew);

        context = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDb();
        addClickListenersToListView();
        context = this;
    }

    private void addClickListenersToListView() {
        listViewCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "On item click", Toast.LENGTH_SHORT).show();

                Catalogue curCatalogue = (Catalogue) getListAdapter().getItem(position);
                //Log.d("View Item",curCat+"");
                //Category curCategory;
                Card curCard;

                if ( curCatalogue.getCategory() != null ) {
                    curCategory = curCatalogue.getCategory();
                    currentCategoryParent = curCategory.getId();

                    setCatalogueByLevel(currentCategoryParent);

                } else if (curCatalogue.getCard() != null ) {
                    curCard = curCatalogue.getCard();
                    Toast.makeText(context,"This is a card: "+curCard.getQuestion(), Toast.LENGTH_SHORT).show();
                    /* @TODO */
                }

            }
        });
        listViewCatalogue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                Catalogue curCatalogue = (Catalogue) getListAdapter().getItem(position);
                //Category curCategory;
                Card curCard;

                if ( curCatalogue.getCategory() != null ) {
                    curCategory = curCatalogue.getCategory();
                    currentCategoryParent = curCategory.getId();
                    makeCard();
                }  else if (curCatalogue.getCard() != null ) {
                    curCard = curCatalogue.getCard();
                    Toast.makeText(context,"(long Click)This is a card: "+curCard.getQuestion(), Toast.LENGTH_SHORT).show();
                    /* TODO */
                }

                return true;
            }
        });
    }

    private void makeCard() {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        promptView = layoutInflater.inflate(R.layout.prompt_add_card, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        final ArrayAdapter<Catalogue> adapter = (ArrayAdapter<Catalogue>) getListAdapter();

        alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final EditText inputQuestion = (EditText) promptView.findViewById(R.id.promptEditTextQuestion);
                final EditText inputAnswer = (EditText) promptView.findViewById(R.id.promptEditTextAnswer);
                final EditText inputHint = (EditText) promptView.findViewById(R.id.promptEditTextHint);
                final RadioGroup group = (RadioGroup) findViewById(R.id.promptRadioGroup);
                final RadioButton multiple = (RadioButton) findViewById(R.id.promptRadioButtonMultipleChoice);
                final RadioButton text = (RadioButton) findViewById(R.id.promptRadioButtonText);
                Card.CardType cardType = Card.CardType.MULTIPLECHOICE;
                List<Answer> answers = new ArrayList<Answer>();
                if (text == null || text.isChecked()) {
                    cardType = Card.CardType.NOTECARD;
                    answers.add(new Answer(inputAnswer.getText().toString()));
                } else {
                            /* @TODO */
                }

                Card card = db.createCard(new Card(cardType, inputQuestion.getText().toString(), answers, false, 0, inputHint.getText().toString(), currentCategoryParent));
                adapter.add(new Catalogue(card));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setCatalogueByLevel(int level) {
        List<Category> categories = db.getCategoriesByLevel(level);
        List<Card> cards = db.getCardsByLevel(level);
        List<de.jonas_kraus.learn_app.Data.Catalogue> catalogue = new ArrayList<de.jonas_kraus.learn_app.Data.Catalogue>();
        for (Category cat : categories) {
            catalogue.add(new de.jonas_kraus.learn_app.Data.Catalogue(cat));
        }
        for (Card card : cards) {
            catalogue.add(new de.jonas_kraus.learn_app.Data.Catalogue(card));
        }
        ArrayAdapter<de.jonas_kraus.learn_app.Data.Catalogue> adapter = new ArrayAdapter<de.jonas_kraus.learn_app.Data.Catalogue>(context, android.R.layout.simple_list_item_1, catalogue);
        setListAdapter(adapter);
        if (categories.size() > 0) {
            curCategory = db.getParentCategory(categories.get(0).getParentId());
        } else if (cards.size() > 0) {
            curCategory = db.getParentCategory(cards.get(0).getCategoryId());
        }
        currentCategoryParent = level;
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.buttonCategoryNew:

                makeCategory();

                break;
            case R.id.buttonCategoryBack:
                if (curCategory != null) {
                    Log.d("CurParentId before ",currentCategoryParent+"");
                    setCatalogueByLevel(curCategory.getParentId());
                    Log.d("CurParentId after ",currentCategoryParent+"");
                } else {
                    Intent myIntent = new Intent(CatalogueActivity.this, Home.class);
                    startActivity(myIntent);
                }
                break;
            case R.id.buttonCardNew:
                makeCard();
                break;
        }
    }

    private void makeCategory() {
        //final ArrayAdapter<Catalogue> adapter = (ArrayAdapter<Catalogue>) getListAdapter();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        promptView = layoutInflater.inflate(R.layout.prompt_add_category, null);
        textViewPrompt = (TextView) findViewById(R.id.textViewPrompt);

        //textViewPrompt.setText("Enter category name:");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.promptAddCatalogueInput);

        alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Category category = db.createCategory(new Category(currentCategoryParent,input.getText().toString()));
                //adapter.add(new de.jonas_kraus.learn_app.Data.Catalogue(category));
                setCatalogueByLevel(currentCategoryParent); // has to be reset to be correctly sorted
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
        buttonAddCategory.setOnClickListener(null);
        listViewCatalogue.setOnItemClickListener(null);
        listViewCatalogue.setOnItemLongClickListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalogue_home, menu);
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
