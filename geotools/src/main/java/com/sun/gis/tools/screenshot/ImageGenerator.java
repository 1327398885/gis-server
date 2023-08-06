package com.sun.gis.tools.screenshot;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;

import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;


import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFField;

/**
 * 该类用于生成一个包含Shapefile和TIFF影像的组合图像。
 * 首先加载Shapefile和TIFF影像，然后根据Shapefile的边界框裁剪TIFF影像，
 * 然后将Shapefile和裁剪后的TIFF影像组合成一个新的图像。
 * 最后将新图像保存为PNG文件。
 */
public class ImageGenerator {

    public static void main(String[] args) throws Exception {
        // Load shapefile
        //首先，通过创建一个File对象来指定要读取的shapefile文件路径。
        //然后，通过FileDataStoreFinder类的静态方法getDataStore()来获取文件的数据存储对象，该对象可以用来读取shapefile文件中的数据。
        //接着，通过数据存储对象的getFeatureSource()方法获取SimpleFeatureSource对象，该对象可以用来获取shapefile中的要素数据。
        //然后，通过SimpleFeatureSource对象的getFeatures()方法获取SimpleFeatureCollection对象，该对象包含了所有的要素数据。
        //接下来，通过SimpleFeatureCollection对象的getBounds()方法获取所有要素的地理范围，即ReferencedEnvelope对象。
        //最后，通过ReferencedEnvelope对象的构造函数，将其转换为与要素数据集坐标系相同的地理范围。
        File shapefile = new File("D:\\data\\wuxi\\demo\\demo.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(shapefile);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        ReferencedEnvelope referencedEnvelope = featureCollection.getBounds();
//        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(collectionBounds, featureSource.getSchema().getCoordinateReferenceSystem());

        // Create style for shapefile
        //这段代码是用来创建一个简单的样式，并设置过滤条件的。
        // 首先，创建一个SimpleStyle对象，并传入FeatureSource的Schema，用于定义样式的基本属性。
        // 然后，通过CommonFactoryFinder获取FilterFactory2对象，用于创建过滤条件。
        // 接着，获取样式的第一个FeatureTypeStyle的第一个Rule，并创建一个Filter对象，设置过滤条件为：属性OBJECTID_1等于VALUE并且包含在referencedEnvelope范围内。
        // 最后，将Filter对象设置为该Rule的过滤条件。
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Rule rule = style.featureTypeStyles().get(0).rules().get(0);
        Filter filter = ff.and(ff.equals(ff.property("OBJECTID_1"), ff.literal("2560")),
                ff.bbox(ff.property(featureSource.getSchema().getGeometryDescriptor().getName()), referencedEnvelope
                ));
        rule.setFilter(filter);

        // Load TIFF image
        //这段代码用于读取指定路径下的一张TIFF格式的图片，并将其转换为Java中的BufferedImage对象。
        // 然后创建一个地图内容对象MapContent，设置其标题为"Image and Shapefile"，并添加一个FeatureLayer图层和一个MapViewport视口。
        // 最后使用StreamingRenderer进行渲染，并将渲染结果显示在地图上。
        // 其中TIFFDecodeParam对象用于设置TIFF解码参数，FeatureLayer图层用于显示矢量数据，StreamingRenderer用于对地图进行渲染。
        File tiffFile = new File("D:\\data\\wuxi\\XS_2_20200524.tif");
        FileSeekableStream stream = new FileSeekableStream(tiffFile);
        TIFFDecodeParam decodeParam = new TIFFDecodeParam();
        decodeParam.setDecodePaletteAsShorts(true);
        ImageDecoder decoder = new TIFFImageDecoder(stream, decodeParam);
        RenderedImage renderedImage = decoder.decodeAsRenderedImage(0);
        BufferedImage tiffImage = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        tiffImage.getGraphics().drawImage(tiffImage, 0, 0, null);

        // Create map content
        // 这段代码创建了一个地图对象MapContent，并设置了它的标题为"Image and Shapefile"。
        // 然后将一个FeatureLayer（要素图层）和一个Style（样式）添加到地图中。
        // 接下来创建了一个MapViewport（地图视口）对象，并将其设置为指定的ReferencedEnvelope（参考范围）。
        // 最后创建了一个GTRenderer（渲染器）对象，并将其设置为StreamingRenderer（流渲染器），并将地图对象设置为其MapContent
        // 。这段代码的作用是创建一个带有要素图层和样式的地图，并将其渲染到屏幕上。
        MapContent map = new MapContent();
        map.setTitle("Image and Shapefile");
//        map.setBackground(new Color(255, 255, 255));
        map.addLayer(new FeatureLayer(featureCollection, style));
        MapViewport mapViewport = new MapViewport(referencedEnvelope);
        map.setViewport(mapViewport);
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);


        // Render map to image
        //这段代码是使用Java绘制地图，并根据shapefile的范围裁剪图像。具体实现步骤如下：
        //
        //1. 创建一个BufferedImage对象，用于绘制地图。
        //2. 获取Graphics2D对象，设置绘制模式，使用renderer.paint方法将地图绘制到BufferedImage对象中。
        //3. 创建一个ReferencedEnvelope对象，获取featureCollection的范围。
        //4. 根据地图的视口大小计算出绘制范围的最小x、y值和最大x、y值，以及裁剪后的宽度和高度。
        //5. 创建一个BufferedImage对象，用于存储裁剪后的图像。
        //6. 获取Graphics2D对象，使用getSubimage方法从原始图像中裁剪出指定范围的图像，绘制到裁剪后的BufferedImage对象中。
        //7. 释放资源，返回裁剪后的BufferedImage对象。
        //
        //需要注意的是，如果裁剪后的宽度或高度小于等于0，则抛出异常。
        BufferedImage mapImage = new BufferedImage(tiffImage.getWidth(), tiffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = mapImage.createGraphics();
        graphics.setComposite(AlphaComposite.Clear); // 清除背景
        graphics.fillRect(0, 0, tiffImage.getWidth(), tiffImage.getHeight()); // 填充背景
        graphics.setComposite(AlphaComposite.SrcOver); // 设置绘制模式
        renderer.paint(graphics, new Rectangle(tiffImage.getWidth(), tiffImage.getHeight()), referencedEnvelope);
        graphics.dispose(); // 释放资源
        // Crop image based on shapefile extent
        ReferencedEnvelope extent = featureCollection.getBounds();
        int imageWidth = tiffImage.getWidth();
        int imageHeight = tiffImage.getHeight();
        double viewportMinX = map.getViewport().getBounds().getMinX();
        double viewportMinY = map.getViewport().getBounds().getMinY();
        double viewportWidth = map.getViewport().getBounds().getWidth();
        double viewportHeight = map.getViewport().getBounds().getHeight();

        int minX = (int) Math.floor((extent.getMinX() - viewportMinX) / viewportWidth * imageWidth);
        int minY = (int) Math.floor((extent.getMinY() - viewportMinY) / viewportHeight * imageHeight);
        int maxX = (int) Math.ceil((extent.getMaxX() - viewportMinX) / viewportWidth * imageWidth);
        int maxY = (int) Math.ceil((extent.getMaxY() - viewportMinY) / viewportHeight * imageHeight);


        int cropWidth = maxX - minX;
        int cropHeight = maxY - minY;
        if (cropWidth <= 0 || cropHeight <= 0) {
            throw new Exception("Invalid crop size");
        }
        BufferedImage croppedImage = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = croppedImage.createGraphics();
        BufferedImage subImage = tiffImage.getSubimage(minX, minY, cropWidth, cropHeight);
        g2d.drawImage(subImage, cropWidth, cropHeight, null);
        g2d.dispose();


        // Save combined image to file
        File outputFile = new File("D:\\data\\wuxi\\output.png");
        ImageIO.write(mapImage, "png", outputFile);
    }

}

