package com.sun.gis.tools.slice;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.opengis.filter.FilterFactory2;



public class VectorDataRenderer {

    public static void main(String[] args) throws IOException {
        File shapeFile = new File("/Users/sungang/Documents/data/tiff/320205/xbz.shp");
        renderShapefileToImage(shapeFile, "/Users/sungang/Documents/data/tiff/320205/output/output-image1.png");
    }

    public static void renderShapefileToImage(File shapeFile, String outputImagePath) throws IOException {
        // Read the shapefile
        Map<String, Object> params = new HashMap<>();
        params.put("url", shapeFile.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

        // Create a MapContent object to hold the map layers
        MapContent mapContent = new MapContent();

        // Create a default style
//        Style style = SLD.createSimpleStyle(featureCollection.getSchema());
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();
        Style style = createCustomStyle(styleFactory, filterFactory);

        // Create a feature layer using the feature source and style, and add it to the map content
        FeatureLayer featureLayer = new FeatureLayer(featureSource, style);
        mapContent.addLayer(featureLayer);

        // Get the bounding box for the features and set the map viewport
        ReferencedEnvelope mapArea = featureSource.getBounds();
        mapContent.getViewport().setBounds(mapArea);

        // Create a renderer
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(mapContent);

//        // Set up the image
//        int imageWidth = 800;
//        int imageHeight = 600;
//        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = image.createGraphics();
//
//        // Render the map
//        Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
//        renderer.paint(graphics, imageBounds, mapArea);
//
//        // Save the image to a file
//        ImageIO.write(image, "png", new File(outputImagePath));
//
//        // Clean up
//        graphics.dispose();
//        mapContent.dispose();

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

        // Save the image to a file
        ImageIO.write(image, "png", new File(outputImagePath));

        // Clean up
        graphics.dispose();
        mapContent.dispose();
    }

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


