package com.sun.gis.tools.WMS;

/**
 * @author sunbt
 * @date 2023/8/27 22:13
 */

import com.sun.gis.tools.sld.SldRenderer;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.ows.ServiceException;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.map.WMSLayer;
import org.geotools.ows.wms.request.GetMapRequest;
import org.geotools.ows.wms.response.GetMapResponse;

import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class WMSLoader {

    // TODO: 2023/8/28 未完成

    public static void main(String[] args) {
        test1();
    }

    public static void test() {
        try {
            URL wmsUrl = new URL("http://218.2.231.242:18081/geowebcache/service/wms");
            WebMapServer wms = new WebMapServer(wmsUrl);
            WMSCapabilities capabilities = wms.getCapabilities();
            List<Layer> layerList = capabilities.getLayerList();
            Layer desiredLayer = null;
            for (Layer layer : layerList) {
                System.out.println(layer.getName());
                if ("Image2022".equals(layer.getTitle())) {
                    desiredLayer = layer;
                    break;
                }
            }

            // Assuming Image2022 is the layer you want
            GetMapRequest request = wms.createGetMapRequest();
            request.setFormat("image/jpeg");
            request.setDimensions(256, 256); // width and height
            request.setSRS("EPSG:4490");
            request.setBBox("116.71875553486,30.937498898273,118.1250055611,32.343748924505");
            request.addLayer(desiredLayer); // Assuming Image2022 is the first layer

            GetMapResponse response = wms.issueRequest(request);
            BufferedImage image = ImageIO.read(response.getInputStream());
            // Now, you have the image and can process/display it

            // Save the image to local file
            File outputFile = new File("E:\\code\\github\\gis-server\\data\\output\\wms\\Image2022.jpg"); // Replace 'path/to/save/location' with your desired path
            ImageIO.write(image, "jpeg", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());


        } catch (IOException | ServiceException e) {
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
            URL wmsUrl = new URL("http://218.2.231.242:18081/geowebcache/service/wms");
            WebMapServer wms = new WebMapServer(wmsUrl);

            // 获取 WMTS 图层信息列表
            WMSCapabilities capabilities = wms.getCapabilities();
            List<Layer> layerList = capabilities.getLayerList();

            // 如果你知道你想加载的具体图层名
            Layer desiredLayer = null;
            for (Layer layer : layerList) {
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
                WMSLayer wmsLayer = new WMSLayer(wms, desiredLayer);

                mapContent.addLayer(wmsLayer);
//                FeatureLayer layer = new FeatureLayer(featureSource, style);
//                mapContent.addLayer(layer);

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

}