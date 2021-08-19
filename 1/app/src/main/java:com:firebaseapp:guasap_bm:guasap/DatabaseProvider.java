package com.firebaseapp.guasap_bm.guasap;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseProvider {
    SQLiteDatabase db;
    String db_name;
    Context context;

    public DatabaseProvider(Context context, String db_name) {
        this.db_name = db_name;
        this.context = context;
    }

    public DatabaseProvider addDB() {
        this.db = this.context.openOrCreateDatabase(this.db_name, MODE_PRIVATE, null);
        return this;
    }

    public DatabaseProvider addStore(String sessionId, String store) {
        if (store.equals("contacts")) {
            try {
                String query = "CREATE TABLE IF NOT EXISTS "+sessionId+"_"+"contacts"+" (_id TEXT PRIMARY KEY, user TEXT)";
                db.execSQL(query);
            } catch (SQLException e){
                Log.d("[DatabaseProvider]","addStore: "+e.toString());
            }
        } else {
            try {
                String query = "CREATE TABLE IF NOT EXISTS "+sessionId+"_"+store+" (_id TEXT PRIMARY KEY, user TEXT, message TEXT, time TEXT, sended TEXT, received TEXT)";
                db.execSQL(query);
            } catch (SQLException e){
                Log.d("[DatabaseProvider]","addStore: "+e.toString());
            }
        }
        return this;
    }

    public DatabaseProvider deleteStore(String sessionId, String store) {
        String query = "DROP TABLE IF EXISTS "+sessionId+"_"+store;
        try {
            db.execSQL(query);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","deleteStore: "+e.toString());
        }
        return this;
    }

    public DatabaseProvider addContact(String user, String sessionId, String id) {
        String query = "INSERT INTO "+sessionId+"_"+"contacts"+" (_id,user) "+"VALUES ('"+id+"', '"+user+"')";
        try {
            db.execSQL(query);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","addContact: "+e.toString());
        }
        return this;
    }

    public DatabaseProvider deleteContact(String sessionId, String id) {
        String query = "DELETE FROM  "+sessionId+"_"+"contacts"+" WHERE _id = '"+id+"'";
        try {
            db.execSQL(query);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","deleteContact: "+e.toString());
        }
        return this;
    }

    public Cursor getCursor(String sessionId, String store) {
        String query = "SELECT * FROM "+sessionId+"_"+store;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public DatabaseProvider addMessage(String sessionId, String store, String messageKey, String user, String message, String time) {
        String query = "INSERT INTO "+sessionId+"_"+store+" (_id,user,message,time,sended,received) "+"VALUES ('"+messageKey+"', '"+user+"', '"+message+"', '"+time+"', '', '')";
        try {
            db.execSQL(query);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","addContact: "+e.toString());
        }
        return this;
    }

    public DatabaseProvider updateMessage(String sessionId, String store, String messageKey, String user, String message, String sended, String received) {
        String query1 = "SELECT * FROM "+sessionId+"_"+store+" WHERE _id = '"+messageKey+"'";
        Cursor cursor = db.rawQuery(query1, null);
        String time = "";
        String query2 = "";
        if (cursor != null && cursor.moveToFirst()) {
            time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            query2 = "UPDATE "+sessionId+"_"+store+" SET _id = '"+messageKey+"', user = '"+user+"', message = '"+message+"', time = '"+time+"', sended = '"+sended+"', received = '"+received+"' WHERE _id = '"+messageKey+"'";
        } else {
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

            time = "["+day+"/"+month+"/"+year+" "+hours+":"+minutes+":"+seconds+"]";

            query2 = "INSERT INTO "+sessionId+"_"+store+" (_id,user,message,time,sended,received) "+"VALUES ('"+messageKey+"', '"+user+"', '"+message+"', '"+time+"', '"+sended+"', '"+received+"')";

        }

        cursor.close();

        try {
            db.execSQL(query2);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","updateMessage: "+e.toString());
        }
        return this;
    }

    public DatabaseProvider clearStore (String sessionId, String store) {
        String query = "DELETE FROM "+sessionId+"_"+store;
        try {
            db.execSQL(query);
        }catch (SQLException e) {
            Log.d("[DatabaseProvider]","deleteContact: "+e.toString());
        }
        return this;
    }

    public String getUser(String sessionId, String id) {
        String query1 = "SELECT * FROM "+sessionId+"_"+"contacts"+" WHERE _id = '"+id+"'";
        Cursor cursor = db.rawQuery(query1, null);
        String user= "";
        if (cursor != null && cursor.moveToFirst()) {
            user = cursor.getString(cursor.getColumnIndexOrThrow("user"));
        }
        cursor.close();
        return user;
    }
}
