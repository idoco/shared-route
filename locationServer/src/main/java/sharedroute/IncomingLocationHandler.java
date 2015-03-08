package sharedroute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import java.io.IOException;
import java.util.List;

/**
* Created by cohid01 on 08/03/2015.
*/
class IncomingLocationHandler implements Handler<Buffer> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Vertx vertx;
    private final Logger _log;
    private final List<LatLng> route;

    public IncomingLocationHandler(Container container, Vertx vertx, List<LatLng> route) {
        this._log = container.logger();
        this.vertx = vertx;
        this.route = route;
    }

    public void handle(Buffer data) {
        try {
            JsonNode rootNode = mapper.readTree(data.getBytes());
            String sessionId = rootNode.get("sessionId").toString();
            String lat = rootNode.get("lat").toString();
            String lng = rootNode.get("lng").toString();
            LatLng newPoint = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

            // Consider braking this into different handlers
            if (MapUtils.findPointOnRoute(newPoint, route, LocationVerticle.ACCURACY_IN_METERS) != null) {
                for (Object connectionObject : vertx.sharedData().getSet(LocationVerticle.CONNECTION_MAP)) {
                    ConnectionWrapper connection = (ConnectionWrapper) connectionObject;
                    if (!connection.getSessionId().equals(sessionId)) {
                        vertx.eventBus().send(connection.getSessionId(), data);
                    }
                }
            }

        } catch (IOException e) {
            _log.error("Error parsing json [" + data.toString() + "]");
        }
    }
}
