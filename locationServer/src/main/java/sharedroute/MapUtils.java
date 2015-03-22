package sharedroute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        List<LatLng> routeData = new ArrayList<>(270);
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
        System.out.println("Size is" + routeData.size());
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

    // Utility method for scanning all the route points and printing an array with mid points
    // for all points that are more than 50 meters a way (No real need to use vertx here)
    @SuppressWarnings("UnusedDeclaration")
    public static List<LatLng> parseRouteAndPrintWithMidPoints(Vertx vertx, String fileName) {
        List<LatLng> routeData = new ArrayList<>(250);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Buffer buffer = vertx.fileSystem().readFileSync(fileName);
            JsonNode jsonNode = mapper.readTree(buffer.getBytes());
            JsonNode features = jsonNode.get("features");
            for (JsonNode feature : features) {
                JsonNode geometry = feature.get("geometry");
                JsonNode coordinates = geometry.get("coordinates");
                ArrayNode newCoordinates = mapper.createArrayNode();
                LatLng prevPoint = null;
                for (JsonNode coordinate : coordinates) {
                    double lng = coordinate.path(0).asDouble();
                    double lat = coordinate.path(1).asDouble();
                    LatLng point = new LatLng(lat, lng);

                    if (prevPoint!=null){
                        double distance = LatLngTool.distance(point, prevPoint, LengthUnit.METER);
                        if (distance/2 > 50){
                            double newLat = (prevPoint.getLatitude() + point.getLatitude()) / 2;
                            double newLng = (prevPoint.getLongitude() + point.getLongitude()) / 2;
                            addPointToJsonArray(mapper, newCoordinates, newLng, newLat);
                        }
                    }

                    addPointToJsonArray(mapper, newCoordinates, lng, lat);
                    prevPoint = point;
                    routeData.add(point);
                }
                System.out.println(newCoordinates);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return routeData;
    }

    private static void addPointToJsonArray(ObjectMapper mapper, ArrayNode newCoordinates, double lng, double lat) {
        ArrayNode pointNode = mapper.createArrayNode();
        pointNode.add(lng).add(lat).add(0);
        newCoordinates.add(pointNode);
    }
}
