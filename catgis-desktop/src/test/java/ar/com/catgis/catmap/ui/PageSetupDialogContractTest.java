package ar.com.catgis.catmap.ui;

import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Headless contract for the values the "Configure page" dialog produces. */
class PageSetupDialogContractTest {

    @Test
    void presetA3PortraitKeepsStandardSheet() {
        LayoutPageSpec spec = PageSetupDialog.buildSpec(
                PageSizePreset.A3, PageOrientation.PORTRAIT,
                0, 0, 0, 0, 0, 0);
        assertEquals("A3", spec.presetName());
        assertEquals(PageOrientation.PORTRAIT, spec.orientation());
        assertEquals(297.0, spec.widthMm(), 0.001);
        assertEquals(420.0, spec.heightMm(), 0.001);
        assertFalse(spec.hasMargins());
    }

    @Test
    void presetA4LandscapeKeepsLegacyDefault() {
        LayoutPageSpec spec = PageSetupDialog.buildSpec(
                PageSizePreset.A4, PageOrientation.LANDSCAPE,
                0, 0, 0, 0, 0, 0);
        assertEquals(LayoutPageSpec.A4_LANDSCAPE, spec);
    }

    @Test
    void customPageUsesPortraitBaseAndMargins() {
        LayoutPageSpec spec = PageSetupDialog.buildSpec(
                null, PageOrientation.LANDSCAPE,
                200.0, 300.0, 10.0, 5.0, 10.0, 5.0);
        assertTrue(spec.isCustom());
        assertEquals(300.0, spec.widthMm(), 0.001);
        assertEquals(200.0, spec.heightMm(), 0.001);
        assertEquals(10.0, spec.marginTopMm(), 0.001);
        assertEquals(5.0, spec.marginRightMm(), 0.001);
        assertEquals(10.0, spec.marginBottomMm(), 0.001);
        assertEquals(5.0, spec.marginLeftMm(), 0.001);
    }

    @Test
    void orientationSwapsPresetDimensions() {
        LayoutPageSpec portrait = PageSetupDialog.buildSpec(
                PageSizePreset.TABLOID, PageOrientation.PORTRAIT, 0, 0, 0, 0, 0, 0);
        LayoutPageSpec landscape = PageSetupDialog.buildSpec(
                PageSizePreset.TABLOID, PageOrientation.LANDSCAPE, 0, 0, 0, 0, 0, 0);
        assertEquals(279.0, portrait.widthMm(), 0.001);
        assertEquals(432.0, portrait.heightMm(), 0.001);
        assertEquals(432.0, landscape.widthMm(), 0.001);
        assertEquals(279.0, landscape.heightMm(), 0.001);
    }

    @Test
    void invalidMarginsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PageSetupDialog.buildSpec(
                null, PageOrientation.PORTRAIT,
                100.0, 100.0, 60.0, 60.0, 60.0, 60.0));
    }

    @Test
    void invalidCustomDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PageSetupDialog.buildSpec(
                null, PageOrientation.PORTRAIT,
                5.0, 100.0, 0, 0, 0, 0));
    }

    @Test
    void missingOrientationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PageSetupDialog.buildSpec(
                PageSizePreset.A4, null, 0, 0, 0, 0, 0, 0));
    }
}
