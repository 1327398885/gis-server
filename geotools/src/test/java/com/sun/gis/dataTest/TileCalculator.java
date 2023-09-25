package com.sun.gis.dataTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
    public static BufferedImage stitchTiles(int startX, int startY, int endX, int endY, String outputFilePath) {
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
        g.dispose();
        return outputImage;
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
//        String s = "a"; // 可以是 'a', 'b', 或 'c'
//        String tileUrl = String.format("https://%s.tile.openstreetmap.org/%d/%d/%d.png", s, z, x, y);
//
        // 转换Google TMS Y坐标到GeoServer TMS Y坐标
        int yGeoServer = (int) (Math.pow(2, z) - 1 - y);

        String workspace = "jiangsu";
        String layerName = "320205_202005";
        String crs = "EPSG:900913"; // 更改这个值以适应你的实际坐标参照系统
        String format = "png"; // 格式可以是png, jpg等

        String tileUrl = String.format("http://localhost:8080/geoserver/gwc/service/tms/1.0.0/%s:%s@%s@%s/%d/%d/%d.%s",
                workspace, layerName, crs, format, z, x, yGeoServer, format);


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
    public static TmsTileInfo tilesInViewport(double lat1, double lon1, double lat2, double lon2, int zoom) {
        int[] tile1 = latLonToTile(lat1, lon1, zoom);
        int[] tile2 = latLonToTile(lat2, lon2, zoom);

        int xTile1 = tile1[0], yTile1 = tile1[1];
        int xTile2 = tile2[0], yTile2 = tile2[1];

        int xCount = Math.abs(xTile2 - xTile1) + 1;
        int yCount = Math.abs(yTile2 - yTile1) + 1;

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
        TmsTileInfo tmsTileInfo = new TmsTileInfo(xCount, yCount, zoom);
        return tmsTileInfo;
    }

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

    public static void demo() {
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

            // 省略了 GeoJSON 字符串和解析的代码段

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
            double zoomFactor = Math.pow(2, bestZoom - 3);

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

            TmsTileInfo tmsTileInfo = tilesInViewport(topLeftLat, topLeftLng, bottomLeftLat, bottomRightLng, bestZoom);
            BufferedImage bufferedImage = stitchTiles(tile1[0], tile1[1], tile2[0], tile2[1], "E:\\code\\github\\gis-server\\data\\output\\dataTest\\stitched_image.png");


            // 裁剪底图
            // 计算偏移量和裁剪区域
            int offsetX = (bufferedImage.getWidth() - imageWidth) / 2;
            int offsetY = (bufferedImage.getHeight() - imageHeight) / 2;

            BufferedImage croppedImage = bufferedImage.getSubimage(offsetX,offsetY,imageWidth,imageHeight);

// 保存裁剪后的图像
            File outputImageFile = new File("E:\\code\\github\\gis-server\\data\\output\\dataTest\\stitched_image_new.png");
            ImageIO.write(croppedImage, "png", outputImageFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        demo();
    }
}
