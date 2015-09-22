package de.jonas_kraus.learn_app.Service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.sql.SQLException;

import de.jonas_kraus.learn_app.Data.Catalogue;
import de.jonas_kraus.learn_app.Database.DbManager;
import de.jonas_kraus.learn_app.R;
import de.jonas_kraus.learn_app.activity.CatalogueActivity;
import de.jonas_kraus.learn_app.activity.Home;
import de.jonas_kraus.learn_app.activity.PlayActivity;

public class DailyNotifyService extends IntentService {

    Intent myIntent;
    String label = "Daily reminder!", text = "You have to learn some Cards!\nClick to start!";

    public DailyNotifyService() {
        super("Daily Notification Servcie");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("daily service", "on handle");
        NotificationManager mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.cardicon, "Notify Alarm strart", System.currentTimeMillis());
        DbManager dbManager = new DbManager(this);
        try {
            dbManager.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbManager.getCardsCount() >= Catalogue.CARDS_THRASHOLD) {
            myIntent = new Intent(this , PlayActivity.class);
        } else {
            myIntent = new Intent(this, Home.class);
            text = "You have no cards to learn today! Click and create some new ones!";
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        notification.setLatestEventInfo(this, label, text, contentIntent);
        // Cancel the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0, notification);
        dbManager.close();
    }
}
