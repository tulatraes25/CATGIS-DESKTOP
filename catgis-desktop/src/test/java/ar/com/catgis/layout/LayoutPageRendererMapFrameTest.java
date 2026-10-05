package ar.com.catgis.layout;

import ar.com.catgis.AppContext;
import ar.com.catgis.MapPanel;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LayoutPageRendererMapFrameTest {

    @Test
    void drawMapFrameScalesTargetZoomToFrameSizeBeforeRequestingMapRender() {
        MapPanel previous = AppContext.mapPanel();
        RecordingRenderMapPanel panel = new RecordingRenderMapPanel();
        AppContext.get().setMapPanel(panel);
        try {
            LayoutSnapshot snapshot = new LayoutSnapshot(
                    new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB),
                    List.of(),
                    "Proyecto",
                    "EPSG:22182",
                    "EPSG:22182",
                    "1:10000",
                    1000d,
                    100d,
                    200d,
                    10d,
                    1200,
                    800
            );
            LayoutInteractionState interactionState = new LayoutInteractionState();
            BufferedImage canvas = new BufferedImage(1200, 900, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            try {
                MapFrameGeometry geometry = LayoutPageRenderer.drawMapFrame(
                        g2,
                        snapshot,
                        new Rectangle(0, 0, 600, 400),
                        interactionState
                );

                assertNotNull(geometry);
                assertEquals(576, panel.lastRenderWidth);
                assertEquals(376, panel.lastRenderHeight);
                assertEquals(576, geometry.imageBounds().width);
                assertEquals(376, geometry.imageBounds().height);
                assertEquals(10d * Math.min(576d / 1200d, 376d / 800d), panel.lastZoomFactor, 1e-9);
            } finally {
                g2.dispose();
            }
        } finally {
            AppContext.get().setMapPanel(previous);
        }
    }

    private static final class RecordingRenderMapPanel extends MapPanel {
        private double lastZoomFactor;
        private int lastRenderWidth;
        private int lastRenderHeight;
        private List<ar.com.catgis.core.model.Layer> lastRenderLayers = new ArrayList<>();

        @Override
        public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor,
                                                int renderWidth, int renderHeight, boolean includeDecorations,
                                                List<ar.com.catgis.core.model.Layer> renderLayers) {
            this.lastZoomFactor = renderZoomFactor;
            this.lastRenderWidth = renderWidth;
            this.lastRenderHeight = renderHeight;
            this.lastRenderLayers = renderLayers != null ? new ArrayList<>(renderLayers) : new ArrayList<>();
            return new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
        }
    }
}
