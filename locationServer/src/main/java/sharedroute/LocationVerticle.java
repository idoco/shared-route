package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class LocationVerticle extends Verticle {

    private Map<String,List<LatLng>> locationHistory = new HashMap<>();
    private Logger _log = container.logger();

    public void start() {
        _log.info("Websocket verticle up");

        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                _log.info("incoming for ["+ws.path()+"]");

                if (ws.path().equals("/app")) {
                    ws.dataHandler(new Handler<Buffer>() {
                        public void handle(Buffer data) {
                            _log.info("writing back ["+data.toString()+"]");
                            ws.writeTextFrame(data.toString()); // Echo it back
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
