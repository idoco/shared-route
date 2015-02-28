package com.sharedroute.app.tasks;

import com.google.android.gms.maps.model.Marker;
import com.sharedroute.app.MainMapActivity;
import com.sharedroute.app.MarkerWrapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

/**
* Created by cohid01 on 20/02/2015.
*/
public class ClearOldMarkersRunnable extends TimerTask {
    public static final int NO_UPDATE_GRACE = 60 * 1000;
    private final MainMapActivity mainMapActivity;

    public ClearOldMarkersRunnable(MainMapActivity mainMapActivity) {
        this.mainMapActivity = mainMapActivity;
    }

    @Override
    public void run() {
        final Map<String, MarkerWrapper> sessionIdToMarkers = mainMapActivity.getSessionIdToMarkers();
        long currentTime = System.currentTimeMillis();
        final Set<String> sessionsToDelete = new HashSet<String>();
        for (String sessionId : sessionIdToMarkers.keySet()) {
            MarkerWrapper markerWrapper = sessionIdToMarkers.get(sessionId);
            if (markerWrapper.getLastTouch() + NO_UPDATE_GRACE < currentTime){
                sessionsToDelete.add(sessionId);
            }
        }

        mainMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (String sessionId : sessionsToDelete) {
                    MarkerWrapper markerWrapper = sessionIdToMarkers.get(sessionId);
                    markerWrapper.getMarker().remove();
                    sessionIdToMarkers.remove(sessionId);
                }
            }
        });
    }
}
