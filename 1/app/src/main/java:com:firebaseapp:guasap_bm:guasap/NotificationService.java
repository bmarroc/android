package com.firebaseapp.guasap_bm.guasap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class NotificationService extends FirebaseMessagingService {
    private DatabaseProvider databaseProvider;
    private SharedPreferences __session;
    private FirebaseDatabase firebaseDatabase;

    @Override
    public void handleIntent(Intent intent) {
       
        Bundle remoteMessage = intent.getExtras();
        Log.d("[NotificationService]", "handleIntent "+remoteMessage);

        this.databaseProvider = new DatabaseProvider(this, "innerdatabase.db");
        this.databaseProvider.addDB();

        this.firebaseDatabase = FirebaseDatabase.getInstance();

        if (ChatActivity.isVisible()) {
            Log.d("[NotificationService]", "ChatActivity visible");
            this.onForegroundMessage(remoteMessage);
        } else {
            Log.d("[NotificationService]", "ChatActivity not visible");
            this.onBackgroundMessage(remoteMessage);
        }
    }

    public void onForegroundMessage(Bundle remoteMessage) {

        String from = remoteMessage.get("_from").toString();
        String to = remoteMessage.get("_to").toString();
        String messageKey = remoteMessage.get("messageKey").toString();
        String message = remoteMessage.get("message").toString();
        String sended = remoteMessage.get("sended").toString();
        String received = remoteMessage.get("received").toString();

        __session = getSharedPreferences("__session", MODE_PRIVATE);
        String sessionId = __session.getString("id", "");
        if (from.equals(sessionId)) {
            Log.d("[NotificationService]", "onForegroundMessage: FROM ME");

            String user = __session.getString("user", "");
            String store = to;
            this.databaseProvider.updateMessage(sessionId, store, messageKey, user, message, sended, received);

            if (to.equals(ChatActivity.getCurrentChat()[0])) {
                Log.d("[NotificationService]", "onForegroundMessage: CURRENTLY CHATING");

                sendBroadcast(new Intent("com.firebaseapp.guasap_bm.guasap.NEW_MESSAGE"));
            }
        } else {
            Log.d("[NotificationService]", "onForegroundMessage: NOT FROM ME");
            String user = NotificationService.this.databaseProvider.getUser(sessionId, from);
            if (!user.equals("")) {
                NotificationService.this.firebaseDatabase.getReference("Messages/"+from+"/"+sessionId+'/'+messageKey).removeValue();

                String store = from;
                messageKey = FirebaseDatabase.getInstance().getReference().push().getKey();
                String time = this.getTime();

                this.databaseProvider.addMessage(sessionId, store, messageKey, user, message, time);

                if (from.equals(ChatActivity.getCurrentChat()[0])) {
                    Log.d("[NotificationService]", "onForegroundMessage: CURRENTLY CHATING");

                    sendBroadcast(new Intent("com.firebaseapp.guasap_bm.guasap.NEW_MESSAGE"));
                } else {
                    Log.d("[NotificationService]", "onForegroundMessage: NOT CURRENTLY CHATING");

                    this.showNotification(user);
                }
            }
        }
    }

    public void onBackgroundMessage(Bundle remoteMessage) {
        
        String from = remoteMessage.get("_from").toString();
        String to = remoteMessage.get("_to").toString();
        String messageKey = remoteMessage.get("messageKey").toString();
        String message = remoteMessage.get("message").toString();
        String sended = remoteMessage.get("sended").toString();
        String received = remoteMessage.get("received").toString();

        __session = getSharedPreferences("__session", MODE_PRIVATE);
        String sessionId = __session.getString("id", "");

        if (from.equals(sessionId)) {
            Log.d("[NotificationService]", "onBackgroundMessage: FROM ME");
            String user = __session.getString("user", "");
            String store = to;
            this.databaseProvider.updateMessage(sessionId, store, messageKey, user, message, sended, received);

            ChatActivity.getNewMessagesFrom().put(to, true);
        } else {
            Log.d("[NotificationService]", "onBackgroundMessage: NOT FROM ME");
            String user = NotificationService.this.databaseProvider.getUser(sessionId, from);
            if (!user.equals("")) {
                NotificationService.this.firebaseDatabase.getReference("Messages/"+from+"/"+sessionId+'/'+messageKey).removeValue();

                String store = from;
                messageKey = FirebaseDatabase.getInstance().getReference().push().getKey();
                String time = this.getTime();

                NotificationService.this.databaseProvider.addMessage(sessionId, store, messageKey, user, message, time);

                this.showNotification(user);

                ChatActivity.getNewMessagesFrom().put(from, true);
            }
        }

    }

    public void showNotification(String user) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channelDescription");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[] {0, 1500});
            notificationChannel.setSound(Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.notification_sound), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channelId");
            notificationBuilder.setContentTitle("¡NUEVO MENSAJE!");
            notificationBuilder.setContentText(user+" te ha escrito");
            notificationBuilder.setSmallIcon(R.drawable.ic_stat);
            
            notificationBuilder.setAutoCancel(true);
            Notification notification = notificationBuilder.build();
            notificationManagerCompat.notify(0, notification);
        } else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "");
            notificationBuilder.setContentTitle("¡NUEVO MENSAJE!");
            notificationBuilder.setContentText(user+" te ha escrito");
            notificationBuilder.setSmallIcon(R.drawable.ic_stat);
            
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setVibrate(new long[] {0, 1500});
            notificationBuilder.setLights(Color.GREEN, 2000, 2000);
            notificationBuilder.setSound(Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.notification_sound));
            Notification notification = notificationBuilder.build();
            notificationManagerCompat.notify(0, notification);
        }
    }

    public String getTime() {
        Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        String day = String.valueOf(d);
        if (d < 10) {
            day = "0".concat(day);
        }

        int m = calendar.get(Calendar.MONTH)+1;
        String month = String.valueOf(m);
        if (m < 10) {
            month = "0".concat(month);
        }

        int y = calendar.get(Calendar.YEAR);
        String year = String.valueOf(y);
        if (y < 10) {
            year = "0".concat(year);
        }

        int h = calendar.get(Calendar.HOUR);
        String hours = String.valueOf(h);
        if (h < 10) {
            hours = "0".concat(hours);
        }

        int _m = calendar.get(Calendar.MINUTE);
        String minutes = String.valueOf(_m);
        if (_m < 10) {
            minutes = "0".concat(minutes);
        }

        int s = calendar.get(Calendar.SECOND);
        String seconds = String.valueOf(s);
        if (s < 10) {
            seconds = "0".concat(seconds);
        }

        String time = "["+day+"/"+month+"/"+year+" "+hours+":"+minutes+":"+seconds+"]";

        return time;
    }

}
