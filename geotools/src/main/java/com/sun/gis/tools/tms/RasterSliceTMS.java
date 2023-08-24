package com.sun.gis.tools.tms;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.*;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.*;

import org.geotools.util.factory.Hints;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * tiff栅格影像数据切片-TMS格式
 * tiff数据坐标系必须为3857，影像中黑色边界需要提前处理
 */
public class RasterSliceTMS {

    // 主函数，创建ImageLab实例并调用getLayersAndDisplay方法
    public static void main(String[] args) throws Exception {
        File rasterFile = new File("E:\\code\\github\\gis-server\\data\\tiff\\320205_3857.tif");
        RasterSliceTMS rasterSliceTMS2 = new RasterSliceTMS();
        String output = "E:\\code\\github\\gis-server\\data\\output\\RasterSliceTMS";
        for (int i = 1; i <= 10; i++) {
            // 从向导中获取用户输入的文件，并调用displayLayers方法显示图层
            rasterSliceTMS2.displayLayers(rasterFile, i, output);
        }

    }

    //这行代码创建了一个 StyleFactory 实例，它是 GeoTools 中的一个工厂类，用于创建样式对象。样式对象用于定义地图中地理要素（如点、线、面）的显示方式，包括颜色、线型、填充等。
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //这行代码创建了一个 FilterFactory2 实例。FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。这个类是 FilterFactory 的一个扩展版本，提供了更多的功能。
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    //这行代码声明了一个 GridCoverage2DReader 类型的私有变量 reader。GridCoverage2DReader 是一个接口，用于读取栅格数据（如 GeoTIFF、NetCDF 等格式）。通过实现这个接口的类（如 GeoTiffReader、NetCDFReader 等），您可以读取和处理栅格数据。
    private GridCoverage2DReader reader;


    /**
     * 叠加栅格数据，并渲染切片数据
     *
     * @param rasterFile 栅格数据
     * @param zoomLevel  切片层级
     * @param outputDir  输出目录
     * @throws Exception 文件异常
     */
    private void displayLayers(File rasterFile, int zoomLevel, String outputDir) throws Exception {
        // 读取栅格文件并设置渲染风格
        // 这行代码使用 GridFormatFinder 类的 findFormat 方法根据输入的栅格数据文件（rasterFile）来确定其格式。findFormat 方法会返回一个 AbstractGridFormat 类型的对象，它表示数据文件的格式
        AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
        GridCoverage2DReader gridCoverage2DReader = format.getReader(rasterFile);

        if (gridCoverage2DReader == null) {
            System.out.println("Reader not found for file: " + rasterFile);
            return;
        }

        GridCoverage2D coverage = gridCoverage2DReader.read(null);
        Envelope envelope = coverage.getEnvelope();
        ReferencedEnvelope refEnvelope = new ReferencedEnvelope(
                envelope.getMinimum(0),
                envelope.getMaximum(0),
                envelope.getMinimum(1),
                envelope.getMaximum(1),
                envelope.getCoordinateReferenceSystem()
        );
        // 这行代码创建了一个 Hints 对象。Hints 类是 GeoTools 中用于存储一组提示或首选项的类，这些提示或首选项可以在数据读取过程中提供一些额外的控制。在这个示例中，hints 对象将用于传递一个特定的坐标轴顺序设置。
        Hints hints = new Hints();
        // 这行代码检查 format 对象是否是 GeoTiffFormat 类型。GeoTiffFormat 类表示 GeoTIFF 格式的栅格数据。
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }
        // 这行代码使用 format 对象的 getReader 方法来创建一个 GridCoverage2DReader 对象。getReader 方法接受两个参数：栅格数据文件（rasterFile）和前面创建的 hints 对象。GridCoverage2DReader 对象用于读取和处理栅格数据。
        reader = format.getReader(rasterFile, hints);
        // 设置栅格文件的渲染样式
//        Style rasterStyle = createGreyscaleStyle(1);
        Style rasterStyle = createRGBStyle();


