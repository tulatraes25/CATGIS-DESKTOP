package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A0.2 characterization contract: layout/export rendering must not corrupt
 * interactive map state, including when the renderer throws.
 */
class MapPanelRenderStateCharacterizationTest {

    @Test
    void restoresInteractiveViewAndRenderOrderAfterPaintFailure() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            FailingPaintMapPanel panel = new FailingPaintMapPanel();
            panel.setSize(440, 310);
            Dimension preferred = new Dimension(450, 320);
            panel.setPreferredSize(preferred);
            panel.viewMinX = -147.25d;
            panel.viewMinY = 59.75d;
            panel.zoomFactor = 2.5d;
            panel.layoutRenderMode = false;

            Layer a = new Layer("A", "", "VECTOR");
            Layer b = new Layer("B", "", "VECTOR");
            panel.failNextPaint = true;

            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> panel.renderMapViewImage(50d, 80d, 8d, 200, 120, false, List.of(a, b)));
            assertEquals("forced painter failure", failure.getMessage());

            assertEquals(440, panel.getWidth(), "Live panel width must survive export failure.");
            assertEquals(310, panel.getHeight(), "Live panel height must survive export failure.");
            assertEquals(preferred, panel.getPreferredSize());
            assertEquals(-147.25d, panel.viewMinX, 0.00001d);
            assertEquals(59.75d, panel.viewMinY, 0.00001d);
            assertEquals(2.5d, panel.zoomFactor, 0.00001d);
            assertFalse(panel.layoutRenderMode);
            assertNull(panel.layerManager.getTemporaryRenderOrderOverrideForTest(),
                    "Failed export may not poison subsequent live layer ordering.");

            BufferedImage retry = panel.renderMapViewImage(0d, 0d, 1d, 110, 70, false);
            assertNotNull(retry, "A transient painter failure must not poison retry.");
            assertEquals(110, retry.getWidth());
            assertEquals(70, retry.getHeight());
            assertEquals(440, panel.getWidth());
            assertEquals(310, panel.getHeight());
            assertEquals(preferred, panel.getPreferredSize());
            assertEquals(-147.25d, panel.viewMinX, 0.00001d);
            assertEquals(59.75d, panel.viewMinY, 0.00001d);
            assertEquals(2.5d, panel.zoomFactor, 0.00001d);
            assertNull(panel.layerManager.getTemporaryRenderOrderOverrideForTest());
        });
    }

    @Test
    void invalidZoomDoesNotChangeLiveMapStateOrInvokeRenderer() throws Exception {
        ReleaseTestSupport.runOnEdt(() -> {
            FailingPaintMapPanel panel = new FailingPaintMapPanel();
            panel.setSize(320, 240);
            panel.viewMinX = 12d;
            panel.viewMinY = -18d;
            panel.zoomFactor = 1.25d;
            panel.failNextPaint = true;

            assertNull(panel.renderMapViewImage(5d, 6d, 0d, 900, 500, false));
            assertTrue(panel.failNextPaint, "Invalid zoom must return before invoking paint.");
            assertEquals(320, panel.getWidth());
            assertEquals(240, panel.getHeight());
            assertEquals(12d, panel.viewMinX, 0d);
            assertEquals(-18d, panel.viewMinY, 0d);
            assertEquals(1.25d, panel.zoomFactor, 0d);
            assertNull(panel.layerManager.getTemporaryRenderOrderOverrideForTest());
        });
    }

    private static final class FailingPaintMapPanel extends MapPanel {
        private boolean failNextPaint;

        @Override
        protected void paintComponent(Graphics g) {
            if (failNextPaint) {
                failNextPaint = false;
                throw new IllegalStateException("forced painter failure");
            }
            g.setColor(Color.CYAN);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
