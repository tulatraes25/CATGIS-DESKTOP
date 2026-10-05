package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MapPanelRenderMapViewImageTest {

    @Test
    void renderMapViewImageUsesRequestedRenderSizeInsteadOfLivePanelSize() {
        RecordingPaintMapPanel panel = new RecordingPaintMapPanel();
        panel.setSize(400, 300);
        panel.setPreferredSize(new Dimension(400, 300));

        BufferedImage image = panel.renderMapViewImage(0d, 0d, 1d, 200, 100, false);

        assertNotNull(image);
        assertEquals(200, image.getWidth());
        assertEquals(100, image.getHeight());
        assertEquals(Color.RED.getRGB(), image.getRGB(199, 50));
        assertEquals(Color.BLUE.getRGB(), image.getRGB(50, 99));
        assertEquals(400, panel.getWidth());
        assertEquals(300, panel.getHeight());
    }

    @Test
    void renderMapViewImageTemporarilyOverridesRenderOrderAndClearsItAfterRender() {
        RecordingPaintMapPanel panel = new RecordingPaintMapPanel();
        Layer layerA = new Layer("A", "VECTOR", "");
        Layer layerB = new Layer("B", "VECTOR", "");
        List<Layer> orderedLayers = List.of(layerA, layerB);

        BufferedImage image = panel.renderMapViewImage(0d, 0d, 1d, 120, 80, false, orderedLayers);

        assertNotNull(image);
        assertEquals(2, panel.lastRenderOrder.size());
        assertSame(layerA, panel.lastRenderOrder.get(0));
        assertSame(layerB, panel.lastRenderOrder.get(1));
        assertNull(panel.layerManager.getTemporaryRenderOrderOverrideForTest());
    }

    private static final class RecordingPaintMapPanel extends MapPanel {
        private List<Layer> lastRenderOrder = List.of();

        @Override
        protected void paintComponent(Graphics g) {
            lastRenderOrder = List.copyOf(layerManager.getRenderOrderLayers());
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.drawLine(getWidth() - 1, 0, getWidth() - 1, Math.max(0, getHeight() - 1));
            g.setColor(Color.BLUE);
            g.drawLine(0, getHeight() - 1, Math.max(0, getWidth() - 1), getHeight() - 1);
        }
    }
}
