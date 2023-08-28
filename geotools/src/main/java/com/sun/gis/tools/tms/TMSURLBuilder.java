package com.sun.gis.tools.tms;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author sunbt
 * @date 2023/8/27 20:21
 */
public class TMSURLBuilder {
    private final String baseURL;

    public TMSURLBuilder(String baseURL) {
        this.baseURL = baseURL;
    }

    public URL getTileURL(int x, int y, int z) throws MalformedURLException {
        String url = baseURL.replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));
        return new URL(url);
    }
}

