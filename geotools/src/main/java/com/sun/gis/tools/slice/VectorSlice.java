package com.sun.gis.tools.slice;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VectorSlice {
    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    //这行代码创建了一个 FilterFactory2 实例。FilterFactory2 是 GeoTools 中的一个工厂类，用于创建过滤器对象。过滤器对象用于筛选地理要素，可以基于属性或几何关系来选择特定的地理要素。这个类是 FilterFactory 的一个扩展版本，提供了更多的功能。
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static void main(String[] args) throws IOException {
        // 读取矢量数据源
        File shapefile = new File("E:\\data\\division\\county.shp");
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapefile);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();

        // 加载样式
        Style style = createCustomStyle(sf, ff);


        // 设置切片级别、切片大小和地理范围
        int zoomLevel = 5;
        int tileSize = 256;
//        ReferencedEnvelope bounds = new ReferencedEnvelope(-180, 180, -90, 90, null);
        CoordinateReferenceSystem crs = featureSource.getSchema().getCoordinateReferenceSystem();

        // 计算切片数量
        int numTiles = (int) Math.pow(2, zoomLevel);

        for (int x = 0; x < numTiles; x++) {
            for (int y = 0; y < numTiles; y++) {
                // 创建图层
                FeatureLayer layer = new FeatureLayer(featureSource, style);

                ReferencedEnvelope bounds = layer.getBounds();
                // 计算切片地理范围
                double minX = bounds.getMinX() + x * bounds.getWidth() / numTiles;
                double maxX = bounds.getMinX() + (x + 1) * bounds.getWidth() / numTiles;
                double minY = bounds.getMinY() + y * bounds.getHeight() / numTiles;
                double maxY = bounds.getMinY() + (y + 1) * bounds.getHeight() / numTiles;
                ReferencedEnvelope tileBounds = new ReferencedEnvelope(minX, maxX, minY, maxY, crs);

                // 创建 MapContent
                MapContent mapContent = new MapContent();
                mapContent.addLayer(layer);
                mapContent.getViewport().setBounds(tileBounds);

                // 渲染切片
                StreamingRenderer renderer = new StreamingRenderer();
                renderer.setMapContent(mapContent);
                BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();

                renderer.paint(graphics, new Rectangle(tileSize, tileSize), mapContent.getViewport().getBounds());

                // 保存切片为PNG格式
                File output = new File("E:\\data\\output\\slice\\tile_" + zoomLevel + "_" + x + "_" + y + ".png");
                ImageIO.write(image, "PNG", output);
                mapContent.dispose();
            }
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
