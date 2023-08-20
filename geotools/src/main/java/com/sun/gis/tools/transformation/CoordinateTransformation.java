package com.sun.gis.tools.transformation;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
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


    /**
     * 该模式适用于匹配两个数字的坐标，例如地理坐标或二维平面上的点，其中每个数字都不能以0开头
     *
     * [1-9]: 匹配一个1到9之间的数字（不包括0）。
     * \\d*: 匹配零个或多个数字字符（\\d表示数字，*表示零次或多次匹配）。
     * \\.?: 匹配零个或一个点字符（.通常在正则表达式中用作特殊字符，所以需要使用\\.来匹配实际的点字符。?表示零次或一次匹配）。
     * \\d*: 再次匹配零个或多个数字字符。
     * \\s: 匹配一个空白字符（例如空格、制表符等）。
     * [1-9]\\d*\\.?\\d*: 和前面相同，匹配一个1到9之间的数字，然后是零个或多个数字，然后是可选的小数点和更多的数字。
     */
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("[1-9]\\d*\\.?\\d*\\s[1-9]\\d*\\.?\\d*");


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
