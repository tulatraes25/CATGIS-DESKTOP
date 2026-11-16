package ar.com.catgis.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Page geometry is document state: preset/custom millimetres, orientation, margins. */
class LayoutPageSpecTest {

    @Test
    void defaultSpecIsLegacyA4Landscape() {
        LayoutPageSpec spec = LayoutPageSpec.A4_LANDSCAPE;
        assertEquals("A4", spec.presetName());
        assertFalse(spec.isCustom());
        assertEquals(PageOrientation.LANDSCAPE, spec.orientation());
        assertEquals(297.0, spec.widthMm(), 0.001);
        assertEquals(210.0, spec.heightMm(), 0.001);
        assertFalse(spec.hasMargins());
    }

    @Test
    void presetOrientationSwapsEffectiveDimensions() {
        LayoutPageSpec portrait = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        assertEquals(297.0, portrait.widthMm(), 0.001);
        assertEquals(420.0, portrait.heightMm(), 0.001);
        assertEquals(297.0, portrait.portraitWidthMm(), 0.001);
        assertEquals(420.0, portrait.portraitHeightMm(), 0.001);

        LayoutPageSpec landscape = portrait.withOrientation(PageOrientation.LANDSCAPE);
        assertEquals(420.0, landscape.widthMm(), 0.001);
        assertEquals(297.0, landscape.heightMm(), 0.001);
        assertEquals("A3", landscape.presetName());
    }

    @Test
    void customSpecKeepsPortraitBaseAndRespectsOrientation() {
        LayoutPageSpec landscape = LayoutPageSpec.custom(200.0, 300.0,
                PageOrientation.LANDSCAPE);
        assertTrue(landscape.isCustom());
        assertEquals("CUSTOM", landscape.presetName());
        assertEquals(300.0, landscape.widthMm(), 0.001);
        assertEquals(200.0, landscape.heightMm(), 0.001);
        assertEquals(200.0, landscape.portraitWidthMm(), 0.001);

        LayoutPageSpec portrait = LayoutPageSpec.custom(200.0, 300.0,
                PageOrientation.PORTRAIT);
        assertEquals(200.0, portrait.widthMm(), 0.001);
        assertEquals(300.0, portrait.heightMm(), 0.001);
    }

    @Test
    void marginsArePreservedAcrossOrientationChanges() {
        LayoutPageSpec spec = LayoutPageSpec.custom(210.0, 297.0,
                PageOrientation.PORTRAIT, 12.0, 8.0, 12.0, 8.0);
        assertTrue(spec.hasMargins());
        assertEquals(12.0, spec.marginTopMm(), 0.001);
        assertEquals(8.0, spec.marginRightMm(), 0.001);
        assertEquals(12.0, spec.marginBottomMm(), 0.001);
        assertEquals(8.0, spec.marginLeftMm(), 0.001);

        LayoutPageSpec rotated = spec.withOrientation(PageOrientation.LANDSCAPE);
        assertTrue(rotated.hasMargins());
        assertEquals(8.0, rotated.marginRightMm(), 0.001);
    }

    @Test
    void marginsCannotConsumeTheWholeSheet() {
        assertThrows(IllegalArgumentException.class,
                () -> LayoutPageSpec.custom(100.0, 100.0, PageOrientation.PORTRAIT,
                        60.0, 60.0, 60.0, 60.0));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutPageSpec.custom(100.0, 100.0, PageOrientation.PORTRAIT,
                        60.0, 0.0, 50.0, 0.0));
    }

    @Test
    void invalidDimensionsAndMarginsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> LayoutPageSpec.custom(5.0, 100.0, PageOrientation.PORTRAIT));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutPageSpec.custom(100.0, LayoutPageSpec.MAX_DIMENSION_MM + 1.0,
                        PageOrientation.PORTRAIT));
        assertThrows(IllegalArgumentException.class,
                () -> LayoutPageSpec.custom(100.0, 100.0, PageOrientation.PORTRAIT,
                        -1.0, 0.0, 0.0, 0.0));
    }

    @Test
    void equalityDistinguishesOrientationSizeAndMargins() {
        LayoutPageSpec a = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        LayoutPageSpec b = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT);
        LayoutPageSpec c = LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.LANDSCAPE);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
        assertFalse(a.equals(LayoutPageSpec.A4_LANDSCAPE));
    }
}
