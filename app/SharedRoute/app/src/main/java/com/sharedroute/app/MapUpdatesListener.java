package com.sharedroute.app;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by cohid01 on 18/02/2015.
 */
public interface MapUpdatesListener {
    void updateMapMarker(String sessionId, LatLng newLatLng);
    void onLocationServiceClose();
}
