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
                    "              121.59100125089134,\n" +
                    "              31.26934950481734\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.5037127674313,\n" +
                    "              31.23044597723178\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.51070770802284,\n" +
                    "              31.19564370235183\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.56655578988659,\n" +
                    "              31.167329748013714\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.6215764927623,\n" +
                    "              31.183257389258316\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.67452874816018,\n" +
                    "              31.222180342290343\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.63564193560251,\n" +
                    "              31.251892536822737\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              121.59100125089134,\n" +
                    "              31.26934950481734\n" +
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
