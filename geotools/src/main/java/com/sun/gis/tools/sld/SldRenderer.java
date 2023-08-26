package com.sun.gis.tools.sld;


import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.geotools.xml.styling.SLDParser;


import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


/**
 * @author sunbt
 * @date 2023/8/26 19:14
 */
public class SldRenderer {

    /**
     * 读取sld文件，并返回第一个样式
     *
     * @param path sld文件地址
     * @return Style
     */
    public static Style readSld(String path) {
        // 解析 SLD
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        File sldFile = new File(path);
        InputStream input = null;
        try {
            input = new FileInputStream(sldFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SLDParser parser = new SLDParser(styleFactory, input);
        Style[] styles = parser.readXML();
        Style style = null;

        if (styles.length > 0) {
            style = styles[0];
        }
        return style;
    }

    /**
     * 读取sld文件，并返回第一个样式
     * 如果sld是1.1.0版本的转1.0.0版本
     *
     * @param path sld文件地址
     * @return Style
     */
    public static Style readSldReturnOne(String path) {
        // 解析 SLD
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);

        File sldFile = new File(path);
        try {
            String sldContent = new String(Files.readAllBytes(sldFile.toPath()), StandardCharsets.UTF_8);

            // 转换 SLD 1.1.0 到 1.0.0
            String convertedSldContent = transformSLD110to100(sldContent);

            InputStream input = new ByteArrayInputStream(convertedSldContent.getBytes(StandardCharsets.UTF_8));
            SLDParser parser = new SLDParser(styleFactory, input);
            Style[] styles = parser.readXML();
            Style style = null;

            if (styles.length > 0) {
                style = styles[0];
            }
            return style;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取sld文件，并返回所有样式
     *
     * @param path sld文件地址
     * @return Style[]
     */
    public static Style[] readSldAll(String path) {
        // 解析 SLD
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        File sldFile = new File(path);
        InputStream input = null;
        try {
            input = new FileInputStream(sldFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SLDParser parser = new SLDParser(styleFactory, input);
        Style[] styles = parser.readXML();

        if (styles.length > 0) {
            return styles;
        }
        return null;
    }

    /**
     * sld1.1.0转sld1.0.0
     * <p>
     * 1. 替换所有的 "se:" 前缀。
     * 2. 将 <SvgParameter> 元素替换为 <CssParameter>。
     *
     * @param sld110Content sld内容
     * @return 1.1.0转换后的1.0.0 文本
     */
    public static String transformSLD110to100(String sld110Content) {
        String sld100Content = sld110Content;

        // 替换 "se:" 前缀
        sld100Content = sld100Content.replaceAll("se:", "");

        // 替换 SvgParameter 为 CssParameter
        sld100Content = sld100Content.replace("<SvgParameter", "<CssParameter");
        sld100Content = sld100Content.replace("</SvgParameter", "</CssParameter");

        return sld100Content;
    }


    public static void main(String[] args) {
        test();
    }


    public static void test() {
        String path = "E:\\code\\github\\gis-server\\data\\sld\\model_1.1.0_320205.sld";
        Style style = SldRenderer.readSldReturnOne(path);
        // 加载 Shapefile
        File file = new File("E:\\code\\github\\gis-server\\data\\shapefile\\320205_county.shp");
        URL shapefileURL = null;
        try {
            shapefileURL = file.toURI().toURL();
            ShapefileDataStore dataStore = new ShapefileDataStore(shapefileURL);
            SimpleFeatureSource featureSource = dataStore.getFeatureSource();

            // 应用样式并添加到 map content
            MapContent mapContent = new MapContent();
            mapContent.setTitle("Your Map Title");
            FeatureLayer layer = new FeatureLayer(featureSource, style);
            mapContent.addLayer(layer);

            // 显示地图内容
            JMapFrame.showMap(mapContent);

            // 注意：最后，确保你关闭了 dataStore
            dataStore.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
