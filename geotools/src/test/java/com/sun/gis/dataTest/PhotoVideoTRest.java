package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PhotoVideoTRest {
//    public static TmsTileInfo latLngToTmsTile(double lat, double lng, int zoom) {
//        int xtile = (int) Math.floor((lng + 180) / 360 * (1 << zoom));
//        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
//        return new TmsTileInfo(xtile, ytile, zoom);
//    }
public static TmsTileInfo latLngToTmsTile(double lat, double lng, int zoom) {
    // 参数验证（可选）
    if (lat < -90 || lat > 90 || lng < -180 || lng > 180 || zoom < 0) {
        throw new IllegalArgumentException("Invalid latitude, longitude or zoom level");
    }

    // 计算xtile（tile的x坐标）
    int xtile = (int) Math.floor((lng + 180) / 360 * (1 << zoom));

    // 预计算一些值以避免重复计算
    double radLat = Math.toRadians(lat);
    double cosRadLat = Math.cos(radLat);

    // 计算ytile（tile的y坐标）
    int ytile = (int) Math.floor((1 - Math.log(Math.tan(radLat) + 1 / cosRadLat) / Math.PI) / 2 * (1 << zoom));

    return new TmsTileInfo(xtile, ytile, zoom);
}


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
                    "              120.41657389458322,\n" +
                    "              31.661678585718462\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              120.42684297625055,\n" +
                    "              31.614698379015323\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              120.49012666274604,\n" +
                    "              31.60495148682884\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              120.50263485233455,\n" +
                    "              31.63814740236795\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              120.47886929211523,\n" +
                    "              31.664589144661747\n" +
                    "            ],\n" +
                    "            [\n" +
                    "              120.41657389458322,\n" +
                    "              31.661678585718462\n" +
                    "            ]\n" +
                    "          ]\n" +
                    "        ],\n" +
                    "        \"type\": \"Polygon\"\n" +
                    "      }";
            JSONObject geoJsonObj = JSON.parseObject(geoJsonStr);
            JSONArray coordinates = geoJsonObj.getJSONArray("coordinates").getJSONArray(0);

            int imageWidth = 800;
            int imageHeight = 600;

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

            // 缩放级别
            int zoom = calculateZoomLevel(minLat, maxLat, minLng, maxLng, imageWidth, imageHeight);// 使用你之前的calculateZoomLevel方法


            // 获取多边形中心点对应的TMS切片
            TmsTileInfo centerTile = latLngToTmsTile(polygonCenterY, polygonCenterX, zoom);

            // 每个切片的大小（通常是256x256像素）
            int tileSize = 256;

            // 计算涉及到的切片数量
            int tilesWide = (int) Math.ceil((double) imageWidth / tileSize);
            int tilesHigh = (int) Math.ceil((double) imageHeight / tileSize);

            // 计算涉及到的切片范围
            int startX = centerTile.x - tilesWide / 2;
            int endX = centerTile.x + tilesWide / 2;
            int startY = centerTile.y - tilesHigh / 2;
            int endY = centerTile.y + tilesHigh / 2;

            System.out.println("需要的TMS切片范围：");
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    System.out.println("X: " + x + ", Y: " + y + ", Zoom: " + zoom);
                }
            }

            String baseUrl = "http://localhost:8080/geoserver/gwc/service/tms/1.0.0/jiangsu%3A320205_202005@EPSG%3A900913@png";

            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    String tileUrl = String.format("%s/%d/%d/%d.png", baseUrl, zoom, x, y);
                    HttpURLConnection connection = (HttpURLConnection) new URL(tileUrl).openConnection();
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() == 200) {
                        try (InputStream is = connection.getInputStream()) {
                            String outputFilePath = String.format("E:\\code\\github\\gis-server\\data\\output\\dataTest\\tms\\tile_%d_%d_%d.png", zoom, x, y);
                            try (OutputStream os = Files.newOutputStream(Paths.get(outputFilePath))) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    } else {
                        System.out.println("Failed to fetch tile " + tileUrl);
                    }

                    connection.disconnect();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
