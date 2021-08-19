package com.firebaseapp.guasap_bm.guasap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

import static android.content.Context.MODE_PRIVATE;

public class UpdateReceiver extends BroadcastReceiver{
    Activity activity;
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public Activity getActivity() {
        return this.activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent.getAction()).equals("com.firebaseapp.guasap_bm.guasap.NEW_MESSAGE")) {
            final ChatActivity activity = (ChatActivity)getActivity();
            String sessionId = activity.__session.getString("id", "");
            Cursor cursor = activity.databaseProvider.getCursor(sessionId, activity.data[0]);
            ((CursorAdapter)activity.chatList.getAdapter()).changeCursor(cursor);

            final ListAdapter listAdapter = activity.chatList.getAdapter();
            activity.chatList.post(new Runnable() {
                @Override
                public void run() {
                    activity.chatList.setSelection(listAdapter.getCount() - 1);
                }
            });
        }
    }
}
