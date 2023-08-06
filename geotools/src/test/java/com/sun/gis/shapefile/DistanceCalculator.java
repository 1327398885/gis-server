package com.sun.gis.shapefile;

import org.locationtech.jts.geom.*;
import org.locationtech.proj4j.*;

import org.locationtech.jts.geom.Geometry;

import org.geotools.geojson.geom.GeometryJSON;

import java.io.StringReader;

/**
 * 使用中
 */
public class DistanceCalculator {

    public static void main(String[] args) {

        // 创建一个Point对象，这将表示你的点
        double lon = 111.7362653962129;  // 设置你的点的经度
        double lat = 40.712992153869635;  // 设置你的点的纬度


        String geoJson = "{\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              111.74282981393162,\n" +
                "              40.72576127838573\n" +
                "            ],\n" +
                "            [\n" +
                "              111.74282981393162,\n" +
                "              40.719385639840056\n" +
                "            ],\n" +
                "            [\n" +
                "              111.75522717309315,\n" +
                "              40.719385639840056\n" +
                "            ],\n" +
                "            [\n" +
                "              111.75522717309315,\n" +
                "              40.72576127838573\n" +
                "            ],\n" +
                "            [\n" +
                "              111.74282981393162,\n" +
                "              40.72576127838573\n" +
                "            ]\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"type\": \"Polygon\"\n" +
                "      }";
        double distance = distanceCalculator(lon, lat, geoJson);
        System.out.println(distance);

    }

    /**
     * 计算点到多边形的距，
     * 如果点在多边形边上或内部则返回-1.0
     *
     * @param lon     点的经度
     * @param lat     点的纬度
     * @param geoJson 地块
     * @return 点到多边形距离(单位米)
     */
    public static double distanceCalculator(double lon, double lat, String geoJson) {

        double distanceInMeters = 0.0;

        // 创建一个GeometryFactory对象，这将被用于创建Point对象
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate coordinate = new Coordinate(lon, lat);

        try {
            // 读取你的GeoJSON数据，并创建一个Polygon对象

            GeometryJSON gjson = new GeometryJSON();
            Geometry geometry = gjson.read(new StringReader(geoJson));

            //获取当前投影坐标系
            String epsgCode = getGaussKrugerZoneEPSGCode(lon);

            // 创建Proj4j的CRSFactory对象
            CRSFactory crsFactory = new CRSFactory();

            // 创建源和目标的坐标参考系统
            CoordinateReferenceSystem srcCRS = crsFactory.createFromName("EPSG:4326");
            CoordinateReferenceSystem dstCRS = crsFactory.createFromName(epsgCode);

            // 创建转换函数
            CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
            CoordinateTransform transform = ctFactory.createTransform(srcCRS, dstCRS);

            // 将点的坐标转换到以米为单位的坐标参考系统
            ProjCoordinate projCoordinate = new ProjCoordinate();
            transform.transform(new ProjCoordinate(coordinate.x, coordinate.y), projCoordinate);
            Point pointTransformed = geometryFactory.createPoint(new Coordinate(projCoordinate.x, projCoordinate.y));

            // 将多边形的每个坐标转换到以米为单位的坐标参考系统
            Coordinate[] coordinates = new Coordinate[geometry.getCoordinates().length];
            for (int i = 0; i < geometry.getCoordinates().length; i++) {
                Coordinate srcCoordinate = geometry.getCoordinates()[i];
                ProjCoordinate dstProjCoordinate = new ProjCoordinate();
                transform.transform(new ProjCoordinate(srcCoordinate.x, srcCoordinate.y), dstProjCoordinate);
                coordinates[i] = new Coordinate(dstProjCoordinate.x, dstProjCoordinate.y);
            }
            Polygon polygonTransformed = geometryFactory.createPolygon(coordinates);

            // 如果点在多边形内部或边上，返回-1
            if (polygonTransformed.contains(pointTransformed) || polygonTransformed.touches(pointTransformed)) {
                return -1;
            }

            // 计算点到多边形的最短距离
            distanceInMeters = pointTransformed.distance(polygonTransformed);

            System.out.println("The shortest distance from the point to the polygon is " + distanceInMeters + " meters.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return distanceInMeters;
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
