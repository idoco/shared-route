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

    public static final int ACCURACY_IN_METERS = 50;

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
        String incomingData = data.toString();
        try {
            _log.debug("Incoming location message [" + incomingData + "]");
            JsonNode rootNode = mapper.readTree(data.getBytes());
            String sessionId = rootNode.get("sessionId").asText();
            double lat = rootNode.get("lat").asDouble();
            double lng = rootNode.get("lng").asDouble();
            LatLng newPoint = new LatLng(lat, lng);

            // Consider braking this into different handlers
            if (pointIsOnRoute(newPoint)) {
                vertx.eventBus().publish(LocationVerticle.LOCATION_PUBLISHED, incomingData);
            } else {
                _log.warn("Filtering out location from connection [" + sessionId + "] location is [" + newPoint + "]");
            }

        } catch (IOException e) {
            _log.error("Error parsing json [" + incomingData + "]",e);
        }
    }

    private boolean pointIsOnRoute(LatLng newPoint) {
        return MapUtils.findPointOnRoute(newPoint, route, ACCURACY_IN_METERS) != null;
    }
}
