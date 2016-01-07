package de.jonas_kraus.learn_app.Util;

import android.content.ClipData;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import de.jonas_kraus.learn_app.R;

/**
 * Created by Jonas on 07.01.2016.
 */
public class RowViewTouchListener implements View.OnTouchListener{
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //Log.d("view ccsds", view.toString() + "-----" + view.getParent().getParent().getParent().toString());
        ListView listView = (ListView)view.getParent().getParent().getParent();
        LinearLayout linearLayout = (LinearLayout)listView.getParent();
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            linearLayout.findViewById(R.id.llButtonsBottom).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.llButtonsDrag).setVisibility(View.VISIBLE);
            /*
            Button buttonDragDelete = (Button)linearLayout.findViewById(R.id.buttonDragDelete);
            Button buttonDragEdit = (Button)linearLayout.findViewById(R.id.buttonDragEdit);
            Button buttonDragExport = (Button)linearLayout.findViewById(R.id.buttonDragExport);
            //RowViewDragListener rowViewDragListener = new RowViewDragListener();

            buttonDragDelete.setOnDragListener(rowViewDragListener);
            buttonDragEdit.setOnDragListener(rowViewDragListener);
            buttonDragExport.setOnDragListener(rowViewDragListener);
            */
            return true;
        } else {
            return false;
        }
    }

}
