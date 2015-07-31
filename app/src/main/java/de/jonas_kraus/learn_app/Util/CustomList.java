package de.jonas_kraus.learn_app.Util;

/**
 * Created by Jonas on 03.03.2015.
 */

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.jonas_kraus.learn_app.Data.Card;
import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Data.Category;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;

public class CustomList extends ArrayAdapter<Catalogue> {
    private final Activity context;
    private final List<Catalogue> catalogue;
    private final int imgCat = R.drawable.categoryicon;
    private final int imgCard = R.drawable.cardicon;
    private boolean checked;
    private  Drawable check,uncheck;
    private ArrayList<Catalogue>checkedList;
    private List<Integer>checkedListPos;
    private DbManager db;

    public CustomList(Activity context,List<Catalogue> catalogue, DbManager db) {
        super(context, R.layout.list_single, catalogue);
        this.db = db;
        this.context = context;
        this.catalogue = catalogue;
        this.checkedList = new ArrayList<Catalogue>();
        //this.marksFromDb = db.getMarks();
        this.checkedListPos = new ArrayList<Integer>();
        check = context.getResources().getDrawable( R.drawable.checkmark );
        uncheck = context.getResources().getDrawable( R.drawable.checkmark_shadow );
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        ImageView box = (ImageView) rowView.findViewById(R.id.checkBox);
        box.setTag(position);
        if (!checkedListPos.isEmpty() && checkedListPos.contains(position)) {
            box.setBackgroundDrawable(check);

        } else {
            box.setBackgroundDrawable(uncheck);
        }
        Category category = catalogue.get(position).getCategory();
        Card card = catalogue.get(position).getCard();

        if (category != null) {
            txtTitle.setText(category.getName());
            imageView.setImageResource(imgCat);
            if(category.isMarked()) {
                box.setBackgroundDrawable(check);
            } else {
                box.setBackgroundDrawable(uncheck);
            }
        } else {
            txtTitle.setText(card.getQuestion());
            imageView.setImageResource(imgCard);
            if(card.isMarked()) {
                box.setBackgroundDrawable(check);
            } else {
                box.setBackgroundDrawable(uncheck);
            }
        }

        //Log.d("box list","checked "+checkedListPos.toString());

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView box = (ImageView) v.findViewById(R.id.checkBox);
                int position = Integer.parseInt(box.getTag().toString());
                Category category = catalogue.get(position).getCategory();
                Card card = catalogue.get(position).getCard();
                //checked = Boolean.parseBoolean(box.getTag().toString());
                //box.setTag(checked);
                Drawable drawable = box.getBackground();
                if (drawable == uncheck) {
                    box.setBackgroundDrawable(check);
                    checkedList.add(catalogue.get(position));
                    checkedListPos.add(position);
                    //db.createMarks(catalogue.get(position));
                    db.markCardOrCategory(catalogue.get(position));
                } else {
                    box.setBackgroundDrawable(uncheck);
                    checkedList.remove(catalogue.get(position));
                    checkedListPos.remove((Integer)position);
                    //db.deleteMark(catalogue.get(position));
                    db.unmarkCardOrCategory(catalogue.get(position));
                }
                //Log.d("Click",card +" "+ category + " equals? " + (drawable +" ," + check+" ," + uncheck));
            }
        });

        return rowView;
    }

    public ArrayList<Catalogue> getCheckedList() {
        return checkedList;
    }

    public List<Integer> getCheckedListPos() {
        return checkedListPos;
    }

}