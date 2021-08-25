package com.app.bmarroc.yamba;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;


public class YambaClient {
    private static Boolean isConnected = true;

    private String id, username, password;

    public class IdEventListener implements ValueEventListener {
        private Boolean done;
        private YambaClient yambaClient;

        public IdEventListener(YambaClient yambaClient) {
            this.yambaClient = yambaClient;
            this.done = false;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                String key = dataSnapshot.getChildren().iterator().next().getKey();
                User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                if (user.getPassword().equals(this.yambaClient.password)) {
                    this.yambaClient.id = key;
                    Log.d("OK: ", "id");
                } else {
                    this.yambaClient.id = "INCORRECT_PASSWORD";
                    Log.d("ERROR: ", "incorrect_password");
                    return;
                }
            } else {
                this.yambaClient.id = "USERNAME_NOT_FOUND";
                Log.d("ERROR: ", "username_not_found");
            }
            this.done = true;
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {
            this.yambaClient.id = "DATABASE_ERROR";
            Log.d("ERROR: ", "database_error");
            this.done = true;
        }
    }


    public class UserEventListener implements ValueEventListener {
        private Boolean done;
        private ArrayList<String> userKey;

        public UserEventListener(ArrayList<String> userKey) {
            this.userKey = userKey;
            this.done = false;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                String key = dataSnapshot.getChildren().iterator().next().getKey();
                this.userKey.add(key);
                Log.d("OK: ", "userKey");
            } else {
                this.userKey.add("USERNAME_NOT_FOUND");
                Log.d("ERROR: ", "username_not_found");
            }
            this.done = true;
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {
            this.userKey.add("DATABASE_ERROR");
            Log.d("ERROR: ", "database_error");
            this.done = true;
        }
    }


    public class TimelineEventListener implements ValueEventListener {
        private Boolean done;
        private ArrayList<Status> statusList;

        public TimelineEventListener(ArrayList<Status> statusList) {
            this.statusList = statusList;
            this.done = false;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Status status = data.getValue(Status.class);
                    status.setKey(data.getKey());
                    this.statusList.add(status);
                }
                Log.d("OK: ", "timeline");
            }
            this.done = true;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("ERROR: ", "database_error");
            this.done = true;
        }
    }


    public static class Status {
        private String key, id, user, message;
        private long createdAt;

        public Status() {

        }

        public Status(String id, String user, String message, long createdAt) {
            this.id = id;
            this.user = user;
            this.message = message;
            this.createdAt = createdAt;
        }

        public String getId() {
            return this.id;
        }

        public String getUser() {
            return this.user;
        }

        public String getMessage() {
            return this.message;
        }

        public long getCreatedAt() {
            return this.createdAt;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }


    public static class User {
        private String user, password;

        public User() {

        }

        public User(String user, String message) {
            this.user = user;
            this.password = message;
        }

        public String getUser() {
            return this.user;
        }

        public String getPassword() {
            return this.password;
        }

    }


    public YambaClient(String username, String password) {
        if (YambaClient.isConnected()) {
            this.username = username;
            this.password = password;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference root = database.getReference();
            DatabaseReference ID = root.child("USER");
            Query query = ID.orderByChild("user").equalTo(username);
            IdEventListener idEventListener = new IdEventListener(this);
            query.addListenerForSingleValueEvent(idEventListener);
            while (true) {
                if (idEventListener.done) {
                    break;
                }
                Log.d("ERROR: ", "id_not_yet");
            }
        } else {
            this.id = "NOT_CONNECTED";
            Log.d("ERROR: ", "not_connected");
        }
    }


    public ArrayList<Status> getTimeline(int limit) throws YambaClientException {
        if ((this.id).equals("NOT_CONNECTED")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("INCORRECT_PASSWORD")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("USERNAME_NOT_FOUND")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("DATABASE_ERROR")) {
            throw new YambaClientException(this.id);
        }
        ArrayList<Status> statusList = new ArrayList<Status>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference root = database.getReference();
        DatabaseReference STATUS = root.child(this.id);
        Query query = STATUS.orderByChild("createdAt").limitToLast(limit);
        TimelineEventListener timelineEventListener = new TimelineEventListener(statusList);
        query.addListenerForSingleValueEvent(timelineEventListener);
        while (true) {
            if (timelineEventListener.done) {
                break;
            }
            Log.d("ERROR: ", "timeline_not_yet");
        }
        return statusList;
    }


    public void postStatus(String tweet) throws YambaClientException {
        if ((this.id).equals("NOT_CONNECTED")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("INCORRECT_PASSWORD")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("USERNAME_NOT_FOUND")) {
            throw new YambaClientException(this.id);
        }
        if ((this.id).equals("DATABASE_ERROR")) {
            throw new YambaClientException(this.id);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference root = database.getReference();

        if (!tweet.startsWith("@")) {
            long now = new Date(System.currentTimeMillis()).getTime();
            Status status = new Status(this.id, this.username, tweet, now);
            DatabaseReference STATUS = root.child(this.id);
            DatabaseReference statusid = STATUS.push();
            statusid.setValue(status);
        } else {
            ArrayList<String> userKey = new ArrayList<String>();
            String[] str1 = tweet.split("@");
            String str2 = str1[1];
            String[] str3 = str2.split(" ");
            String user = str3[0];
            String tw = str3[1];

            DatabaseReference ID = root.child("USER");
            Query query = ID.orderByChild("user").equalTo(user);
            UserEventListener userEventListener = new UserEventListener(userKey);
            query.addListenerForSingleValueEvent(userEventListener);
            while (true) {
                if (userEventListener.done) {
                    break;
                }
                Log.d("ERROR: ", "user_not_yet");
            }

            if ((userEventListener.userKey.get(0)).equals("USERNAME_NOT_FOUND")) {
                throw new YambaClientException(this.id);
            }
            if ((userEventListener.userKey.get(0)).equals("DATABASE_ERROR")) {
                throw new YambaClientException(this.id);
            }

            long now = new Date(System.currentTimeMillis()).getTime();
            Status status = new Status(this.id, this.username, tweet, now);

            DatabaseReference STATUS = root.child(this.id);
            DatabaseReference statusid = STATUS.push();
            statusid.setValue(status);

            DatabaseReference STATUS2 = root.child(userKey.get(0));
            DatabaseReference status2id = STATUS2.push();
            status2id.setValue(status);
        }
    }

    public static void isConnected(Boolean bool) {
        YambaClient.isConnected = bool;
    }

    public static Boolean isConnected() {
        return YambaClient.isConnected;
    }
}
