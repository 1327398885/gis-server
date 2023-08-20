package com.sun.gis.tools.transformation;

import com.sun.gis.pojo.PointPojo;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 坐标转换工具
 *
 * @author sunbt
 * @date 2023/8/20 0:55
 */
public class CoordinateTransformation {

    private static final Logger log = LoggerFactory.getLogger(CoordinateTransformation.class);

    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    static WKTReader reader = new WKTReader(geometryFactory);

    private static final Pattern COORDINATE_PATTERN = Pattern.compile("[1-9]\\d*\\.?\\d*\\s[1-9]\\d*\\.?\\d*");


    /**
     * 点坐标转换
     *
     * @param x         横坐标
     * @param y         纵坐标
     * @param sourceSRS 源数据坐标 EPSG:4549
     * @param targetSRS 目标数据坐标 EPSG:4326
     * @return PointPojo
     */
    public static PointPojo transformation(Double x, Double y, String sourceSRS, String targetSRS) {
        PointPojo pointPojo = new PointPojo();

        try {
            //封装点，x,y 要反过来，不反过来就报纬度超过90的异常
            String point = "POINT(" + y + " " + x + ")";
            Geometry geometry = reader.read(point);
            //这里要选择转换的坐标系是可以随意更换的
            CoordinateReferenceSystem source = CRS.decode(sourceSRS);
            CoordinateReferenceSystem target = CRS.decode(targetSRS);

            MathTransform transform = CRS.findMathTransform(source, target, true);

            //转换坐标
            Geometry trans = JTS.transform(geometry, transform);
            pointPojo.setX(trans.getCentroid().getX());
            pointPojo.setY(trans.getCentroid().getY());
        } catch (FactoryException | TransformException | ParseException e) {
            e.printStackTrace();
        }


        return pointPojo;
    }

    /**
     * Geometry Multi 坐标转换
     * 支持所有类型Geometry 坐标转换
     *
     * @param geom           地理实体
     * @param sourceEPSGCode 源坐标系
     * @param targetEPSGCode 目标坐标系
     * @return Geometry
     */
    public static Geometry geometryTransformMulti(Geometry geom, int sourceEPSGCode, int targetEPSGCode) {
        try {
            CoordinateReferenceSystem crsResource = CRS.decode("EPSG:" + sourceEPSGCode);
            CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:" + targetEPSGCode);
            MathTransform transform = CRS.findMathTransform(crsResource, crsTarget, true);
            String wktString = geom.toString();
            log.debug("转换前的WKT字符串：" + wktString);

            Matcher matcher = COORDINATE_PATTERN.matcher(wktString);
            while (matcher.find()) {
                String cordStr = matcher.group();
                String[] cord = cordStr.split("\\s+");
                double x = Double.parseDouble(cord[0]);
                double y = Double.parseDouble(cord[1]);

                String point = "POINT(" + y + " " + x + ")";
                Geometry pointGeom = reader.read(point);
                Geometry resGeom = JTS.transform(pointGeom, transform);
                String[] str = resGeom.toString().substring(resGeom.toString().lastIndexOf("(") + 1, resGeom.toString().length() - 1).split("\\s+");
                String cordStr2 = str[1] + " " + str[0];
                wktString = wktString.replaceAll(cordStr, cordStr2);
            }
            log.debug("转换后的WKT字符串：" + wktString);
            return reader.read(wktString);
        } catch (Exception e) {
            log.debug("坐标系转换出错：" + e.getMessage());
            return null;
        }
    }


}
