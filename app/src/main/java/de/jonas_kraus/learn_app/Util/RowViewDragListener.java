package de.jonas_kraus.learn_app.Util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
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

import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.R;

/**
 * Created by Jonas on 07.01.2016.
 */
public class RowViewDragListener implements View.OnDragListener {

    private Context context;
    private Vibrator vibrator;
    private LinearLayout linearLayout;
    public RowViewDragListener(Context context) {
        this.context = context;
        this.vibrator = (Vibrator)this.context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    private void switchDragButtonsOff(View v) {
        Log.d("diese view", v.toString());
        if (!(v instanceof Button)) {
            Log.d("dropped on", "kein butt");
        } else if (v instanceof TableLayout) {
            Log.d("dropped on", "list of cards");
        } else {
            Log.d("dropped on", "ein butt");
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

    private void switchDragButtonsOn(View view) {
        Log.d("switch on voew", view.toString()+ "-----"+ view.getParent().getParent().toString());
        linearLayout = (LinearLayout)view.getParent().getParent();
        linearLayout.findViewById(R.id.llButtonsBottom).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.llButtonsDrag).setVisibility(View.VISIBLE);
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
                switchDragButtonsOn(v);
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //v.setBackgroundDrawable(enterShape);
                vibrator.vibrate(40);
                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.blue_1));
                }
                Log.d("Drag", "Hier entered");
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                //v.setBackgroundDrawable(normalShape);
                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                }
                Log.d("Drag", "Hier exited");
                break;
            case DragEvent.ACTION_DROP:
                if (tableRow != null) {
                    tableRow.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
                }
                Log.d("Drag", "Hier drop");
                switchDragButtonsOff(v);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d("Drag", "Hier ended");
                //v.invalidate();
            default:
                break;
        }
        return true;
    }
}
