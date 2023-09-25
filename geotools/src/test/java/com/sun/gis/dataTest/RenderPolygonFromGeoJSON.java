package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RenderPolygonFromGeoJSON {

    public static int calculateZoomLevel(double minX, double maxX, double minY, double maxY, int imgWidth, int imgHeight) {
        int maxZoom = 18;
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        double xZoom = xRange / imgWidth;
        double yZoom = yRange / imgHeight;

        double zoom = Math.min(xZoom, yZoom);
        for (int i = 0; i <= maxZoom; i++) {
            double resolution = 20037508.34 * 2 / (256 * Math.pow(2, i));
            if (resolution <= zoom) {
                return i;
            }
        }
        return maxZoom;
    }

    public static int renderPolygon(String geoJsonStr, int imgWidth, int imgHeight) throws IOException {
        // 解析GeoJSON字符串
        JSONObject geoJsonObj = JSON.parseObject(geoJsonStr);
        JSONArray coordinates = geoJsonObj.getJSONArray("coordinates").getJSONArray(0);

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgWidth, imgHeight);

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < coordinates.size(); i++) {
            JSONArray point = coordinates.getJSONArray(i);
            double x = point.getDoubleValue(0);
            double y = point.getDoubleValue(1);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        int zoomLevel = calculateZoomLevel(minX, maxX, minY, maxY, imgWidth, imgHeight);

        double xScale = imgWidth / (maxX - minX);
        double yScale = imgHeight / (maxY - minY);
        double scale = Math.min(xScale, yScale);

        Polygon polygon = new Polygon();

        for (int i = 0; i < coordinates.size(); i++) {
            JSONArray point = coordinates.getJSONArray(i);
            double x = point.getDoubleValue(0);
            double y = point.getDoubleValue(1);
            int scaledX = (int) ((x - minX) * scale);
            int scaledY = (int) ((y - minY) * scale);
            polygon.addPoint(scaledX, imgHeight - scaledY);  // 注意：这里翻转了y坐标
        }

        g.setColor(new Color(255, 0, 0, 128));
        g.fillPolygon(polygon);
        g.dispose();

        ImageIO.write(image, "png", new File("E:\\code\\github\\gis-server\\data\\output\\dataTest\\output.png"));

        return zoomLevel;
    }

    public static void main(String[] args) {
        String geoJsonStr = "{\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              120.49260317333017,\n" +
                "              31.619643076821475\n" +
                "            ],\n" +
                "            [\n" +
                "              120.49269519023414,\n" +
                "              31.61621942444208\n" +
                "            ],\n" +
                "            [\n" +
                "              120.49670146764805,\n" +
                "              31.616339977720642\n" +
                "            ],\n" +
                "            [\n" +
                "              120.49655282484696,\n" +
                "              31.619727461000494\n" +
                "            ],\n" +
                "            [\n" +
                "              120.49260317333017,\n" +
                "              31.619643076821475\n" +
                "            ]\n" +
                "          ]\n" +
                "        ],\n" +
                "        \"type\": \"Polygon\"\n" +
                "      }";
        try {
            int i = renderPolygon(geoJsonStr, 640, 480);
            System.out.println(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
