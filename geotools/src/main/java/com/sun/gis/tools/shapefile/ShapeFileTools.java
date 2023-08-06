package com.sun.gis.tools.shapefile;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;

import java.io.File;
import java.util.List;

public class ShapeFileTools {

    public static void main(String[] args) throws Exception {
        // 指定 Shapefile 文件的路径
        File file = new File("/Users/sungang/Desktop/division/32/city.shp");

        // 使用 FileDataStoreFinder 找到并打开 Shapefile
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);

        // 读取 Shapefile 的内容
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();

        SimpleFeatureType schema = featureSource.getSchema();
        System.out.println(schema.toString());
        List<AttributeDescriptor> attributeDescriptors = schema.getAttributeDescriptors();
        for (int i = 0; i < attributeDescriptors.size(); i++) {
            Name name = attributeDescriptors.get(i).getName();
            System.out.println(name);
            AttributeType type = attributeDescriptors.get(i).getType();
            System.out.println(type);
            System.out.println(attributeDescriptors.get(i).toString());
        }
        Name name = schema.getName();
        System.out.println(name);
        String typeName = schema.getTypeName();
        System.out.println(typeName);
        List<AttributeType> types = schema.getTypes();
        System.out.println(types);
        for (int i = 0; i < types.size(); i++) {
            System.out.println(types.get(i).getName());
            System.out.println(types.get(i).toString());
            System.out.println(types.get(i).getBinding());
        }
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> features = collection.features();

        // 遍历并打印所有特性
        try {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
//                System.out.println(feature.getID() + ": " + feature.getDefaultGeometryProperty().getValue());
            }
        } finally {
            features.close();
            dataStore.dispose();
        }
    }

}
