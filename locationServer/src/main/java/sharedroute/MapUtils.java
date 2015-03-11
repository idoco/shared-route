package sharedroute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cohid01 on 27/02/2015.
 * Static methods for map
 */
public class MapUtils {

    // very specific GeoJSON parsing
    public static List<LatLng> parseRouteFromGeoJson(Vertx vertx, String fileName) {
        List<LatLng> routeData = new ArrayList<>(250);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Buffer buffer = vertx.fileSystem().readFileSync(fileName);
            JsonNode jsonNode = mapper.readTree(buffer.getBytes());
            JsonNode features = jsonNode.get("features");
            for (JsonNode feature : features) {
                JsonNode geometry = feature.get("geometry");
                JsonNode coordinates = geometry.get("coordinates");
                for (JsonNode coordinate : coordinates) {
                    double lng = coordinate.path(0).asDouble();
                    double lat = coordinate.path(1).asDouble();
                    routeData.add(new LatLng(lat,lng));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return routeData;
    }

    // This is a naive implementation which needs to be optimized
    public static LatLng findPointOnRoute(LatLng newPoint, List<LatLng> route, double minimumDistanceInMeters) {
        for (LatLng routePoint : route) {
            double distance = LatLngTool.distance(routePoint, newPoint, LengthUnit.METER);
            if (distance < minimumDistanceInMeters){
                return routePoint;
            }
        }
        return null;
    }
}
