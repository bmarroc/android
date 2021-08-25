package com.app.bmarroc.yamba;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 42;
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        int count = intent.getIntExtra("count", 0);
        PendingIntent operation = PendingIntent.getActivity(context, -1, new Intent(context, Main2Activity.class), PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setContentTitle("New tweets!");
        notificationBuilder.setContentText("You've got "+count+" new tweets");
        notificationBuilder.setSmallIcon(R.drawable.icon_message);
        notificationBuilder.setContentIntent(operation);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setVibrate(new long[] {0, 1500});
        notificationBuilder.setLights(Color.CYAN, 2000, 2000);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
