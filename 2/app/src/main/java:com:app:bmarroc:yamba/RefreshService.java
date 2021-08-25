package com.app.bmarroc.yamba;

import android.app.IntentService;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;



public class RefreshService extends IntentService {
    static final String TAG = "RefreshService";

    public RefreshService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean bool = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        YambaClient.isConnected(bool);
        


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username","");
        String password = prefs.getString("password","");
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please update your username and password", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG,"onStarted");
        
        ContentValues values = new ContentValues();

        YambaClient cloud = new YambaClient(username, password);
        try {
            int count = 0;
            ArrayList<YambaClient.Status> timeline = cloud.getTimeline(5);
            for (YambaClient.Status status:timeline) {
                values.clear();
                values.put(StatusContract.Column.ID, status.getKey());
                values.put(StatusContract.Column.USER, status.getUser());
                values.put(StatusContract.Column.MESSAGE, status.getMessage());
                values.put(StatusContract.Column.CREATED_AT, status.getCreatedAt());
               
                Uri uri = getContentResolver().insert(StatusContract.CONTENT_URI, values);
                if (uri != null) {
                    count++;
                    Log.d(TAG, String.format("%s : %s", status.getUser(), status.getMessage()));
                }

            }
            if (count>0) {
                
                Intent event = new Intent("com.app.bmarroc.yamba.NEW_STATUSES");
                
                event.putExtra("count", count);
                sendBroadcast(event);
            }
        } catch (YambaClientException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to fetch the timeline");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");
    }
}
