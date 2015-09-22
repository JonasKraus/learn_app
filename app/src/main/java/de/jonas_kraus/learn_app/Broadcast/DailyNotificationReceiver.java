package de.jonas_kraus.learn_app.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.jonas_kraus.learn_app.Service.DailyNotifyService;

public class DailyNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("Alarm Recieved!", "YAAAY");
        Intent i = new Intent(context, DailyNotifyService.class);
        context.startService(i);
    }
}
