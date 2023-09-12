package com.sun.gis.dataTest;

public class PhotoVideoTRest {
    public static TmsTileInfo latLngToTmsTile(double lat, double lng, int zoom) {
        int xtile = (int) Math.floor((lng + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
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
        // GeoJSON多边形的中心点
        double centerLat = 37.75;
        double centerLng = -122.45;

        // 缩放级别
        int zoom = calculateZoomLevel(37.7, 37.8, -122.5, -122.4, 800, 600); // 使用你之前的calculateZoomLevel方法

        // 视图大小，单位是像素
        int viewWidth = 800;
        int viewHeight = 600;

        // 获取多边形中心点对应的TMS切片
        TmsTileInfo centerTile = latLngToTmsTile(centerLat, centerLng, zoom);

        // 每个切片的大小（通常是256x256像素）
        int tileSize = 256;

        // 计算涉及到的切片数量
        int tilesWide = (int) Math.ceil((double) viewWidth / tileSize);
        int tilesHigh = (int) Math.ceil((double) viewHeight / tileSize);

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
    }
}
