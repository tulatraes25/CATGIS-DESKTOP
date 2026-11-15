package ar.com.catgis;

import ar.com.catgis.layout.MapFrameRenderer;
import ar.com.catgis.layout.MapFrameViewport;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DC-3 contract: the atlas/layout export paths delegate to the live
 * {@code MapPanel} when the project has raster or online layers. That
 * delegation must run on the EDT even when it is requested from a background
 * worker, and it must not paint off the EDT.
 */
class ExportRenderEdtContractTest {

    @Test
    void frameRendererDelegationPaintsOnTheEdtFromABackgroundWorker() throws Exception {
        ReleaseTestSupport.initializeAppContext("export-edt-contract");
        try {
            AppContext.project().setProjectCRS("EPSG:22182");
            AppContext.project().addLayer(new RasterLayer("dem", ""));
            ThreadRecordingMapPanel panel = new ThreadRecordingMapPanel();
            AppContext.get().setMapPanel(panel);

            BufferedImage image = CompletableFuture.supplyAsync(() -> {
                MapFrameRenderer renderer = new MapFrameRenderer(new MapFrameViewport(0d, 0d, 1000d, 1000d));
                return renderer.render(120, 80, 96);
            }).get(30, TimeUnit.SECONDS);

            assertNotNull(image, "El render del marco debe devolver una imagen.");
            assertEquals(120, image.getWidth());
            assertEquals(80, image.getHeight());
            assertTrue(panel.paintedOnEventDispatchThread,
                    "El render de exportacion debe pintar en el Event Dispatch Thread.");
            assertFalse(panel.paintedOffEventDispatchThread,
                    "Ningun paso del render de exportacion debe pintar fuera del EDT.");
        } finally {
            ReleaseTestSupport.clearAppContext();
        }
    }

    private static final class ThreadRecordingMapPanel extends MapPanel {
        private volatile boolean paintedOnEventDispatchThread;
        private volatile boolean paintedOffEventDispatchThread;

        @Override
        protected void paintComponent(Graphics g) {
            if (SwingUtilities.isEventDispatchThread()) {
                paintedOnEventDispatchThread = true;
            } else {
                paintedOffEventDispatchThread = true;
            }
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
