package com.sun.gis.tools.shapefile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author sunbt
 * @date 2023/8/13 22:29
 * <p>
 * GeoJson转换Shapefile工具方法
 */
public class GeoJsonToShapeFile {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    /**
     * geoJson对象转shapefile文件
     *
     * @param jsonObject    geoJson对象
     * @param shapefilePath 矢量文件地址
     */
    public static void geoJsonToShapeFile(JSONObject jsonObject, String shapefilePath) {
        // 获取地理对象数组
        JSONArray features = jsonObject.getJSONArray("features");
        // 拿到第一个json对象
        JSONObject feature0 = features.getJSONObject(0);
        //获取json对象属性的键值用生成属性表表头
        Set properties = feature0.getJSONObject("properties").keySet();
        // 获取geoJson对象的类型（Point、LineString、Polygon等）
        String strType = feature0.getJSONObject("geometry").getString("type");
        // 指定shapefile文件位置
        File file = new File(shapefilePath);
        Map<String, Serializable> params = new HashMap<>();
        try {
            Class<?> geoType = null;
            switch (strType) {
                case "Point":
                    geoType = Point.class;
                    break;
                case "MultiPoint":
                    geoType = MultiPoint.class;
                    break;
                case "LineString":
                    geoType = LineString.class;
                    break;
                case "MultiLineString":
                    geoType = MultiLineString.class;
                    break;
                case "Polygon":
                    geoType = Polygon.class;
                    break;
                case "MultiPolygon":
                    geoType = MultiPolygon.class;
                    break;
                default:
                    break;
            }
            //创建ShapefileDataStore用于存储边界数据和属性数据
            params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
            ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
            //设置dataStore的编码为GBK
            ds.setCharset(Charset.forName("GBK"));
            // 创建属性构造器，用于构造属性表字段
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            // 设置数据的坐标系，我这使用WGS84经纬度坐标系
            tb.setCRS(DefaultGeographicCRS.WGS84);
            //设置表名
            tb.setName("shapefile");
            //设置第一个字段为地理属性字段（the_geom）,这里需要第一个字段设置为地理坐标并且字段名就为（the_geom）
            tb.add("the_geom", geoType);
            tb.add("id", Integer.class);//编号
            // 添加属性
            for (Object property : properties) {
                String str = property.toString();
                // 这里注意一下，设置属性的标题不能超过10个字符（比如：SHAPE_Area可以）
                tb.add(str, String.class);
            }
            //创建属性表
            ds.createSchema(tb.buildFeatureType());
            //开启对象写入服务
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
            SimpleFeature feature = null;
            //遍历写入属性值
            for (int i = 0; i < features.size(); i++) {
                feature = writer.next();
                JSONObject object = features.getJSONObject(i);
                JSONObject geometryJson = object.getJSONObject("geometry");
                JSONObject propertiesJson = object.getJSONObject("properties");
                // 根据类型创建地理对象，下面详细介绍
                Geometry geometry = createGeometry(strType, geometryJson);
                feature.setAttribute("the_geom", geometry);
                feature.setAttribute("id", i + 1);
                for (Object property : properties) {
                    String str = property.toString();
                    String s = propertiesJson.getString(str);
                    feature.setAttribute(str, s);
                }
            }
            writer.write();
            //关闭输出流
            writer.close();
            ds.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据类型将GeoJson转换成Geometry实体
     *
     * @param type         数据类型
     * @param geometryJson geoJson对象
     * @return Geometry
     */
    public static Geometry createGeometry(String type, JSONObject geometryJson) {

        CoordinateReferenceSystem sourceCRS = null;
        CoordinateReferenceSystem targetCRS = null;

        try {
            sourceCRS = CRS.decode("EPSG:4326");
            targetCRS = CRS.decode("EPSG:4326");
        } catch (FactoryException e) {
            e.printStackTrace();
        }


        switch (type) {
            // 多边形
            case "Polygon":
                return createPolygon(geometryJson, sourceCRS, targetCRS);
            // 多重多边形
            case "MultiPolygon":
                return createMultiPolygon(geometryJson, sourceCRS, targetCRS);
            // 线
            case "LineString":
                return createLineString(geometryJson, sourceCRS, targetCRS);
            // 多线
            case "MultiLineString":
                return createMultiLineString(geometryJson, sourceCRS, targetCRS);
            // 点
            case "Point":
                return createPoint(geometryJson, sourceCRS, targetCRS);
            // 多点
            case "MultiPoint":
                return createMultiPoint(geometryJson, sourceCRS, targetCRS);
            default:
                break;
        }
        return null;
    }

    /**
     * 根据json对象创建点（Point）实例
     *
     * @param geometryJson geoJson对象
     * @return Point
     */
    public static Point createPoint(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray coordinatesJson = geometryJson.getJSONArray("coordinates");
        double lon = coordinatesJson.getDoubleValue(0);
        double lat = coordinatesJson.getDoubleValue(1);
        Coordinate coordinate = new Coordinate(lon, lat);

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            JTS.transform(coordinate, coordinate, mathTransform);
        } catch (FactoryException | TransformException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            return null; // or throw a custom exception
        }

        return geometryFactory.createPoint(coordinate);
    }

    /**
     * 根据json对象创建点（MultiPoint）实例
     *
     * @param geometryJson geoJson对象
     * @return MultiPoint
     */
    public static MultiPoint createMultiPoint(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray coordinatesJsonArray = geometryJson.getJSONArray("coordinates");
        int length = coordinatesJsonArray.size();
        Coordinate[] coordinates = new Coordinate[length];

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            for (int i = 0; i < length; i++) {
                JSONArray coordinateJson = coordinatesJsonArray.getJSONArray(i);
                double lon = coordinateJson.getDoubleValue(0);
                double lat = coordinateJson.getDoubleValue(1);
                Coordinate coordinate = new Coordinate(lon, lat);
                JTS.transform(coordinate, coordinate, mathTransform);
                coordinates[i] = coordinate;
            }
        } catch (FactoryException | TransformException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            // or throw a custom exception
            return null;
        }

        return geometryFactory.createMultiPoint(coordinates);
    }


