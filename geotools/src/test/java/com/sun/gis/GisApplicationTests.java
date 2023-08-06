package com.sun.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class GisApplicationTests {

    @Test
    void Test() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Polygon polygon1 = geometryFactory.createPolygon(new CoordinateArraySequence(new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(10, 10),
                new Coordinate(0, 10),
                new Coordinate(0, 0)
        }));

        Polygon polygon2 = geometryFactory.createPolygon(new CoordinateArraySequence(new Coordinate[] {
                new Coordinate(10, 10),
                new Coordinate(20, 10),
                new Coordinate(20, 20),
                new Coordinate(10, 20),
                new Coordinate(10, 10)
        }));

        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[] { polygon1, polygon2 });

        Point point = geometryFactory.createPoint(new Coordinate(7, 7));

        System.out.println(multiPolygon.contains(point)); // prints true
    }

}
