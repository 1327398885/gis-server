package com.sun.gis.tools.transformation;

import org.geotools.data.*;
import org.geotools.data.shapefile.*;
import org.geotools.data.simple.*;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoordinateTransformationShapefile {

    public static void main(String[] args) {
        try {
            File inFile = new File("E:\\data\\output\\county_4326.shp");
            File outFile = new File("E:\\data\\output\\county_3857.shp");
            String targetCRSStr = "EPSG:3857";
            transformation(inFile, outFile, targetCRSStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void transformation(File inFile, File outFile, String targetCRSStr) throws IOException, FactoryException, TransformException {
        // 读取 Shapefile
        DataStore inputDataStore = new ShapefileDataStore(inFile.toURI().toURL());
        String typeName = inputDataStore.getTypeNames()[0];
        SimpleFeatureSource source = inputDataStore.getFeatureSource(typeName);
        SimpleFeatureType schema = source.getSchema();

        // 定义转换前后的坐标参照系统
        CoordinateReferenceSystem sourceCRS = schema.getCoordinateReferenceSystem();
        CoordinateReferenceSystem targetCRS = CRS.decode(targetCRSStr);  // EPSG:3857

        // 创建转换函数
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        // 准备写出转换后的 Shapefile
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<>();
        create.put("url", outFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore newDataStore = factory.createNewDataStore(create);

        // 创建新的 schema
        SimpleFeatureType newSchema = SimpleFeatureTypeBuilder.retype(schema, targetCRS);
        newDataStore.createSchema(newSchema);

        // 读取并转换几何体，写出新的 Feature

        //从源 Shapefile 数据中获取所有的地理特征
        SimpleFeatureCollection featureCollection = source.getFeatures();
        //获取一个迭代器，用于遍历所有的地理特征
        SimpleFeatureIterator iterator = featureCollection.features();
        //创建一个新的事务，用于管理对新 Shapefile 数据的修改
        Transaction transaction = new DefaultTransaction("Reproject");
        // 获取新 Shapefile 数据的第一个类型名称
        String newDataStoreTypeName = newDataStore.getTypeNames()[0];
        // 获取新 Shapefile 数据的特征存储，这个特征存储将用于写入转换后的地理特征
        SimpleFeatureStore featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(newDataStoreTypeName);
        // 设置特征存储的事务
        featureStore.setTransaction(transaction);
        // 创建一个新的列表，用于存储转换后的地理特征
        List<SimpleFeature> list = new ArrayList<>();
        // 遍历所有的地理特征，将每个特征的几何体从源坐标参照系统转换到目标坐标参照系统，然后将转换后的特征添加到列表中
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Geometry geometry2 = JTS.transform(geometry, transform);
            SimpleFeature feature2 = SimpleFeatureBuilder.build(newSchema, feature.getAttributes(), null);
            feature2.setDefaultGeometry(geometry2);
            list.add(feature2);
        }
        // 将转换后的地理特征写入到新的 Shapefile 文件中
        featureStore.addFeatures(DataUtilities.collection(list));
        // 提交事务，确保对新 Shapefile 文件的修改被保存
        transaction.commit();
        // 关闭事务
        transaction.close();
        // 关闭特征迭代器
        iterator.close();
        // 释放源 Shapefile 数据的资源
        inputDataStore.dispose();
        // 释放新 Shapefile 数据的资源
        newDataStore.dispose();
    }

}
