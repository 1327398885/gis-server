package com.sun.gis.tools.mapContent;

import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.opengis.filter.FilterFactory2;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 渲染shape文件，并显示在地图上
 */
public class ShapeFileLab {

    //这行代码创建了一个 StyleFactory 实例，它是 GeoTools 中的一个工厂类，用于创建样式对象。样式对象用于定义地图中地理要素（如点、线、面）的显示方式，包括颜色、线型、填充等。
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //这行代码创建了一个 FilterFactory2 实例。FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。这个类是 FilterFactory 的一个扩展版本，提供了更多的功能。
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    //这行代码声明了一个 JMapFrame 类型的私有变量 frame。JMapFrame 是 GeoTools 提供的一个 Swing 组件，用于在窗口中显示地图。通过这个变量，您可以创建一个地图窗口，添加图层、工具栏等组件。
    private JMapFrame frame;
    //这行代码声明了一个 GridCoverage2DReader 类型的私有变量 reader。GridCoverage2DReader 是一个接口，用于读取栅格数据（如 GeoTIFF、NetCDF 等格式）。通过实现这个接口的类（如 GeoTiffReader、NetCDFReader 等），您可以读取和处理栅格数据。
    private GridCoverage2DReader reader;

    public static void main(String[] args) {
        ShapeFileLab app = new ShapeFileLab();
        app.displayShapefile();
    }

    private void displayShapefile() {
        // 创建参数列表，矢量文件（Shapefile）
        List<Parameter<?>> list = new ArrayList<>();
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
        // 从向导中获取用户输入的文件，并调用displayLayers方法显示图层
        displayLayers(shapeFile);
    }

    private void displayLayers(File shapeFile) {
        // 接Shapefile并设置渲染风格
        FileDataStore dataStore = null;
        SimpleFeatureSource shapefileSource = null;
        try {
            dataStore = FileDataStoreFinder.getDataStore(shapeFile);
            shapefileSource = dataStore.getFeatureSource();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 创建一个带有黄线且无填充的基本样式
//        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);
        Style shpStyle = createCustomStyle(sf, ff);
        // 创建JMapFrame并设置菜单、工具栏等
        final MapContent map = new MapContent();
        map.setTitle("图层叠加");

        Layer shpLayer = new FeatureLayer(shapefileSource, shpStyle);
        map.addLayer(shpLayer);

        // C创建一个JMapFrame框架
        frame = new JMapFrame(map);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
//        frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
        frame.enableToolBar(true);
        frame.enableLayerTable(true);

        // 显示地图窗口，当窗口关闭时，应用程序将退出
        frame.setVisible(true);
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
