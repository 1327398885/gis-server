package com.sun.gis.tools.transformation;

import java.math.BigDecimal;

public class CoordinateConverterForChina {
    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;

    // 将WGS84经纬度坐标转换为百度坐标
    public static double[] wgs84ToBaidu(double longitude, double latitude) {
        double x = longitude;
        double y = latitude;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
        double bdLon = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[]{bdLon, bdLat};
    }

    // 将百度坐标转换为WGS84经纬度坐标
    public static double[] baiduToWgs84(double bdLon, double bdLat) {
        double x = bdLon - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double wgLon = z * Math.cos(theta);
        double wgLat = z * Math.sin(theta);
        return new double[]{wgLon, wgLat};
    }

    // 将WGS84经纬度坐标转换为高德坐标
    public static double[] wgs84ToGaode(double longitude, double latitude) {
        double wgLon = longitude;
        double wgLat = latitude;
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * Math.PI);
        double mgLat = wgLat + dLat;
        double mgLon = wgLon + dLon;
        return new double[]{mgLon, mgLat};
    }

    // 将高德坐标转换为WGS84经纬度坐标
    public static double[] gaodeToWgs84(double mgLon, double mgLat) {
        double dLat = transformLat(mgLon - 105.0, mgLat - 35.0);
        double dLon = transformLon(mgLon - 105.0, mgLat - 35.0);
        double radLat = mgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (A / sqrtMagic * Math.cos(radLat) * Math.PI);
        double wgLat = mgLat - dLat;
        double wgLon = mgLon - dLon;
        return new double[]{wgLon, wgLat};
    }

    // 辅助方法：纬度转换
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    // 辅助方法：经度转换
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    public static void main(String[] args) {
        double wgsLon = 116.404;
        double wgsLat = 39.915;

        double[] baiduCoord = wgs84ToBaidu(wgsLon, wgsLat);
        double[] gaodeCoord = wgs84ToGaode(wgsLon, wgsLat);

        System.out.println("WGS84经纬度坐标：");
        System.out.println("经度：" + wgsLon);
        System.out.println("纬度：" + wgsLat);
        System.out.println();

        System.out.println("百度坐标：");
        System.out.println("经度：" + baiduCoord[0]);
        System.out.println("纬度：" + baiduCoord[1]);
        System.out.println();

        System.out.println("高德坐标：");
        System.out.println("经度：" + gaodeCoord[0]);
        System.out.println("纬度：" + gaodeCoord[1]);
    }
}
