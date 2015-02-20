package com.sharedroute.app;

import android.location.Location;
import android.os.Build;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

/**
 * Created by cohid01 on 13/02/2015.
 *
 */
public class SharedLocationService {

    private final MessageParser messageParser;
    private final WebSocketClient mWebSocketClient;

    private final static String SERVER_URI = "ws://10.0.2.2:8080/app";
    private final String sessionId = UUID.randomUUID().toString();
    private MapUpdatesListener mapUpdatesListener;

    public SharedLocationService(MapUpdatesListener mapUpdatesListener) {
        this.mapUpdatesListener = mapUpdatesListener;
        this.mWebSocketClient = buildWebSocketClient();
        this.messageParser = new MessageParser();
    }

    public WebSocketClient buildWebSocketClient() {

        URI uri;
        try {
            uri = new URI(SERVER_URI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(final String s) {
                Log.i("Websocket","Incoming message "+ s);
                Map<String, LatLng> locationUpdatesMap = messageParser.parseLocationUpdateJson(s);
                updateLocationsOnMap(locationUpdatesMap);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
                mapUpdatesListener.onLocationServiceClose();
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
        return mWebSocketClient;
    }

    private void updateLocationsOnMap(Map<String, LatLng> locationUpdatesMap) {
        for (String sessionId : locationUpdatesMap.keySet()) {
            LatLng newLatLng = locationUpdatesMap.get(sessionId);
            mapUpdatesListener.updateMapMarker(sessionId, newLatLng);
        }
    }

    @SuppressWarnings("unchecked")
    public void sendLocationUpdate(LatLng latLng){
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("sessionId", sessionId);
        jsonLocation.put("lat", latLng.latitude);
        jsonLocation.put("lng", latLng.longitude);
        mWebSocketClient.send(jsonLocation.toString());
    }
}
