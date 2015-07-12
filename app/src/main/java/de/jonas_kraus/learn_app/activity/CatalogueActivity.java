package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Answer;
import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.Util.CustomList;

public class CatalogueActivity extends ListActivity {
    private DbManager db;
    private ListView listViewCatalogue;
    private View promptView;
    private Button buttonAddCategory;
    private Button buttonCategoryBack;
    private int currentCategoryParent = -1;
    private Category curCategory;
    private Context context;
    private Bitmap categoryIcon, cardIcon;
    private final int CHAR_THRESHOLD = 30; // Maximum Chars that should be displayed in a Dialog's Title
    private Drawable categoryIconScaled, cardIconScaled;
    private CustomList customListAdapter;
    private ArrayList<Catalogue>checkedList;
    private int MARKED_THRESHOLD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);
        context = this;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        options.inPurgeable = true;
        options.inScaled = true;
        cardIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.cardicon,options);
        categoryIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.categoryicon, options);
        categoryIconScaled = new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(categoryIcon, 50, 50, true));
        cardIconScaled = new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(cardIcon, 50, 50, true));
        openDb();
        //listViewCatalogue = getListView();
        buttonAddCategory = (Button) findViewById(R.id.buttonCategoryNew);
        buttonCategoryBack = (Button) findViewById(R.id.buttonCategoryBack);
        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent= -1;
            } else {
                currentCategoryParent= extras.getInt("currentCategoryParent");
            }
        }
        setListViewWithCatalogueByLevel(currentCategoryParent);
        checkedList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDb();
        context = this;
        setListViewWithCatalogueByLevel(currentCategoryParent);
        addClickListenersToListView();
    }

    private void addClickListenersToListView() {
        listViewCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "On item click", Toast.LENGTH_SHORT).show();
                //Log.d("Adapter", getListAdapter()+"");
                Catalogue curCatalogue = (Catalogue) getListAdapter().getItem(position);
                //Log.d("View Item",curCat+"");
                //Category curCategory;
                Card curCard;

                if (curCatalogue.getCategory() != null) { // Jump to subcategory
                    curCategory = curCatalogue.getCategory();
                    currentCategoryParent = curCategory.getId();
                    setListViewWithCatalogueByLevel(currentCategoryParent);

                    buttonCategoryBack.setText("../" + curCategory.getName());

                } else if (curCatalogue.getCard() != null) { /* @TODO Preview of Card */
                    curCard = curCatalogue.getCard();
                    //Toast.makeText(context,"This is a card: "+curCard.getQuestion(), Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                    myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                    myIntent.putExtra("card", curCatalogue.getCard());
                    startActivity(myIntent);
                }

            }
        });
        listViewCatalogue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final Catalogue curCatalogue = (Catalogue) getListAdapter().getItem(position);

                if (curCatalogue.getCategory() != null) {
                    curCategory = curCatalogue.getCategory();

                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    promptView = layoutInflater.inflate(R.layout.prompt_add_category, null);

                    EditText name = (EditText) promptView.findViewById(R.id.promptAddCatalogueInput);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setView(promptView);

                    alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText name = (EditText) promptView.findViewById(R.id.promptAddCatalogueInput);
                            curCategory.setName(name.getText().toString());
                            db.updateCategory(curCategory);
                            setListViewWithCatalogueByLevel(currentCategoryParent);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).setNeutralButton("Export", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            try {
                                exportCategory(curCatalogue);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();


                    if (curCategory.getName().length() > CHAR_THRESHOLD) {
                        alertDialog.setTitle("Edit: " + curCategory.getName().substring(0, CHAR_THRESHOLD - 2) + "...");
                    } else {
                        alertDialog.setTitle("Edit: " + curCategory.getName());
                    }
                    alertDialog.setIcon(categoryIconScaled);
                    name.setTextKeepState(curCategory.getName());
                    alertDialog.show();

                } else if (curCatalogue.getCard() != null) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    db.deleteCard(curCatalogue.getCard().getId());
                                    setListViewWithCatalogueByLevel(currentCategoryParent);
                                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete: ");
                    builder.setIcon(cardIconScaled);
                    String quest = curCatalogue.getCard().getQuestion();
                    if (quest.length() > CHAR_THRESHOLD) {
                        builder.setTitle("Delete: " + quest.substring(0, CHAR_THRESHOLD - 2) + "...");
                    } else {
                        builder.setTitle("Delete: " + quest);
                    }
                    builder.setMessage("Are you sure you want to delete this card?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                return true;
            }
        });
    }

    private void exportCategory(Catalogue curCatalogue) throws IOException, JSONException {
        /*@TODO export to json */
        curCatalogue.unsetIcon();
        Category category = curCatalogue.getCategory();
        List<Card> cards =  db.getCards(category);
        Gson gson = new Gson();
        Log.d("gson", gson.toJson(curCatalogue)+" category_ "+ gson.toJson(category) + " cards_ " +  gson.toJson(cards)+ " dir "+ context.getFilesDir()+"");

        String filenameCat = "category.json";
        String filenameCards = "cards.json";
        String stringCards = gson.toJson(cards);
        Log.d("to gson cards ", stringCards);
        String stringCat = gson.toJson(category);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filenameCat, Context.MODE_PRIVATE);
            ContentResolver cr = getContentResolver();
            File file1 = Environment.getDataDirectory();
            File file2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            String state = Environment.getExternalStorageState();
            Boolean aBoolean = false;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                aBoolean =  true;
            } else {
                aBoolean = false;
            }
            File fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "flashcards");
            if (! fileNew.exists()){
                Log.d("dir ", "file dosent exist");
                if (! fileNew.mkdirs()){
                    Log.e("dir ", "Directory not created");
                }
            }
            Log.d("content res",  " " + fileNew.exists() +  " " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +"category.json"));
            outputStream.write(stringCat.getBytes());
            outputStream.close();
            outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +"cards.json"));
            outputStream.write(stringCards.getBytes());
            outputStream.close();
            outputStream = openFileOutput(filenameCards, Context.MODE_PRIVATE);
            outputStream.write(stringCards.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importCategory() {
        Gson gson = new Gson();

        String filenameCat = "category.json";
        String filenameCards = "cards.json";
        File fileCat = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+File.separator+"flashcards"+File.separator+filenameCat);
        File fileCards = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+File.separator+"flashcards"+File.separator+filenameCards);
        String retCat = "";
        String retCards = "";

        ContentResolver cr = getContentResolver();
        //InputStream is = cr.openInputStream(imageUri);

        try {
            InputStream inputStreamCat = new FileInputStream(fileCat.getPath());

            if ( inputStreamCat != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStreamCat);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveStringCat = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveStringCat = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveStringCat);
                }

                inputStreamCat.close();
                retCat = stringBuilder.toString();

            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        try {
            InputStream inputStreamCards = new FileInputStream(fileCards.getPath());

            if ( inputStreamCards != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStreamCards);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveStringCards = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveStringCards = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveStringCards);
                }
                inputStreamCards.close();
                retCards = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        Category importedCat = gson.fromJson(retCat, Category.class);
        importedCat.setName(importedCat.getName() + " *imported*");
        //importedCat.setParentId(-1); //make sure to bee in root @TODO make choosable
        Type listType = new TypeToken<ArrayList<Card>>() {
        }.getType();
        List<Card> importedCards = new Gson().fromJson(retCards, listType);
        db.createCards(importedCards, db.createCategory(importedCat).getId()); // Creates category and cards
        setListViewWithCatalogueByLevel(currentCategoryParent);
    }

    @Deprecated
    private void makePromptAddCard() {

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
                adapter.add(new Catalogue(card, categoryIcon));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setListViewWithCatalogueByLevel(int level) {
        List<Category> categories = db.getCategoriesByLevel(level);
        List<Card> cards = db.getCardsByLevel(level);
        List<Catalogue> catalogue = new ArrayList<Catalogue>();

        for (Category cat : categories) {
            catalogue.add(new Catalogue(cat, categoryIcon));
        }
        for (Card card : cards) {
            catalogue.add(new Catalogue(card,cardIcon));
        }

        if (categories != null && categories.size() > 0) {
            curCategory = db.getParentCategory(categories.get(0).getParentId());
        } else if (cards != null && cards.size() > 0) {
            curCategory = db.getParentCategory(cards.get(0).getCategoryId());
        }
        currentCategoryParent = level;

        customListAdapter = new CustomList(CatalogueActivity.this, catalogue, db);
        setListAdapter(customListAdapter);
        listViewCatalogue=getListView();
        listViewCatalogue.setAdapter(customListAdapter);

    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.buttonCategoryNew:
                makeCategory();
                break;
            case R.id.buttonCategoryBack:
                if (curCategory != null) {
                    setListViewWithCatalogueByLevel(curCategory.getParentId());
                    if (curCategory == null) {
                        buttonCategoryBack.setText("back to home");
                    } else {
                        buttonCategoryBack.setText("../"+curCategory.getName());
                    }
                } else {
                    Intent myIntent = new Intent(CatalogueActivity.this, Home.class);
                    startActivity(myIntent);
                }
                break;
            case R.id.buttonCardNew:
                Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                myIntent.putExtra("currentCategoryParent",currentCategoryParent);
                startActivity(myIntent);
                //makePromptAddCard();
                break;
            case R.id.startCards:
                checkedList = customListAdapter.getCheckedList();
                /* @TODO new threshold */
                if (db.isViewRandomCards()) {
                    MARKED_THRESHOLD = 0;
                }
                if (db.getMarkedCards().size() >= MARKED_THRESHOLD) {
                    Intent myIntentPlay = new Intent(CatalogueActivity.this, PlayActivity.class);
                    myIntentPlay.putExtra("currentCategoryParent", currentCategoryParent);
                    //myIntentPlay.putParcelableArrayListExtra("catalogue", checkedList);
                    startActivity(myIntentPlay);
                } else {
                    Toast.makeText(context,"You have to choose at least "+MARKED_THRESHOLD+" cards!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.statistics:
                Intent myIntentStats = new Intent(CatalogueActivity.this, StatisticsActivity.class);
                myIntentStats.putExtra("currentCategoryParent",currentCategoryParent);
                startActivity(myIntentStats);
                break;
            case R.id.buttonImport: {
                importCategory();
            }
        }
    }

    private void makeCategory() {
        //final ArrayAdapter<Catalogue> adapter = (ArrayAdapter<Catalogue>) getListAdapter();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        promptView = layoutInflater.inflate(R.layout.prompt_add_category, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.promptAddCatalogueInput);

        alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Category category = db.createCategory(new Category(currentCategoryParent,input.getText().toString()));
                setListViewWithCatalogueByLevel(currentCategoryParent); // has to be reset to be correctly sorted
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setIcon(categoryIconScaled);
        alertDialog.setTitle("Enter category name:");
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
        listViewCatalogue.setOnItemClickListener(null);
        listViewCatalogue.setOnItemLongClickListener(null);
        System.gc();
    }

}
