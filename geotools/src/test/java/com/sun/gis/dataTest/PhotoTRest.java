package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class PhotoTRest {

    public static void main(String[] args) {
        try {
            // GeoJSON 字符串
            String geoJsonStr = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.5, 37.7],[-122.4, 37.7],[-122.4, 37.8],[-122.5, 37.8],[-122.5, 37.7]]]}";

            // 使用 FastJSON 解析 GeoJSON
            JSONObject geoJsonObj = JSON.parseObject(geoJsonStr);
            JSONArray coordinates = geoJsonObj.getJSONArray("coordinates").getJSONArray(0);

            double[][] geojsonPolygon = new double[coordinates.size()][2];

            for (int i = 0; i < coordinates.size(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                geojsonPolygon[i][0] = point.getDoubleValue(0);
                geojsonPolygon[i][1] = point.getDoubleValue(1);
            }
            // 加载现有图片 (请确保路径正确)
            BufferedImage image = ImageIO.read(new File("your_image_path_here.png"));

            int[] xPoints = new int[geojsonPolygon.length];
            int[] yPoints = new int[geojsonPolygon.length];

            double minLng = Arrays.stream(geojsonPolygon).mapToDouble(coord -> coord[0]).min().orElse(0);
            double maxLng = Arrays.stream(geojsonPolygon).mapToDouble(coord -> coord[0]).max().orElse(0);
            double minLat = Arrays.stream(geojsonPolygon).mapToDouble(coord -> coord[1]).min().orElse(0);
            double maxLat = Arrays.stream(geojsonPolygon).mapToDouble(coord -> coord[1]).max().orElse(0);

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            for (int i = 0; i < geojsonPolygon.length; i++) {
                xPoints[i] = lngToPixel(geojsonPolygon[i][0], minLng, maxLng, imageWidth);
                yPoints[i] = latToPixel(geojsonPolygon[i][1], minLat, maxLat, imageHeight);
            }

            // 在图片上绘制
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(255, 0, 0, 128));
            g.fillPolygon(xPoints, yPoints, geojsonPolygon.length);
            g.dispose();

            // 保存新图片
            ImageIO.write(image, "png", new File("new_image_path_here.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int lngToPixel(double lng, double minLng, double maxLng, int imageWidth) {
        return (int) ((lng - minLng) / (maxLng - minLng) * imageWidth);
    }

    public static int latToPixel(double lat, double minLat, double maxLat, int imageHeight) {
        return (int) ((maxLat - lat) / (maxLat - minLat) * imageHeight);
    }
}
