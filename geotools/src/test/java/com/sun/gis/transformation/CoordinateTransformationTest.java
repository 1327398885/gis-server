package com.sun.gis.transformation;

import com.sun.gis.pojo.PointPojo;
import com.sun.gis.tools.transformation.CoordinateTransformation;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author sunbt
 * @date 2023/8/20 12:41
 */
@SpringBootTest
public class CoordinateTransformationTest {

    /**
     * 4326坐标转3857坐标
     */
    @Test
    public void transformationTest1() {
        //某点的经纬度
        Double y = 34.26095856;
        Double x = 108.94237668;
        PointPojo pointPojo = CoordinateTransformation.transformation(x, y, "EPSG:4326", "EPSG:3857");
        System.out.println(pointPojo.toString());
    }

    /**
     * 4549转4326坐标
     */
    @Test
    public void transformationTest2() {
        //某点的经纬度
        double y = 3514147.96;
        double x = 350543.244;
        PointPojo pointPojo = CoordinateTransformation.transformation(x, y, "EPSG:4549", "EPSG:4326");
        System.out.println(pointPojo.toString());

    }

    /**
     * Geometry 坐标转换 点
     * 4326转3857
     */
    @Test
    public void pointTest() {
        try {
            // 一个位于中国的点（例如杭州附近）
            String wktPoint = "POINT(119.81410595703113 29.86877021528116)";
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wktPoint);
            // WGS 84
            int sourceEPSGCode = 4326;
            // Web Mercator
            int targetEPSGCode = 3857;

            Geometry transformedGeom = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);
            System.out.println("原始图形：" + wktPoint);
            System.out.println("转换后的图形：" + transformedGeom.toText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Geometry 坐标转换 多点
     * 4326转3857
     */
    @Test
    public void multiPointTest() {
        try {
            // 一个位于中国的点（例如杭州附近）
            String wkt = "MULTIPOINT(30 20, 40 30, 20 40, 50 60)";
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wkt);
            // WGS 84
            int sourceEPSGCode = 4326;
            // Web Mercator
            int targetEPSGCode = 3857;

            Geometry transformedGeom = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);
            System.out.println("原始图形：" + wkt);
            System.out.println("转换后的图形：" + transformedGeom.toText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Geometry 坐标转换 线
     * 4326 to 3857
     */
    @Test
    public void lineStringTest() {
//        // 创建一个LineString对象
//        Coordinate[] coords = new Coordinate[]{
//                new Coordinate(10, 45),
//                new Coordinate(11, 45),
//                new Coordinate(12, 46)
//        };
//        GeometryFactory factory = new GeometryFactory();
//        LineString lineString = factory.createLineString(coords);
//        // 输出原始LineString
//        System.out.println("原始图形: " + lineString.toText());
        // 另一个WKT格式的多边形示例
        String wkt = "LINESTRING(30 10, 40 30, 20 40, 50 60)";
        WKTReader reader = new WKTReader();
        try {
            Geometry geom = reader.read(wkt);
            System.out.println("原始图形: " + geom);
            // 进行坐标转换
            int sourceEPSGCode = 4326;
            int targetEPSGCode = 3857;
            // 使用上述方法进行转换
            Geometry transformedLineString = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);

            // 输出转换后的LineString
            System.out.println("转换后的图形: " + transformedLineString.toText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Geometry 坐标转换 线
     * 4326 to 3857
     */
    @Test
    public void multiLineStringTest() {
        String wkt = "MULTILINESTRING((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10))";
        WKTReader reader = new WKTReader();
        try {
            Geometry geom = reader.read(wkt);
            System.out.println("原始图形: " + geom);
            // 进行坐标转换
            int sourceEPSGCode = 4326;
            int targetEPSGCode = 3857;
            // 使用上述方法进行转换
            Geometry transformedLineString = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);

            // 输出转换后的LineString
            System.out.println("转换后的图形: " + transformedLineString.toText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Geometry 坐标转换 多边形
     * 4326 to 3857
     */
    @Test
    public void polygonTest() {
        // 另一个WKT格式的多边形示例
        String wkt = "POLYGON ((10 10, 50 10, 50 50, 10 50, 10 10), (20 20, 40 20, 40 40, 20 40, 20 20))";

        WKTReader reader = new WKTReader();
        try {
            Geometry geom = reader.read(wkt);
            System.out.println("原始图形: " + geom);
            // 进行坐标转换
            int sourceEPSGCode = 4326;
            int targetEPSGCode = 3857;
            Geometry transformedPolygon = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);
            System.out.println("转换后的图形: " + transformedPolygon);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Geometry 坐标转换 多多边形
     * 4326 to 3857
     */
    @Test
    public void multiPolygonTest() {
        // 另一个WKT格式的多边形示例
        String wkt = "MULTIPOLYGON(((75 14, 135 14, 135 53, 75 53, 75 14)), ((60 30, 90 30, 90 40, 60 40, 60 30)))";

        WKTReader reader = new WKTReader();
        try {
            Geometry geom = reader.read(wkt);
            System.out.println("原始图形: " + geom);
            // 进行坐标转换
            int sourceEPSGCode = 4326;
            int targetEPSGCode = 3857;
            Geometry transformedPolygon = CoordinateTransformation.geometryTransformMulti(geom, sourceEPSGCode, targetEPSGCode);
            System.out.println("转换后的图形: " + transformedPolygon);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
