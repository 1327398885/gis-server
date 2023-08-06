package com.sun.gis.dataTest;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;

import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;


import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonTest {

    //这行代码创建了一个 StyleFactory 实例，它是 GeoTools 中的一个工厂类，用于创建样式对象。样式对象用于定义地图中地理要素（如点、线、面）的显示方式，包括颜色、线型、填充等。
    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //这行代码创建了一个 FilterFactory2 实例。FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。这个类是 FilterFactory 的一个扩展版本，提供了更多的功能。
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static void main(String[] args) throws IOException {
//        testGeoJson();
        MapContentTest();
    }

    public static void testGeoJson() throws IOException {
        String geoJson = "{ \"type\": \"MultiPolygon\", \"coordinates\": [ [ [ [ 120.390677571223392, 31.575328528060808 ], [ 120.388054745744512, 31.575104134954064 ], [ 120.387760680066236, 31.576412336311236 ], [ 120.390370847242096, 31.576969983208802 ], [ 120.390677571223392, 31.575328528060808 ] ] ] ] }";

        GeometryJSON gJson = new GeometryJSON();
        Geometry geometry = gJson.read(new StringReader(geoJson));

        System.out.println(geometry);
        System.out.println(geometry.getGeometryType());
    }


    public static void MapContentTest() {
        // 创建 SimpleFeatureType
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("MyFeatureType");
        builder.setCRS(DefaultGeographicCRS.WGS84); // 设置坐标参考系统
        builder.add("location", MultiPolygon.class);
        SimpleFeatureType featureType = builder.buildFeatureType();

        // 创建 SimpleFeature
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        String geoJson = "{ \"type\": \"MultiPolygon\", \"coordinates\": [ [ [ [ 120.390677571223392, 31.575328528060808 ], [ 120.388054745744512, 31.575104134954064 ], [ 120.387760680066236, 31.576412336311236 ], [ 120.390370847242096, 31.576969983208802 ], [ 120.390677571223392, 31.575328528060808 ] ] ] ] }";
        GeometryJSON gJson = new GeometryJSON();
        Geometry geometry = null;
        try {
            geometry = gJson.read(new StringReader(geoJson));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        featureBuilder.add(geometry);
        SimpleFeature feature = featureBuilder.buildFeature(null);

        // 将 SimpleFeature 添加到 SimpleFeatureCollection
        List<SimpleFeature> featureList = new ArrayList<>();
        featureList.add(feature);
        SimpleFeatureCollection collection = new ListFeatureCollection(featureType, featureList);

        // 创建 Layer，并添加到 MapContent
        Style style = createCustomStyle(sf, ff);
        Layer layer = new FeatureLayer(collection, style);
        MapContent mapContent = new MapContent();
        mapContent.addLayer(layer);

        JMapFrame frame;
        // C创建一个JMapFrame框架
        frame = new JMapFrame(mapContent);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.enableLayerTable(true);

        // 显示地图窗口，当窗口关闭时，应用程序将退出
        frame.setVisible(true);
    }

    public static Geometry geoJsonToGeometry(String geoJson) throws IOException {
        GeometryJSON gJson = new GeometryJSON();
        Geometry geometry = gJson.read(new StringReader(geoJson));
        System.out.println(geometry.getGeometryType());
        if (geometry instanceof Point) {
            return (Point) geometry;
        } else if (geometry instanceof LineString) {
            return (LineString) geometry;
        } else if (geometry instanceof Polygon) {
            return (Polygon) geometry;
        } else if (geometry instanceof MultiPoint) {
            return (MultiPoint) geometry;
        } else if (geometry instanceof MultiLineString) {
            return (MultiLineString) geometry;
        } else if (geometry instanceof MultiPolygon) {
            return (MultiPolygon) geometry;
        } else if (geometry instanceof GeometryCollection) {
            return (GeometryCollection) geometry;
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }
    }

    /**
     * 创建矢量图层样式
     * @param styleFactory 样式工厂
     * @param filterFactory 过滤器工厂
     * @return  矢量图层样式
     */
    public static Style createCustomStyle(StyleFactory styleFactory, FilterFactory2 filterFactory) {
        // Create a black line for the stroke
        Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.RED), filterFactory.literal(1));

        // Create a line symbolizer using the stroke
        LineSymbolizer lineSymbolizer = styleFactory.createLineSymbolizer(stroke, null);

        // Create a rule with the line symbolizer
        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(lineSymbolizer);

        // Create a style with the rule
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(styleFactory.createFeatureTypeStyle(rule));

        return style;
    }
}
