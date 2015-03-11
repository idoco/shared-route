package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
A simple Java verticle which receives user location messages and publish them to all other users.
 */
public class LocationVerticle extends Verticle {

    public static final String CONNECTION_SET = "sharedroute.connections";

    public void start() {
        final Logger _log = container.logger();
        final Pattern chatUrlPattern = Pattern.compile("/app");
        final List<LatLng> route = MapUtils.parseRouteFromGeoJson(vertx, "map.geojson");
        _log.info("Websocket verticle up");

        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                final Matcher matcher = chatUrlPattern.matcher(ws.path());
                if (matcher.matches()) {
                    final String wsHandlerId = ws.binaryHandlerID();
                    registerConnection(wsHandlerId);
                    ws.dataHandler(new IncomingLocationHandler(container, vertx, wsHandlerId, route));
                    ws.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            unregisterConnection(wsHandlerId);
                        }
                    });
                    ws.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable event) {
                            _log.error("Connection error", event);
                            unregisterConnection(wsHandlerId);
                        }
                    });

                } else {
                    _log.warn("Rejecting connection by uri: " + ws.path());
                    ws.reject();
                }
            }

            private void registerConnection(String wsHandlerId) {
                vertx.sharedData().getSet(CONNECTION_SET).add(wsHandlerId);
                _log.info("Connection registered [id: " + wsHandlerId + "] " +
                        "current number of connections: " + getNumberOfConnections());
            }

            private void unregisterConnection(String wsHandlerId) {
                vertx.sharedData().getSet(CONNECTION_SET).remove(wsHandlerId);
                _log.info("Connection closed [id: " + wsHandlerId + "] " +
                        "current number of connections: " + getNumberOfConnections());
            }

            private int getNumberOfConnections() {
                return vertx.sharedData().getSet(CONNECTION_SET).size();
            }

        }).listen(8080);
    }
}
