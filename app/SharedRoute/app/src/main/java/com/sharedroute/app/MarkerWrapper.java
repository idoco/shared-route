package com.sharedroute.app;

import com.google.android.gms.maps.model.Marker;

/**
* Created by cohid01 on 28/02/2015.
 * Wraps a map marker and also holds its last update time in milliseconds
 * This is done to enable a timer task to delete all markers older than a threshold time
*/
public class MarkerWrapper {
    private Marker marker;
    private Long lastTouch;

    public MarkerWrapper(Marker marker, Long lastTouch) {
        this.marker = marker;
        this.lastTouch = lastTouch;
    }

    public Marker getMarker() {
        return marker;
    }

    public Long getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(Long lastTouch) {
        this.lastTouch = lastTouch;
    }
}
