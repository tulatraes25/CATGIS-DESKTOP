package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedSymbologyTest {

    private Layer layer;
    private SimpleFeature cityFeature;
    private SimpleFeature townFeature;
    private SimpleFeature villageFeature;
    private GeometryFactory gf = new GeometryFactory();

    @BeforeEach
    void setUp() {
        layer = new Layer("test", "/fake/path.shp", "Shapefile");

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("test");
        tb.add("geometry", Point.class);
        tb.add("poblacion", Integer.class);
        tb.add("nombre", String.class);
        tb.add("area", Double.class);
        SimpleFeatureType type = tb.buildFeatureType();

        cityFeature = SimpleFeatureBuilder.build(type, new Object[]{gf.createPoint(new Coordinate(0, 0)), 500000, "Metropolis", 1500.0}, "city1");
        townFeature = SimpleFeatureBuilder.build(type, new Object[]{gf.createPoint(new Coordinate(1, 1)), 25000, "Pueblo", 200.0}, "town1");
        villageFeature = SimpleFeatureBuilder.build(type, new Object[]{gf.createPoint(new Coordinate(2, 2)), 500, "Aldea", 10.0}, "village1");
    }

    @Test
    void testRuleBasedSymbologyNotEnabledByDefault() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        assertFalse(symb.isConfigured());
        assertNull(symb.resolve(cityFeature));
    }

    @Test
    void testSimpleExpressionRule() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule bigCity = new RuleBasedStyleRule("Grandes ciudades");
        bigCity.setFilterExpression("[poblacion] > 100000");
        bigCity.setPrimaryColor(Color.RED);
        bigCity.setPointSize(14);
        symb.addRule(bigCity);

        // City matches
        CategoryStyleRule result = symb.resolve(cityFeature);
        assertNotNull(result);
        assertEquals(Color.RED, result.getPrimaryColor());
        assertEquals(14, result.getPointSize());

        // Town does not match
        assertNull(symb.resolve(townFeature));
    }

    @Test
    void testMultipleRulesWithFirstMatch() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule big = new RuleBasedStyleRule("Grande");
        big.setFilterExpression("[poblacion] > 100000");
        big.setPrimaryColor(Color.RED);
        symb.addRule(big);

        RuleBasedStyleRule medium = new RuleBasedStyleRule("Mediano");
        medium.setFilterExpression("[poblacion] > 10000");
        medium.setPrimaryColor(Color.BLUE);
        symb.addRule(medium); // This would also match city, but city matches first

        RuleBasedStyleRule small = new RuleBasedStyleRule("Pequeño");
        small.setFilterExpression("[poblacion] <= 10000");
        small.setPrimaryColor(Color.GREEN);
        symb.addRule(small);

        // City matches first rule
        assertEquals(Color.RED, symb.resolve(cityFeature).getPrimaryColor());
        // Town matches second rule
        assertEquals(Color.BLUE, symb.resolve(townFeature).getPrimaryColor());
        // Village matches third rule
        assertEquals(Color.GREEN, symb.resolve(villageFeature).getPrimaryColor());
    }

    @Test
    void testElseRule() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule big = new RuleBasedStyleRule("Grande");
        big.setFilterExpression("[poblacion] > 100000");
        big.setPrimaryColor(Color.RED);
        symb.addRule(big);

        RuleBasedStyleRule catchAll = new RuleBasedStyleRule("Otros");
        catchAll.setElseRule(true);
        catchAll.setPrimaryColor(Color.GRAY);
        symb.addRule(catchAll);

        assertEquals(Color.RED, symb.resolve(cityFeature).getPrimaryColor());
        assertEquals(Color.GRAY, symb.resolve(townFeature).getPrimaryColor());
        assertEquals(Color.GRAY, symb.resolve(villageFeature).getPrimaryColor());
    }

    @Test
    void testNestedRules() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        // Parent: all populated places
        RuleBasedStyleRule populated = new RuleBasedStyleRule("Con población");
        populated.setFilterExpression("[poblacion] > 0");
        populated.setPrimaryColor(Color.BLUE);
        populated.setPointSize(8);
        symb.addRule(populated);

        // Child: big cities override style
        RuleBasedStyleRule bigChild = new RuleBasedStyleRule("Grandes");
        bigChild.setFilterExpression("[poblacion] > 100000");
        bigChild.setPrimaryColor(Color.RED);
        bigChild.setPointSize(16);
        populated.addChild(bigChild);

        // Child: medium towns override style
        RuleBasedStyleRule medChild = new RuleBasedStyleRule("Medianos");
        medChild.setFilterExpression("[poblacion] > 10000");
        medChild.setPrimaryColor(Color.ORANGE);
        medChild.setPointSize(12);
        populated.addChild(medChild);

        // City matches parent + big child
        CategoryStyleRule cityResult = symb.resolve(cityFeature);
        assertNotNull(cityResult);
        assertEquals(Color.RED, cityResult.getPrimaryColor());
        assertEquals(16, cityResult.getPointSize());

        // Town matches parent + medium child
        CategoryStyleRule townResult = symb.resolve(townFeature);
        assertNotNull(townResult);
        assertEquals(Color.ORANGE, townResult.getPrimaryColor());
        assertEquals(12, townResult.getPointSize());

        // Village matches parent only (no child matches)
        CategoryStyleRule villageResult = symb.resolve(villageFeature);
        assertNotNull(villageResult);
        assertEquals(Color.BLUE, villageResult.getPrimaryColor());
        assertEquals(8, villageResult.getPointSize());
    }

    @Test
    void testScaleRangeFiltering() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule rule = new RuleBasedStyleRule("Visible only at large scale");
        rule.setFilterExpression("[poblacion] > 0");
        rule.setScaleMin(0);
        rule.setScaleMax(50000); // visible only at 1:0 to 1:50,000
        rule.setPrimaryColor(Color.RED);
        symb.addRule(rule);

        // Within scale range
        assertNotNull(symb.resolve(cityFeature, 25000));
        // Outside scale range
        assertNull(symb.resolve(cityFeature, 100000));
    }

    @Test
    void testStringComparisonFilter() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule rule = new RuleBasedStyleRule("Named places");
        rule.setFilterExpression("[nombre] == \"Metropolis\"");
        rule.setPrimaryColor(Color.MAGENTA);
        symb.addRule(rule);

        assertNotNull(symb.resolve(cityFeature));
        assertNull(symb.resolve(townFeature));
        assertNull(symb.resolve(villageFeature));
    }

    @Test
    void testCompoundExpression() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule rule = new RuleBasedStyleRule("Big cities with large area");
        rule.setFilterExpression("[poblacion] > 100000 && [area] > 1000");
        rule.setPrimaryColor(Color.CYAN);
        symb.addRule(rule);

        // City: pop=500000, area=1500 -> matches
        assertNotNull(symb.resolve(cityFeature));
        // Town: pop=25000, area=200 -> not match
        assertNull(symb.resolve(townFeature));
    }

    @Test
    void testNoFilterRuleMatchesAll() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule rule = new RuleBasedStyleRule("All features");
        rule.setFilterExpression(""); // empty filter = match all
        rule.setPrimaryColor(Color.YELLOW);
        symb.addRule(rule);

        assertNotNull(symb.resolve(cityFeature));
        assertNotNull(symb.resolve(townFeature));
        assertNotNull(symb.resolve(villageFeature));
    }

    @Test
    void testNullFeatureReturnsNull() {
        RuleBasedSymbology symb = layer.getPointRuleBasedSymbology();
        symb.setEnabled(true);

        RuleBasedStyleRule rule = new RuleBasedStyleRule("Test");
        rule.setFilterExpression("[poblacion] > 0");
        symb.addRule(rule);

        assertNull(symb.resolve(null));
    }

    @Test
    void testRuleDeepCopy() {
        RuleBasedStyleRule parent = new RuleBasedStyleRule("Parent");
        parent.setFilterExpression("[poblacion] > 0");
        parent.setPrimaryColor(Color.RED);
        parent.setPointSize(12);

        RuleBasedStyleRule child = new RuleBasedStyleRule("Child");
        child.setFilterExpression("[poblacion] > 100000");
        child.setPrimaryColor(Color.BLUE);
        parent.addChild(child);

        RuleBasedStyleRule copy = parent.copy();

        // Check values
        assertEquals(parent.getDescription(), copy.getDescription());
        assertEquals(parent.getFilterExpression(), copy.getFilterExpression());
        assertEquals(Color.RED, copy.getPrimaryColor());
        assertEquals(12, copy.getPointSize());

        // Check children were copied
        assertEquals(1, copy.getChildren().size());
        assertEquals("Child", copy.getChildren().get(0).getDescription());
        assertEquals(Color.BLUE, copy.getChildren().get(0).getPrimaryColor());

        // Verify deep copy (modifying child doesn't affect original)
        copy.getChildren().get(0).setPrimaryColor(Color.GREEN);
        assertEquals(Color.BLUE, parent.getChildren().get(0).getPrimaryColor());
    }

    @Test
    void testTotalRuleCount() {
        RuleBasedStyleRule r1 = new RuleBasedStyleRule("R1");
        RuleBasedStyleRule r2 = new RuleBasedStyleRule("R2");
        RuleBasedStyleRule r2a = new RuleBasedStyleRule("R2a");
        r2.addChild(r2a);
        RuleBasedStyleRule r2b = new RuleBasedStyleRule("R2b");
        r2.addChild(r2b);
        RuleBasedStyleRule r3 = new RuleBasedStyleRule("R3");
        RuleBasedStyleRule r3a = new RuleBasedStyleRule("R3a");
        r3a.addChild(new RuleBasedStyleRule("R3a1"));
        r3.addChild(r3a);

        RuleBasedSymbology symb = new RuleBasedSymbology();
        symb.setEnabled(true);
        symb.addRule(r1);
        symb.addRule(r2);
        symb.addRule(r3);

        // R1(1) + R2(1) + R2a(1) + R2b(1) + R3(1) + R3a(1) + R3a1(1) = 7
        assertEquals(7, symb.totalRuleCount());
    }
}
