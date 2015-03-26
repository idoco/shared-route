package com.sharedroute.app;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import com.cocoahero.android.geojson.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cohid01 on 16/02/2015.
 * Should contain static methods to help with the initial setup of the map
 */
public class MapBuildUtils {


    public static final int MIN_ZOOM = 14;
    public static final int MAX_ZOOM = 17;
    public static final String ROUTE_DATA_GEOJSON = "route_data/map.geojson";

    public static void customizeMap(final GoogleMap mMap, AssetManager assetManager) {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.getUiSettings().setZoomControlsEnabled(false);
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

        addRouteToMap(mMap, assetManager);
    }

    private static void addRouteToMap(GoogleMap mMap, AssetManager assetManager) {
        try {
            InputStream in = assetManager.open(ROUTE_DATA_GEOJSON);
            GeoJSONObject geoJSON = GeoJSON.parse(in);
            String type = geoJSON.getType();
            if (type.equalsIgnoreCase("FeatureCollection")){
                FeatureCollection featureCollection = (FeatureCollection) geoJSON;
                for (Feature feature : featureCollection.getFeatures()) {
                    addFeatureToMap(mMap, feature);
                }
            } else {
                Log.w("SharedRoute", "Unexpected geoJson structure");
            }
        }
        catch (Exception e) {
            Log.e("SharedRoute", "Failed to parse map geoJson");
        }
    }

    private static void addFeatureToMap(GoogleMap mMap, Feature feature) throws JSONException {
        Geometry geometry = feature.getGeometry();
        if (geometry.getType().equalsIgnoreCase("LineString")) {
            LineString lineString = (LineString) geometry;
            List<LatLng> line = new ArrayList<LatLng>(100);
            for (Position position : lineString.getPositions()) {
                line.add(new LatLng(position.getLatitude(), position.getLongitude()));
            }
            JSONObject properties = feature.getProperties();
            String colorHex = properties.getString("stroke");
            int colorInt = Color.parseColor(colorHex);
            addLineToMap(mMap, line, colorInt);
        } else {
            Log.w("SharedRoute", "Unrecognized map geometry");
        }
    }

    @SuppressWarnings("UnusedParameters")
    public static void addLineToMap(GoogleMap mMap, List<LatLng> route4, int color) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(color)
                .zIndex(1)
                .addAll(route4);
        mMap.addPolyline(polylineOptions);
    }

    static float distance(LatLng location1, LatLng location2) {
        float[] distanceResultArray = new float[1];
        Location.distanceBetween(
                location1.latitude, location1.longitude,
                location2.latitude, location2.longitude, distanceResultArray);
        return distanceResultArray[0];
    }
}
