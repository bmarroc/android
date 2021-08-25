package com.app.bmarroc.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class Main2Activity extends AppCompatActivity {
    BootReceiver bootReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu, m);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_tweet) {
            Intent intent = new Intent(this, StatusActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            Intent intent = new Intent(this, RefreshService.class);
            startService(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_purge) {
            int rows = getContentResolver().delete(StatusContract.CONTENT_URI, null, null);
            Toast.makeText(this, "Deleted " + rows + " rows", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

}
