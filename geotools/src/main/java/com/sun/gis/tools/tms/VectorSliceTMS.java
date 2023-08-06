package com.sun.gis.tools.tms;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Stroke;
import org.geotools.styling.*;
import org.locationtech.jts.geom.Envelope;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * shapefile文件数据切片-TMS格式
 * shapefile文件数据坐标系必须为3857
 */
public class VectorSliceTMS {

    //StyleFactory是 GeoTools 中的一个工厂类，用于创建样式对象。样式对象用于定义地图中地理要素（如点、线、面）的显示方式，包括颜色、线型、填充等。
    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    /**
     * 读取矢量数据源（shapefile），根据给定的输出目录（outputDir）和缩放级别（zoomLevel）将矢量数据渲染成切片。
     *
     * @param shapefile 矢量数据源文件，通常是一个.shp文件
     * @param outputDir 输出目录，用于存储生成的切片文件
     * @param zoomLevel 地图缩放级别，决定切片的数量和精细程度
     * @throws IOException
     * @throws FactoryException
     */
    private static void processVectorData(File shapefile, String outputDir, int zoomLevel) throws IOException, FactoryException {
        // 读取矢量数据源
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapefile);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        // 数据边界
        ReferencedEnvelope dataBounds = featureSource.getBounds();
        //  Web Mercator 世界边界
        ReferencedEnvelope worldBounds = new ReferencedEnvelope(
                -20037508.34, 20037508.34, // 左右边界
                -20037508.34, 20037508.34, // 上下边界
                CRS.decode("EPSG:3857", true) // 坐标参考系统
        );

        // 如果矢量数据为空，则跳过后续的切片处理
        if (worldBounds.isEmpty()) {
            return;
        }

        // 加载矢量样式
        Style style = createCustomStyle(sf, ff);


        // 设置切片大小和地理范围
        int tileSize = 256;

        // 计算切片数量
        int numTiles = (int) Math.pow(2, zoomLevel);

        // 遍历切片并渲染
        renderTiles(featureSource, style, worldBounds, dataBounds, zoomLevel, tileSize, numTiles, outputDir);
    }

    /**
     * 这段代码的主要功能是遍历所有可能的切片并根据给定的参数进行渲染。具体实现包括计算切片地理范围、判断切片与矢量数据的交集、创建图层和MapContent、进行渲染、保存切片为PNG格式等步骤。
     *
     * @param featureSource 矢量数据源，包含地理空间特征的数据集
     * @param style         地图样式，用于定义地图要素的显示样式
     * @param worldBounds   Web Mercator世界边界，表示整个地图范围的地理边界
     * @param dataBounds    矢量数据源的地理边界，用于确定渲染切片的范围
     * @param zoomLevel     地图缩放级别，决定切片的数量和精细程度
     * @param tileSize      切片的大小（像素），通常为256x256
     * @param numTiles      切片的数量，根据缩放级别计算得出
     * @param outputDir     输出目录，用于存储生成的切片文件
     */
    private static void renderTiles(SimpleFeatureSource featureSource, Style style, ReferencedEnvelope worldBounds,
                                    ReferencedEnvelope dataBounds, int zoomLevel, int tileSize, int numTiles, String outputDir) {
        // 获取矢量原始坐标系
        CoordinateReferenceSystem sourceCRS = dataBounds.getCoordinateReferenceSystem();
        // 矢量目标坐标系
        CoordinateReferenceSystem targetCRS = null;

        // 将地理范围转换为 Web Mercator 投影坐标系下的地理范围
        ReferencedEnvelope projectedDataBounds = null;
        ReferencedEnvelope transformDataBounds = null;
        try {
            targetCRS = CRS.decode("EPSG:3857", true);
            projectedDataBounds = worldBounds.transform(targetCRS, true);
            transformDataBounds = dataBounds.transform(targetCRS, true);

            // 计算世界范围在 Web Mercator 投影坐标系下的宽度和高度
            double dataWidth = projectedDataBounds.getWidth();
            double dataHeight = projectedDataBounds.getHeight();
            double dataMinX = projectedDataBounds.getMinX();
            double dataMinY = projectedDataBounds.getMinY();

            // 遍历切片
            for (int y = 0; y < numTiles; y++) {
                for (int x = 0; x < numTiles; x++) {
                    // 计算切片在 Web Mercator 投影坐标系下的地理范围
                    double tileMinX = dataMinX + x * dataWidth / numTiles;
                    double tileMaxX = dataMinX + (x + 1) * dataWidth / numTiles;
                    double tileMinY = dataMinY + (numTiles - y - 1) * dataHeight / numTiles;
                    double tileMaxY = dataMinY + (numTiles - y) * dataHeight / numTiles;
                    ReferencedEnvelope tileBounds = new ReferencedEnvelope(tileMinX, tileMaxX, tileMinY, tileMaxY, targetCRS);

                    // 如果切片与矢量数据不存在交集，则跳过该切片
                    if (!transformDataBounds.intersects((Envelope) tileBounds)) {
                        continue;
                    }

                    // 创建图层
                    FeatureLayer layer = new FeatureLayer(featureSource, style);

                    // 创建 MapContent
                    MapContent mapContent = new MapContent();
                    mapContent.addLayer(layer);
                    mapContent.getViewport().setBounds(tileBounds);

                    // 渲染切片
                    StreamingRenderer renderer = new StreamingRenderer();
                    renderer.setMapContent(mapContent);
                    BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = image.createGraphics();

                    renderer.paint(graphics, new Rectangle(tileSize, tileSize), tileBounds);

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
                    mapContent.dispose();
                }
            }
        } catch (FactoryException | TransformException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建矢量图层样式
     *
     * @param styleFactory  样式工厂
     * @param filterFactory 过滤器工厂
     * @return 矢量图层样式
     */
    public static Style createCustomStyle(StyleFactory styleFactory, FilterFactory2 filterFactory) {
        // Create a black line for the stroke
        Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.RED), filterFactory.literal(1));

        // Create a semi-transparent blue fill
        Fill fill = styleFactory.createFill(filterFactory.literal(new Color(0, 0, 255, 128)));


        // Create a line symbolizer using the stroke
//        LineSymbolizer lineSymbolizer = styleFactory.createLineSymbolizer(stroke, null);

        // Create a polygon symbolizer using the stroke and fill
        PolygonSymbolizer polygonSymbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, null);


        // Create a rule with the line symbolizer
        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(polygonSymbolizer);

        // Create a style with the rule
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(styleFactory.createFeatureTypeStyle(rule));

        return style;
    }

    public static void main(String[] args) throws IOException, FactoryException, TransformException {

        File shapefile = new File("E:\\data\\output\\county_4490.shp");
        String outputDir = "E:\\data\\output\\slice9";
        for (int i = 1; i <= 15; i++) {
            processVectorData(shapefile, outputDir, i);
        }
    }


}
