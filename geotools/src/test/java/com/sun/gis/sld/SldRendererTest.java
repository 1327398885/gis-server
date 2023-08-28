package com.sun.gis.sld;

import com.sun.gis.tools.sld.SldRenderer;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author sunbt
 * @date 2023/8/26 22:17
 */
@SpringBootTest
public class SldRendererTest {

    @Test
    public void test() {
        String path = "E:\\code\\github\\gis-server\\data\\sld\\model_1.1.0_320205.sld";
        Style style = SldRenderer.readSldReturnOne(path);
        // 加载 Shapefile
        File file = new File("E:\\code\\github\\gis-server\\data\\shapefile\\320205_county.shp");
        URL shapefileURL = null;
        try {
            shapefileURL = file.toURI().toURL();
            ShapefileDataStore dataStore = new ShapefileDataStore(shapefileURL);
            SimpleFeatureSource featureSource = dataStore.getFeatureSource();

            // 应用样式并添加到 map content
            MapContent mapContent = new MapContent();
            mapContent.setTitle("Your Map Title");
            FeatureLayer layer = new FeatureLayer(featureSource, style);
            mapContent.addLayer(layer);

            // 显示地图内容
//            JMapFrame.showMap(mapContent);

            // 注意：最后，确保你关闭了 dataStore
            dataStore.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
