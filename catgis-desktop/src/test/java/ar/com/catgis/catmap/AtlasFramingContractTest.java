package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Atlas framing centres the feature, applies the buffer and fits the frame aspect. */
class AtlasFramingContractTest {

    private static Envelope square() {
        return new Envelope(-70.0, -68.0, -45.0, -43.0);
    }

    @Test
    void centresTheFeature() {
        AtlasFraming.Framing framing = AtlasFraming.forFeature(square(), 200, 100, 0);
        assertEquals(-69.0, framing.centerX(), 1.0e-9);
        assertEquals(-44.0, framing.centerY(), 1.0e-9);
    }

    @Test
    void fitsTheFrameAspectRatioWithoutStretching() {
        AtlasFraming.Framing wide = AtlasFraming.forFeature(square(), 400, 100, 0);
        assertEquals(8.0, wide.width(), 1.0e-9);
        assertEquals(2.0, wide.height(), 1.0e-9);
        assertEquals(0.25, wide.height() / wide.width(), 1.0e-9);

        AtlasFraming.Framing tall = AtlasFraming.forFeature(square(), 100, 400, 0);
        assertEquals(2.0, tall.width(), 1.0e-9);
        assertEquals(8.0, tall.height(), 1.0e-9);
        assertEquals(4.0, tall.height() / tall.width(), 1.0e-9);
    }

    @Test
    void bufferExpandsEachSide() {
        AtlasFraming.Framing buffered = AtlasFraming.forFeature(square(), 100, 100, 25);
        assertEquals(3.0, buffered.width(), 1.0e-9);
        assertEquals(3.0, buffered.height(), 1.0e-9);
        assertEquals(-70.5, buffered.minX(), 1.0e-9);
        assertEquals(-42.5, buffered.maxY(), 1.0e-9);
    }

    @Test
    void degeneratePointFeatureGetsAFiniteFrame() {
        Envelope point = new Envelope(5.0, 5.0, 5.0, 5.0);
        AtlasFraming.Framing framing = AtlasFraming.forFeature(point, 100, 100, 10);
        assertTrue(framing.width() > 0);
        assertTrue(framing.height() > 0);
        assertEquals(5.0, framing.centerX(), 1.0e-6);
        assertEquals(5.0, framing.centerY(), 1.0e-6);
    }

    @Test
    void worldPerPixelMatchesFrameWidth() {
        AtlasFraming.Framing framing = AtlasFraming.forFeature(square(), 200, 100, 0);
        assertEquals(4.0 / 200.0, framing.worldPerPixel(200), 1.0e-12);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> AtlasFraming.forFeature(null, 100, 100, 10));
        assertThrows(IllegalArgumentException.class,
                () -> AtlasFraming.forFeature(square(), 0, 100, 10));
        assertThrows(IllegalArgumentException.class,
                () -> AtlasFraming.forFeature(square(), 100, -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> AtlasFraming.forFeature(square(), 100, 100, -1));
        assertThrows(IllegalArgumentException.class,
                () -> AtlasFraming.forFeature(square(), 100, 100, 101));
    }
}
