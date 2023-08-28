package com.sun.gis.tools.wmts;

/**
 * @author sunbt
 * @date 2023/8/27 20:46
 */


import com.sun.gis.tools.sld.SldRenderer;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.map.WMTSMapLayer;

import org.geotools.ows.wmts.model.WMTSCapabilities;
import org.geotools.ows.wmts.model.WMTSLayer;
import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.net.URL;
import java.util.List;

public class GeoServerWMTSLoader {

    public static void main(String[] args) {
//        test_4326();
        test_3857();
    }

    public static void test_4326() {
        String path = "E:\\code\\github\\gis-server\\data\\sld\\model_1.1.0_320205.sld";
        Style style = SldRenderer.readSldReturnOne(path);
        // 加载 Shapefile
        File file = new File("E:\\code\\github\\gis-server\\data\\shapefile\\320205_county.shp");
        URL shapefileURL = null;

        try {
            URL wmtsURL = new URL("http://localhost:8080/geoserver/gwc/service/wmts?REQUEST=GetCapabilities");
            WebMapTileServer wmtsServer = new WebMapTileServer(wmtsURL);

            // 获取 WMTS 图层信息列表
            WMTSCapabilities capabilities = wmtsServer.getCapabilities();
            List<WMTSLayer> layerList = capabilities.getLayerList();

            // 如果你知道你想加载的具体图层名
            WMTSLayer desiredLayer = null;
            for (WMTSLayer layer : layerList) {
                if ("320205_202005".equals(layer.getTitle())) {
                    desiredLayer = layer;
                    break;
                }
            }

            if (desiredLayer != null) {
                shapefileURL = file.toURI().toURL();
                ShapefileDataStore dataStore = new ShapefileDataStore(shapefileURL);
                SimpleFeatureSource featureSource = dataStore.getFeatureSource();
                ReferencedEnvelope bound = featureSource.getBounds();
                CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
                // 应用样式并添加到 map content
                MapContent mapContent = new MapContent();
                mapContent.setTitle("Your Map Title");
                WMTSMapLayer wmtsMapLayer = new WMTSMapLayer(wmtsServer, desiredLayer, crs);
                mapContent.addLayer(wmtsMapLayer);
                FeatureLayer layer = new FeatureLayer(featureSource, style);
                mapContent.addLayer(layer);
                mapContent.getViewport().setCoordinateReferenceSystem(crs);
                mapContent.getViewport().setBounds(bound);

                // 例如：显示在 GUI 中或进行其他处理
                JMapFrame.showMap(mapContent);
            } else {
                System.out.println("Desired layer not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test_3857() {
        String path = "E:\\code\\github\\gis-server\\data\\sld\\model_1.1.0_320205.sld";
        Style style = SldRenderer.readSldReturnOne(path);
        // 加载 Shapefile
        File file = new File("E:\\code\\github\\gis-server\\data\\shapefile\\320205_county.shp");
        URL shapefileURL = null;

        try {
            URL wmtsURL = new URL("http://localhost:8080/geoserver/gwc/service/wmts?REQUEST=GetCapabilities");
            WebMapTileServer wmtsServer = new WebMapTileServer(wmtsURL);

            // 获取 WMTS 图层信息列表
            WMTSCapabilities capabilities = wmtsServer.getCapabilities();
            List<WMTSLayer> layerList = capabilities.getLayerList();

            // 如果你知道你想加载的具体图层名
            WMTSLayer desiredLayer = null;
            for (WMTSLayer layer : layerList) {
                if ("320205_202005".equals(layer.getTitle())) {
                    desiredLayer = layer;
                    break;
                }
            }

            if (desiredLayer != null) {
                shapefileURL = file.toURI().toURL();
                ShapefileDataStore dataStore = new ShapefileDataStore(shapefileURL);
                SimpleFeatureSource featureSource = dataStore.getFeatureSource();
                ReferencedEnvelope bound = featureSource.getBounds();
                CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
                // 应用样式并添加到 map content
                MapContent mapContent = new MapContent();
                mapContent.setTitle("Your Map Title");
                WMTSMapLayer wmtsMapLayer = new WMTSMapLayer(wmtsServer, desiredLayer, crs);
                mapContent.addLayer(wmtsMapLayer);
                FeatureLayer layer = new FeatureLayer(featureSource, style);
                mapContent.addLayer(layer);
                mapContent.getViewport().setCoordinateReferenceSystem(crs);
//                mapContent.getViewport().setBounds(bound);

                // 例如：显示在 GUI 中或进行其他处理
                JMapFrame.showMap(mapContent);
            } else {
                System.out.println("Desired layer not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void test1() {
        String path = "E:\\code\\github\\gis-server\\data\\sld\\model_1.1.0_320205.sld";
        Style style = SldRenderer.readSldReturnOne(path);
        // 加载 Shapefile
        File file = new File("E:\\code\\github\\gis-server\\data\\shapefile\\320205_county.shp");
        URL shapefileURL = null;

        try {
            URL wmtsURL = new URL("http://218.2.231.242:18081/geowebcache/service/wmts?REQUEST=GetCapabilities");
            WebMapTileServer wmtsServer = new WebMapTileServer(wmtsURL);

            // 获取 WMTS 图层信息列表
            WMTSCapabilities capabilities = wmtsServer.getCapabilities();
            List<WMTSLayer> layerList = capabilities.getLayerList();

            // 如果你知道你想加载的具体图层名
            WMTSLayer desiredLayer = null;
            for (WMTSLayer layer : layerList) {
                if ("Image2022".equals(layer.getTitle())) {
                    desiredLayer = layer;
                    break;
                }
            }

            if (desiredLayer != null) {
                shapefileURL = file.toURI().toURL();
                ShapefileDataStore dataStore = new ShapefileDataStore(shapefileURL);
                SimpleFeatureSource featureSource = dataStore.getFeatureSource();
                ReferencedEnvelope bound = featureSource.getBounds();
                CoordinateReferenceSystem crs = CRS.decode("EPSG:4490");
                // 应用样式并添加到 map content
                MapContent mapContent = new MapContent();
                mapContent.setTitle("Your Map Title");
                WMTSMapLayer wmtsMapLayer = new WMTSMapLayer(wmtsServer, desiredLayer, crs);
                mapContent.addLayer(wmtsMapLayer);
                FeatureLayer layer = new FeatureLayer(featureSource, style);
                mapContent.addLayer(layer);
                mapContent.getViewport().setCoordinateReferenceSystem(crs);

//                mapContent.getViewport().setBounds(new ReferencedEnvelope(116.31104850800011, 122.00151254800005, 30.748834610000074, 35.16798210100008, crs));


                mapContent.getViewport().setBounds(bound);

                // 例如：显示在 GUI 中或进行其他处理
                JMapFrame.showMap(mapContent);
            } else {
                System.out.println("Desired layer not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}



