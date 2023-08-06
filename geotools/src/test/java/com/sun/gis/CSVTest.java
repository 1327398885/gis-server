package com.sun.gis;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import com.csvreader.CsvReader;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class CSVTest {

    @Test
    public void test() throws Exception {
        List<String> cities = new ArrayList<>();
        URL url = CSVTest.class.getResource("locations.csv");
        File file = new File(url.toURI());
        try (FileReader reader = new FileReader(file)) {
            CsvReader locations = new CsvReader(reader);
            locations.readHeaders();
            while (locations.readRecord()) {
                cities.add(locations.get("CITY"));
            }
        }
        assertTrue(cities.contains("Victoria"));
    }
    

}