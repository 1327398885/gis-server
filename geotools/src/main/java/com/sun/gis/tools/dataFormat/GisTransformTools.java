package com.sun.gis.tools.dataFormat;

import com.alibaba.fastjson.JSONObject;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * WKT-GeoJSON-Geometry 互相关转换方法
 */
public class GisTransformTools {

    private static final Logger log = LoggerFactory.getLogger(GisTransformTools.class);

    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    static WKTReader reader = new WKTReader(geometryFactory);

    /**
     * wkt字符串转Geometry
     *
     * @param wkt 字符串
     * @return Geometry
     */
    public static Geometry wktStrToGeometry(String wkt) {
        try {
            return reader.read(wkt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * wkt转GeoJson包括属性值（feature）
     *
     * @param wkt wkt
     * @param map 属性值
     * @return GeoJson
     */
    public static HashMap<Object, Object> wktToJson(String wkt, Map<String, Object> map) {
        String json = null;
        // geoJson
        HashMap<Object, Object> feature = new HashMap<>();
        try {
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);
            StringWriter writer = new StringWriter();
            GeometryJSON g = new GeometryJSON();
            g.write(geometry, writer);
            JSONObject jsonObject = JSONObject.parseObject(writer.toString());
            feature.put("type", "Feature");
            feature.put("geometry", jsonObject);
            feature.put("properties", map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feature;
    }

    /**
     * wkt转GeoJson（geometry）  不包括属性
     *
     * @param wkt wkt
     * @return geoJson
     */
    public static JSONObject wktToJson(String wkt) {
        JSONObject jsonObject = null;
        try {
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);
            StringWriter writer = new StringWriter();
            GeometryJSON g = new GeometryJSON();
            g.write(geometry, writer);
            jsonObject = JSONObject.parseObject(writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * geoJson转Wkt（没有属性）
     *
     * @param geoJson geoJson
     * @return wkt
     */
    public static String geoJsonToWkt(String geoJson) {
        String wkt = null;
        GeometryJSON gJson = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        try {
            Geometry geometry = gJson.read(reader);

            wkt = geometry.toText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wkt;
    }


    /**
     * Geometry转GeoJson（无属性）
     *
     * @param geometry Geometry
     * @return JSONObject
     */
    public static JSONObject geometryToGeoJson(Geometry geometry) {
        String ret = null;
        try {
            StringWriter writer = new StringWriter();
            GeometryJSON g = new GeometryJSON();
            g.write(geometry, writer);
            ret = writer.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return JSONObject.parseObject(ret);
    }

    /**
     * GeoJson转Geometry
     *
     * @param geoJson 字符串类型GeoJson
     * @return Geometry
     */
    public static Geometry geoJsonToGeometry(String geoJson) {
        GeometryJSON json = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        try {
            return json.read(reader);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {
        String wkt = "MULTIPOLYGON (((120.22452128700002 32.566831636000074, 119.50959153000008 32.458417937000036, 119.10844561400006 32.481514920000052, 119.07520520000003 32.452880795000056, 119.03290032700011 32.518119566000053, 118.98780268200005 32.504359306000026, 118.92580064200001 32.56200029300004, 118.88197783500004 32.556636972000035, 118.90400399000009 32.593799503000071, 118.84150398400004 32.569171840000024, 118.81550038400007 32.606095375000052, 118.78620102000002 32.583500968000067, 118.71629154700008 32.617443698000045, 118.62963377800008 32.576794737000057, 118.58571502400002 32.598674052000035, 118.55775861600011 32.572150347000047, 118.61143082800004 32.517253606000054, 118.58500004600012 32.482521182000028, 118.68339829700005 32.473899767000034, 118.69959959900007 32.357428972000037, 118.6510942220001 32.304752081000061, 118.66549809900005 32.246841048000078, 118.61908851200008 32.206496283000035, 118.50160157400001 32.196529386000066, 118.49205625100001 32.122242626000059, 118.37759755900004 32.060101033000024, 118.35780272700003 31.932368262000068, 118.49419916500005 31.844931660000043, 118.47619156700011 31.778659271000038, 118.53849606500012 31.765881300000046, 118.52900400800002 31.736719448000031, 118.62979487300004 31.762350468000079, 118.67610366100007 31.729489533000049, 118.68740250200005 31.708019447000027, 118.63830068100003 31.678359137000029, 118.65019528800008 31.645878210000035, 118.72830076000002 31.630100723000055, 118.76850589800006 31.685778089000053, 118.7929981310001 31.623019805000069, 118.85799801400003 31.619609904000072, 118.85947950000002 31.422155278000048, 118.69570300900011 31.300368343000059, 118.78730479900003 31.233179477000078, 119.09470688200008 31.238702120000028, 119.17759786500005 31.303199921000044, 119.25919903100009 31.252451582000049, 119.33490260100007 31.262639013000069, 119.34818509000002 31.301483340000061, 119.3721972940001 31.268281286000047, 119.35395665800002 31.205916262000073, 119.38459985400004 31.171208997000065, 119.52571624500001 31.162264709000056, 119.57360360200005 31.122031837000065, 119.66410125500011 31.169848178000052, 119.91040045400007 31.172069754000063, 119.98821120900004 31.034763786000042, 120.13388450800005 30.943786881000051, 120.36903284000005 30.952368267000054, 120.3631932620001 30.886669745000063, 120.42290033500001 30.92820167900004, 120.45780278400002 30.812000373000046, 120.50859351600002 30.765439607000076, 120.58419947100003 30.858969566000042, 120.69570283500002 30.871599010000068, 120.6860940470001 30.968411235000076, 120.88569333000009 31.006478505000075, 120.89185233500007 31.08970037000006, 120.84969738700011 31.106550877000075, 120.86799787000007 31.135110616000077, 121.06639648000009 31.160998764000055, 121.05219731900002 31.270338848000051, 121.1508982900001 31.28761946700007, 121.10019507000004 31.35988078400004, 121.15320284300003 31.410571573000027, 121.14160176300004 31.446911913000065, 121.38030260400001 31.547729314000037, 121.09639642000002 31.764149589000056, 121.19348325300007 31.832955883000068, 121.32063385700008 31.858526222000023, 121.44332814900008 31.761568666000073, 121.56265593400008 31.71497322700003, 121.71122903600008 31.721018578000042, 121.87414431600007 31.685496048000061, 121.90816374200006 31.72122825100007, 121.84540803100003 31.933577275000061, 121.72171715000002 32.031693843000028, 121.56881965400009 32.09877743900006, 121.42473532700001 32.110048579000079, 121.43172816000003 32.166039918000024, 121.37774926700001 32.166556932000049, 121.36515666200012 32.238196769000069, 121.40474805600002 32.256168971000079, 121.36896916000001 32.274733171000037, 121.39778190700008 32.321098418000076, 121.37359581600003 32.399037645000078, 121.05824182800006 32.535245714000041, 120.9914879800001 32.520912822000071, 121.0004439600001 32.58107857400006, 120.92536791300006 32.606614395000065, 120.93853697600002 32.645071986000062, 120.90427896300002 32.657330940000065, 120.87331583300011 32.732171156000049, 120.88955477900004 32.783140035000031, 120.22452128700002 32.566831636000074)))";
        JSONObject jsonObject = wktToJson(wkt);
        System.out.println(jsonObject);
    }

}
