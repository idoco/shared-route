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
import org.java_websocket.client.WebSocketClient;


public class MainMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;

    private final String TAG = "SharedRouteApp";
    private Marker userLocationMarker;
    private WebSocketClient mWebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        setUpMapIfNeeded();
        String serverURI = getString(R.string.server_uri);
        mWebSocketClient = LocationUpdatesService.connectWebSocket(serverURI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            return;
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(getResources().getAssets());
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(customMapTileProvider));

        addRoutesToMap(mMap);

        LatLng initialLatLng = new LatLng(32.0807898,34.7731816);
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(initialLatLng, 16);
        mMap.moveCamera(upd);

    }

    private void addRoutesToMap(GoogleMap mMap) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

//    private void addRoute() {
//        LatLng a = new LatLng(32.0807898,34.7731816);
//        LatLng b = new LatLng(32.0799603,34.7738789);
//        PolylineOptions polylineOptions = new PolylineOptions().add(a,b).color(Color.RED);
//        mMap.addPolyline(polylineOptions);
//    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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
    public void onLocationChanged(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (userLocationMarker == null) {
            MarkerOptions initialUserLocation = new MarkerOptions()
                    .position(userLatLng)
                    .title("Me")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            userLocationMarker = mMap.addMarker(initialUserLocation);
        } else {
            userLocationMarker.setPosition(userLatLng);
        }

        //move camera to the user
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(userLatLng, 16);
        mMap.moveCamera(upd);
    }
}
