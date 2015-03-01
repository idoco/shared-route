package com.sharedroute.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class RideActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
    }

    public void foo(View view) {

    }

    public void goo(View view) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I'm on my way!");
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (Exception e) {
            Log.e("sharedroute","Error could not start WhatsApp");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sharing Failed");
            builder.setMessage("Could not start WhatsApp");
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }
}
