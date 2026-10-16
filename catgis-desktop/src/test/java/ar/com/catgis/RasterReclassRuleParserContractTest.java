package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RasterReclassRuleParserContractTest {

    @Test
    void parsesSignedDecimalsScientificNotationAndLabels() {
        List<RasterReclassifyEngine.ReclassRule> rules =
                RasterReclassRuleParser.parse(
                        "-5--1=7=frio; -1e0-+2.5E1=300.25=templado");

        assertEquals(2, rules.size());
        assertEquals(-5.0, rules.get(0).from(), 0.0);
        assertEquals(-1.0, rules.get(0).to(), 0.0);
        assertEquals(7.0, rules.get(0).newValue(), 0.0);
        assertEquals("frio", rules.get(0).label());
        assertEquals(-1.0, rules.get(1).from(), 0.0);
        assertEquals(25.0, rules.get(1).to(), 0.0);
        assertEquals(300.25, rules.get(1).newValue(), 0.0);
        assertEquals("templado", rules.get(1).label());
    }

    @Test
    void rejectsBlankMalformedReversedAndNonfiniteRules() {
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse(""));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse("0-1=2;"));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse("broken"));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse("10-1=2"));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse("0-1=NaN"));
        assertThrows(IllegalArgumentException.class,
                () -> RasterReclassRuleParser.parse("0-1=Infinity"));
    }

    @Test
    void preservesRuleOrderForFirstMatchSemantics() {
        List<RasterReclassifyEngine.ReclassRule> rules =
                RasterReclassRuleParser.parse("0-20=1;20-40=2");

        assertEquals(1.0, rules.get(0).newValue(), 0.0);
        assertEquals(2.0, rules.get(1).newValue(), 0.0);
    }
}
