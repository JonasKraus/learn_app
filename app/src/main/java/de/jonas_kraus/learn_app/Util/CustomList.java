package de.jonas_kraus.learn_app.Util;

/**
 * Created by Jonas on 03.03.2015.
 */

import android.app.Activity;
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
import de.jonas_kraus.learn_app.R;

public class CustomList extends ArrayAdapter<Catalogue> {
    private final Activity context;
    private final List<Catalogue> catalogue;
    private final int imgCat = R.drawable.categoryicon;
    private final int imgCard = R.drawable.cardicon;

    public CustomList(Activity context,List<Catalogue> catalogue) {
        super(context, R.layout.list_single, catalogue);
        this.context = context;
        this.catalogue = catalogue;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        Category category = catalogue.get(position).getCategory();
        Card card = catalogue.get(position).getCard();

        if (category != null) {
            txtTitle.setText(category.getName());
            imageView.setImageResource(imgCat);
        } else {
            txtTitle.setText(card.getQuestion());
            imageView.setImageResource(imgCard);
        }
        return rowView;
    }
}