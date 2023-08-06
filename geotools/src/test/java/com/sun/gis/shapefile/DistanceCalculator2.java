package com.sun.gis.shapefile;
import org.geotools.geojson.geom.GeometryJSON;

import java.io.IOException;
import java.io.StringReader;
import net.sf.geographiclib.*;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;



public class DistanceCalculator2 {
    public static void main(String[] args) throws Exception {
        System.out.println(getGaussKrugerZoneEPSGCode( 117.29340126883068));
    }

    private void test(){
        GeometryFactory geometryFactory = new GeometryFactory();

        String geoJson = "{ \"type\": \"Polygon\", \"coordinates\": [ [ [117.30023734851835, 34.50315865884127], [117.30023734851835, 34.49754522106805], [117.31126991423395, 34.49754522106805], [117.31126991423395, 34.50315865884127], [117.30023734851835, 34.50315865884127] ] ] }";

        Geometry polygon = null;
        try {
            polygon = new GeometryJSON().read(new StringReader(geoJson));
        } catch (IOException e) {
            e.printStackTrace();
        }

        double lon = 117.29340126883068;
        double lat = 34.500623132110206;
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < polygon.getCoordinates().length - 1; i++) {
            Coordinate c1 = polygon.getCoordinates()[i];
            Coordinate c2 = polygon.getCoordinates()[i+1];

            double[] start = new double[]{c1.x, c1.y};
            double[] end = new double[]{c2.x, c2.y};

            double d = computeDistance(start, end, new double[]{lon, lat});
            if (d < minDistance) {
                minDistance = d;
            }
        }

        System.out.println("Distance in meters: " + minDistance);
    }

    // Compute the shortest distance from a point to a line segment
    static double computeDistance(double[] start, double[] end, double[] point) {
        GeodesicData g1 = Geodesic.WGS84.Inverse(start[1], start[0], point[1], point[0]);
        GeodesicData g2 = Geodesic.WGS84.Inverse(point[1], point[0], end[1], end[0]);
        GeodesicData g3 = Geodesic.WGS84.Inverse(start[1], start[0], end[1], end[0]);

        if (g1.s12 + g2.s12 == g3.s12) {
            // The point is on the line segment
            return Math.min(g1.s12, g2.s12);
        } else {
            // The point is not on the line segment, compute the distance to each end
            return Math.min(computeDistanceToVertex(start, point), computeDistanceToVertex(end, point));
        }
    }

    // Compute the distance from a point to a vertex
    static double computeDistanceToVertex(double[] vertex, double[] point) {
        GeodesicData g = Geodesic.WGS84.Inverse(vertex[1], vertex[0], point[1], point[0]);
        return g.s12;
    }

    public static int getGaussKrugerZone(double longitude) {
        int zoneNumber = (int) Math.floor((longitude + 1.5) / 3.0) + 1;
        if (zoneNumber < 1) {
            zoneNumber = 1;
        } else if (zoneNumber > 60) {
            zoneNumber = 60;
        }
        return zoneNumber;
    }

    public static String getGaussKrugerZoneEPSGCode(double longitude) {
        int zoneNumber = getGaussKrugerZone(longitude);
        int epsgCode = 4487 + zoneNumber -1;
        return "EPSG:" + epsgCode;
    }

}