    /**
     * 根据json对象创建线（LineString）实例
     *
     * @param geometryJson geoJson对象
     * @return LineString
     */
    public static LineString createLineString(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray coordinatesJson = geometryJson.getJSONArray("coordinates");
        int length = coordinatesJson.size();
        Coordinate[] coordinates = new Coordinate[length];

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            for (int i = 0; i < length; i++) {
                JSONArray coord = coordinatesJson.getJSONArray(i);
                double lon = coord.getDoubleValue(0);
                double lat = coord.getDoubleValue(1);
                Coordinate coordinate = new Coordinate(lon, lat);
                JTS.transform(coordinate, coordinate, mathTransform);
                coordinates[i] = coordinate;
            }
        } catch (TransformException | FactoryException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            // or throw a custom exception
            return null;
        }

        return geometryFactory.createLineString(coordinates);
    }

    /**
     * 根据json对象创建线（MultiLineString）实例
     *
     * @param geometryJson geoJson对象
     * @return MultiLineString
     */
    public static MultiLineString createMultiLineString(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray lineStringsJsonArray = geometryJson.getJSONArray("coordinates");
        int length = lineStringsJsonArray.size();
        LineString[] lineStrings = new LineString[length];

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            for (int i = 0; i < length; i++) {
                JSONArray coordinatesJsonArray = lineStringsJsonArray.getJSONArray(i);
                int coordinateLength = coordinatesJsonArray.size();
                Coordinate[] coordinates = new Coordinate[coordinateLength];

                for (int j = 0; j < coordinateLength; j++) {
                    JSONArray coordinateJson = coordinatesJsonArray.getJSONArray(j);
                    double lon = coordinateJson.getDoubleValue(0);
                    double lat = coordinateJson.getDoubleValue(1);
                    Coordinate coordinate = new Coordinate(lon, lat);
                    JTS.transform(coordinate, coordinate, mathTransform);
                    coordinates[j] = coordinate;
                }

                lineStrings[i] = geometryFactory.createLineString(coordinates);
            }
        } catch (FactoryException | TransformException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            return null; // or throw a custom exception
        }

        return geometryFactory.createMultiLineString(lineStrings);
    }

    /**
     * 根据json对象创建面（Polygon）实例
     * 没有内部孔
     *
     * @param geometryJson geoJson对象
     * @return Polygon
     */
    public static Polygon createPolygon(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray coordinatesJson = geometryJson.getJSONArray("coordinates").getJSONArray(0);
        int length = coordinatesJson.size();
        Coordinate[] coordinates = new Coordinate[length];

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            for (int i = 0; i < length; i++) {
                JSONArray coord = coordinatesJson.getJSONArray(i);
                double lon = coord.getDoubleValue(0);
                double lat = coord.getDoubleValue(1);
                Coordinate coordinate = new Coordinate(lon, lat);
                JTS.transform(coordinate, coordinate, mathTransform);
                coordinates[i] = coordinate;
            }
        } catch (TransformException | FactoryException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            // or throw a custom exception
            return null;
        }
        // 线性环
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        // 面的内部孔; null表示没有孔
        return geometryFactory.createPolygon(ring, null);
    }


    /**
     * 根据json对象创建面（MultiPolygon）实例
     *
     * @param geometryJson geoJson对象
     * @return MultiPolygon
     */
    public static MultiPolygon createMultiPolygon(JSONObject geometryJson, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (geometryJson == null || !geometryJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid geoJson object");
        }

        JSONArray polygonsJsonArray = geometryJson.getJSONArray("coordinates");
        int length = polygonsJsonArray.size();
        Polygon[] polygons = new Polygon[length];

        try {
            MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            for (int i = 0; i < length; i++) {
                JSONArray polygonJsonArray = polygonsJsonArray.getJSONArray(i);
                JSONArray outerRingJsonArray = polygonJsonArray.getJSONArray(0);
                Coordinate[] outerCoordinates = convertCoordinates(outerRingJsonArray, mathTransform);
                LinearRing outerRing = geometryFactory.createLinearRing(outerCoordinates);

                int innerLength = polygonJsonArray.size() - 1;
                LinearRing[] innerRings = new LinearRing[innerLength];
                for (int j = 1; j <= innerLength; j++) {
                    JSONArray innerRingJsonArray = polygonJsonArray.getJSONArray(j);
                    Coordinate[] innerCoordinates = convertCoordinates(innerRingJsonArray, mathTransform);
                    innerRings[j - 1] = geometryFactory.createLinearRing(innerCoordinates);
                }

                polygons[i] = geometryFactory.createPolygon(outerRing, innerRings);
            }
        } catch (FactoryException | TransformException e) {
            // Handle exceptions as needed for your application
            e.printStackTrace();
            return null; // or throw a custom exception
        }

        return geometryFactory.createMultiPolygon(polygons);
    }

    /**
     * 将JSON数组转换为JTS的Coordinate对象数组
     */
    private static Coordinate[] convertCoordinates(JSONArray coordinatesJsonArray, MathTransform mathTransform)
            throws TransformException {
        int coordinateLength = coordinatesJsonArray.size();
        Coordinate[] coordinates = new Coordinate[coordinateLength];
        for (int i = 0; i < coordinateLength; i++) {
            JSONArray coordinateJson = coordinatesJsonArray.getJSONArray(i);
            double lon = coordinateJson.getDoubleValue(0);
            double lat = coordinateJson.getDoubleValue(1);
            Coordinate coordinate = new Coordinate(lon, lat);
            JTS.transform(coordinate, coordinate, mathTransform);
            coordinates[i] = coordinate;
        }
        return coordinates;
    }


}
