package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

public class LayoutTemplateManagerTest {

    @Test
    public void testSaveAndLoadRoundtrip() throws Exception {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("lbl-1", "Titulo de Prueba", 12, 10, 270, 16);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setColor(new Color(0x1B2638));
        label.setZOrder(1);
        label.setName("Titulo");
        model.addElement(label);

        LayoutLegend legend = new LayoutLegend("leg-1", 155, 55, 75, 40);
        legend.setZOrder(2);
        legend.setAutoHeight(true);
        legend.setTitle("Leyenda de Prueba");
        legend.getItems().add(new LayoutLegend.LegendItem("Capa 1", Color.RED, "POLYGON"));
        legend.getItems().add(new LayoutLegend.LegendItem("Capa 2", Color.BLUE, "LINE"));
        model.addElement(legend);

        LayoutMap map = new LayoutMap("map-1", 15, 25, 267, 160);
        map.setZOrder(0);
        map.setName("Mapa principal");
        map.setShowGrid(true);
        map.setGridCols(5);
        map.setGridRows(4);
        map.setGridByDistance(true);
        map.setGridIntervalX(250);
        map.setGridIntervalY(250);
        map.setGridUnit("m");
        map.setTargetScaleDenominator(10000);
        model.addElement(map);

        File tmp = File.createTempFile("test-roundtrip", ".catmap");
        tmp.deleteOnExit();

        LayoutTemplateManager.saveTemplate(tmp, model, 297, 210, "landscape");

        LayoutModel loaded = new LayoutModel();
        LayoutTemplateManager.loadTemplate(tmp, loaded);

        assertEquals(3, loaded.size());
        boolean foundLabel = false, foundLegend = false, foundMap = false;
        for (LayoutElement el : loaded.getElements()) {
            if (el instanceof LayoutLabel) {
                foundLabel = true;
                assertEquals("Titulo de Prueba", ((LayoutLabel)el).getText());
            }
            if (el instanceof LayoutLegend) {
                foundLegend = true;
                assertEquals("Leyenda de Prueba", ((LayoutLegend)el).getTitle());
            }
            if (el instanceof LayoutMap) {
                foundMap = true;
                assertTrue(((LayoutMap)el).isShowGrid());
                assertTrue(((LayoutMap)el).isGridByDistance());
            }
        }
        assertTrue(foundLabel && foundLegend && foundMap);
        tmp.delete();
    }

    @Test
    public void testTemplateList() {
        var templates = LayoutTemplateManager.getTemplateList();
        assertTrue(templates.size() >= 6);
        assertTrue(templates.containsKey("A4_AMBIENTAL"));
        assertTrue(templates.containsKey("A4_TECNICO"));
        assertTrue(templates.containsKey("A3_TECNICO"));
    }

    @Test
    public void testApplyTemplate() {
        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.applyTemplate("A4_AMBIENTAL", model);
        assertTrue(model.size() > 0);
        boolean hasMap = false, hasLegend = false, hasTitle = false;
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutMap) hasMap = true;
            if (el instanceof LayoutLegend) hasLegend = true;
            if (el instanceof LayoutLabel) hasTitle = true;
        }
        assertTrue(hasMap && hasLegend && hasTitle);
    }
}
