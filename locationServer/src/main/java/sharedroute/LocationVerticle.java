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

    public static final int ACCURACY_IN_METERS = 50;
    public static final String CONNECTION_MAP = "connectionsSet";

    public void start() {
        final Logger _log = container.logger();
        final Pattern chatUrlPattern = Pattern.compile("/app/(\\w+)");
        final List<LatLng> route = MapUtils.parseRouteFromCsv("route_4.csv");
        _log.info("Websocket verticle up");

        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                final Matcher matcher = chatUrlPattern.matcher(ws.path());
                if (matcher.matches()) {
                    final ConnectionWrapper connection = registerNewConnection(ws, matcher);
                    ws.dataHandler(new IncomingLocationHandler(container, vertx, route));

                    ws.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            closeConnection(connection);
                        }
                    });

                    ws.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable event) {
                            _log.error("Connection error", event);
                            closeConnection(connection);
                        }
                    });

                } else {
                    _log.warn("rejecting by uri");
                    ws.reject();
                }
            }

            private ConnectionWrapper registerNewConnection(ServerWebSocket ws, Matcher matcher) {
                final String sessionId = matcher.group(1);
                final String connectionId = ws.binaryHandlerID();
                final ConnectionWrapper connection = new ConnectionWrapper(sessionId,connectionId);
                vertx.sharedData().getSet(CONNECTION_MAP).add(connection);
                _log.info("registering new connection with id: " + connectionId + " for session: " + sessionId +
                        " current number of connections: " + vertx.sharedData().getSet(CONNECTION_MAP).size());
                return connection;
            }

            private void closeConnection(ConnectionWrapper connection) {
                vertx.sharedData().getSet(CONNECTION_MAP).remove(connection);
                if (_log.isDebugEnabled()) {
                    _log.info("Connection closed. current number of connections: " +
                            vertx.sharedData().getSet(CONNECTION_MAP).size());
                }
            }

        }).listen(8080);
    }
}
