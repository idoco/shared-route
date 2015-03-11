package com.sharedroute.app;

import android.app.Application;

/**
 * Created by cohid01 on 20/02/2015.
 * Used to share common components. (Like the websocket connection wrapper)
 */
public class SharedRouteApp extends Application {

    private boolean isSharingRoute = false;
    private MainMapActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean isSharingRoute() {
        return isSharingRoute;
    }

    public void setSharingRoute(boolean isSharingRoute) {
        this.isSharingRoute = isSharingRoute;
    }

    public void setMainActivity(MainMapActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MainMapActivity getMainActivity() {
        return mainActivity;
    }
}
