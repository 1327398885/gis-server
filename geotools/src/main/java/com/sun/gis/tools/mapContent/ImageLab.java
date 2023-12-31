package com.sun.gis.tools.mapContent;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.geotools.util.factory.Hints;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.ContrastMethod;

/**
 * 渲染栅格数据和矢量数据，并显示在地图窗口中
 */
public class ImageLab {

    //这行代码创建了一个 StyleFactory 实例，它是 GeoTools 中的一个工厂类，用于创建样式对象。样式对象用于定义地图中地理要素（如点、线、面）的显示方式，包括颜色、线型、填充等。
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //这行代码创建了一个 FilterFactory2 实例。FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。这个类是 FilterFactory 的一个扩展版本，提供了更多的功能。
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    //这行代码声明了一个 JMapFrame 类型的私有变量 frame。JMapFrame 是 GeoTools 提供的一个 Swing 组件，用于在窗口中显示地图。通过这个变量，您可以创建一个地图窗口，添加图层、工具栏等组件。
    private JMapFrame frame;
    //这行代码声明了一个 GridCoverage2DReader 类型的私有变量 reader。GridCoverage2DReader 是一个接口，用于读取栅格数据（如 GeoTIFF、NetCDF 等格式）。通过实现这个接口的类（如 GeoTiffReader、NetCDFReader 等），您可以读取和处理栅格数据。
    private GridCoverage2DReader reader;

    // 主函数，创建ImageLab实例并调用getLayersAndDisplay方法
    public static void main(String[] args) throws Exception {
        ImageLab me = new ImageLab();
        me.getLayersAndDisplay();
    }

    /**
     * 提示用户输入一个GeoTIFF文件和一个Shapefile，然后将它们传递给displayLayers方法
     */
    private void getLayersAndDisplay() throws Exception {
        // 创建参数列表，包括图像文件和矢量文件（Shapefile）
        List<Parameter<?>> list = new ArrayList<>();
        list.add(
                new Parameter<>(
                        "image",
                        File.class,
                        "GeoTiff底图",
                        "选择GeoTiff文件作为地图底图",
                        new KVP(Parameter.EXT, "tif")));
        list.add(
                new Parameter<>(
                        "shape",
                        File.class,
                        "Shapefile文件",
                        "选择需要叠加的Shapefile文件",
                        new KVP(Parameter.EXT, "shp")));
        // 创建一个向导，让用户输入这些参数
        JParameterListWizard wizard =
                new JParameterListWizard("图层叠加展示", "Fill in the following layers", list);
        int finish = wizard.showModalDialog();
        // 如果用户没有完成输入，则退出
        if (finish != JWizard.FINISH) {
            System.exit(0);
        }
        File imageFile = (File) wizard.getConnectionParameters().get("image");
        File shapeFile = (File) wizard.getConnectionParameters().get("shape");
        // 从向导中获取用户输入的文件，并调用displayLayers方法显示图层
        displayLayers(imageFile, shapeFile);
    }

    /**
     * 显示一个叠加了Shapefile的GeoTIFF文件
     *
     * @param rasterFile the GeoTIFF文件
     * @param shpFile    the Shapefile文件
     */
    private void displayLayers(File rasterFile, File shpFile) throws Exception {
        // 读取栅格文件并设置渲染风格
        // 这行代码使用 GridFormatFinder 类的 findFormat 方法根据输入的栅格数据文件（rasterFile）来确定其格式。findFormat 方法会返回一个 AbstractGridFormat 类型的对象，它表示数据文件的格式
        AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
        // 这行代码创建了一个 Hints 对象。Hints 类是 GeoTools 中用于存储一组提示或首选项的类，这些提示或首选项可以在数据读取过程中提供一些额外的控制。在这个示例中，hints 对象将用于传递一个特定的坐标轴顺序设置。
        Hints hints = new Hints();
        // 这行代码检查 format 对象是否是 GeoTiffFormat 类型。GeoTiffFormat 类表示 GeoTIFF 格式的栅格数据。
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }
        // 这行代码使用 format 对象的 getReader 方法来创建一个 GridCoverage2DReader 对象。getReader 方法接受两个参数：栅格数据文件（rasterFile）和前面创建的 hints 对象。GridCoverage2DReader 对象用于读取和处理栅格数据。
        reader = format.getReader(rasterFile, hints);
        // 设置栅格文件的渲染样式
        //Style rasterStyle = createGreyscaleStyle(1);
        Style rasterStyle = createRGBStyle();

