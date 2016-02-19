package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import de.jonas_kraus.learn_app.Util.RowViewDragListener;

public class CatalogueActivity extends ListActivity  {
    private DbManager db;
    private ListView listViewCatalogue;
    private View promptView;
    private Button buttonAddCategory, buttonAddCard, buttonAddImport, dropZoneDelete, dropZoneExport, dropZoneEdit;
    private Button buttonCategoryBack, buttonStats, buttonPlay, buttonRoundAddMenu;
    private LinearLayout linearLayoutAdd, llCard, llCat, llImport;;
    private LinearLayout llButtonsBottom;
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
        buttonAddCard = (Button) findViewById(R.id.buttonCardNew);
        buttonAddImport = (Button) findViewById(R.id.buttonImportImport);
        addScaledIconToButton(buttonAddCategory,R.drawable.categoryicon);
        addScaledIconToButton(buttonAddCard,R.drawable.cardicon);
        addScaledIconToButton(buttonAddImport, R.drawable.android_download);
        buttonCategoryBack = (Button) findViewById(R.id.buttonCategoryBack);
        buttonStats = (Button) findViewById(R.id.statistics);
        buttonPlay = (Button) findViewById(R.id.startCards);
        buttonRoundAddMenu = (Button) findViewById(R.id.buttonRoundAddMenu);
        linearLayoutAdd = (LinearLayout) findViewById(R.id.linearLayoutAdd);
        llButtonsBottom = (LinearLayout) findViewById(R.id.llButtonsBottom);
        llCat = (LinearLayout)findViewById(R.id.llNewCat);
        llCard = (LinearLayout)findViewById(R.id.llNewCard);
        llImport = (LinearLayout)findViewById(R.id.llImport);
        dropZoneDelete = (Button)findViewById(R.id.buttonDragDelete);
        dropZoneEdit = (Button)findViewById(R.id.buttonDragEdit);
        dropZoneExport = (Button)findViewById(R.id.buttonDragExport);


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

    private void addScaledIconToButton(Button button, int img_src) {
        Drawable drawable = getResources().getDrawable(img_src);
        drawable.setBounds(20, 0, (int) (drawable.getIntrinsicWidth() * 0.27 + 20),
                (int) (drawable.getIntrinsicHeight() * 0.27));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 1f, 1f);
        button.setCompoundDrawables(sd.getDrawable(), null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDb();
        context = this;
        setListViewWithCatalogueByLevel(currentCategoryParent);
        addClickListenersToListView();
        addDropZones();
    }

    private void addDropZones() {
        RowViewDragListener rowViewDragListener = new RowViewDragListener(this);
        dropZoneDelete.setOnDragListener(rowViewDragListener);
        dropZoneEdit.setOnDragListener(rowViewDragListener);
        dropZoneExport.setOnDragListener(rowViewDragListener);
    }

    private void unsetDropZones() {
        dropZoneDelete.setOnDragListener(null);
        dropZoneEdit.setOnDragListener(null);
        dropZoneExport.setOnDragListener(null);
    }

