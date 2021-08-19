package com.firebaseapp.guasap_bm.guasap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

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

public class ApplyActivity extends AppCompatActivity {
    private TextInputEditText userInput;
    private TextInputEditText passwordInput;
    private TextInputEditText idInput;
    private MaterialButton sendButton;
    private MaterialButton  cancelButton;
    private TextView errorOutput;
    private FirebaseAuth firebaseAuth;
    private final static String URL_POST = ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        userInput = (TextInputEditText)findViewById(R.id.userInput);
        passwordInput = (TextInputEditText)findViewById(R.id.passwordInput);
        idInput = (TextInputEditText)findViewById(R.id.idInput);

        sendButton = (MaterialButton )findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = userInput.getText().toString();
                String id = idInput.getText().toString();
                String password = passwordInput.getText().toString();
                String messagingToken = FirebaseInstanceId.getInstance().getToken();

                try {
                    JSONObject request = new JSONObject();
                    request.put("user", user);
                    request.put("id", id);
                    request.put("password", password);
                    request.put("messagingToken", messagingToken);

                    ApplyTask applyTask = new ApplyTask();
                    applyTask.execute(request);

                    userInput.setText("");
                    idInput.setText("");
                    passwordInput.setText("");

                    Toast.makeText(ApplyActivity.this, "Cargando...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("[ApplyActivity]", "onClick: "+e.toString());
                    errorOutput.setText("ERROR: Ha ocurrido un error. Intente nuevamente.");
                }
            }
        });

        cancelButton = (MaterialButton )findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ApplyActivity.this, LoginActivity.class);
                startActivity(intent);

                ApplyActivity.this.finish();
            }
        });

        errorOutput = (TextView) findViewById(R.id.errorOutput);

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d("[ApplyActivity]","onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("[ApplyActivity]","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("[ApplyActivity]","onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("[ApplyActivity]","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("[ApplyActivity]","onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("[ApplyActivity]","onDestroy");
    }


    private class ApplyTask extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject result = new JSONObject();;
            try {
                URL url = new URL(URL_POST);
                URLConnection uc = url.openConnection();

                uc.setDoOutput(true);
                OutputStream o = uc.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));

                String user = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("user"), "UTF-8");
                String id = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("id"), "UTF-8");
                String password = URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("password"), "UTF-8");
                String messagingToken = URLEncoder.encode("messagingToken", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("messagingToken"), "UTF-8");
                String data = user + "&" + id + "&" + password + "&" + messagingToken;

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
                Log.d("[ApplyActivity]", "doInbackground: "+e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            try {
                if (result.has("error")) {
                    String error = result.getString("error");
                    errorOutput.setText(error);
                    Log.d("[ApplyActivity]", "onPostExecute: "+error);
                } else {
                    String authToken = result.getString("authToken");

                    final String user = result.getString("user");
                    final String id = result.getString("id");
                    final String messagingToken = result.getString("messagingToken");

                    firebaseAuth.signInWithCustomToken(authToken).addOnCompleteListener(ApplyActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String authId = firebaseAuth.getCurrentUser().getUid();
                                Log.d("[ApplyActivity]", "signInWithCustomToken: "+authId);

                                Intent intent = new Intent(ApplyActivity.this, ContactsActivity.class);
                                String[] __session = {authId, user, id, messagingToken};
                                intent.putExtra("__session", __session);
                                startActivity(intent);

                                ApplyActivity.this.finish();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.d("[ApplyActivity]", "onPostExecute: "+e.toString());
                errorOutput.setText("ERROR: Ha ocurrido un error. Intente nuevamente.");
            }

        }
    }
}
