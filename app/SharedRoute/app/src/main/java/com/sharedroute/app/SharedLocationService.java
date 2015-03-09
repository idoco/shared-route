package com.sharedroute.app;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by cohid01 on 13/02/2015.
 *
 */
public class SharedLocationService {

    private final MessageParser messageParser;
    private final WebSocketClient mWebSocketClient;

    private final static String SERVER_URI = "ws://sharedroute.cloudapp.net/app";
//    private final static String SERVER_URI = "ws://10.0.2.2:8080/app";

    private final String sessionId;
    private MapUpdatesListener mapUpdatesListener;

    public SharedLocationService(MapUpdatesListener mapUpdatesListener, String sessionId) {
        this.mapUpdatesListener = mapUpdatesListener;
        this.sessionId = sessionId;
        this.mWebSocketClient = buildWebSocketClient();
        this.messageParser = new MessageParser();
    }

    public WebSocketClient buildWebSocketClient() {

        URI uri;
        try {
            uri = new URI(SERVER_URI + "/" + sessionId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("SharedRoute", "Websocket Opened");
            }

            @Override
            public void onMessage(final String s) {
                Log.i("SharedRoute","Websocket incoming message "+ s);
                Map<String, LatLng> locationUpdatesMap = messageParser.parseLocationUpdateJson(s);
                updateLocationsOnMap(locationUpdatesMap);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("SharedRoute", "Websocket Closed " + s);
                mapUpdatesListener.onLocationServiceClose();
            }

            @Override
            public void onError(Exception e) {
                Log.i("SharedRoute", "Websocket Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
        return mWebSocketClient;
    }

    private void updateLocationsOnMap(Map<String, LatLng> locationUpdatesMap) {
        for (String sessionId : locationUpdatesMap.keySet()) {
            LatLng newLatLng = locationUpdatesMap.get(sessionId);
            mapUpdatesListener.addOrUpdateMapMarker(sessionId, newLatLng);
        }
    }

    @SuppressWarnings("unchecked")
    public void sendLocationUpdate(LatLng latLng){
        JSONObject jsonLocation = messageParser.createLocationJson(latLng, sessionId);
        mWebSocketClient.send(jsonLocation.toString());
    }

    public boolean isClosed() {
        return mWebSocketClient.getConnection().isClosed();
    }

    public static final class Builder {
        private MapUpdatesListener mapUpdatesListener;
        private String sessionId;

        public Builder(MapUpdatesListener mapUpdatesListener) {
            this.mapUpdatesListener = mapUpdatesListener;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public SharedLocationService build() {
            return new SharedLocationService(mapUpdatesListener, sessionId);
        }
    }
}
