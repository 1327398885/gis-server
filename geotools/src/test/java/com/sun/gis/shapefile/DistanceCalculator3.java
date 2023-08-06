package com.sun.gis.shapefile;



public class DistanceCalculator3 {
    public static void main(String[] args) throws Exception {
        System.out.println(getGaussKrugerZoneEPSGCode( 117.29340126883068));
    }

    public static boolean isPointInGaussKrugerZone(double longitude, double latitude) {
        return (longitude >= 73.5 && longitude <= 136.5) && (latitude >= 0 && latitude <= 54);
    }

    public static int getGaussKrugerZone(double longitude) {
        if (longitude < 73.5 || longitude > 136.5) {
            throw new IllegalArgumentException("Longitude is not within CGCS2000 / 3-degree Gauss-Kruger zone range.");
        }
        return (int) Math.floor((longitude + 1.5) / 3.0) + 1;
    }

    public static String getGaussKrugerZoneEPSGCode(double longitude) {
        int zoneNumber = getGaussKrugerZone(longitude);
        int epsgCode = 4487 + zoneNumber;
        return "EPSG:" + epsgCode;
    }

}

