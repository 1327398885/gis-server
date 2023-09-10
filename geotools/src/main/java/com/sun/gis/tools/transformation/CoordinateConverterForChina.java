package com.sun.gis.tools.transformation;


/**
 * 坐标转换工具类
 * 天地图使用的是地球坐标系(WGS84);
 * 高德地图使用的是火星坐标系(GCJ02);
 * 百度地图使用的是百度坐标(bd09II)
 *
 * @author sunbt
 */
public class CoordinateConverterForChina {

    private static final double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    /**
     * 百度坐标系 (BD-09) 转 火星坐标系 (GCJ-02)
     * 即 百度 转 谷歌、高德
     *
     * @param bdLng 百度经度
     * @param bdLat 百度纬度
     * @return GCJ-02 坐标系中的经度和纬度
     */
    public static double[] bd09togcj02(double bdLng, double bdLat) {
        // 经纬度偏移量
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
        double ggLng = z * Math.cos(theta);
        double ggLat = z * Math.sin(theta);
        return new double[]{ggLng, ggLat};
    }

    /**
     * 火星坐标系 (GCJ-02) 转 百度坐标系 (BD-09)
     * 即 谷歌、高德 转 百度
     *
     * @param lng GCJ-02经度
     * @param lat GCJ-02纬度
     * @return BD-09 坐标系中的经度和纬度
     */
    public static double[] gcj02tobd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[]{bdLng, bdLat};
    }

    /**
     * WGS-84 转 GCJ-02
     *
     * @param lng WGS-84经度
     * @param lat WGS-84纬度
     * @return GCJ-02 坐标系中的经度和纬度
     */
    public static double[] wgs84togcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat};
        } else {
            double dlat = transformlat(lng - 105.0, lat - 35.0);
            double dlng = transformlng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - ee * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new double[]{mglng, mglat};
        }
    }

    /**
     * GCJ-02 转换为 WGS-84
     *
     * @param lng GCJ-02经度
     * @param lat GCJ-02纬度
     * @return WGS-84 坐标系中的经度和纬度
     */
    public static double[] gcj02towgs84(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat};
        } else {
            double dlat = transformlat(lng - 105.0, lat - 35.0);
            double dlng = transformlng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - ee * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new double[]{lng * 2 - mglng, lat * 2 - mglat};
        }
    }


    /**
     * 纬度坐标转换
     *
     * @param lng 原坐标经度
     * @param lat 原坐标纬度
     * @return double
     */
    public static double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度坐标转换
     *
     * @param lng 原坐标经度
     * @param lat 原坐标纬度
     * @return double
     */
    public static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }


    /**
     * 判断坐标是否在国内
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 若在国内返回 false，否则返回 true
     */
    public static boolean outOfChina(double lng, double lat) {
        // 纬度 3.86~53.55, 经度 73.66~135.05
        return !(lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
    }


    public static void main(String[] args) {
        double baiduLon = 121.699506;
        double baiduLat = 31.316136;
        double[] gcj02Point = bd09togcj02(baiduLon, baiduLat);
        double[] wgs84Point = gcj02towgs84(gcj02Point[0], gcj02Point[1]);
        System.out.println("百度原坐标：" + baiduLon + "---" + baiduLat);
        System.out.println("百度转wgs84坐标：" + wgs84Point[0] + "---" + wgs84Point[1]);
    }
}
