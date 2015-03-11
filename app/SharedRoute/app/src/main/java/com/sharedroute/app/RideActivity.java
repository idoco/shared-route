package com.sharedroute.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;


public class RideActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
    }

    public void exitClicked(View view) {
        SharedRouteApp app = (SharedRouteApp) getApplication();
        app.setSharingRoute(false);
        app.getMainActivity().stopServices();
        finish();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    public void shareToWhatsAppClicked(View view) {
        try {
            shareToWhatsApp();
        } catch (Exception e) {
            Log.e("sharedroute","Error could not start WhatsApp");
            showSharingFailedDialog();
        }
    }

    private void shareToWhatsApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I'm on my way!");
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    private void showSharingFailedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sharing Failed");
        builder.setMessage("Could not start WhatsApp");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
