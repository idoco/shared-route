package com.sharedroute.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MainMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, MapUpdatesListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Marker userLocationMarker;

    // accessed only from the UI tread
    private final Map<String,Marker> sessionIdToMarkers = new HashMap<String, Marker>();

    private final String TAG = "SharedRoute";
    private boolean shareRoute = false;
    private String uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        shareRoute = false;
        setContentView(R.layout.activity_main_map);
        setUpMapIfNeeded();
        buildGoogleApiClientIfNeeded();
        connectToSharedLocationServicesIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shareRoute = false;
        setUpMapIfNeeded();
        buildGoogleApiClientIfNeeded();
        connectToSharedLocationServicesIfNeeded();
    }

    public void iAmOnButtonClicked(View view) {
        shareRoute = true;
        if (userLocationMarker != null && userLocationMarker.getPosition() != null) {
            shareLocation(userLocationMarker.getPosition());
        }
        Intent intent = new Intent(this, RideActivity.class);
        startActivity(intent);
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            return;
        }

        MapBuildUtils.customizeMap(mMap, getResources().getAssets());
        MapBuildUtils.addRoutesToMap(mMap);

        LatLng initialLatLng = new LatLng(32.0807898,34.7731816);
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(initialLatLng, 16);
        mMap.moveCamera(upd);
    }

    protected synchronized void buildGoogleApiClientIfNeeded() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    public void connectToSharedLocationServicesIfNeeded() {
        SharedRouteApp app = (SharedRouteApp) getApplication();
        SharedLocationService sharedLocationService = app.getSharedLocationService();
        if (sharedLocationService == null || sharedLocationService.isClosed()) { //should I reuse the old connection?
            sharedLocationService = new SharedLocationService.Builder(this)
                    .setSessionId(uniqueId)
                    .build();
            app.setSharedLocationService(sharedLocationService);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (userLocationMarker == null) {
            MarkerOptions initialUserLocation = new MarkerOptions()
                    .position(userLatLng)
                    .title("Me")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            userLocationMarker = mMap.addMarker(initialUserLocation);

            //move camera to the user on the first location update
            CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(userLatLng, 16);
            mMap.moveCamera(upd);
        } else {
            userLocationMarker.setPosition(userLatLng);
        }

        if (shareRoute){
            shareLocation(userLatLng);
        }
    }

    private void shareLocation(LatLng userLatLng) {
        SharedRouteApp app = (SharedRouteApp) getApplication();
        SharedLocationService sharedLocationService = app.getSharedLocationService();
        sharedLocationService.sendLocationUpdate(userLatLng);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    //todo split and move
    @Override
    public void updateMapMarker(final String sessionId, final LatLng newLatLng) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Marker marker = sessionIdToMarkers.get(sessionId);
                if (marker != null){
                    marker.setPosition(newLatLng);
                } else {
                    final MarkerOptions markerOptions = new MarkerOptions()
                            .position(newLatLng)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    Marker newMarker = mMap.addMarker(markerOptions);
                    sessionIdToMarkers.put(sessionId,newMarker);
                }
            }
        });
    }

    @Override
    public void onLocationServiceClose() {
        runOnUiThread(new ConnectionLostRunnable(this));
    }
}
