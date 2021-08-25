package com.app.bmarroc.yamba;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    public DbHelper(Context context) {
        super(context, StatusContract.DB_NAME, null, StatusContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s LONG)",StatusContract.TABLE, StatusContract.Column.ID, StatusContract.Column.USER, StatusContract.Column.MESSAGE, StatusContract.Column.CREATED_AT);
        db.execSQL(sql);
        Log.d(TAG, "onCreate with SQL:" + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StatusContract.TABLE);
        onCreate(db);
    }

}
