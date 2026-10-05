package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class LayoutIntegrationTest {

    @Test
    public void testFullWorkflow() {
        // Simulate full CATMAP workflow in code
        LayoutModel model = new LayoutModel();

        // 1. Add a map
        LayoutMap map = new LayoutMap("map1", 15, 36, 267, 130);
        map.setZOrder(model.nextZ());
        map.setName("Mapa principal");
        map.setShowGrid(true); map.setGridCols(4); map.setGridRows(3);
        map.setGridByDistance(true); map.setGridIntervalX(500); map.setGridIntervalY(500);
        map.setGridUnit("m");
        model.addElement(map);
        assertEquals(1, model.size());

        // 2. Add title
        LayoutLabel title = new LayoutLabel("title", "Mapa Final", 12, 8, 273, 16);
        title.setZOrder(model.nextZ());
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        title.setName("Titulo");
        model.addElement(title);

        // 3. Select and move title
        model.clearSelection();
        title.setSelected(true);
        assertEquals(title, model.getSelected());
        title.setBoundsMm(12, 10, 273, 16);

        // 4. Add legend
        LayoutLegend legend = new LayoutLegend("leg", 12, 172, 130, 40);
        legend.setZOrder(model.nextZ());
        legend.setAutoHeight(true);
        legend.setName("Leyenda");
        legend.setTitle("Referencias");
        legend.getItems().add(new LayoutLegend.LegendItem("Pozos", Color.RED, "POINT"));
        legend.getItems().add(new LayoutLegend.LegendItem("Caminos", Color.BLUE, "LINE"));
        legend.getItems().add(new LayoutLegend.LegendItem("Lotes", new Color(0, 200, 0), "POLYGON"));
        model.addElement(legend);

        // 5. Verify legend has items
        assertEquals(3, legend.getIncludedItems().size());

        // 6. Add scale bar
        LayoutScaleBar scale = new LayoutScaleBar("scale", 12, 208, 100, 10);
        scale.setZOrder(model.nextZ());
        scale.setMapScaleDenominator(25000);
        scale.setName("Escala");
        model.addElement(scale);

        // 7. Add north
        LayoutNorthArrow north = new LayoutNorthArrow("north", 270, 155, 16, 16);
        north.setZOrder(model.nextZ());
        north.setName("Norte");
        model.addElement(north);

        // 8. Verify total count
        assertEquals(5, model.size());

        // 9. Visibility: hide legend
        legend.setVisible(false);
        assertEquals(4, model.getVisibleElementsSortedByZ().size());

        // 10. Lock map
        map.setLocked(true);
        assertTrue(map.isLocked());

        // 11. Find topmost element
        LayoutElement found = model.findTopmostElementAtMm(50, 50);
        assertNotNull(found);
        assertEquals("Mapa principal", found.getName());

        // 12. Test alignment of elements
        // Align title and legend left edges
        double refX = title.getBoundsMm().x;
        legend.setVisible(true);
        legend.setBoundsMm(refX, legend.getBoundsMm().y,
            legend.getBoundsMm().width, legend.getBoundsMm().height);
        assertEquals(refX, legend.getBoundsMm().x, 0.01);
    }

    @Test
    public void testDragAndResizeSimulation() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r = new LayoutRectangle("r1", 10, 10, 100, 60);
        r.setZOrder(1); r.setVisible(true);
        model.addElement(r);

        // Simulate drag: move by 5mm in each direction
        double startX = r.getBoundsMm().x;
        double startY = r.getBoundsMm().y;
        r.setBoundsMm(startX + 5, startY + 3, r.getBoundsMm().width, r.getBoundsMm().height);
        assertEquals(startX + 5, r.getBoundsMm().x, 0.01);
        assertEquals(startY + 3, r.getBoundsMm().y, 0.01);

        // Simulate resize: change width and height
        r.setBoundsMm(r.getBoundsMm().x, r.getBoundsMm().y, 200, 120);
        assertEquals(200, r.getBoundsMm().width, 0.01);
        assertEquals(120, r.getBoundsMm().height, 0.01);
    }

    @Test
    public void testUndoReduScenario() {
        LayoutModel model = new LayoutModel();
        LayoutRectangle r1 = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1.setZOrder(1);
        model.addElement(r1);
        assertEquals(1, model.size());

        // Simulate undo by removing
        model.removeElement("r1");
        assertEquals(0, model.size());

        // Redo by re-adding
        LayoutRectangle r1redo = new LayoutRectangle("r1", 0, 0, 100, 100);
        r1redo.setZOrder(1);
        model.addElement(r1redo);
        assertEquals(1, model.size());
    }

    @Test
    public void testCopyPasteScenario() {
        LayoutModel model = new LayoutModel();
        LayoutLabel original = new LayoutLabel("orig", "Copiar esto", 10, 10, 100, 20);
        original.setZOrder(1);
        original.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        original.setColor(Color.BLUE);
        model.addElement(original);

        // Simulate copy-paste: create duplicate at offset
        LayoutLabel copy = new LayoutLabel("copy", original.getText(),
            original.getBoundsMm().x + 10, original.getBoundsMm().y + 10,
            original.getBoundsMm().width, original.getBoundsMm().height);
        copy.setZOrder(model.nextZ());
        copy.setFont(original.getFont());
        copy.setColor(original.getColor());
        copy.setName(original.getName() + " copia");
        model.addElement(copy);

        assertEquals(2, model.size());
        assertEquals("Copiar esto", ((LayoutLabel)model.getElements().get(1)).getText());
    }
}
