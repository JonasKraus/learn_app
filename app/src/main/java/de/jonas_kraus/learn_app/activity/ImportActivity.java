package de.jonas_kraus.learn_app.activity;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.Util.CustomList;

public class ImportActivity extends ListActivity {

    private ListView listViewCatalogue;
    private Button buttonCancel, buttonImport, buttonMarkAll;
    private TextView textViewNumCat, textViewNumCards;
    private Context context;
    private int currentCategoryParent;
    private CustomList customListAdapter;
    private ArrayList<Catalogue> catalogue;
    private List<Card> importedCards;
    private Gson gson;
    private DbManager db;
    private Boolean markAll = false;
    private int numCats, numCards = 0;

    private Bitmap categoryIcon, cardIcon;
    private Drawable categoryIconScaled, cardIconScaled;

    private String path;

    @Override
    protected void onResume() {
        super.onResume();
        context = this;

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }
        if (getIntent().hasExtra("currentCategoryParent")) {
            currentCategoryParent = getIntent().getExtras().getInt("currentCategoryParent");
        } else {
            currentCategoryParent = -1;
        }
        setTitle(path);

        db = new DbManager(this);
        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catalogue = getPopulationForListView();
        customListAdapter = new CustomList(ImportActivity.this, catalogue);
        setListAdapter(customListAdapter);
        listViewCatalogue = getListView();
        addClickListenersToListView();
        textViewNumCat.setText(numCats+"");
        textViewNumCards.setText(numCards+"");
    }

    private void addClickListenersToListView() {
        listViewCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
        listViewCatalogue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
    }

    private ArrayList<Catalogue> getPopulationForListView() {
        ArrayList<Catalogue> catalogueList = new ArrayList<Catalogue>();
        //File fileCat = new File(path);
        //File fileCards = new File(path);
        String retCat = "";
        String retCards = "";
        boolean isCategory = false;

        try {
            InputStream inputStreamCat = new FileInputStream(path);

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
        try {
            Gson gson = new Gson();
            Category importedCat = gson.fromJson(retCat, Category.class);
            importedCat.setMarked(true);
            catalogueList.add(new Catalogue(importedCat, categoryIcon));
            isCategory = true;
            numCats++;
        } catch (Exception e) {
            isCategory = false;
        }

        if (!isCategory) {
            try {
                InputStream inputStreamCards = new FileInputStream(path);

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
            for (Card card : importedCards) {
                card.setMarked(true);
                catalogueList.add(new Catalogue(card, cardIcon));
                numCards++;
            }
        }
        return catalogueList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        //buttonImport = (Button)findViewById(R.id.buttonImportImport);
        buttonMarkAll = (Button)findViewById(R.id.buttonImportMarkAll);
        textViewNumCat = (TextView)findViewById(R.id.textViewNumCat);
        textViewNumCards = (TextView)findViewById(R.id.textViewNumCards);

        Bundle extras;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                currentCategoryParent= -1;
            } else {
                currentCategoryParent= extras.getInt("currentCategoryParent");
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        options.inPurgeable = true;
        options.inScaled = true;
        categoryIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.categoryicon, options);
        categoryIconScaled = new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(categoryIcon, 50, 50, true));
        //cardIconScaled = new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(cardIcon, 50, 50, true));
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.buttonImportCancel:
                Intent myIntent = new Intent(ImportActivity.this, CatalogueActivity.class);
                myIntent.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntent);
                break;
            case R.id.buttonImportImport:
                for(Catalogue c : catalogue) {
                    if (c.getCategory() != null) {
                        if (c.getCategory().isMarked()) {
                            c.getCategory().setParentId(currentCategoryParent);
                            db.createCategory(c.getCategory());
                        }
                    } else {
                        if (c.getCard().isMarked()) {
                            c.getCard().setCategoryId(currentCategoryParent);
                            db.createCard(c.getCard());
                        }
                    }
                }

                Intent myIntentImported = new Intent(ImportActivity.this, CatalogueActivity.class);
                myIntentImported.putExtra("currentCategoryParent", currentCategoryParent);
                startActivity(myIntentImported);
                break;
            case R.id.buttonImportMarkAll:
                for(Catalogue c : catalogue) {
                    c.setMark(markAll);
                }
                customListAdapter = new CustomList(ImportActivity.this, catalogue, db);
                setListAdapter(customListAdapter);
                listViewCatalogue=getListView();
                listViewCatalogue.setAdapter(customListAdapter);
                if (markAll) {
                    buttonMarkAll.setText("Unmark all");
                } else {
                    buttonMarkAll.setText("Mark all");
                }
                markAll = !markAll;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_import, menu);
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
