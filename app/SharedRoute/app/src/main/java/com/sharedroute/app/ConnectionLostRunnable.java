package com.sharedroute.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
* Created by cohid01 on 20/02/2015.
*/
class ConnectionLostRunnable implements Runnable {
    private final MainMapActivity mainMapActivity;

    public ConnectionLostRunnable(MainMapActivity mainMapActivity) {
        this.mainMapActivity = mainMapActivity;
    }

    @Override
    public void run() {
        alertUser(mainMapActivity, "Connection to server lost", "Try to reconnect?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainMapActivity.connectToSharedLocationServicesIfNeeded();
                    }
                });
    }

    private void alertUser(Activity activity, String title, String message,
                           DialogInterface.OnClickListener callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", callBack);
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
