package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试访问在线切片服务，并下载矢量在可视范围内缩放居中的相关切片
 *
 * @author sunbt
 * @date 2023/9/13 20:36
 */
public class TileCalculator {

    // 用于保存下载的切片的列表
    public static List<BufferedImage> downloadedTiles = new ArrayList<>();

    /**
     * 将多个瓦片图像拼接成一个完整的图像。
     *
     * @param startX         拼接区域的起始X坐标
     * @param startY         拼接区域的起始Y坐标
     * @param endX           拼接区域的结束X坐标
     * @param endY           拼接区域的结束Y坐标
     * @param outputFilePath 输出图像的保存路径
     */
    public static void stitchTiles(int startX, int startY, int endX, int endY, String outputFilePath) {
        int tileWidth = 256;
        int tileHeight = 256;

        int outputImageWidth = (endX - startX + 1) * tileWidth;
        int outputImageHeight = (endY - startY + 1) * tileHeight;
        BufferedImage outputImage = new BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = outputImage.createGraphics();

        int tileIndex = 0;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                BufferedImage tile = downloadedTiles.get(tileIndex++);
                int xPos = (x - startX) * tileWidth;
                int yPos = (y - startY) * tileHeight;
                g.drawImage(tile, xPos, yPos, null);
            }
        }

        g.dispose();

        try {
            ImageIO.write(outputImage, "png", new java.io.File(outputFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据经纬度范围和预期的图像大小来计算最佳的缩放级别。
     *
     * @param minLat    纬度范围的最小值
     * @param maxLat    纬度范围的最大值
     * @param minLng    经度范围的最小值
     * @param maxLng    经度范围的最大值
     * @param mapWidth  预期图像的宽度
     * @param mapHeight 预期图像的高度
     * @return 最佳的缩放级别
     */
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


    /**
     * 将给定的经纬度和缩放级别转换为瓦片坐标。
     *
     * @param lat  纬度
     * @param lon  经度
     * @param zoom 缩放级别
     * @return 对应的瓦片坐标[x, y]
     */
    public static int[] latLonToTile(double lat, double lon, int zoom) {
        int n = (int) Math.pow(2, zoom);
        int xTile = (int) Math.floor((lon + 180.0) / 360.0 * n);
        int yTile = (int) Math.floor((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * n);

        return new int[]{xTile, yTile};
    }

    /**
     * 根据指定的瓦片坐标下载瓦片图像。
     *
     * @param z 缩放级别
     * @param x 瓦片的X坐标
     * @param y 瓦片的Y坐标
     * @throws IOException 当下载过程出现问题时抛出
     */
    public static void downloadTile(int z, int x, int y) throws IOException {
        String s = "a"; // 可以是 'a', 'b', 或 'c'
        String tileUrl = String.format("https://%s.tile.openstreetmap.org/%d/%d/%d.png", s, z, x, y);
        URL url = new URL(tileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 设置User-Agent来模拟谷歌浏览器
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537");


        InputStream inStream = conn.getInputStream();
        BufferedImage image = ImageIO.read(inStream);

        ImageIO.write(image, "png", new java.io.File(String.format("E:\\code\\github\\gis-server\\data\\output\\dataTest\\tms\\%d_%d_%d.png", z, x, y)));

        downloadedTiles.add(image);
    }

    /**
     * 下载给定视窗内的所有瓦片图像。
     *
     * @param lat1 视窗的左上角纬度
     * @param lon1 视窗的左上角经度
     * @param lat2 视窗的右下角纬度
     * @param lon2 视窗的右下角经度
     * @param zoom 缩放级别
     */
    public static void tilesInViewport(double lat1, double lon1, double lat2, double lon2, int zoom) {
        int[] tile1 = latLonToTile(lat1, lon1, zoom);
        int[] tile2 = latLonToTile(lat2, lon2, zoom);

        int xTile1 = tile1[0], yTile1 = tile1[1];
        int xTile2 = tile2[0], yTile2 = tile2[1];

        for (int x = Math.min(xTile1, xTile2); x <= Math.max(xTile1, xTile2); x++) {
            for (int y = Math.min(yTile1, yTile2); y <= Math.max(yTile1, yTile2); y++) {
                try {
                    downloadTile(zoom, x, y);
                    System.out.println(String.format("Downloaded tile at zoom=%d, x=%d, y=%d", zoom, x, y));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(String.format("Failed to download tile at zoom=%d, x=%d, y=%d", zoom, x, y));
                }
            }
        }
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

            // 省略了 GeoJSON 字符串和解析的代码段

            int imageWidth = 640;
            int imageHeight = 480;

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
            double zoomFactor = Math.pow(2, bestZoom - 1);

            double xScale = (imageWidth / 256.0) * zoomFactor;
            double yScale = (imageHeight / 256.0) * zoomFactor;

            double polygonCenterX = (minLng + maxLng) / 2;
            double polygonCenterY = (minLat + maxLat) / 2;

            double imageCenterX = imageWidth / 2.0;
            double imageCenterY = imageHeight / 2.0;

            // 计算四个角点的经纬度
            double topLeftLat = polygonCenterY + (imageCenterY - 0) / yScale;
            double topLeftLng = polygonCenterX + (0 - imageCenterX) / xScale;

            double topRightLat = polygonCenterY + (imageCenterY - 0) / yScale;
            double topRightLng = polygonCenterX + (imageWidth - imageCenterX) / xScale;

            double bottomLeftLat = polygonCenterY + (imageCenterY - imageHeight) / yScale;
            double bottomLeftLng = polygonCenterX + (0 - imageCenterX) / xScale;

            double bottomRightLat = polygonCenterY + (imageCenterY - imageHeight) / yScale;
            double bottomRightLng = polygonCenterX + (imageWidth - imageCenterX) / xScale;

            System.out.println("Top-Left Corner: Lat = " + topLeftLat + ", Lng = " + topLeftLng);
            System.out.println("Top-Right Corner: Lat = " + topRightLat + ", Lng = " + topRightLng);
            System.out.println("Bottom-Left Corner: Lat = " + bottomLeftLat + ", Lng = " + bottomLeftLng);
            System.out.println("Bottom-Right Corner: Lat = " + bottomRightLat + ", Lng = " + bottomRightLng);

            tilesInViewport(topLeftLat, topLeftLng, bottomLeftLat, bottomRightLng, bestZoom);


            int[] tile1 = latLonToTile(topLeftLat, topLeftLng, bestZoom);
            int[] tile2 = latLonToTile(bottomRightLat, bottomRightLng, bestZoom);

            tilesInViewport(topLeftLat, topLeftLng, bottomLeftLat, bottomRightLng, bestZoom);
            stitchTiles(tile1[0], tile1[1], tile2[0], tile2[1], "E:\\code\\github\\gis-server\\data\\output\\dataTest\\stitched_image.png");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
