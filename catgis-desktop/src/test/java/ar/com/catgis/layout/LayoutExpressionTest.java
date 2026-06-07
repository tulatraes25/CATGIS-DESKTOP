package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LayoutExpressionTest {

    @Test
    public void testScaleExpression() {
        String result = LayoutExpressionEvaluator.evaluate("Escala @scale", 25000, "Proj", 0, 1);
        assertTrue(result.contains("1:25"), "Expected scale to be replaced, got: " + result);
        assertFalse(result.contains("@scale"), "@scale should be replaced");
    }

    @Test
    public void testDateExpression() {
        String result = LayoutExpressionEvaluator.evaluate("Fecha: @date", 1000, "P", 0, 1);
        assertTrue(result.contains("Fecha: "));
        assertFalse(result.contains("@date"));
    }

    @Test
    public void testProjectExpression() {
        String result = LayoutExpressionEvaluator.evaluate("Proyecto @project", 1000, "CATGIS", 0, 1);
        assertEquals("Proyecto CATGIS", result);
    }

    @Test
    public void testPageExpression() {
        String result = LayoutExpressionEvaluator.evaluate("Pag @page de @pagetotal", 1000, "P", 0, 5);
        assertEquals("Pag 1 de 5", result);
    }

    @Test
    public void testPageExpressionMiddle() {
        String result = LayoutExpressionEvaluator.evaluate("@page/@pagetotal", 1000, "P", 2, 10);
        assertEquals("3/10", result);
    }

    @Test
    public void testNullExpression() {
        assertEquals("", LayoutExpressionEvaluator.evaluate(null, 1, null, 0, 1));
    }

    @Test
    public void testEmptyExpression() {
        assertEquals("", LayoutExpressionEvaluator.evaluate("", 1, null, 0, 1));
    }

    @Test
    public void testAtlasRendererInterface() {
        LayoutAtlasRenderer.PageRenderer pr = (name, idx, ctx) -> null;
        assertNotNull(pr);
    }
}
