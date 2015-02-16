package com.sharedroute.app;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

public class MainMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker userLocationMarker;

    private final String TAG = getString(R.string.app_name);
    private final String serverURI = getString(R.string.server_uri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        buildGoogleApiClientIfNeeded();
        setUpMapIfNeeded();
        SharedLocationService.requestMapUpdates(serverURI, mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        buildGoogleApiClientIfNeeded();
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
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }
}
