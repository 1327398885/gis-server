package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PhotoTRest {

    public static int calculateZoomLevel(double minLat, double maxLat, double minLng, double maxLng, int mapWidth, int mapHeight) {
        int maxZoom = 18;
        for (int zoom = maxZoom; zoom >= 0; zoom--) {
            double latZoom = 256 * Math.pow(2, zoom) / 180;
            double lngZoom = 256 * Math.pow(2, zoom) / 360;
            if ((maxLat - minLat) * latZoom <= mapHeight && (maxLng - minLng) * lngZoom <= mapWidth) {
                return zoom;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        try {
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
            JSONObject geoJsonObj = JSON.parseObject(geoJsonStr);
            JSONArray coordinates = geoJsonObj.getJSONArray("coordinates").getJSONArray(0);

            int imageWidth = 640;
            int imageHeight = 480;

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imageWidth, imageHeight);

            double minLng = Double.POSITIVE_INFINITY, maxLng = Double.NEGATIVE_INFINITY;
            double minLat = Double.POSITIVE_INFINITY, maxLat = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < coordinates.size(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double lng = point.getDoubleValue(0);
                double lat = point.getDoubleValue(1);
                minLng = Math.min(minLng, lng);
                maxLng = Math.max(maxLng, lng);
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
            }

            int bestZoom = calculateZoomLevel(minLat, maxLat, minLng, maxLng, imageWidth, imageHeight);
            double zoomFactor = Math.pow(2, bestZoom-1);

            double xScale = (imageWidth / 256.0) * zoomFactor;
            double yScale = (imageHeight / 256.0) * zoomFactor;

            double polygonCenterX = (minLng + maxLng) / 2;
            double polygonCenterY = (minLat + maxLat) / 2;

            double imageCenterX = imageWidth / 2.0;
            double imageCenterY = imageHeight / 2.0;

            double xOffset = imageCenterX - polygonCenterX * xScale;
            double yOffset = imageCenterY + polygonCenterY * yScale;

            g.setColor(new Color(255, 0, 0, 128));
            Polygon polygon = new Polygon();

            for (int i = 0; i < coordinates.size(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double lng = point.getDoubleValue(0);
                double lat = point.getDoubleValue(1);
                int x = (int) (lng * xScale + xOffset);
                int y = (int) (yOffset - lat * yScale);
                polygon.addPoint(x, y);
            }

            g.fillPolygon(polygon);
            g.dispose();

            ImageIO.write(image, "png", new File("E:\\code\\github\\gis-server\\data\\output\\dataTest\\generated_image.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