        // 接Shapefile并设置渲染风格
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);
        SimpleFeatureSource shapefileSource = dataStore.getFeatureSource();

        // 创建一个带有黄线且无填充的基本样式
        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        // 创建JMapFrame并设置菜单、工具栏等
        final MapContent map = new MapContent();
        map.setTitle("图层叠加");

        // 创建一个栅格图层和一个矢量图层，并将它们添加到地图中
        Layer rasterLayer = new GridReaderLayer(reader, rasterStyle);
        map.addLayer(rasterLayer);

        Layer shpLayer = new FeatureLayer(shapefileSource, shpStyle);
        map.addLayer(shpLayer);

        // C创建一个JMapFrame框架
        frame = new JMapFrame(map);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
//        frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
        frame.enableToolBar(true);
        frame.enableLayerTable(true);

        // 设置Raster菜单，可以动态的选择底图渲染样式
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu menu = new JMenu("Raster");
        menuBar.add(menu);

        menu.add(
                new SafeAction("Grayscale display") {
                    public void action(ActionEvent e) throws Throwable {
                        Style style = createGreyscaleStyle();
                        if (style != null) {
                            ((StyleLayer) map.layers().get(0)).setStyle(style);
                            frame.repaint();
                        }
                    }
                });

        menu.add(
                new SafeAction("RGB display") {
                    public void action(ActionEvent e) throws Throwable {
                        Style style = createRGBStyle();
                        if (style != null) {
                            ((StyleLayer) map.layers().get(0)).setStyle(style);
                            frame.repaint();
                        }
                    }
                });
       // 显示地图窗口，当窗口关闭时，应用程序将退出
        frame.setVisible(true);
    }

    /**
     * 创建一个Style，将GeoTIFF图像的选定波段显示为灰度图层
     *
     * @return 一个新的Style实例，以渲染图像为灰度图
     */
    private Style createGreyscaleStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        int numBands = cov.getNumSampleDimensions();
        Integer[] bandNumbers = new Integer[numBands];
        for (int i = 0; i < numBands; i++) {
            bandNumbers[i] = i + 1;
        }
        Object selection =
                JOptionPane.showInputDialog(
                        frame,
                        "Band to use for greyscale display",
                        "Select an image band",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        bandNumbers,
                        1);
        if (selection != null) {
            int band = ((Number) selection).intValue();
            return createGreyscaleStyle(band);
        }
        return null;
    }

    /**
     * 创建一个Style，将GeoTIFF图像的指定波段显示为灰度图层。
     *
     <p>此方法是createGreyscaleStyle()的辅助方法，当应用程序首次启动时，它也会被displayLayers()方法直接调用。
     *
     *
     * @param band 用于灰度显示的图像波段
     * @return 一个新的Style实例，以渲染图像为灰度图
     */
    private Style createGreyscaleStyle(int band) {
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    /**
     * 此方法检查提供的覆盖层中样本维度的名称，寻找"red..."、"green..."和"blue..."（不区分大小写匹配）。
     * 如果找不到这些名称，它将使用波段1、2和3作为红、绿和蓝通道。然后设置一个光栅符号化器，并将其包装在一个Style中。
     *
     * @return 一个新的Style对象，包含为RGB图像设置的光栅符号化器
     */
    private Style createRGBStyle() {
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

}
