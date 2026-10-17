package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Fail-closed containment until genuine FlatGeobuf header CRS decoding is certified. */
class FlatGeobufCrsFailClosedContractTest {
    @Test
    void unknownCrsIsRejectedBeforeReadingOrAttachingSource() {
        assertThrows(IllegalStateException.class,
                () -> LayerVectorDataSupport.requireDeclaredFgbSourceCrs(null));
        assertThrows(IllegalStateException.class,
                () -> LayerVectorDataSupport.requireDeclaredFgbSourceCrs("  "));
        assertDoesNotThrow(() ->
                LayerVectorDataSupport.requireDeclaredFgbSourceCrs("EPSG:32719"));
    }
}
