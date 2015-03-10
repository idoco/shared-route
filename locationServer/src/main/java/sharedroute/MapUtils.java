package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cohid01 on 27/02/2015.
 * Static methods for map
 */
public class MapUtils {

    public static List<LatLng> parseRouteFromCsv(Vertx vertx, String fileName) {
        List<LatLng> routeData = new ArrayList<>(100);
        try {
            Buffer buffer = vertx.fileSystem().readFileSync(fileName);
            StringReader stringReader = new StringReader(buffer.toString());
            BufferedReader reader = new BufferedReader(stringReader);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                String lat = RowData[0];
                String lng = RowData[1];
                LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                routeData.add(latLng);
            }
        } catch (IOException e) {
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
