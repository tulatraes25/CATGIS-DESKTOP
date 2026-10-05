package ar.com.catgis.sld;

import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.RuleBasedStyleRule;
import ar.com.catgis.RuleBasedSymbology;
import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SldSupportTest {

    @TempDir
    Path tempDir;

    @Test
    void exportToSldReturnsValidXml() {
        Layer layer = new Layer("Capa Test", "", "SHAPEFILE");
        String sld = SldSupport.exportToSld(layer);
        assertNotNull(sld);
        assertTrue(sld.contains("<?xml"));
        assertTrue(sld.contains("StyledLayerDescriptor"));
        assertTrue(sld.contains("Capa Test"));
        assertTrue(sld.contains("</StyledLayerDescriptor>"));
    }

    @Test
    void exportNullLayerReturnsEmpty() {
        assertEquals("", SldSupport.exportToSld(null));
    }

    @Test
    void exportWithRulesIncludesSymbology() {
        Layer layer = new Layer("Rios", "", "SHAPEFILE");
        RuleBasedStyleRule rule = new RuleBasedStyleRule("principal");
        rule.setPrimaryColor(new Color(51, 136, 255));
        rule.setSecondaryColor(new Color(30, 41, 59));
        rule.setLineWidth(2.5f);
        rule.setPointSize(10);

        RuleBasedSymbology symb = layer.getLineRuleBasedSymbology();
        symb.addRule(rule);

        String sld = SldSupport.exportToSld(layer);
        assertTrue(sld.contains("<Rule>"));
        assertTrue(sld.contains("principal"));
        assertTrue(sld.contains("3388ff")); // primary color hex
        assertTrue(sld.contains("LineSymbolizer"));
    }

    @Test
    void exportWithPolygonRules() {
        Layer layer = new Layer("Zonas", "", "SHAPEFILE");
        RuleBasedStyleRule rule = new RuleBasedStyleRule("residencial");
        rule.setPrimaryColor(new Color(170, 221, 255));
        RuleBasedSymbology symb = layer.getPolygonRuleBasedSymbology();
        symb.addRule(rule);

        String sld = SldSupport.exportToSld(layer);
        assertTrue(sld.contains("PolygonSymbolizer"));
        assertTrue(sld.contains("aaddff")); // fill color hex
    }

    @Test
    void importFromSldParsesRules() {
        String sld = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.1.0\"\n"
                + "  xmlns=\"http://www.opengis.net/sld\">\n"
                + "  <NamedLayer>\n"
                + "    <Name>Test</Name>\n"
                + "    <UserStyle>\n"
                + "      <FeatureTypeStyle>\n"
                + "        <Rule>\n"
                + "          <Name>rule1</Name>\n"
                + "          <LineSymbolizer>\n"
                + "            <Stroke>\n"
                + "              <CssParameter name=\"stroke\">#ff0000</CssParameter>\n"
                + "              <CssParameter name=\"stroke-width\">3</CssParameter>\n"
                + "            </Stroke>\n"
                + "          </LineSymbolizer>\n"
                + "          <PointSymbolizer>\n"
                + "            <Graphic>\n"
                + "              <Mark><WellKnownName>circle</WellKnownName></Mark>\n"
                + "              <Size>8</Size>\n"
                + "            </Graphic>\n"
                + "          </PointSymbolizer>\n"
                + "        </Rule>\n"
                + "      </FeatureTypeStyle>\n"
                + "    </UserStyle>\n"
                + "  </NamedLayer>\n"
                + "</StyledLayerDescriptor>";

        List<CategoryStyleRule> rules = SldSupport.importFromSld(sld);
        assertNotNull(rules);
        assertEquals(1, rules.size());
        assertEquals("rule1", rules.get(0).getValue());
        assertEquals(Color.RED, rules.get(0).getSecondaryColor()); // stroke color
        assertEquals(3.0f, rules.get(0).getLineWidth(), 0.01);
        assertEquals(8, rules.get(0).getPointSize());
    }

    @Test
    void importEmptySldReturnsEmpty() {
        assertTrue(SldSupport.importFromSld("").isEmpty());
        assertTrue(SldSupport.importFromSld(null).isEmpty());
    }

    @Test
    void roundTripPreservesRules() throws Exception {
        Layer layer = new Layer("RoundTrip", "", "SHAPEFILE");
        RuleBasedStyleRule rule = new RuleBasedStyleRule("categoria_a");
        rule.setPrimaryColor(Color.BLUE);
        rule.setSecondaryColor(Color.BLACK);
        rule.setLineWidth(1.5f);
        rule.setPointSize(6);
        layer.getPolygonRuleBasedSymbology().addRule(rule);

        String exported = SldSupport.exportToSld(layer);
        List<CategoryStyleRule> imported = SldSupport.importFromSld(exported);

        assertEquals(1, imported.size());
        assertEquals("categoria_a", imported.get(0).getValue());
        // Stroke color survives roundtrip via LineSymbolizer
        assertEquals(Color.BLACK, imported.get(0).getSecondaryColor());
        assertEquals(1.5f, imported.get(0).getLineWidth(), 0.01);
        assertEquals(6, imported.get(0).getPointSize());
    }

    @Test
    void exportToFileWritesSld() throws Exception {
        Layer layer = new Layer("Archivo", "", "SHAPEFILE");
        File output = tempDir.resolve("test.sld").toFile();
        SldSupport.exportToFile(layer, output);

        assertTrue(output.exists());
        assertTrue(output.length() > 100);
        String content = java.nio.file.Files.readString(output.toPath());
        assertTrue(content.contains("StyledLayerDescriptor"));
    }
}