    private void addClickListenersToListView() {
        listViewCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Catalogue curCatalogue = (Catalogue) getListAdapter().getItem(position);

                if (curCatalogue.getCategory() != null) { // Jump to subcategory
                    curCategory = curCatalogue.getCategory();
                    currentCategoryParent = curCategory.getId();

                    buttonCategoryBack.setText("◀ " + curCategory.getName());
                    /**
                     * Alternative to start next view
                     Intent myIntent = new Intent(CatalogueActivity.this, CatalogueActivity.class);
                     myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                     startActivity(myIntent);
                     */
                    setListViewWithCatalogueByLevel(currentCategoryParent);

                } else if (curCatalogue.getCard() != null) {
                    Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                    myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                    myIntent.putExtra("cardId", curCatalogue.getCard().getId());
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
                Card.unsetCardsData(cards);
                String stringCards = gson.toJson(cards);
                //String stringCat = gson.toJson(category);
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



    }

    private void setListViewWithCatalogueByLevel(int level) {
        List<Category> categories = db.getCategoriesByLevel(level);
        List<Card> cards = db.getCardsByLevelForListView(level);
        //List<Card> cards = db.getCardsForListView(); recover of lost cards
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
        /* recover of lost cards
        try {
            exportCardsLocal(true);
            exportCardsLocal(false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }

    private String exportCardsLocal(final boolean isLocal) throws IOException, JSONException {

        //final Catalogue catalogue = curCatalogue;
        final File fileNew;
        if (isLocal) {
            fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/flashcards");
        } else {
            fileNew = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/flashcards/.upload/.cache");
        }
        final String exportedFilePath = fileNew.getPath() + File.separator +"Cards.json";

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

                //catalogue.unsetIcon();
                //Category category = catalogue.getCategory();
                List<Card> cards =  db.getMarkedCards();
                //Log.d("exp", " "+ cards.size());
                mProgressDialog.setProgress(25);
                Gson gson = new Gson();
                Card.unsetCardsData(cards);
                String stringCards = gson.toJson(cards);
                //String stringCat = gson.toJson(category);
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

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.llNewCat:
            case R.id.buttonCategoryNew:
                makeCategory();
                toggleButtonRoundAddMenu();
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
            case R.id.llNewCard:
            case R.id.buttonCardNew:
                if (currentCategoryParent == -1) {
                    Toast.makeText(this, "Please create and/or select a Category at First!", Toast.LENGTH_LONG).show();
                } else {
                    Intent myIntent = new Intent(CatalogueActivity.this, cardActivity.class);
                    myIntent.putExtra("currentCategoryParent",currentCategoryParent);
                    myIntent.putExtra("addNewCard",true);
                    startActivity(myIntent);
                    toggleButtonRoundAddMenu();
                }
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
            case R.id.llImport:
            case R.id.buttonImportImport:
                if (currentCategoryParent == -1) {
                    Toast.makeText(this, "Please create and/or select a Category at First!", Toast.LENGTH_LONG).show();
                } else {
                    importCategory();
                    toggleButtonRoundAddMenu();
                }
                break;
            case R.id.buttonRoundAddMenu:
                toggleButtonRoundAddMenu();
                break;
        }
    }

    private void toggleButtonRoundAddMenu() {
        if (linearLayoutAdd.getVisibility() == View.GONE) {
            linearLayoutAdd.setVisibility(View.VISIBLE);
            //linearLayoutAdd.bringToFront();
            llButtonsBottom.setEnabled(false);
            listViewCatalogue.setEnabled(false);
            listViewCatalogue.setAlpha(.3f);
            buttonRoundAddMenu.setBackgroundResource(R.drawable.pressed_circle_button);
            buttonCategoryBack.setEnabled(false);
            buttonStats.setEnabled(false);
            buttonPlay.setEnabled(false);
            RotateAnimation rotateAnimation = new RotateAnimation(0f,360f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            buttonRoundAddMenu.startAnimation(rotateAnimation);
            TranslateAnimation translateAnimation1 = new TranslateAnimation(-100,Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(-1000,Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            TranslateAnimation translateAnimation3 = new TranslateAnimation(-4000,Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            translateAnimation1.setDuration(600);
            translateAnimation2.setDuration(600);
            translateAnimation3.setDuration(600);
            llCat.startAnimation(translateAnimation1);
            llCard.startAnimation(translateAnimation2);
            llImport.startAnimation(translateAnimation3);
        } else {
            linearLayoutAdd.setVisibility(View.GONE);
            llButtonsBottom.setEnabled(true);
            listViewCatalogue.setEnabled(true);
            //listViewCatalogue.setAlpha(.8f);
            listViewCatalogue.setAlpha(1f);
            buttonRoundAddMenu.setBackgroundResource(R.drawable.idle_circle_button);
            buttonCategoryBack.setEnabled(true);
            buttonStats.setEnabled(true);
            buttonPlay.setEnabled(true);
            RotateAnimation rotateAnimation = new RotateAnimation(0f,-360f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            buttonRoundAddMenu.startAnimation(rotateAnimation);
            TranslateAnimation translateAnimation1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-100, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-1000, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            TranslateAnimation translateAnimation3 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-4000, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
            translateAnimation1.setDuration(600);
            translateAnimation2.setDuration(600);
            translateAnimation3.setDuration(600);
            llCat.startAnimation(translateAnimation1);
            llCard.startAnimation(translateAnimation2);
            llImport.startAnimation(translateAnimation3);
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
        unsetDropZones();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
