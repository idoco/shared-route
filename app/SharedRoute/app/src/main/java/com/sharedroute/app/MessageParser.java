package com.sharedroute.app;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cohid01 on 18/02/2015.
 * Decode and Encode json messages from server
 */
public class MessageParser {

    private final JSONParser parser;

    public MessageParser() {
        parser =new JSONParser();
    }

    /**
     * parse a json string to a map between session id strings and their latest LatLag
     */
    public Map<String, LatLng> parseLocationUpdateJson(String jsonString) {
        Map<String,LatLng> locationUpdatesMap = new HashMap<String, LatLng>();
        try {
            Object message = parser.parse(jsonString);
            JSONObject locationUpdateJson = (JSONObject) message;
            String sessionId = locationUpdateJson.get("sessionId").toString();
            String lat = locationUpdateJson.get("lat").toString();
            String lng = locationUpdateJson.get("lng").toString();
            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            locationUpdatesMap.put(sessionId, latLng);
        } catch (ParseException e) {
            Log.e("SharedRoute", "JSONParser error while parsing message " + jsonString, e);
        }
        return locationUpdatesMap;
    }


    public JSONObject createLocationJson(LatLng latLng, String sessionId) {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("sessionId", sessionId);
        jsonLocation.put("lat", latLng.latitude);
        jsonLocation.put("lng", latLng.longitude);
        return jsonLocation;
    }

}
