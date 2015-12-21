package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private static int CARDS_THRESHOLD = Catalogue.CARDS_THRASHOLD;;

    private File exportedFile;

    private List<Card> importedCards;

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
        //setListViewWithCatalogueByLevel(currentCategoryParent);
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

                //Card curCard;

                if (curCatalogue.getCategory() != null) { // Jump to subcategory
                    curCategory = curCatalogue.getCategory();
                    currentCategoryParent = curCategory.getId();
                    //Log.d("click on cat", "cat: " + curCatalogue.getCategory() + " ---- ");
                    //if (curCategory)
                    buttonCategoryBack.setText("◀ " + curCategory.getName());
                    /**
                     * Alternative to start next view
                     Intent myIntent = new Intent(CatalogueActivity.this, CatalogueActivity.class);
                     myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                     startActivity(myIntent);
                     */
                    setListViewWithCatalogueByLevel(currentCategoryParent);


                } else if (curCatalogue.getCard() != null) { /* @TODO Preview of Card */
                    //curCard = curCatalogue.getCard();
                    //Toast.makeText(context,"This is a card: "+curCard.getQuestion(), Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                    myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                    myIntent.putExtra("cardId", curCatalogue.getCard().getId());
                    //myIntent.putExtra("card", curCatalogue.getCard());
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

                            //AsyncExport asyncExport = new AsyncExport();
                            //asyncExport.doExport(curCatalogue, db, CatalogueActivity.this);

                            new AlertDialog.Builder(context)
                                    .setTitle("Export")
                                    .setMessage("Do you want to export to local storage or online?")
                                    .setPositiveButton("local", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                exportCategoryLocal(true, curCatalogue);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton("online", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            String pathToExportedFile = "";
                                            try {
                                                pathToExportedFile = exportCategoryLocal(false, curCatalogue);
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                            Intent myIntentFiles = new Intent(CatalogueActivity.this, FileBrowserActivity.class);
                                            myIntentFiles.putExtra("currentCategoryParent", currentCategoryParent);
                                            myIntentFiles.putExtra("isLocal", false);
                                            myIntentFiles.putExtra("pathToExportedFile", pathToExportedFile);
                                            startActivity(myIntentFiles);
                                        }
                                    })
                                    .setNeutralButton("cancle", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .setIcon(R.drawable.android_download)
                                    .show();
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();


                    if (curCategory.getName().length() > CHAR_THRESHOLD) {
                        alertDialog.setTitle("Edit: " + curCategory.getName().substring(0, CHAR_THRESHOLD - 2) + "...");
                    } else {
                        alertDialog.setTitle("Edit: " + curCategory.getName());
                    }
                    alertDialog.setIcon(R.drawable.categoryicon);
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
                    builder.setIcon(R.drawable.cardicon);
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

    private String exportCategoryLocal(final boolean isLocal, Catalogue curCatalogue) throws IOException, JSONException {

        final Catalogue catalogue = curCatalogue;
        final File fileNew;
        if (isLocal) {
            fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/flashcards");
        } else {
            fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/flashcards/.upload/.cache");
        }
        final String exportedFilePath = fileNew.getPath() + File.separator +catalogue.getCategory().getName().replace(" ","_")+".json";

        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Exporting Cards");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Thread mThread = new Thread() {
            @Override
            public void run() {

                catalogue.unsetIcon();
                Category category = catalogue.getCategory();
                List<Card> cards =  db.getCards(category);
                mProgressDialog.setProgress(25);
                Gson gson = new Gson();

                String stringCards = gson.toJson(cards);
                String stringCat = gson.toJson(category);
                FileOutputStream outputStream;
                mProgressDialog.setProgress(50);
                try {
                    if (! fileNew.exists()){
                        Log.d("dir ", "file dosent exist");
                        if (! fileNew.mkdirs()){
                            Log.e("dir ", "Directory not created");
                        }
                    }
                    mProgressDialog.setProgress(75);
                    /*
                    outputStream = new FileOutputStream(new File(fileNew.getPath() + File.separator +category.getName()+"_category.json"));
                    outputStream.write(stringCat.getBytes());
                    outputStream.close();
                    */
                    exportedFile = new File(exportedFilePath);
                    outputStream = new FileOutputStream(exportedFile);
                    outputStream.write(stringCards.getBytes());
                    outputStream.close();/*
                    outputStream = context.openFileOutput(filenameCards, Context.MODE_PRIVATE);
                    outputStream.write(stringCards.getBytes());
                    outputStream.close();*/
                    mProgressDialog.setProgress(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
        mProgressDialog.dismiss();
        return exportedFilePath;
    }

    private void importCategory() {

        new AlertDialog.Builder(context)
                .setTitle("Import")
                .setMessage("Do you want to import from local storage or online?")
                .setPositiveButton("local", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntentFiles = new Intent(CatalogueActivity.this, FileBrowserActivity.class);
                        myIntentFiles.putExtra("currentCategoryParent", currentCategoryParent);
                        myIntentFiles.putExtra("isLocal", true);
                        startActivity(myIntentFiles);
                    }
                })
                .setNegativeButton("online", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntentFiles = new Intent(CatalogueActivity.this, FileBrowserActivity.class);
                        myIntentFiles.putExtra("currentCategoryParent", currentCategoryParent);
                        myIntentFiles.putExtra("isLocal", false);
                        startActivity(myIntentFiles);
                    }
                })
                .setNeutralButton("cancle", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.android_download)
                .show();


        /*
         * is now in own activity
        Gson gson = new Gson();

        String filenameCat = "category.json";
        String filenameCards = "cards.json";
        File fileCat = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "flashcards" + File.separator + filenameCat);
        File fileCards = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "flashcards" + File.separator + filenameCards);
        String retCat = "";
        String retCards = "";

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(sdCardRoot, Environment.DIRECTORY_DOWNLOADS + File.separator + "flashcards");
        for (File f : yourDir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                Log.d("file name: ", name);
            }
        }

        if(!fileCards.exists()) {
            Toast.makeText(context,"No Cards file to import!", Toast.LENGTH_LONG).show();
            return;

        } else  {

            try {
                InputStream inputStreamCards = new FileInputStream(fileCards.getPath());

                InputStreamReader inputStreamReader = new InputStreamReader(inputStreamCards);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveStringCards = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveStringCards = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveStringCards);
                }
                inputStreamCards.close();
                retCards = stringBuilder.toString();

            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
            Type listType = new TypeToken<ArrayList<Card>>() {
            }.getType();
            importedCards = new Gson().fromJson(retCards, listType);

        }
        if(!fileCat.exists() && fileCards.exists()) {

            //Toast.makeText(context, "Category file dosen't exists", Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    db.createCards(importedCards, currentCategoryParent);
                    setListViewWithCatalogueByLevel(currentCategoryParent);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setIcon(categoryIconScaled);
            alertDialog.setTitle("Can't find Category!");
            if (curCategory != null) {
                alertDialog.setMessage("Proceed and import cards to current Category '"+curCategory.getName()+"'?");
            } else {
                alertDialog.setMessage("Proceed and import cards to current Category?");
            }

            alertDialog.show();

        } else if (fileCards.exists() && fileCat.exists()) {

            try {
                InputStream inputStreamCat = new FileInputStream(fileCat.getPath());

                if (inputStreamCat != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStreamCat);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveStringCat = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveStringCat = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveStringCat);
                    }

                    inputStreamCat.close();
                    retCat = stringBuilder.toString();

                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
                Toast.makeText(context, "copie cards.json and category.json to " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "flashcards").toURI(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
            Category importedCat = gson.fromJson(retCat, Category.class);
            if (importedCards != null) {
                importedCat.setName(importedCat.getName());
                importedCat.setParentId(currentCategoryParent); //make sure to bee in root @TODO make choosable
                db.createCards(importedCards, db.createCategory(importedCat).getId()); // Creates category and cards
                setListViewWithCatalogueByLevel(currentCategoryParent);
            }
        }
         */
    }

    /*
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
    */

    private void setListViewWithCatalogueByLevel(int level) {
        List<Category> categories = db.getCategoriesByLevel(level);
        List<Card> cards = db.getCardsByLevelForListView(level);
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
                        buttonCategoryBack.setText("◀ back");
                    } else {
                        buttonCategoryBack.setText("◀ "+curCategory.getName());
                    }
                } else {
                    Intent myIntent = new Intent(CatalogueActivity.this, Home.class);
                    startActivity(myIntent);
                }
                break;
            case R.id.buttonCardNew:
                Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                myIntent.putExtra("currentCategoryParent",currentCategoryParent);
                myIntent.putExtra("addNewCard",true);
                startActivity(myIntent);
                //makePromptAddCard();
                break;
            case R.id.startCards:
                checkedList = customListAdapter.getCheckedList();
                /* @TODO new threshold */
                if (db.isViewRandomCards()) {
                    CARDS_THRESHOLD = 0;
                }
                if (db.getMarkedCards().size() >= CARDS_THRESHOLD) {
                    Intent myIntentPlay = new Intent(CatalogueActivity.this, PlayActivity.class);
                    myIntentPlay.putExtra("currentCategoryParent", currentCategoryParent);
                    //myIntentPlay.putParcelableArrayListExtra("catalogue", checkedList);
                    startActivity(myIntentPlay);
                } else {
                    Toast.makeText(context,"You have to choose at least "+ CARDS_THRESHOLD +" cards!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.statistics:
                Intent myIntentStats = new Intent(CatalogueActivity.this, StatisticsActivity.class);
                myIntentStats.putExtra("currentCategoryParent",currentCategoryParent);
                startActivity(myIntentStats);
                break;
            case R.id.buttonImportImport: {
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
                db.createCategory(new Category(currentCategoryParent,input.getText().toString()));
                setListViewWithCatalogueByLevel(currentCategoryParent); // has to be reset to be correctly sorted
                setListViewWithCatalogueByLevel(currentCategoryParent); // has to be reset to be correctly sorted
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setIcon(R.drawable.categoryicon);
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
        //db.close();// must be in onDestroy course of possibly running export thread
        listViewCatalogue.setOnItemClickListener(null);
        listViewCatalogue.setOnItemLongClickListener(null);
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
