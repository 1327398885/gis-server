package com.sun.gis.tools.transformation;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.referencing.CRS;

import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.IOException;

public class CoordinateTransformationTiff {

    public static void main(String[] args) throws Exception {
        File inFile = new File("E:\\data\\output\\20200543261.tif");
        File outFile = new File("E:\\data\\output\\2020053857.tif");
        String targetCRSStr = "EPSG:3857";
        transformation(inFile, outFile, targetCRSStr);


//        File inFile = new File("E:\\data\\output\\2020054326.tif");
//        File outFile = new File("E:\\data\\output\\20200543261.tif");
//        fixCoordinates(inFile,outFile);
    }


    public static void fixCoordinates(File inFile, File outFile) throws Exception {
        long startTime = System.currentTimeMillis();
        // 读取文件
        AbstractGridFormat format = GridFormatFinder.findFormat(inFile);
        GridCoverage2DReader reader = format.getReader(inFile);
        GridCoverage2D coverage = reader.read(null);

        // 获取原始坐标参考系
        CoordinateReferenceSystem sourceCRS = coverage.getCoordinateReferenceSystem();

        // 创建新的坐标参考系，交换经纬度
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
        if (!CRS.getAxisOrder(targetCRS).equals(CRS.AxisOrder.NORTH_EAST)) {
            targetCRS = CRS.decode("EPSG:4326", true);
        }

        // 创建转换
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        // 转换数据
        GridCoverage2D transformedCoverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCRS, null, null);

        // 写回文件
        GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
        GeoTiffWriter writer = (GeoTiffWriter) geoTiffFormat.getWriter(outFile);
        writer.write(transformedCoverage, null);
        writer.dispose();

        long endTime = System.currentTimeMillis();
        System.out.println("Method execution time: " + (endTime - startTime) / 1000.0 + " seconds");
    }




    public static void transformation(File inFile, File outFile, String targetCRSStr) throws FactoryException, IOException {
        long startTime = System.currentTimeMillis();
        // 找到输入文件的格式。对于GeoTiff文件，这将是GeoTiff格式
        AbstractGridFormat format = GridFormatFinder.findFormat(inFile);
        // 创建一个读取器来读取输入文件。读取器的类型取决于文件的格式
        GridCoverage2DReader reader = format.getReader(inFile);

        // 获取输入文件的坐标参考系统。这是原始的坐标系统
        CoordinateReferenceSystem sourceCRS = reader.getCoordinateReferenceSystem();

        // 创建目标坐标参考系统  "EPSG:3857"
        CoordinateReferenceSystem targetCRS = CRS.decode(targetCRSStr);

        // 创建一个从原始CRS到目标CRS的转换。findMathTransform函数将计算需要的转换
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

        // 读取输入文件的内容，并创建一个GridCoverage2D对象。GridCoverage2D是一个表示地理数据的类
        GridCoverage2D coverage = reader.read(new GeneralParameterValue[0]);
        GridCoverage2D transformedCoverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCRS, null, null);

        // 创建一个新的GeoTiff格式对象，用于写入转换后的数据
        GeoTiffFormat geoTiffFormat = new GeoTiffFormat();
        // 创建一个新的GeoTiff写入器，用于将转换后的数据写入输出文件
        GeoTiffWriter writer = (GeoTiffWriter) geoTiffFormat.getWriter(outFile);
        // 将转换后的数据写入输出文件
        writer.write(transformedCoverage, null);
        // 关闭写入器并释放任何占用的资源
        writer.dispose();

        long endTime = System.currentTimeMillis();
        System.out.println("Method execution time: " + (endTime - startTime) / 1000.0 + " seconds");
    }

}
