package com.firebaseapp.guasap_bm.guasap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ContactsActivity extends AppCompatActivity {
    private FloatingActionButton logoutButton;
    private ListView contactsList;
    private FirebaseAuth firebaseAuth;
    private final static String URL_POST = ;
    private String[] data;
    private SharedPreferences __session;
    private DatabaseProvider databaseProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        __session = getSharedPreferences("__session", MODE_PRIVATE);

        data = getIntent().getStringArrayExtra("data");
        if (data != null) {
            SharedPreferences.Editor prefsEditor = __session.edit();
            prefsEditor.putString("authId", data[0]);
            prefsEditor.putString("user", data[1]);
            prefsEditor.putString("id", data[2]);
            prefsEditor.putString("messagingToken", data[3]);
            prefsEditor.commit();
        }

        firebaseAuth = FirebaseAuth.getInstance();

        this.databaseProvider = new DatabaseProvider(this, "innerdatabase.db");
        String sessionId = __session.getString("id", "");
        this.databaseProvider.addDB().addStore(sessionId, "contacts");

        Toolbar contactsToolBar = (Toolbar)findViewById(R.id.contactsToolBar);
        setSupportActionBar(contactsToolBar);

        Cursor cursor = this.databaseProvider.getCursor(sessionId, "contacts");
    
        this.contactsList = (ListView) findViewById(R.id.contactsList);

        this.contactsList.setAdapter(new CursorAdapter(this, cursor) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.contact_layout, parent, false);
            }

            @Override
            public void bindView(final View view, Context context, Cursor cursor) {
                TextView contactsList_id = (TextView) view.findViewById(R.id.contactsList_id);
                TextView contactsList_user = (TextView) view.findViewById(R.id.contactsList_user);

                final String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                String user = cursor.getString(cursor.getColumnIndexOrThrow("user"));

                contactsList_id.setText(id);
                contactsList_user.setText(user);

                FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.contactsList_button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT, R.attr.actionOverflowMenuStyle, 0);
                        popup.inflate(R.menu.delete_contact_menu);
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String sessionId = __session.getString("id", "");
                                ContactsActivity.this.databaseProvider.deleteStore(sessionId, id).deleteContact(sessionId, id);

                                Cursor cursor = ContactsActivity.this.databaseProvider.getCursor(sessionId, "contacts");
                                ((CursorAdapter)ContactsActivity.this.contactsList.getAdapter()).changeCursor(cursor);
                                return false;
                            }
                        });
                    }
                });
            }
        });
        this.contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView contactsList_id = (TextView) view.findViewById(R.id.contactsList_id);
                TextView contactsList_user = (TextView) view.findViewById(R.id.contactsList_user);

                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                String[] data = {contactsList_id.getText().toString(), contactsList_user.getText().toString()};
                intent.putExtra("data", data);
                startActivity(intent);
            }
        });

        logoutButton = (FloatingActionButton) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
                LayoutInflater inflater = ContactsActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.exit_layout, null);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                MaterialButton acceptButton = (MaterialButton) dialogView.findViewById(R.id.acceptButton);
                acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String authId = ContactsActivity.this.__session.getString("authId", "");
                        String user = __session.getString("user", "");
                        String id = __session.getString("id", "");
                        String messagingToken = __session.getString("messagingToken", "");

                        try {
                            JSONObject request = new JSONObject();
                            request.put("authId", authId);
                            request.put("user", user);
                            request.put("id", id);
                            request.put("messagingToken", messagingToken);

                            LogoutTask logoutTask = new LogoutTask();
                            logoutTask.execute(request);

                            alertDialog.dismiss();

                            Toast.makeText(ContactsActivity.this, "Cerrando...", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d("[ContactsActivity]", "onClick: " + e.toString());
                            Toast.makeText(ContactsActivity.this, "ERROR: Ha ocurrido un error. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                MaterialButton cancelButton = (MaterialButton) dialogView.findViewById(R.id.cancelButton);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.d("[ContactsActivity]","firebaseUser");
            Intent intent = new Intent(ContactsActivity.this, LoginActivity.class);
            startActivity(intent);
            ContactsActivity.this.finish();
        }
        Log.d("[ContactsActivity]", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("[ContactsActivity]", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("[ContactsActivity]", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("[ContactsActivity]", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("[ContactsActivity]", "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_addContact) {
            Log.d("action","addContact");

            AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
            LayoutInflater inflater = ContactsActivity.this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.add_contact_layout, null);

            builder.setView(dialogView);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            MaterialButton acceptButton = (MaterialButton)dialogView.findViewById(R.id.acceptButton);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sessionId = ContactsActivity.this.__session.getString("id","");

                    TextInputEditText userInput = (TextInputEditText)dialogView.findViewById(R.id.userInput);
                    TextInputEditText idInput = (TextInputEditText)dialogView.findViewById(R.id.idInput);

                    String user = userInput.getText().toString();
                    String id = idInput.getText().toString();

                    if (!user.equals("") && !id.equals("")) {
                        ContactsActivity.this.databaseProvider.addStore(sessionId, id).addContact(user, sessionId, id);
                    }
                    userInput.setText("");
                    idInput.setText("");
                    alertDialog.dismiss();

                    Cursor cursor = ContactsActivity.this.databaseProvider.getCursor(sessionId, "contacts");
                    ((CursorAdapter)ContactsActivity.this.contactsList.getAdapter()).changeCursor(cursor);
                }
            });
            MaterialButton cancelButton = (MaterialButton)dialogView.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            return true;
        }
        return false;
    }

    public class LogoutTask extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject result = new JSONObject();;
            try {
                URL url = new URL(URL_POST);
                URLConnection uc = url.openConnection();

                uc.setDoOutput(true);
                OutputStream o = uc.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));

                String authId = URLEncoder.encode("authId", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("authId"), "UTF-8");
                String user = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("user"), "UTF-8");
                String id = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("id"), "UTF-8");
                String messagingToken = URLEncoder.encode("messagingToken", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("messagingToken"), "UTF-8");
                String data = authId + "&" + user + "&" + id + "&" + messagingToken;

                out.write(data);
                out.flush();
                out.close();

                InputStream i = uc.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(i));
                String json = "";
                while (true) {
                    String text = in.readLine();
                    if (text == null) {
                        break;
                    }
                    json = json + text;
                }
                result = new JSONObject(json);
            } catch (Exception e) {
                Log.d("[ContactsActivity]", "doInbackground: "+e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            try {
                if (result.has("error")) {
                    String error = result.getString("error");
                    Toast.makeText(ContactsActivity.this, error, Toast.LENGTH_SHORT).show();
                    Log.d("[ContactsActivity]", "onPostExecute: "+error);
                } else {
                    firebaseAuth.signOut();

                    SharedPreferences.Editor prefsEditor = __session.edit();
                    prefsEditor.clear().commit();


                    Intent intent = new Intent(ContactsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    ContactsActivity.this.finish();
                }
            } catch (Exception e) {
                Log.d("[ContactsActivity]", "onPostExecute: "+e.toString());
                Toast.makeText(ContactsActivity.this, "ERROR: Ha ocurrido un error. Intente nuevamente.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
