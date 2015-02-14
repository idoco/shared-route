package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class LocationHttpVerticle extends Verticle {

    private Map<String,List<LatLng>> locationHistory = new HashMap<>();

    public void start() {

        container.logger().info("I'm up");

        RouteMatcher rm = new RouteMatcher();

        rm.get("/location/user/:userId", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                String userId = req.params().get("userId");
                List<LatLng> userLocationHistory = locationHistory.get(userId);
                if (userLocationHistory!=null && !userLocationHistory.isEmpty()) {
                    LatLng lastLocation = userLocationHistory.get(0);
                    req.response().end("User id: " + userId + " Location:"+lastLocation);
                } else {
                    req.response().end("User id: " + userId + " has no history");
                }
            }
        });

        rm.put("/location/user/:userId", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                String userId = req.params().get("userId");

                LatLng latLng = getLatLngFromRequest(req);
                addUserLocationToHistory(userId, latLng);

                req.response().end();
            }
        });

        // Catch all - serve the index page
        rm.getWithRegEx(".*", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                req.response().sendFile("index.html");
            }
        });

        vertx.createHttpServer().requestHandler(rm).listen(8080);
    }

    private void addUserLocationToHistory(String userId, LatLng latLng) {
        if (!locationHistory.containsKey(userId)) {
            locationHistory.put(userId, new ArrayList<LatLng>());
        }
        locationHistory.get(userId).add(latLng);
    }

    private LatLng getLatLngFromRequest(HttpServerRequest req) {
        String lat = req.params().get("lat");
        String lng = req.params().get("lng");
        double latDouble = Double.parseDouble(lat);
        double lngDouble = Double.parseDouble(lng);
        return new LatLng(latDouble, lngDouble);
    }
}
