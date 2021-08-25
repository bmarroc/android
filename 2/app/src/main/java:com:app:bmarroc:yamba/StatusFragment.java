package com.app.bmarroc.yamba;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


public class StatusFragment extends Fragment {
    private static final String TAG = "StatusActivity";
    private EditText editStatus;
    private Button buttonTweet;
    private TextView textCount;
    private SharedPreferences prefs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        editStatus = (EditText)view.findViewById(R.id.editStatus);

        buttonTweet = (Button)view.findViewById(R.id.buttonTweet);

        textCount = (TextView)view.findViewById(R.id.textCount);
        textCount.setTextColor(Color.GREEN);

        buttonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = editStatus.getText().toString();
                Log.d(TAG, "onClicked with status: "+ status);
                new PostTask().execute(status); 
            }
        });

        editStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                int count = 140 - editStatus.length();
                textCount.setText(Integer.toString(count));
                if (count>=50) {
                    buttonTweet.setEnabled(true);
                    textCount.setTextColor(Color.GREEN);
                }
                if (count<50&&count>=10) {
                    buttonTweet.setEnabled(true);
                    textCount.setTextColor(Color.YELLOW);
                }
                if (count<10&&count>=0) {
                    buttonTweet.setEnabled(true);
                    textCount.setTextColor(Color.RED);
                }
                if (count<0) {
                    buttonTweet.setEnabled(false);
                    textCount.setTextColor(Color.RED);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });

        return view;
    }

    private final class PostTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String username = prefs.getString("username","");
            String password = prefs.getString("password","");
            Log.d("username", username);
            Log.d("password", password);
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                getActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
                return "Please update your username and password";
            }

            YambaClient yambaCloud = new YambaClient(username,password);
            try {
                yambaCloud.postStatus(params[0]);
                return "Successfully posted";
            } catch (YambaClientException e) {
                e.printStackTrace();
                return "Failed to post to yamba service";
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(StatusFragment.this.getActivity(), result, Toast.LENGTH_LONG).show();
            ((EditText)StatusFragment.this.editStatus).setText("");
        }
    }

}
