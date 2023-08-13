package com.sun.gis.shapefile;

import com.alibaba.fastjson.JSONObject;
import com.sun.gis.tools.shapefile.GeoJsonToShapeFile;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author sunbt
 * @date 2023/8/14 0:07
 */
public class GeoJsonToShapeFileTest {

    public static void main(String[] args) {
//        geoJsonToPolygonTest();
//        geoJsonToMultiPolygonTest();
//        geoJsonToLineStringTest();
        geoJsonToPointTest();
    }

    /**
     * GeoJson转Shapefile：多边形
     */
    private static void geoJsonToPolygonTest() {
        long start = System.currentTimeMillis();
        try {
            // 读文件到StringBuilder
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            String jsonPath = "E:\\code\\github\\gis-server\\data\\geoJson\\geoJson_Polygon.json";
            try {
                br = new BufferedReader(new FileReader(jsonPath));
                String str;
                while ((str = br.readLine()) != null) {
                    // 逐行读取
                    sb.append(str).append("\r\n");
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            JSONObject json = JSONObject.parseObject(sb.toString());
            String shpPath = "E:\\code\\github\\gis-server\\data\\output\\geoJson_Polygon.shp";
            GeoJsonToShapeFile.geoJsonToShapeFile(json, shpPath);
            System.out.println("共耗时" + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GeoJson转Shapefile：多重多边形
     */
    private static void geoJsonToMultiPolygonTest() {
        long start = System.currentTimeMillis();
        try {
            // 读文件到StringBuilder
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            String jsonPath = "E:\\code\\github\\gis-server\\data\\geoJson\\geoJson_MultiPolygon.json";
            try {
                br = new BufferedReader(new FileReader(jsonPath));
                String str;
                while ((str = br.readLine()) != null) {
                    // 逐行读取
                    sb.append(str).append("\r\n");
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            JSONObject json = JSONObject.parseObject(sb.toString());
            String shpPath = "E:\\code\\github\\gis-server\\data\\output\\geoJson_MultiPolygon.shp";
            GeoJsonToShapeFile.geoJsonToShapeFile(json, shpPath);
            System.out.println("共耗时" + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GeoJson转Shapefile：线
     */
    private static void geoJsonToLineStringTest() {
        long start = System.currentTimeMillis();
        try {
            // 读文件到StringBuilder
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            String jsonPath = "E:\\code\\github\\gis-server\\data\\geoJson\\geoJson_LineString.json";
            try {
                br = new BufferedReader(new FileReader(jsonPath));
                String str;
                while ((str = br.readLine()) != null) {
                    // 逐行读取
                    sb.append(str).append("\r\n");
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            JSONObject json = JSONObject.parseObject(sb.toString());
            String shpPath = "E:\\code\\github\\gis-server\\data\\output\\geoJson_LineString.shp";
            GeoJsonToShapeFile.geoJsonToShapeFile(json, shpPath);
            System.out.println("共耗时" + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GeoJson转Shapefile：线
     */
    private static void geoJsonToPointTest() {
        long start = System.currentTimeMillis();
        try {
            // 读文件到StringBuilder
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            String jsonPath = "E:\\code\\github\\gis-server\\data\\geoJson\\geoJson_Point.json";
            try {
                br = new BufferedReader(new FileReader(jsonPath));
                String str;
                while ((str = br.readLine()) != null) {
                    // 逐行读取
                    sb.append(str).append("\r\n");
                }
                br.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            JSONObject json = JSONObject.parseObject(sb.toString());
            String shpPath = "E:\\code\\github\\gis-server\\data\\output\\geoJson_Point.shp";
            GeoJsonToShapeFile.geoJsonToShapeFile(json, shpPath);
            System.out.println("共耗时" + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
