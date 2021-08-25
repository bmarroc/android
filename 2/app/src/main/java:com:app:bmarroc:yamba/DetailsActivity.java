package com.app.bmarroc.yamba;

import android.app.Activity;
import android.os.Bundle;

public class DetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {//1
            DetailsFragment fragment = new DetailsFragment();//2
            getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }
}
