package com.sun.gis.tools.projection;

/**
 * 投影工具
 */
public class ProjectedCoordinateSystem {
    public static void main(String[] args) throws Exception {
        // return EPSG:4527
        System.out.println(getGaussKrugerZoneEPSGCode(117.29340126883068));
    }

    /**
     * 判断给定的经度和纬度是否位于高斯-克吕格投影坐标系的范围内
     */
    public static boolean isPointInGaussKrugerZone(double longitude, double latitude) {
        return (longitude >= 73.5 && longitude <= 136.5) && (latitude >= 0 && latitude <= 54);
    }

    /**
     * 根据给定的经度获取高斯-克吕格投影坐标系中的区域编号
     *
     * @throws IllegalArgumentException 如果经度不在73.5到136.5的范围内
     */
    public static int getGaussKrugerZone(double longitude) {
        if ((longitude < 73.5 || longitude > 136.5)) {
            throw new IllegalArgumentException("Longitude is not within CGCS2000 / 3-degree Gauss-Kruger zone range.");
        }
        return (int) Math.floor((longitude + 1.5) / 3.0) + 1;
    }

    /**
     * 根据给定的经度获取高斯-克吕格投影坐标系中的EPSG代码
     */
    public static String getGaussKrugerZoneEPSGCode(double longitude) {
        int zoneNumber = getGaussKrugerZone(longitude);
        int epsgCode = 4487 + zoneNumber;
        return "EPSG:" + epsgCode;
    }
}
