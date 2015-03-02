package de.jonas_kraus.learn_app.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class CatalogueHome extends ListActivity {
    private DbManager db;
    private ListView listViewCatalogue;
    private Button buttonAddCategory;
    private int currentCategoryParent = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue_home);

        openDb();
        List<Category> categories = db.getCategoriesByLevel(currentCategoryParent);
        List<Card> cards = db.getCardsByLevel(currentCategoryParent);
        List<Catalogue> catalogue = new ArrayList<Catalogue>();
        for (Category cat : categories) {
            catalogue.add(new Catalogue(cat));
        }
        for (Card card : cards) {
            catalogue.add(new Catalogue(card));
        }
        ArrayAdapter<Catalogue> adapter = new ArrayAdapter<Catalogue>(this, android.R.layout.simple_list_item_1, catalogue);
        setListAdapter(adapter);
        listViewCatalogue = getListView();
        buttonAddCategory = (Button) findViewById(R.id.buttonNewCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDb();
        addClickListenersToListView();
    }

    private void addClickListenersToListView() {
        listViewCatalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "On item click", Toast.LENGTH_SHORT).show();
            }
        });
        listViewCatalogue.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "On long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public void onClick(View view) {
        Log.d("CatalogueHome", "click: " + view.getId());
        final ArrayAdapter<Catalogue> adapter = (ArrayAdapter<Catalogue>) getListAdapter();
        switch(view.getId()) {
            case R.id.buttonNewCategory:
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View promptView = layoutInflater.inflate(R.layout.prompt_add_category, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setView(promptView);
                final EditText input = (EditText) promptView.findViewById(R.id.promptAddCategoryInput);

                alertDialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Category category = db.createCategory(new Category(currentCategoryParent,input.getText().toString()));
                        adapter.add(new Catalogue(category));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;
        }
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