        // 创建JMapFrame并设置菜单、工具栏等
        final MapContent map = new MapContent();
        map.setTitle("图层叠加");

        // 创建一个栅格图层和一个矢量图层，并将它们添加到地图中
        Layer rasterLayer = new GridReaderLayer(reader, rasterStyle);
        map.addLayer(rasterLayer);


        //  Web Mercator 世界边界
        ReferencedEnvelope worldBounds = new ReferencedEnvelope(
                -20037508.34, 20037508.34, // 左右边界
                -20037508.34, 20037508.34, // 上下边界
                CRS.decode("EPSG:3857", true) // 坐标参考系统
        );

        // 设置切片大小和地理范围
        int tileSize = 256;

        // 计算切片数量
        int numTiles = (int) Math.pow(2, zoomLevel);

        ReferencedEnvelope projectedDataBounds = null;
        // 矢量目标坐标系
        CoordinateReferenceSystem targetCRS = null;
        targetCRS = CRS.decode("EPSG:3857", true);
        projectedDataBounds = worldBounds.transform(targetCRS, true);

        // 计算矢量数据源的地理范围在 Web Mercator 投影坐标系下的宽度和高度
        double dataWidth = projectedDataBounds.getWidth();
        double dataHeight = projectedDataBounds.getHeight();
        double dataMinX = projectedDataBounds.getMinX();
        double dataMinY = projectedDataBounds.getMinY();

        for (int y = 0; y < numTiles; y++) {
            for (int x = 0; x < numTiles; x++) {
                // 计算切片在 Web Mercator 投影坐标系下的地理范围
                double tileMinX = dataMinX + x * dataWidth / numTiles;
                double tileMaxX = dataMinX + (x + 1) * dataWidth / numTiles;
                double tileMinY = dataMinY + (numTiles - y - 1) * dataHeight / numTiles;
                double tileMaxY = dataMinY + (numTiles - y) * dataHeight / numTiles;
                ReferencedEnvelope tileBounds = new ReferencedEnvelope(tileMinX, tileMaxX, tileMinY, tileMaxY, targetCRS);

                // 如果切片与数据不存在交集，则跳过该切片
                if (!refEnvelope.intersects((org.locationtech.jts.geom.Envelope) tileBounds)) {
                    continue;
                }

                // 渲染地图

                StreamingRenderer renderer = new StreamingRenderer();
                renderer.setMapContent(map);

                BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();

                // Set the background color to transparent
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, tileSize, tileSize);
                graphics.setComposite(AlphaComposite.SrcOver);

                // Render the map
                Rectangle imageBounds = new Rectangle(0, 0, tileSize, tileSize);
                renderer.paint(graphics, imageBounds, tileBounds);
                // 检查并创建相应的 Z, X 文件夹
                File zDir = new File(outputDir + File.separator + zoomLevel);
                if (!zDir.exists()) {
                    zDir.mkdirs();
                }

                // 计算 Y 坐标，符合 TMS 标准
                int tmsY = (int) Math.pow(2, zoomLevel) - y - 1;
                File xDir = new File(zDir, String.valueOf(x));
                if (!xDir.exists()) {
                    xDir.mkdirs();
                }

                // 保存切片为 PNG 格式，使用递归的文件夹结构（Z/X/Y.png）
                File output = new File(xDir, tmsY + ".png");
                ImageIO.write(image, "PNG", output);
            }
        }


    }

    /**
     * 创建一个Style，将GeoTIFF图像的指定波段显示为灰度图层。
     *
     * <p>此方法是createGreyscaleStyle()的辅助方法，当应用程序首次启动时，它也会被displayLayers()方法直接调用。
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
            if (name.matches("red.*")) {
                channelNum[RED] = i + 1;
            } else if (name.matches("green.*")) {
                channelNum[GREEN] = i + 1;
            } else if (name.matches("blue.*")) {
                channelNum[BLUE] = i + 1;
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
