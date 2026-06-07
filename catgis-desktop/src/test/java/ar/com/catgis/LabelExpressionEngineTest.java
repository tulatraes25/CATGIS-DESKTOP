package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LabelExpressionEngine.
 */
public class LabelExpressionEngineTest {

    private static GeometryFactory GF = new GeometryFactory();
    private static SimpleFeature feature;

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
}
