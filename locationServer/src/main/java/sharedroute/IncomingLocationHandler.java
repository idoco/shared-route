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
import java.util.Set;

/**
* Created by cohid01 on 08/03/2015.
*/
class IncomingLocationHandler implements Handler<Buffer> {

    public static final int ACCURACY_IN_METERS = 50;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Vertx vertx;
    private final Logger _log;
    private final List<LatLng> route;
    private final String wsHandlerId;

    public IncomingLocationHandler(Container container, Vertx vertx, String wsHandlerId, List<LatLng> route) {
        this._log = container.logger();
        this.vertx = vertx;
        this.wsHandlerId = wsHandlerId;
        this.route = route;
    }

    public void handle(Buffer data) {
        try {
            _log.debug("Incoming location message [" + data.toString() + "]");
            JsonNode rootNode = mapper.readTree(data.getBytes());
            // String sessionId = rootNode.get("sessionId").asText();
            double lat = rootNode.get("lat").asDouble();
            double lng = rootNode.get("lng").asDouble();
            LatLng newPoint = new LatLng(lat, lng);

            // Consider braking this into different handlers
            if (pointIsOnRoute(newPoint)) {
                Set<String> connections = vertx.sharedData().getSet(LocationVerticle.CONNECTION_SET);
                for (String registeredWsHandlerId : connections) {
                    // do not send incoming message to its sender
                    if (!wsHandlerId.equals(registeredWsHandlerId)) {
                        vertx.eventBus().send(registeredWsHandlerId, data);
                    }
                }
            } else {
                _log.warn("Filtering out location from connection ["+wsHandlerId+"] location is ["+newPoint+"]");
            }

        } catch (IOException e) {
            _log.error("Error parsing json [" + data.toString() + "]");
        }
    }

    private boolean pointIsOnRoute(LatLng newPoint) {
        return MapUtils.findPointOnRoute(newPoint, route, ACCURACY_IN_METERS) != null;
    }
}
