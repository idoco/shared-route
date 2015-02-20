package com.sharedroute.app;

import android.app.Application;

/**
 * Created by cohid01 on 20/02/2015.
 * Used to share common components. (Like the websocket connection wrapper)
 */
public class SharedRouteApp extends Application {

    private SharedLocationService sharedLocationService;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public SharedLocationService getSharedLocationService() {
        return sharedLocationService;
    }

    public void setSharedLocationService(SharedLocationService sharedLocationService) {
        this.sharedLocationService = sharedLocationService;
    }
}
