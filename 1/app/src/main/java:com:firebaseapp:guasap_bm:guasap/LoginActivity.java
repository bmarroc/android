package com.firebaseapp.guasap_bm.guasap;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText userInput;
    private TextInputEditText passwordInput;
    private MaterialButton sendButton;
    private MaterialButton cancelButton;
    private MaterialButton applyButton;
    private TextView errorOutput;
    private FirebaseAuth firebaseAuth;
    private final static String URL_POST = ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userInput = (TextInputEditText)findViewById(R.id.userInput);
        passwordInput = (TextInputEditText)findViewById(R.id.passwordInput);

        sendButton = (MaterialButton)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = userInput.getText().toString();
                String password = passwordInput.getText().toString();
                String messagingToken = FirebaseInstanceId.getInstance().getToken();
                try {
                    JSONObject request = new JSONObject();
                    request.put("user", user);
                    request.put("password", password);
                    request.put("messagingToken", messagingToken);

                    LoginTask loginTask = new LoginTask();
                    loginTask.execute(request);

                    userInput.setText("");
                    passwordInput.setText("");

                    Toast.makeText(LoginActivity.this, "Cargando...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("[LoginActivity]", "onClick: "+e.toString());
                    errorOutput.setText("ERROR: Ha ocurrido un error. Intente nuevamente.");
                }

            }
        });

        cancelButton = (MaterialButton)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.finish();
            }
        });

        applyButton = (MaterialButton)findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ApplyActivity.class);
                startActivity(intent);

                LoginActivity.this.finish();
            }
        });

        errorOutput = (TextView) findViewById(R.id.errorOutput);

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d("[LoginActivity]","onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("[LoginActivity]","onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("[LoginActivity]","onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("[LoginActivity]","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("[LoginActivity]","onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("[LoginActivity]","onDestroy");
    }


    private class LoginTask extends AsyncTask<JSONObject, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            JSONObject result = new JSONObject();
            try {
                URL url = new URL(URL_POST);
                URLConnection uc = url.openConnection();

                uc.setDoOutput(true);
                OutputStream o = uc.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"));

                String user = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("user"), "UTF-8");
                String password = URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("password"), "UTF-8");
                String messagingToken = URLEncoder.encode("messagingToken", "UTF-8") + "=" + URLEncoder.encode(params[0].getString("messagingToken"), "UTF-8");
                String data = user + "&" + password + "&" + messagingToken;

                out.write(data);
                out.flush();
                out.close();

                InputStream i = uc.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(i));
                String json = "";
                while (true) {
                    String text = in.readLine();
                    Log.d("[LoginActivity.java]", "text "+text);
                    if (text == null) {
                        in.read();
                        break;
                    }
                    json = json + text;
                }
                result = new JSONObject(json);
            } catch (Exception e) {
                Log.d("[LoginActivity]", "doInbackground: "+e.toString());
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
                    Log.d("[LoginActivity]", "onPostExecute: "+error);
                } else {
                    String authToken = result.getString("authToken");

                    final String user = result.getString("user");
                    final String id = result.getString("id");
                    final String messagingToken = result.getString("messagingToken");

                    firebaseAuth.signInWithCustomToken(authToken).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String authId = firebaseAuth.getCurrentUser().getUid();
                                Log.d("[LoginActivity]", "signInWithCustomToken: "+authId);

                                Intent intent = new Intent(LoginActivity.this, ContactsActivity.class);
                                String[] data = {authId, user, id, messagingToken};
                                intent.putExtra("data", data);
                                startActivity(intent);

                                LoginActivity.this.finish();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.d("[LoginActivity]", "onPostExecute: "+e.toString());
                errorOutput.setText("ERROR: Ha ocurrido un error. Intente nuevamente.");
            }

        }
    }
}
