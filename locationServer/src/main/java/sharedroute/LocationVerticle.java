package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple Java verticle which receives user location messages and publish them to all other users.
 **/
public class LocationVerticle extends Verticle {

    public static final String LOCATION_PUBLISHED = "location.published";
    public Set<ServerWebSocket> connectedClients = new HashSet<>();

    public void start() {
        final Logger _log = container.logger();
        final Pattern chatUrlPattern = Pattern.compile("/app");
        final List<LatLng> route = MapUtils.parseRouteFromGeoJson(vertx, "accurate_map.geojson");
        _log.info("Websocket verticle is up");

        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                final Matcher matcher = chatUrlPattern.matcher(ws.path());
                if (matcher.matches()) {
                    connectedClients.add(ws);
                    _log.info("Connection registered. Current number of connections: " + connectedClients.size());
                    ws.dataHandler(new IncomingLocationHandler(container, vertx, route));

                    ws.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            connectedClients.remove(ws);
                            _log.info("Connection closed. Current number of connections: " + connectedClients.size());
                        }
                    });

                    ws.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable event) {
                            connectedClients.remove(ws);
                            _log.error("Connection error. Current number of connections: " + connectedClients.size());
                        }
                    });

                } else {
                    _log.warn("Rejecting connection by uri: " + ws.path());
                    ws.reject();
                }
            }
        }).listen(8080);

        // Publish new location updates to all connected clients
        vertx.eventBus().registerHandler(LOCATION_PUBLISHED,new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> event) {
                String locationUpdateJson = event.body();
                for (ServerWebSocket serverWebSocket : connectedClients) {
                    serverWebSocket.writeTextFrame(locationUpdateJson);
                }
            }
        });

    }
}
