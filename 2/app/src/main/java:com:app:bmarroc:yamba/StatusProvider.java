package com.app.bmarroc.yamba;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class StatusProvider extends ContentProvider {
    private static final String TAG = StatusProvider.class.getSimpleName();
    private DbHelper dbHelper;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(StatusContract.AUTHORITY, StatusContract.TABLE, StatusContract.STATUS_DIR);
        sURIMatcher.addURI(StatusContract.AUTHORITY, StatusContract.TABLE + "/*", StatusContract.STATUS_ITEM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        Log.d(TAG, "onCreated");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        if (sURIMatcher.match(uri) == StatusContract.STATUS_DIR) {
            Log.d(TAG, "gotType: " + StatusContract.STATUS_TYPE_DIR);
            return StatusContract.STATUS_TYPE_DIR;

        }
        if (sURIMatcher.match(uri) == StatusContract.STATUS_ITEM) {
            Log.d(TAG, "gotType: " + StatusContract.STATUS_TYPE_ITEM);
            return StatusContract.STATUS_TYPE_ITEM;
        }
        throw new IllegalArgumentException("Illegal uri: " + uri);
    }

    @Override
    public Uri insert (Uri uri, ContentValues values) {
        Uri ret = null;

        if (sURIMatcher.match(uri) != StatusContract.STATUS_DIR) {
            throw  new IllegalArgumentException("Illegal uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insertWithOnConflict(StatusContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (rowId != -1) {
            String id = values.getAsString(StatusContract.Column.ID);
            ret = Uri.withAppendedPath(uri, id);
            Log.d(TAG, "inserted uri: " + ret);
            getContext().getContentResolver().notifyChange(uri, null);

        }
        return ret;
    }

    @Override
    public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String where;
        if (sURIMatcher.match(uri) == StatusContract.STATUS_DIR) {
            where = selection;
        } else if (sURIMatcher.match(uri) == StatusContract.STATUS_ITEM ) {
            long id = ContentUris.parseId(uri);
            where = StatusContract.Column.ID + "=" + id + (TextUtils.isEmpty(selection) ? "":"and (" + selection + ")");
        } else {
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.update(StatusContract.TABLE, values, where, selectionArgs);

        if (ret>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(TAG, "updated records: " + ret);
        return ret;
    }

    @Override
    public int delete (Uri uri, String selection, String[] selectionArgs) {
        String where;
        if (sURIMatcher.match(uri) == StatusContract.STATUS_DIR) {
            where = (selection == null) ? "1" : selection;
        } else if (sURIMatcher.match(uri) == StatusContract.STATUS_ITEM ) {
            long id = ContentUris.parseId(uri);
            where = StatusContract.Column.ID + "=" + id + (TextUtils.isEmpty(selection) ? "":"and (" + selection + ")");
        } else {
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.delete(StatusContract.TABLE, where, selectionArgs);

        if (ret>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(TAG, "deleted records: " + ret);
        return ret;
    }

    @Override
    public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(StatusContract.TABLE);
        if (sURIMatcher.match(uri) == StatusContract.STATUS_DIR) {

        } else if (sURIMatcher.match(uri) == StatusContract.STATUS_ITEM ) {
            qb.appendWhere(StatusContract.Column.ID + "='" + uri.getLastPathSegment() + "'");
        } else {
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        String orderBy = (TextUtils.isEmpty(sortOrder)) ? StatusContract.DEFAULT_SORT : sortOrder;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "queried records: "+cursor.getCount());
        return cursor;
    }



}
