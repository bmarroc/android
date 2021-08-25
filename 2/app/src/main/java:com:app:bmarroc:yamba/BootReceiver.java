package com.app.bmarroc.yamba;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    private static final String DEFAULT_INTERVAL = "900000";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = prefs.getString("interval", "");
        if (TextUtils.isEmpty(interval)) {
            interval = DEFAULT_INTERVAL;
        }
        long time = Long.parseLong(interval);
        PendingIntent operation = PendingIntent.getService(context, -1, new Intent(context, RefreshService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (time == 0) {
            alarmManager.cancel(operation);
            Log.d(TAG, "cancelling repeat operation");
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), time, operation);
            Log.d(TAG, "setting repeat operation for: "+interval);
        }
        Log.d("BootReceiver", "onReceived");
    }
}
