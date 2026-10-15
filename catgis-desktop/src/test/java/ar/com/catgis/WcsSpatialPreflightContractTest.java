package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WcsSpatialPreflightContractTest {

    @Test
    void acceptsFiniteOrderedExtentsAndProjectedCoordinates() throws Exception {
        WcsSpatialPreflight.validate("-70,-46,-69,-45", "EPSG:4326");
        WcsSpatialPreflight.validate(" -500000, 4500000, -400000, 4600000 ", "EPSG:32719");
    }

    @Test
    void rejectsMalformedExtentAndImpossibleGeographicDegrees() {
        for (String bbox : new String[] {
                "", " ", "0,1,2", "0,1,2,3,4", "NaN,0,1,2",
                "0,0,Infinity,1", "2,0,1,3", "0,3,2,1",
                "-181,-20,0,20", "-70,-91,-69,-45",
                "-70,-46,181,-45", "0,0,0,1", "0,0,1,0",
                "0,0,1,2&REQUEST=Delete" }) {
            assertThrows(IOException.class,
                    () -> WcsSpatialPreflight.validate(bbox, "EPSG:4326"), bbox);
        }
        assertThrows(IOException.class,
                () -> WcsSpatialPreflight.validate(null, "EPSG:4326"));
    }

    @Test
    void rejectsBlankOrUnsafeCrs() {
        for (String crs : new String[] {"", " ", "EPSG:4326&FORMAT=other",
                "EPSG:4326\\r\\nX-Injected: yes", "EPSG 4326", "bad"}) {
            assertThrows(IOException.class,
                    () -> WcsSpatialPreflight.validate("-70,-46,-69,-45", crs));
        }
        assertThrows(IOException.class,
                () -> WcsSpatialPreflight.validate("-70,-46,-69,-45", null));
    }

    @Test
    void invalidSpatialRequestLeavesPreviousFileUntouchedBeforeNetwork() throws Exception {
        Path destination = Files.createTempFile("catgis WCS previous ", ".tif");
        try {
            byte[] previous = "previous valid user data".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            Files.write(destination, previous);
            assertThrows(IOException.class, () -> WcsClient.downloadCoverageToFile(
                    "http://127.0.0.1:1/unused", "sample",
                    "NaN,-46,-69,-45", "EPSG:4326", 16, 16, destination.toFile()));
            assertArrayEquals(previous, Files.readAllBytes(destination));
        } finally {
            Files.deleteIfExists(destination);
        }
    }
}
