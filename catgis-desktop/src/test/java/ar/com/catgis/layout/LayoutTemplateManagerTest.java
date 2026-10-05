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
        assertTrue(templates.size() >= 70);
        assertTrue(templates.containsKey("A4_AMBIENTAL"));
        assertTrue(templates.containsKey("A4_TECNICO"));
        assertTrue(templates.containsKey("A3_TECNICO"));
        assertTrue(templates.containsKey("A4_REFERENCIA"));
        assertTrue(templates.containsKey("A4_ACCESIBILIDAD"));
        assertTrue(templates.containsKey("A4_EMPLAZAMIENTO"));
        assertTrue(templates.containsKey("A4_PERFIL"));
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

    @Test
    public void testReferenciaTemplate() {
        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.applyTemplate("A4_REFERENCIA", model);
        assertTrue(model.size() >= 6);
        boolean hasCartouche = false;
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutCartouche) hasCartouche = true;
        }
        assertTrue(hasCartouche);
    }

    @Test
    public void testAccesibilidadTemplate() {
        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.applyTemplate("A4_ACCESIBILIDAD", model);
        assertTrue(model.size() >= 6);
    }

    @Test
    public void testEmplazamientoTemplate() {
        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.applyTemplate("A4_EMPLAZAMIENTO", model);
        assertTrue(model.size() >= 6);
    }

    @Test
    public void testPerfilTemplate() {
        LayoutModel model = new LayoutModel();
        LayoutTemplateManager.applyTemplate("A4_PERFIL", model);
        assertTrue(model.size() >= 6);
        boolean hasTable = false;
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutTable) hasTable = true;
        }
        assertTrue(hasTable);
    }

    @Test
    public void testAllNewA4Templates() {
        String[] a4 = {"A4_TECNICO_INFERIOR","A4_CATASTRAL","A4_HIDROLOGIA","A4_TOPOGRAFIA","A4_URBANO","A4_PARCELARIO","A4_INFRAESTRUCTURA"};
        for (String key : a4) {
            LayoutModel model = new LayoutModel();
            LayoutTemplateManager.applyTemplate(key, model);
            assertTrue(model.size() >= 3, key + " should have >= 3 elements");
            boolean hasMap = false;
            for (LayoutElement el : model.getElements()) if (el instanceof LayoutMap) hasMap = true;
            assertTrue(hasMap, key + " should have a map");
        }
    }

    @Test
    public void testAllNewA3Templates() {
        String[] a3 = {"A3_AMBIENTAL","A3_CATASTRAL","A3_SATELITAL","A3_PARCELARIO","A3_HIDROLOGIA","A3_TOPOGRAFIA","A3_PRESENTACION"};
        for (String key : a3) {
            LayoutModel model = new LayoutModel();
            LayoutTemplateManager.applyTemplate(key, model);
            assertTrue(model.size() >= 3, key + " should have >= 3 elements");
        }
    }
}
