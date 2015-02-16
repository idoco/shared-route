package com.sharedroute.app;

import android.content.res.AssetManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.TileOverlayOptions;

/**
 * Created by cohid01 on 16/02/2015.
 * Should contain static methods to help with the initial setup of the map
 */
public class MapBuildUtils {


    public static void customizeMap(final GoogleMap mMap, AssetManager assetManager) {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(assetManager);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(customMapTileProvider));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom < 15){
                    CameraUpdate fixedZoomUpdate= CameraUpdateFactory.newLatLngZoom(cameraPosition.target, 15);
                    mMap.moveCamera(fixedZoomUpdate);
                } else if (cameraPosition.zoom > 17){
                    CameraUpdate fixedZoomUpdate= CameraUpdateFactory.newLatLngZoom(cameraPosition.target, 17);
                    mMap.moveCamera(fixedZoomUpdate);
                }
            }
        });
    }

    @SuppressWarnings("UnusedParameters")
    public static void addRoutesToMap(GoogleMap mMap) {
//        LatLng a = new LatLng(32.0807898,34.7731816);
//        LatLng b = new LatLng(32.0799603,34.7738789);
//        PolylineOptions polylineOptions = new PolylineOptions().add(a,b).color(Color.RED);
//        mMap.addPolyline(polylineOptions);
    }

}
