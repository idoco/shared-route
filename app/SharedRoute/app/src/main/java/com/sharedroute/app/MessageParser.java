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
            JSONObject locationUpdatesJson = (JSONObject) message;
            for (Object key : locationUpdatesJson.keySet()) {
                String keyString = key.toString();
                Object locationJson = locationUpdatesJson.get(keyString);
                String lat = ((JSONObject) locationJson).get("lat").toString();
                String lng = ((JSONObject) locationJson).get("lng").toString();
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                locationUpdatesMap.put(keyString, latLng);
            }
        } catch (ParseException e) {
            Log.e("JSONParser", "Error while parsing message " + jsonString, e);
        }
        return locationUpdatesMap;
    }

}
