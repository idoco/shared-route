package com.sharedroute.app;

import android.content.res.AssetManager;
import android.graphics.Color;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cohid01 on 16/02/2015.
 * Should contain static methods to help with the initial setup of the map
 */
public class MapBuildUtils {


    public static final int MIN_ZOOM = 14;
    public static final int MAX_ZOOM = 17;

    public static void customizeMap(final GoogleMap mMap, AssetManager assetManager) {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(assetManager);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(customMapTileProvider));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom < MIN_ZOOM){
                    CameraUpdate fixedZoomUpdate= CameraUpdateFactory.newLatLngZoom(cameraPosition.target, MIN_ZOOM);
                    mMap.moveCamera(fixedZoomUpdate);
                } else if (cameraPosition.zoom > MAX_ZOOM){
                    CameraUpdate fixedZoomUpdate= CameraUpdateFactory.newLatLngZoom(cameraPosition.target, MAX_ZOOM);
                    mMap.moveCamera(fixedZoomUpdate);
                }
            }
        });

        List<LatLng> route4 = parseRouteFromCsv("route_4.csv",assetManager);
        addRouteToMap(mMap, route4, Color.BLUE);
    }

    private static List<LatLng> parseRouteFromCsv(String fileName, AssetManager assetManager) {
        List<LatLng> routeData = new ArrayList<LatLng>(100);
        try {
            InputStream in = assetManager.open("route_data/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                String lat = RowData[0];
                String lng = RowData[1];
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                routeData.add(latLng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return routeData;
    }

    @SuppressWarnings("UnusedParameters")
    public static void addRouteToMap(GoogleMap mMap, List<LatLng> route4, int color) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(color)
                .zIndex(1)
                .addAll(route4);
        mMap.addPolyline(polylineOptions);
    }

}
