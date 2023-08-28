package com.sun.gis.tools.tms;


import org.geotools.map.MapContent;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.map.WMTSMapLayer;
import org.geotools.ows.wmts.model.WMTSCapabilities;
import org.geotools.ows.wmts.model.WMTSLayer;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.net.URL;


/**
 * @author sunbt
 * @date 2023/8/27 20:13
 */
public class TMSLoader {

    // TODO: 2023/8/28 未完成

    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:8080/geoserver/gwc/service/wmts?REQUEST=GetCapabilities");
            WebMapTileServer wmts = new WebMapTileServer(url);
            WMTSCapabilities capabilities = wmts.getCapabilities();

            // 假设您要加载的图层是WMTS中的第一个图层
            WMTSLayer wmtsLayer = capabilities.getLayerList().get(0);
            WMTSMapLayer mapLayer = new WMTSMapLayer(wmts, wmtsLayer);

            MapContent mapContent = new MapContent();
            mapContent.addLayer(mapLayer);

            // 使用 JMapFrame 显示地图
            org.geotools.swing.JMapFrame.showMap(mapContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}