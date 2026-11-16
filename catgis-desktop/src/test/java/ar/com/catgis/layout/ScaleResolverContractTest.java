package ar.com.catgis.layout;

import ar.com.catgis.MapPanel;
import ar.com.catgis.core.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Scale denominators derive from the map frame instead of a manual number. */
class ScaleResolverContractTest {

    @AfterEach
    void restoreHost() {
        LayoutRenderHosts.reset();
    }

    private static void installProject(String crs) {
        Project project = new Project("Escala");
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

    private static LayoutMap map(double zoom) {
        LayoutMap map = new LayoutMap("m1", 10, 10, 100, 50);
        map.setOwnExtent(true);
        map.setOwnViewMinX(0.0);
        map.setOwnViewMinY(0.0);
        map.setOwnZoomFactor(zoom);
        return map;
    }

    private static LayoutModel modelWith(LayoutScaleBar bar, LayoutMap map) {
        LayoutModel model = new LayoutModel();
        model.addElement(map);
        model.addElement(bar);
        return model;
    }

    @Test
    void projectedMetersResolveToTheExpectedDenominator() {
        installProject("EPSG:32719");
        LayoutMap frame = map(1.0); // one metre per pixel
        int dpi = 96;
        double denominator = ScaleResolver.resolveDenominator(frame, dpi);
        assertEquals(1000.0 * dpi / 25.4, denominator, 1.0e-6);
        assertTrue(denominator > 3000 && denominator < 4000);
    }

    @Test
    void degreesResolveWithLatitudeCompression() {
        installProject("EPSG:4326");
        LayoutMap frame = map(0.001); // 0.001 degrees per pixel
        double denominator = ScaleResolver.resolveDenominator(frame, 96);
        assertTrue(Double.isFinite(denominator));
        assertTrue(denominator > 200_000 && denominator < 800_000,
                "unexpected geographic denominator: " + denominator);
    }

    @Test
    void unresolvedFramesReturnNaN() {
        LayoutRenderHosts.install(new LayoutRenderHost() {
            @Override
            public Project project() {
                return null;
            }

            @Override
            public MapPanel mapPanel() {
                return null;
            }
        });
        // A frame without data falls back to the viewport default extent; only
        // null or degenerate frames are genuinely unresolvable.
        LayoutMap noExtent = new LayoutMap("m2", 0, 0, 100, 50);
        assertTrue(Double.isFinite(ScaleResolver.resolveDenominator(noExtent, 96)));
        assertTrue(Double.isNaN(ScaleResolver.resolveDenominator(null, 96)));
        LayoutMap degenerate = new LayoutMap("m3", 0, 0, 0, 0);
        degenerate.setOwnExtent(true);
        assertTrue(Double.isNaN(ScaleResolver.resolveDenominator(degenerate, 96)));
    }

    @Test
    void renderCopyCarriesTheDerivedScaleAndKeepsTheModelIntact() {
        installProject("EPSG:32719");
        LayoutScaleBar bar = new LayoutScaleBar("s1", 10, 70, 80, 12);
        bar.setMapScaleDenominator(10000);
        LayoutMap frame = map(1.0);
        LayoutModel model = modelWith(bar, frame);

        LayoutElement resolved = ScaleResolver.withResolvedScale(bar, model, 96);
        assertNotSame(bar, resolved);
        double derived = ((LayoutScaleBar) resolved).getMapScaleDenominator();
        assertEquals(1000.0 * 96.0 / 25.4, derived, 1.0e-6);
        assertEquals(10000, bar.getMapScaleDenominator(),
                "the document scale bar must not be mutated at render time");
    }

    @Test
    void manualScaleAndMapLessLayoutsStayUntouched() {
        LayoutScaleBar manual = new LayoutScaleBar("s1", 10, 70, 80, 12);
        manual.setAutoScale(false);
        LayoutModel withMap = modelWith(manual, map(1.0));
        assertSame(manual, ScaleResolver.withResolvedScale(manual, withMap, 96));

        LayoutModel withoutMap = new LayoutModel();
        withoutMap.addElement(manual);
        assertSame(manual, ScaleResolver.withResolvedScale(manual, withoutMap, 96));
    }
}
