package com.firebaseapp.guasap_bm.guasap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import androidx.annotation.IntDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;


public class ChatActivity extends AppCompatActivity {
    private static boolean isVisible = false;
    private static String[] currentChat = null;
    private static HashMap<String,Boolean> newMessages = new HashMap<String, Boolean>();
    public String[] data;
    public ListView chatList;
    public DatabaseProvider databaseProvider;
    public SharedPreferences __session;
    private TextInputEditText messageInput;
    private FloatingActionButton sendButton;
    private FirebaseDatabase firebaseDatabase;
    private UpdateReceiver updateReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.firebaseDatabase = FirebaseDatabase.getInstance();

        this.__session = getSharedPreferences("__session", MODE_PRIVATE);

        this.data = getIntent().getStringArrayExtra("data");

        ChatActivity.currentChat = data;

        final androidx.appcompat.widget.Toolbar chatToolBar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.chatToolBar);
        setSupportActionBar(chatToolBar);
        getSupportActionBar().setTitle(ChatActivity.currentChat[1]);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.databaseProvider = new DatabaseProvider(this, "innerdatabase.db");
        String sessionId = this.__session.getString("id", "");
        Cursor cursor = this.databaseProvider.addDB().getCursor(sessionId, data[0]);

        this.chatList = (ListView) findViewById(R.id.chatList);

        this.chatList.setAdapter(new CursorAdapter(this, cursor) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView chatList_messageKey = (TextView) view.findViewById(R.id.chatList_messageKey);
                TextView chatList_user = (TextView) view.findViewById(R.id.chatList_user);
                TextView chatList_message = (TextView) view.findViewById(R.id.chatList_message);
                TextView chatList_time = (TextView) view.findViewById(R.id.chatList_time);
                TextView chatList_sended = (TextView) view.findViewById(R.id.chatList_sended);
                TextView chatList_received = (TextView) view.findViewById(R.id.chatList_received);

                String messageKey = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                String user = cursor.getString(cursor.getColumnIndexOrThrow("user"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String sended = cursor.getString(cursor.getColumnIndexOrThrow("sended"));
                String received = cursor.getString(cursor.getColumnIndexOrThrow("received"));

                chatList_messageKey.setText(messageKey);
                chatList_user.setText(user);
                chatList_message.setText(message);
                chatList_time.setText(time);
                chatList_sended.setText(sended);
                chatList_received.setText(received);
            }
        });

        this.chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = ((TextView) view.findViewById(R.id.chatList_message)).getText().toString();

                Intent intent = new Intent(ChatActivity.this, ZoomActivity.class);
                intent.putExtra("data", data);
                startActivity(intent);
            }
        });
        this.chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String data = ((TextView) view.findViewById(R.id.chatList_message)).getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("data", data);
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(ChatActivity.this, "Copiado", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        final ListAdapter listAdapter = this.chatList.getAdapter();
        this.chatList.post(new Runnable() {
            @Override
            public void run() {
                ChatActivity.this.chatList.setSelection(listAdapter.getCount() - 1);
            }
        });

        this.messageInput = (TextInputEditText) findViewById(R.id.messageInput);
        this.messageInput.setVerticalScrollBarEnabled(true);


        this.sendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sessionId = __session.getString("id", "");
                String user = __session.getString("user", "");

                String message = messageInput.getText().toString();
                if (!message.equals("")) {
                    Calendar  calendar = Calendar.getInstance();

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

                    String messageKey = FirebaseDatabase.getInstance().getReference().push().getKey();

                    ChatActivity.this.databaseProvider.addMessage(sessionId, data[0], messageKey, user, message, time);

                    ChatActivity.this.firebaseDatabase.getReference("Messages/"+sessionId+"/"+ChatActivity.this.data[0]+"/"+messageKey).setValue(message);

                    messageInput.setText("");
                    Cursor cursor = ChatActivity.this.databaseProvider.getCursor(sessionId, data[0]);
                    ((CursorAdapter)ChatActivity.this.chatList.getAdapter()).changeCursor(cursor);

                    final ListAdapter listAdapter = ChatActivity.this.chatList.getAdapter();
                    ChatActivity.this.chatList.post(new Runnable() {
                        @Override
                        public void run() {
                            ChatActivity.this.chatList.setSelection(listAdapter.getCount() - 1);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ChatActivity.isVisible(true);
        Log.d("[ChatActivity]", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        ChatActivity.isVisible(true);

        this.updateUI();

        IntentFilter intentFilter = new IntentFilter("com.firebaseapp.guasap_bm.guasap.NEW_MESSAGE");
        this.updateReceiver = new UpdateReceiver();
        this.updateReceiver.setActivity(this);
        registerReceiver(this.updateReceiver, intentFilter);
        Log.d("[ChatActivity]", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        ChatActivity.isVisible(false);

        unregisterReceiver(this.updateReceiver);

        Log.d("[ChatActivity]", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        ChatActivity.isVisible(false);
        Log.d("[ChatActivity]", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ChatActivity.isVisible(false);
        Log.d("[ChatActivity]", "onDestroy");
    }

    public static Boolean isVisible() {
        return ChatActivity.isVisible;
    }

    public static void isVisible(Boolean bool) {
        ChatActivity.isVisible = bool;
    }

    public static String[] getCurrentChat() {
        return ChatActivity.currentChat;
    }

    public static HashMap<String, Boolean> getNewMessagesFrom() {
        return ChatActivity.newMessages;
    }

    public static Boolean getAreNewMessages(String id) {
        if (ChatActivity.newMessages.get(id) != null) {
            return ChatActivity.newMessages.get(id);
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_messages_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_deleteMessages) {
            String sessionId = __session.getString("id", "");
            this.databaseProvider.clearStore(sessionId, data[0]);
            Cursor cursor = ChatActivity.this.databaseProvider.getCursor(sessionId, data[0]);
            ((CursorAdapter)ChatActivity.this.chatList.getAdapter()).changeCursor(cursor);
            return true;
        }
        return false;
    }

    public void updateUI() {
        String sessionId = ChatActivity.this.__session.getString("id", "");
        Cursor cursor = ChatActivity.this.databaseProvider.getCursor(sessionId, data[0]);
        ((CursorAdapter)ChatActivity.this.chatList.getAdapter()).changeCursor(cursor);

        if (ChatActivity.getAreNewMessages(ChatActivity.getCurrentChat()[0])) {
            final ListAdapter listAdapter = ChatActivity.this.chatList.getAdapter();
            ChatActivity.this.chatList.post(new Runnable() {
                @Override
                public void run() {
                    ChatActivity.this.chatList.setSelection(listAdapter.getCount() - 1);
                }
            });
            ChatActivity.getNewMessagesFrom().put(ChatActivity.getCurrentChat()[0], false);
        }
    }
}
