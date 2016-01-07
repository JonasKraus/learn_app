package de.jonas_kraus.learn_app.Util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.apache.http.util.LangUtils;

import java.util.List;

import de.jonas_kraus.learn_app.R;

/**
 * Created by Jonas on 07.01.2016.
 */
public class RowViewDragListener implements View.OnDragListener {

    private Context context;
    public RowViewDragListener(Context context) {
        this.context = context;
    }
    private void switchDragButtonsOff(View v) {
        LinearLayout linearLayout;
        Log.d("view butt", v.toString()+" "+v.getParent());
        if (!(v instanceof Button)) {
            ListView listView = (ListView)v.getParent();
            linearLayout = (LinearLayout)listView.getParent();
            Log.d("dropped on", "kein butt"+linearLayout.toString());
        } else {
            linearLayout = (LinearLayout)v.getParent().getParent();
            Log.d("dropped on", "ein butt"+linearLayout.toString());
        }

        /*
        Button buttonDragDelete = (Button)linearLayout.findViewById(R.id.buttonDragDelete);
        Button buttonDragEdit = (Button)linearLayout.findViewById(R.id.buttonDragEdit);
        Button buttonDragExport = (Button)linearLayout.findViewById(R.id.buttonDragExport);

        buttonDragDelete.setOnDragListener(null);
        buttonDragEdit.setOnDragListener(null);
        buttonDragExport.setOnDragListener(null);
        */
        linearLayout.findViewById(R.id.llButtonsBottom).setVisibility(View.VISIBLE);
        linearLayout.findViewById(R.id.llButtonsDrag).setVisibility(View.GONE);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();

        TableRow tableRow = null;
        if (v instanceof TableLayout) {
            tableRow = (TableRow) v.findViewById(R.id.single_list_row);
        }
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // do nothing
                Log.d("Drag", "Hier start");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //v.setBackgroundDrawable(enterShape);

                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.blue_1));
                }
                v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle_button_droptarget));

                Log.d("Drag", "Hier entered");
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                //v.setBackgroundDrawable(normalShape);
                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                }
                v.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle_button));

                Log.d("Drag", "Hier exited");
                break;
            case DragEvent.ACTION_DROP:
                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                }
                switchDragButtonsOff(v);
                Log.d("Drag", "Hier drop");
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d("Drag", "Hier ended");

            default:
                break;
        }
        return true;
    }
}
