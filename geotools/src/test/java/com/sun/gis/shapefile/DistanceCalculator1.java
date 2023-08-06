package com.sun.gis.shapefile;


import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.io.StringReader;

public class DistanceCalculator1 {
    public static void main(String[] args) {
        String geoJson = "{ \"coordinates\": [ [ [ 117.30023734851835, 34.50315865884127 ], [ 117.30023734851835, 34.49754522106805 ], [ 117.31126991423395, 34.49754522106805 ], [ 117.31126991423395, 34.50315865884127 ], [ 117.30023734851835, 34.50315865884127 ] ] ], \"type\": \"Polygon\" }";
        double lon = 117.29340126883068;  // 设置你的点的经度
        double lat = 34.500623132110206;  // 设置你的点的纬度

        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));

        GeometryJSON gjson = new GeometryJSON();
        try {
            Geometry polygon = gjson.read(new StringReader(geoJson));

            double minDistance = Double.MAX_VALUE;
            for (Coordinate vertex : polygon.getCoordinates()) {
                double distance = haversine(lat, lon, vertex.y, vertex.x);
                minDistance = Math.min(minDistance, distance);
            }

            System.out.println("Distance in meters: " + minDistance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        int r = 6371000; // metres
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r * c;
    }
}

