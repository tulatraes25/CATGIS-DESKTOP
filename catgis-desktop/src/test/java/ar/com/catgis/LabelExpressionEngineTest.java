package ar.com.catgis;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.LinearRing;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LabelExpressionEngine.
 */
public class LabelExpressionEngineTest {

    private static GeometryFactory GF = new GeometryFactory();
    private static SimpleFeature feature;
    private static SimpleFeature polygonFeature;
    private static SimpleFeature lineFeature;

    @BeforeAll
    static void setup() {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("test");
        tb.add("geometry", Point.class);
        tb.add("name", String.class);
        tb.add("code", String.class);
        tb.add("value", Integer.class);
        tb.add("area_ha", Double.class);
        tb.add("status", String.class);
        tb.setDefaultGeometry("geometry");

        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(tb.buildFeatureType());
        fb.set("name", "Lote A");
        fb.set("code", "LA-001");
        fb.set("value", 42);
        fb.set("area_ha", 15.5);
        fb.set("status", "active");
        fb.set("geometry", GF.createPoint(new Coordinate(100, 200)));
        feature = fb.buildFeature("1");

        SimpleFeatureTypeBuilder ptb = new SimpleFeatureTypeBuilder();
        ptb.setName("poly_test");
        ptb.add("geometry", Polygon.class);
        ptb.add("name", String.class);
        ptb.setDefaultGeometry("geometry");
        SimpleFeatureBuilder pfb = new SimpleFeatureBuilder(ptb.buildFeatureType());
        LinearRing shell = GF.createLinearRing(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10),
                new Coordinate(0, 10), new Coordinate(0, 0)});
        pfb.set("geometry", GF.createPolygon(shell));
        pfb.set("name", "Square");
        polygonFeature = pfb.buildFeature("2");

        SimpleFeatureTypeBuilder ltb = new SimpleFeatureTypeBuilder();
        ltb.setName("line_test");
        ltb.add("geometry", LineString.class);
        ltb.add("name", String.class);
        ltb.setDefaultGeometry("geometry");
        SimpleFeatureBuilder lfb = new SimpleFeatureBuilder(ltb.buildFeatureType());
        lfb.set("geometry", GF.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 5), new Coordinate(10, 0)}));
        lfb.set("name", "VLine");
        lineFeature = lfb.buildFeature("3");
    }

    @Test
    public void testSimpleFieldRef() {
        assertEquals("Lote A", LabelExpressionEngine.evaluate("[name]", feature));
        assertEquals("LA-001", LabelExpressionEngine.evaluate("[code]", feature));
        assertEquals("42", LabelExpressionEngine.evaluate("[value]", feature));
    }

    @Test
    public void testSimpleFieldRefWithNumericField() {
        assertEquals("15.5", LabelExpressionEngine.evaluate("[area_ha]", feature));
    }

    @Test
    public void testNullExpression() {
        assertNull(LabelExpressionEngine.evaluate(null, feature));
    }

    @Test
    public void testBlankExpression() {
        assertNull(LabelExpressionEngine.evaluate("   ", feature));
    }

    @Test
    public void testConcatenation() {
        assertEquals("Lote A (LA-001)",
                LabelExpressionEngine.evaluate("[name] || \" (\" || [code] || \")\"", feature));
    }

    @Test
    public void testStringConcatWithLiteral() {
        assertEquals("Código: LA-001",
                LabelExpressionEngine.evaluate("\"Código: \" || [code]", feature));
    }

    @Test
    public void testUpperFunction() {
        assertEquals("LOTE A", LabelExpressionEngine.evaluate("upper([name])", feature));
    }

    @Test
    public void testLowerFunction() {
        assertEquals("lote a", LabelExpressionEngine.evaluate("lower([name])", feature));
    }

    @Test
    public void testTrimFunction() {
        assertEquals("hello", LabelExpressionEngine.evaluate("trim(\"  hello  \")", feature));
    }

    @Test
    public void testSubstrFunction() {
        assertEquals("Lote", LabelExpressionEngine.evaluate("substr([name], 0, 4)", feature));
        assertEquals("A", LabelExpressionEngine.evaluate("substr([name], 5, 1)", feature));
    }

    @Test
    public void testReplaceFunction() {
        assertEquals("Lote B", LabelExpressionEngine.evaluate("replace([name], \"A\", \"B\")", feature));
    }

    @Test
    public void testRoundFunction() {
        assertEquals("3", LabelExpressionEngine.evaluate("str(round(3.14))", feature));
        assertEquals("3.14", LabelExpressionEngine.evaluate("str(round(3.14159, 2))", feature));
    }

    @Test
    public void testFloorCeilAbs() {
        assertTrue(evalNum("floor(3.9)", feature) == 3.0);
        assertTrue(evalNum("ceil(3.1)", feature) == 4.0);
        assertTrue(evalNum("abs(-5)", feature) == 5.0);
    }

    @Test
    public void testFormatFunction() {
        // format uses US locale DecimalFormat
        String result = LabelExpressionEngine.evaluate("format([value], \"000\")", feature);
        assertEquals("042", result);
    }

    @Test
    public void testIfFunction() {
        assertEquals("Alto", LabelExpressionEngine.evaluate("if([value] > 40, \"Alto\", \"Bajo\")", feature));
        assertEquals("Bajo", LabelExpressionEngine.evaluate("if([value] < 10, \"Alto\", \"Bajo\")", feature));
    }

    @Test
    public void testNumFunction() {
        assertEquals("42", LabelExpressionEngine.evaluate("str(num([value]))", feature));
    }

    @Test
    public void testStrFunction() {
        assertEquals("42", LabelExpressionEngine.evaluate("str([value])", feature));
    }

    @Test
    public void testMinMaxFunctions() {
        assertEquals("10", LabelExpressionEngine.evaluate("str(min(10, 20, 30))", feature));
        assertEquals("30", LabelExpressionEngine.evaluate("str(max(10, 20, 30))", feature));
    }

    @Test
    public void testArithmetic() {
        assertEquals("52", LabelExpressionEngine.evaluate("str([value] + 10)", feature));
        assertEquals("32", LabelExpressionEngine.evaluate("str([value] - 10)", feature));
        assertEquals("84", LabelExpressionEngine.evaluate("str([value] * 2)", feature));
        assertEquals("21", LabelExpressionEngine.evaluate("str([value] / 2)", feature));
    }

    @Test
    public void testComplexExpression() {
        // "[name] || ": " || round([area_ha] * 100) || " m²""
        String result = LabelExpressionEngine.evaluate(
                "[name] || \": \" || round([area_ha] * 100) || \" m²\"", feature);
        // area_ha = 15.5, *100 = 1550, round = 1550.0
        assertEquals("Lote A: 1550 m²", result);
    }

    @Test
    public void testComparisonEquals() {
        // [status] == "active" should be true
        assertEquals("yes", LabelExpressionEngine.evaluate(
                "if([status] == \"active\", \"yes\", \"no\")", feature));
    }

    @Test
    public void testComparisonNotEquals() {
        assertEquals("yes", LabelExpressionEngine.evaluate(
                "if([status] != \"inactive\", \"yes\", \"no\")", feature));
    }

    @Test
    public void testFieldNotFound() {
        assertEquals("", LabelExpressionEngine.evaluate("[nonexistent]", feature));
    }

    @Test
    public void testExpressionExceptionOnUnclosedBracket() {
        assertThrows(LabelExpressionEngine.ExpressionException.class, () -> {
            LabelExpressionEngine.evaluate("[name", feature);
        });
    }

    @Test
    public void testExpressionExceptionOnUnclosedString() {
        assertThrows(LabelExpressionEngine.ExpressionException.class, () -> {
            LabelExpressionEngine.evaluate("\"hello", feature);
        });
    }

    @Test
    public void testComplexConcatenation() {
        // Build: "LA-001 - Lote A (42)"
        String result = LabelExpressionEngine.evaluate(
                "[code] || \" - \" || [name] || \" (\" || [value] || \")\"", feature);
        assertEquals("LA-001 - Lote A (42)", result);
    }

    @Test
    public void testGeometryFunctions() {
        // Create a feature with a polygon for area/length/perimeter tests
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("test_geom");
        tb.add("geometry", Polygon.class);
        tb.setDefaultGeometry("geometry");

        Polygon polygon = GF.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(10, 0),
                new Coordinate(10, 10), new Coordinate(0, 10),
                new Coordinate(0, 0)
        });

        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(tb.buildFeatureType());
        fb.set("geometry", polygon);
        SimpleFeature polyFeature = fb.buildFeature("1");

        // Area of 10x10 square = 100
        double area = LabelExpressionEngine.evaluateNumeric("area()", polyFeature);
        assertEquals(100.0, area, 0.001);

        // Perimeter = 40
        double perimeter = LabelExpressionEngine.evaluateNumeric("perimeter()", polyFeature);
        assertEquals(40.0, perimeter, 0.001);
    }

    @Test
    public void testStringComparison() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "str([name] == \"Lote A\")", feature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "str([name] == \"Lote B\")", feature));
    }

    @Test
    public void testNestedFunctionCalls() {
        assertEquals("LOTE", LabelExpressionEngine.evaluate(
                "upper(substr([name], 0, 4))", feature));
    }

    // Helper to evaluate numeric expressions
    private static double evalNum(String expr, SimpleFeature f) {
        String result = LabelExpressionEngine.evaluate("str(" + expr + ")", f);
        try {
            return Double.parseDouble(result);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // --- New expression function tests ---

    @Test
    public void testHaversine() {
        double result = evalNum("haversine(0, 0, 0, 1)", feature);
        assertTrue(result > 111000 && result < 112000,
                "haversine(0,0,0,1) should be ~111km: " + result);
    }

    @Test
    public void testAzimuth() {
        double result = evalNum("azimuth(0, 0, 1, 0)", feature);
        assertEquals(90.0, result, 1.0, "azimuth east should be ~90 degrees");
    }

    @Test
    public void testDestpoint() {
        String result = LabelExpressionEngine.evaluate(
                "destpoint(0, 100000, 0, 0)", feature);
        assertNotNull(result);
        assertTrue(result.contains(","), "destpoint should return 'lon,lat': " + result);
    }

    @Test
    public void testConvexHull() {
        String result = LabelExpressionEngine.evaluate(
                "convexhull()", polygonFeature);
        assertNotNull(result);
        assertTrue(result.contains("POLYGON") || result.contains("POINT"),
                "convexhull should return geometry WKT: " + result);
    }

    @Test
    public void testCentroidDistance() {
        double result = evalNum("centroid_distance()", polygonFeature);
        assertTrue(result > 0, "centroid_distance from origin should be > 0: " + result);
    }

    @Test
    public void testBoundary() {
        String result = LabelExpressionEngine.evaluate("boundary()", polygonFeature);
        assertNotNull(result);
        assertTrue(result.contains("LINESTRING") || result.contains("LINEARRING"),
                "boundary of polygon should be linear ring: " + result);
    }

    @Test
    public void testConvexHullArea() {
        double result = evalNum("convex_hull_area()", polygonFeature);
        assertEquals(100.0, result, 0.1, "10x10 square area should be 100");
    }

    @Test
    public void testOrientedArea() {
        double result = evalNum("oriented_area()", polygonFeature);
        assertTrue(Math.abs(result) > 0, "oriented_area should be non-zero: " + result);
    }

    @Test
    public void testSinAngle() {
        double result = evalNum("sin_angle(30)", feature);
        assertEquals(0.5, result, 0.01, "sin(30deg) = 0.5");
    }

    @Test
    public void testCosAngle() {
        double result = evalNum("cos_angle(60)", feature);
        assertEquals(0.5, result, 0.01, "cos(60deg) = 0.5");
    }

    @Test
    public void testTanAngle() {
        double result = evalNum("tan_angle(45)", feature);
        assertEquals(1.0, result, 0.01, "tan(45deg) = 1.0");
    }

    @Test
    public void testSmooth() {
        String result = LabelExpressionEngine.evaluate("smooth(1)", polygonFeature);
        assertNotNull(result);
        assertTrue(result.contains("POLYGON"), "smooth should return polygon WKT: " + result);
    }

    @Test
    public void testVoronoi() {
        String result = LabelExpressionEngine.evaluate("voronoi()", polygonFeature);
        assertNotNull(result);
        assertTrue(result.length() > 10, "voronoi should return non-empty WKT");
    }

    @Test
    public void testConcaveHull() {
        String result = LabelExpressionEngine.evaluate("concave_hull(0.5)", polygonFeature);
        assertNotNull(result);
        assertTrue(result.length() > 10, "concave_hull should return geometry WKT");
    }

    @Test
    public void testIsSimple() {
        assertEquals("true", LabelExpressionEngine.evaluate("is_simple()", polygonFeature));
    }

    @Test
    public void testIsClosed() {
        SimpleFeatureTypeBuilder ctb = new SimpleFeatureTypeBuilder();
        ctb.setName("closed_line_test");
        ctb.add("geometry", LineString.class);
        ctb.add("name", String.class);
        ctb.setDefaultGeometry("geometry");
        SimpleFeatureBuilder cfb = new SimpleFeatureBuilder(ctb.buildFeatureType());
        cfb.set("geometry", GF.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 5), new Coordinate(0, 0)}));
        cfb.set("name", "ClosedLine");
        SimpleFeature closedLine = cfb.buildFeature("4");
        assertEquals("true", LabelExpressionEngine.evaluate("is_closed()", closedLine));
        assertEquals("false", LabelExpressionEngine.evaluate("is_closed()", lineFeature));
    }

    @Test
    public void testXMin() {
        double result = evalNum("x_min()", polygonFeature);
        assertEquals(0.0, result, 0.1, "x_min of 0-10 square");
    }

    @Test
    public void testXMax() {
        double result = evalNum("x_max()", polygonFeature);
        assertEquals(10.0, result, 0.1, "x_max of 0-10 square");
    }

    @Test
    public void testYMin() {
        double result = evalNum("y_min()", polygonFeature);
        assertEquals(0.0, result, 0.1, "y_min of 0-10 square");
    }

    @Test
    public void testYMax() {
        double result = evalNum("y_max()", polygonFeature);
        assertEquals(10.0, result, 0.1, "y_max of 0-10 square");
    }

    @Test
    public void testEnvelopeWidth() {
        double result = evalNum("envelope_width()", polygonFeature);
        assertEquals(10.0, result, 0.1, "width of 0-10 square");
    }

    @Test
    public void testEnvelopeHeight() {
        double result = evalNum("envelope_height()", polygonFeature);
        assertEquals(10.0, result, 0.1, "height of 0-10 square");
    }

    @Test
    public void testRelate() {
        String result = LabelExpressionEngine.evaluate(
                "relate(\"POLYGON((0 0, 5 0, 5 5, 0 5, 0 0))\")", polygonFeature);
        assertNotNull(result);
        assertEquals(9, result.length(), "DE-9IM string should be 9 chars");
    }

    @Test
    public void testWithinDistance() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "within_distance(1, \"POINT(1 1)\")", polygonFeature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "within_distance(0.1, \"POINT(100 100)\")", polygonFeature));
    }

    @Test
    public void testOverlaps() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "overlaps(\"POLYGON((5 5, 15 5, 15 15, 5 15, 5 5))\")", polygonFeature));
    }

    @Test
    public void testTouches() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "touches(\"POLYGON((10 0, 20 0, 20 10, 10 10, 10 0))\")", polygonFeature));
    }

    @Test
    public void testCrosses() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "crosses(\"LINESTRING(-1 5, 11 5)\")", polygonFeature));
    }

    @Test
    public void testDisjoint() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "disjoint(\"POLYGON((50 50, 60 50, 60 60, 50 60, 50 50))\")", polygonFeature));
    }

    @Test
    public void testGeomContains() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "geom_contains(\"POINT(5 5)\")", polygonFeature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "geom_contains(\"POINT(50 50)\")", polygonFeature));
    }

    @Test
    public void testIntersects() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "intersects(\"POLYGON((1 1, 9 1, 9 9, 1 9, 1 1))\")", polygonFeature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "intersects(\"POLYGON((50 50, 60 50, 60 60, 50 60, 50 50))\")", polygonFeature));
    }

    @Test
    public void testIntersection() {
        String result = LabelExpressionEngine.evaluate(
                "intersection(\"POLYGON((1 1, 9 1, 9 9, 1 9, 1 1))\")", polygonFeature);
        assertNotNull(result);
        assertTrue(result.contains("POLYGON") || result.contains("GEOMETRYCOLLECTION"),
                "intersection should return geometry: " + result);
    }

    @Test
    public void testUnionGeom() {
        String result = LabelExpressionEngine.evaluate(
                "union_geom(\"POLYGON((1 1, 9 1, 9 9, 1 9, 1 1))\")", polygonFeature);
        assertNotNull(result);
        assertTrue(result.length() > 10, "union_geom should return non-empty WKT");
    }

    @Test
    public void testDifference() {
        String result = LabelExpressionEngine.evaluate(
                "difference(\"POLYGON((1 1, 9 1, 9 9, 1 9, 1 1))\")", polygonFeature);
        assertNotNull(result);
        assertTrue(result.contains("POLYGON") || result.contains("GEOMETRYCOLLECTION"),
                "difference should return geometry: " + result);
    }

    @Test
    public void testClip() {
        String result = LabelExpressionEngine.evaluate(
                "clip(\"POLYGON((1 1, 9 1, 9 9, 1 9, 1 1))\")", polygonFeature);
        assertNotNull(result);
        assertTrue(result.length() > 10, "clip should return non-empty WKT");
    }

    @Test
    public void testEqualsExact() {
        assertEquals("true", LabelExpressionEngine.evaluate(
                "equals_exact(0.01, \"POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))\")", polygonFeature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "equals_exact(0.01, \"POLYGON((0 0, 5 0, 5 5, 0 5, 0 0))\")", polygonFeature));
    }

    @Test
    public void testWktRelationalOnNullGeometry() {
        String result = LabelExpressionEngine.evaluate(
                "intersects(\"POINT(0 0)\")", feature);
        assertNotNull(result);
    }

    @Test
    public void testAbsVal() {
        assertEquals("5", LabelExpressionEngine.evaluate("str(abs_val(-5))", feature));
        assertEquals("5", LabelExpressionEngine.evaluate("str(abs_val(5))", feature));
    }

    @Test
    public void testSignum() {
        assertEquals("-1", LabelExpressionEngine.evaluate("str(signum(-3))", feature));
        assertEquals("1", LabelExpressionEngine.evaluate("str(signum(3))", feature));
        assertEquals("0", LabelExpressionEngine.evaluate("str(signum(0))", feature));
    }

    @Test
    public void testExp2() {
        double result = evalNum("exp2(3)", feature);
        assertEquals(8.0, result, 0.01);
    }

    @Test
    public void testLog2() {
        double result = evalNum("log2(8)", feature);
        assertEquals(3.0, result, 0.01);
    }

    @Test
    public void testCbrt() {
        double result = evalNum("cbrt(27)", feature);
        assertEquals(3.0, result, 0.01);
    }

    @Test
    public void testToDegrees() {
        double result = evalNum("to_degrees(3.14159)", feature);
        assertEquals(180.0, result, 1.0);
    }

    @Test
    public void testToRadians() {
        double result = evalNum("to_radians(180)", feature);
        assertEquals(Math.PI, result, 0.01);
    }

    @Test
    public void testLerp() {
        double result = evalNum("lerp(0, 10, 0.5)", feature);
        assertEquals(5.0, result, 0.01);
    }

    // --- Phase 0: regex, timezone, cot/sec/csc ---

    @Test
    public void testRegexMatch() {
        // Simple literal match (no backslash); subject first, pattern second
        assertEquals("true", LabelExpressionEngine.evaluate(
                "regex_match(\"hello\", \"hello\")", feature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "regex_match(\"hello\", \"world\")", feature));
    }

    @Test
    public void testRegexMatchDigits() {
        // regex_match(subject, pattern) — subject first
        assertEquals("true", LabelExpressionEngine.evaluate(
                "regex_match(\"123-4567\", \"[0-9]{3}-[0-9]{4}\")", feature));
        assertEquals("false", LabelExpressionEngine.evaluate(
                "regex_match(\"abc-defg\", \"[0-9]{3}-[0-9]{4}\")", feature));
    }

    @Test
    public void testRegexReplace() {
        // regex_replace(subject, pattern, replacement) — subject first
        // replaceAll replaces ALL matches, so [0-9]{3} matches both 123 and 456
        assertEquals("XXX-XXX7", LabelExpressionEngine.evaluate(
                "regex_replace(\"123-4567\", \"[0-9]{3}\", \"XXX\")", feature));
        assertEquals("hello world", LabelExpressionEngine.evaluate(
                "regex_replace(\"hello   world\", \" +\", \" \")", feature));
    }

    @Test
    public void testCot() {
        double result = evalNum("cot(45)", feature);
        assertEquals(1.0, result, 0.01);
    }

    @Test
    public void testSec() {
        double result = evalNum("sec(60)", feature);
        assertEquals(2.0, result, 0.01);
    }

    @Test
    public void testCsc() {
        double result = evalNum("csc(30)", feature);
        assertEquals(2.0, result, 0.01);
    }

    @Test
    public void testTimezoneOffset() {
        double result = evalNum("timezone_offset()", feature);
        // offset is platform-dependent; just verify it returns a finite value
        assertTrue(Double.isFinite(result), "timezone_offset should return a finite number");
    }

    @Test
    public void testTimezoneName() {
        String result = LabelExpressionEngine.evaluate("timezone_name()", feature);
        assertNotNull(result);
        assertFalse(result.isEmpty(), "timezone_name should return a non-empty string");
    }
}
