package de.jonas_kraus.learn_app.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.jonas_kraus.learn_app.R;


/**
 * Created by Jonas on 15.09.2015.
 */
public class CustomListFileBrowser extends ArrayAdapter<List> {

    private final Context context;
    private final List values;

    public CustomListFileBrowser(Context context, List values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_single, parent, false);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        ImageView box = (ImageView) rowView.findViewById(R.id.checkBox);
        txtTitle.setText(values.get(position).toString());
        // change the icon for Windows and iPhone
        String s = values.get(position).toString();
        if (s.endsWith(".json")) {
            imageView.setImageResource(R.drawable.android_download);
        } else {
            imageView.setImageResource(R.drawable.android_folder);
        }

        return rowView;
    }
}

