package sharedroute;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cohid01 on 27/02/2015.
 * Static methods for map
 */
public class MapUtils {

    public static List<LatLng> parseRouteFromCsv(String fileName) {
        List<LatLng> routeData = new ArrayList<>(100);
        try {
            InputStream in = new FileInputStream("src/main/resources/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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
