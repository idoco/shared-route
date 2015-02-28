package sharedroute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
A simple Java verticle which receives user location messages and publish them to all other users.
 */
public class LocationVerticle extends Verticle {

    public static final int ACCURACY_IN_METERS = 50;

    private Set<ServerWebSocket> sockets = new HashSet<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void start() {
        final Logger _log = container.logger();
        _log.info("Websocket verticle up");

        final List<LatLng> route = MapUtils.parseRouteFromCsv("route_4.csv");

        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                // add version from the start
                if (ws.path().equals("/app")) {
                    sockets.add(ws);
                    _log.info("Incoming connection. current number of connections: " + sockets.size());
                    ws.dataHandler(new Handler<Buffer>() {
                        public void handle(Buffer data) {
                            _log.debug("Incoming location message [" + data.toString() + "]");
                            try {
                                JsonNode rootNode = mapper.readTree(data.getBytes());
                                // String sessionId = rootNode.get("sessionId").toString();
                                String lat = rootNode.get("lat").toString();
                                String lng = rootNode.get("lng").toString();
                                LatLng newPoint = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                                // Consider braking this into different handlers
                                if (MapUtils.findPointOnRoute(newPoint, route, ACCURACY_IN_METERS) != null) {
                                    for (ServerWebSocket socket : sockets) {
                                        if (!ws.equals(socket)) { //skip this socket
                                            socket.writeBinaryFrame(data);
                                        }
                                    }
                                }

                            } catch (IOException e) {
                                _log.error("Error parsing json [" + data.toString() + "]");
                            }
                        }
                    });

                    ws.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            sockets.remove(ws);
                            _log.info("Connection closed. current number of connections: " + sockets.size());
                        }
                    });

                    ws.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable event) {
                            _log.error("Connection error",event);
                            sockets.remove(ws);
                            _log.info("Connection closed. current number of connections: " + sockets.size());
                        }
                    });

                } else {
                    _log.warn("rejecting by uri");
                    ws.reject();
                }
            }
        }).listen(8080);
    }
}
