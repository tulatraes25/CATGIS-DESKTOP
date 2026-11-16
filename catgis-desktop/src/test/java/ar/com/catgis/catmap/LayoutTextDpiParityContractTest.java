package ar.com.catgis.catmap;

import ar.com.catgis.MapPanel;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutMap;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutRenderHost;
import ar.com.catgis.layout.LayoutRenderHosts;
import ar.com.catgis.layout.LayoutScaleBar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T5/T6 integration: text keeps its physical size across render DPI, and a
 * scale bar with auto scale follows the map frame zoom.
 */
class LayoutTextDpiParityContractTest {

    @AfterEach
    void restoreHost() {
        LayoutRenderHosts.reset();
    }

    private static void installProject(String crs) {
        Project project = new Project("Paridad");
        project.setProjectCRS(crs);
        LayoutRenderHosts.install(new LayoutRenderHost() {
            @Override
            public Project project() {
                return project;
            }

            @Override
            public MapPanel mapPanel() {
                return null;
            }
        });
    }

    private static LayoutModel labelModel() {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("lbl", "MMMM", 20, 20, 120, 30);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        model.addElement(label);
        return model;
    }

    /** Height and width of the ink produced by the element on the page. */
    private static int[] inkBox(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                if (rgb != 0xFFFFFF) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            return new int[]{0, 0};
        }
        return new int[]{maxX - minX + 1, maxY - minY + 1};
    }

    @Test
    void labelTextKeepsItsPhysicalSizeAcrossRenderDpi() {
        LayoutModel model = labelModel();
        BufferedImage at96 = LayoutExportService.renderPage(model, 0, 96);
        BufferedImage at192 = LayoutExportService.renderPage(model, 0, 192);
        int[] small = inkBox(at96);
        int[] large = inkBox(at192);
        assertTrue(small[0] > 10 && small[1] > 5);
        double widthRatio = (double) large[0] / small[0];
        double heightRatio = (double) large[1] / small[1];
        assertTrue(widthRatio > 1.85 && widthRatio < 2.15,
                "text width must scale with DPI, ratio=" + widthRatio);
        assertTrue(heightRatio > 1.85 && heightRatio < 2.15,
                "text height must scale with DPI, ratio=" + heightRatio);
        at96.flush();
        at192.flush();
    }

    @Test
    void autoScaleBarFollowsTheMapFrameZoom() {
        installProject("EPSG:32719");
        LayoutModel model = new LayoutModel();
        LayoutMap map = new LayoutMap("m1", 10, 10, 100, 50);
        map.setOwnExtent(true);
        map.setOwnViewMinX(1000.0);
        map.setOwnViewMinY(2000.0);
        map.setOwnZoomFactor(1.0);
        LayoutScaleBar bar = new LayoutScaleBar("s1", 10, 70, 180, 30);
        bar.setAutoScale(true);
        model.addElement(map);
        model.addElement(bar);

        BufferedImage zoomOne = LayoutExportService.renderPage(model, 0, 96);
        double barOne = bar.getMapScaleDenominator();
        map.setOwnZoomFactor(4.0);
        BufferedImage zoomFour = LayoutExportService.renderPage(model, 0, 96);
        double barFour = bar.getMapScaleDenominator();

        assertEquals(10000.0, barOne, 1.0e-9, "render must not mutate the document scale bar");
        assertNotEquals(java.util.Arrays.hashCode(zoomOne.getRGB(0, 0,
                        zoomOne.getWidth(), zoomOne.getHeight(), null, 0, zoomOne.getWidth())),
                java.util.Arrays.hashCode(zoomFour.getRGB(0, 0,
                        zoomFour.getWidth(), zoomFour.getHeight(), null, 0,
                        zoomFour.getWidth())),
                "a four times wider map must render a different scale bar");
        zoomOne.flush();
        zoomFour.flush();
    }
}
