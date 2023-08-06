package com.sun.gis.tools.mapContent;

import org.geotools.data.Parameter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;

import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;

import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShpToPng {
    public static void main(String[] args) throws IOException {
        List<Parameter<?>> list = new ArrayList<>();
        // 选择shp文件
        list.add(
                new Parameter<>(
                        "shape",
                        File.class,
                        "Shapefile文件",
                        "选择需要叠加的Shapefile文件",
                        new KVP(Parameter.EXT, "shp")));

        // 创建一个向导，让用户输入这些参数
        JParameterListWizard wizard =
                new JParameterListWizard("矢量图层叠加展示", "Fill in the following layers", list);
        int finish = wizard.showModalDialog();
        // 如果用户没有完成输入，则退出
        if (finish != JWizard.FINISH) {
            System.exit(0);
        }
        File shapeFile = (File) wizard.getConnectionParameters().get("shape");
        // 读取shp文件
        ShapefileDataStore store = new ShapefileDataStore(shapeFile.toURI().toURL());
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();

        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        ReferencedEnvelope mapArea = featureSource.getBounds();

        // 创建地图内容
        MapContent map = new MapContent();
        map.setTitle("ShpToPng");
        FeatureLayer layer = new FeatureLayer(featureCollection, shpStyle);
        map.addLayer(layer);

        // 渲染地图
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);
        int imageWidth = 800;
        int imageHeight = 600;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // Set the background color to transparent
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, imageWidth, imageHeight);
        graphics.setComposite(AlphaComposite.SrcOver);

        // Render the map
        Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
        renderer.paint(graphics, imageBounds, mapArea);
        File pngFile = new File("/Users/sungang/Documents/data/tiff/320205/output/output.png");
        ImageIO.write(image, "png", pngFile);

        System.out.println("Output file: " + pngFile.getAbsolutePath());

        // Clean up
        graphics.dispose();
//        map.dispose();

        // 显示地图
        JMapFrame.showMap(map);



    }
}
