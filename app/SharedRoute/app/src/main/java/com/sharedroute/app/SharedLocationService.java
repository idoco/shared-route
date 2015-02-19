package com.sharedroute.app;

import android.location.Location;
import android.os.Build;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

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

    private final static String SERVER_URI = "ws://10.0.2.2:8080/app";
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
                this.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(final String s) {
                try {
                    Map<String, LatLng> locationUpdatesMap = messageParser.parseLocationUpdateJson(s);
                    updateLocationsOnMap(locationUpdatesMap);
                } catch (Exception e) {
                    Log.e("Websocket","message is "+ s ,e);
                }
                Log.i("Websocket","message is "+ s);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
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

    @SuppressWarnings("UnusedDeclaration")
    public void sendLocationUpdate(Location location){
        mWebSocketClient.send(location.toString());
    }
}
