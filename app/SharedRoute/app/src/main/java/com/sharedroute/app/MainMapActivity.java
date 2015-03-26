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
import com.sharedroute.app.tasks.AddOrUpdateMarkerRunnable;
import com.sharedroute.app.tasks.ClearOldMarkersTimerTask;
import com.sharedroute.app.tasks.ClearToolTipTask;
import com.sharedroute.app.tasks.ConnectionLostRunnable;
import com.sharedroute.app.views.TooltipView;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class MainMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, MapUpdatesListener {

    public static final int TOOLTIP_APPEARANCE_TIME = 7 * 1000;
    public static final int MARKER_CLEANER_INTERVAL = 60 * 1000;
    public static final int LOCATION_REQUEST_INTERVAL = 5 * 1000;
    public static final LatLng CITY_CENTER_LOCATION = new LatLng(32.0807898, 34.7731816); //TLV center
    public static final int DEFAULT_ZOOM = 16;
    public static final String TAG = "SharedRoute";

    private GoogleMap mainMap;
    private GoogleApiClient googleApiClient;
    private Marker userLocationMarker;
    private final Map<String, MarkerWrapper> sessionIdToMarkers = new ConcurrentHashMap<String, MarkerWrapper>();

    private String uniqueId;
    private Timer timer;
    private TooltipView tooltipView;
    private SharedRouteApp sharedRouteApp;
    private SharedLocationService sharedLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        setContentView(R.layout.activity_main_map);
        setupToolTip();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!sharedRouteApp.isSharingRoute()){
            stopServices();
        }
    }

    private void init() {
        sharedRouteApp = (SharedRouteApp) getApplication();
        sharedRouteApp.setSharingRoute(false);
        sharedRouteApp.setMainActivity(this);
        setUpMapIfNeeded();
        buildGoogleApiClientIfNeeded();
        connectToSharedLocationServicesIfNeeded();
        startTimer();
    }

    public void stopServices() {
        sharedLocationService.closeConnection();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }


    public void setupToolTip() {
        tooltipView = (TooltipView) findViewById(R.id.tooltip_1);
        tooltipView.setClickable(true);
        tooltipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tooltipView.setVisibility(View.INVISIBLE);
            }
        });
        new Timer().schedule(new ClearToolTipTask(this), TOOLTIP_APPEARANCE_TIME);
    }

    private void startTimer() {
        if (timer != null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new ClearOldMarkersTimerTask(this), MARKER_CLEANER_INTERVAL, MARKER_CLEANER_INTERVAL);
    }

    public void iAmOnButtonClicked(View view) {
        sharedRouteApp.setSharingRoute(true);
        if (userLocationMarker != null && userLocationMarker.getPosition() != null) {
            shareLocation(userLocationMarker.getPosition());
        }
        Intent intent = new Intent(this, RideActivity.class);
        startActivity(intent);
    }

    private void setUpMapIfNeeded() {
        if (mainMap != null) {
            return;
        }
        mainMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mainMap == null) {
            return;
        }

        MapBuildUtils.customizeMap(mainMap, getResources().getAssets());

        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(CITY_CENTER_LOCATION, DEFAULT_ZOOM);
        mainMap.moveCamera(upd);
    }

    protected synchronized void buildGoogleApiClientIfNeeded() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            googleApiClient.reconnect();
        }
    }

    public void connectToSharedLocationServicesIfNeeded() {
        if (sharedLocationService == null || sharedLocationService.isClosed()) { //should I reuse the old connection?
            sharedLocationService = new SharedLocationService.Builder(this)
                    .setSessionId(uniqueId)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL);

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

            userLocationMarker = mainMap.addMarker(initialUserLocation);

            //move camera to the user on the first location update

            float userDistanceFromCenter = MapBuildUtils.distance(CITY_CENTER_LOCATION, userLatLng);
            if (userDistanceFromCenter < 5000) {
                CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM);
                mainMap.moveCamera(upd);
            }

        } else {
            userLocationMarker.setPosition(userLatLng);
        }
        shareLocation(userLatLng);
    }

    private void shareLocation(LatLng userLatLng) {
        if (sharedRouteApp.isSharingRoute()) {
            sharedLocationService.sendLocationUpdate(userLatLng);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void addOrUpdateMapMarker(final String sessionId, final LatLng newLatLng) {
        this.runOnUiThread(new AddOrUpdateMarkerRunnable(this, sessionId, newLatLng));
    }

    @Override
    public void onLocationServiceClose() {
        runOnUiThread(new ConnectionLostRunnable(this));
    }

    public Map<String, MarkerWrapper> getSessionIdToMarkers() {
        return sessionIdToMarkers;
    }

    public GoogleMap getMainMap() {
        return mainMap;
    }

    public TooltipView getTooltipView() {
        return tooltipView;
    }
}
