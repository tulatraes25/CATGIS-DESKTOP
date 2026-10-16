package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real optional metadata files; pixel checks do not imply embedded GeoTIFF validation. */
class RasterSidecarValidationContractTest {
    @TempDir Path directory;

    @Test
    void geographicAndProjectedExtentsRoundTripWithoutClippingOrUnitChanges() throws Exception {
        for (Envelope area : List.of(new Envelope(-69.125, -68.625, -46.875, -45.375),
                new Envelope(1000000.125, 1000300.625, 4500000.875, 4500800.375))) {
            String crs = area.getMinX() < 0 ? "EPSG:4326" : "EPSG:3857";
            Path raster = directory.resolve(crs.substring(5) + ".tif");
            Envelope original = new Envelope(area);
            RasterSidecarSupport.write(raster.toFile(), area, crs);
            var metadata = RasterSidecarSupport.read(raster.toFile());
            assertNotNull(metadata);
            assertEquals(original, metadata.envelope());
            assertEquals(crs, metadata.sourceCrs());
            assertEquals(original, area);
            assertFalse(Files.exists(raster), "Writing metadata must not invent a raster file.");
        }
    }

    @Test
    void realTiffPixelsAndBothFilesRemainUnchangedAfterReadingMetadata() throws Exception {
        Path raster = directory.resolve("real samples.tif");
        byte[] pixels = tiff();
        Files.write(raster, pixels);
        RasterSidecarSupport.write(raster.toFile(), validArea(), "EPSG:3857");
        Path sidecar = sidecar(raster);
        byte[] metadataBytes = Files.readAllBytes(sidecar);
        assertNotNull(RasterSidecarSupport.read(raster.toFile()));
        assertArrayEquals(pixels, Files.readAllBytes(raster));
        assertArrayEquals(metadataBytes, Files.readAllBytes(sidecar));
        BufferedImage image = ImageIO.read(raster.toFile());
        assertNotNull(image);
        assertEquals(17, image.getRaster().getSample(0, 0, 0));
        assertEquals(254, image.getRaster().getSample(1, 1, 0));
        image.flush();
        Path renamed = directory.resolve("metadata released.properties");
        Files.move(sidecar, renamed);
        Files.delete(renamed);
        Files.delete(raster);
    }

    @Test
    void everyMissingCoordinateIsUnknownNotANaNEnvelope() throws Exception {
        for (String key : new String[] {"minX", "maxX", "minY", "maxY"}) {
            Properties properties = validProperties();
            properties.remove(key);
            assertRejected(properties);
        }
    }

    @Test
    void everyNonfiniteAxisIsRejectedWithoutChangingMetadataBytes() throws Exception {
        for (String key : new String[] {"minX", "maxX", "minY", "maxY"}) {
            for (String value : new String[] {"NaN", "Infinity", "-Infinity"}) {
                Properties properties = validProperties();
                properties.setProperty(key, value);
                assertRejected(properties);
            }
        }
    }

    @Test
    void serializedReversalZeroAndOverflowAreRejectedBeforeJtsCanReorderThem() throws Exception {
        for (double[] bounds : new double[][] {{2, 1, 0, 1}, {0, 1, 2, 1},
                {1, 1, 0, 1}, {0, 1, 1, 1},
                {-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1},
                {0, 1, -Double.MAX_VALUE, Double.MAX_VALUE}}) {
            Properties properties = validProperties();
            String[] keys = {"minX", "maxX", "minY", "maxY"};
            for (int i = 0; i < keys.length; i++) {
                properties.setProperty(keys[i], Double.toString(bounds[i]));
            }
            assertRejected(properties);
        }
    }

    @Test
    void absentOrBlankCrsNeverBecomesAnInventedWgs84Declaration() throws Exception {
        Properties absent = validProperties();
        absent.remove("sourceCrs");
        assertRejected(absent);
        for (String blank : new String[] {"", " ", "\t\n"}) {
            Properties properties = validProperties();
            properties.setProperty("sourceCrs", blank);
            assertRejected(properties);
        }
    }

    @Test
    void versionMalformedNumbersAndInvalidPropertiesRetainReadNullCompatibility() throws Exception {
        for (String version : new String[] {"", "2", "not-a-version"}) {
            Properties properties = validProperties();
            properties.setProperty("version", version);
            assertRejected(properties);
        }
        Properties properties = validProperties();
        properties.setProperty("minX", "not-a-number");
        assertRejected(properties);
        Path raster = directory.resolve("broken.tif");
        String text = "version=1\nsourceCrs=EPSG:4326\nminX=0\nmaxX=1\nminY=0\nmaxY=1\nbroken="
                + "\\" + "uNOTHEX\n";
        Files.writeString(sidecar(raster), text, StandardCharsets.ISO_8859_1);
        byte[] before = Files.readAllBytes(sidecar(raster));
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        assertArrayEquals(before, Files.readAllBytes(sidecar(raster)));
    }

    @Test
    void exactByteBudgetIsReadableAndOneExtraByteIsRejected() throws Exception {
        assertEquals(65536, RasterSidecarSupport.MAX_SIDECAR_BYTES);
        Path raster = directory.resolve("bounded.tif");
        byte[] prefix = "version=1\nsourceCrs=EPSG:3857\nminX=0\nmaxX=1\nminY=0\nmaxY=1\n#"
                .getBytes(StandardCharsets.ISO_8859_1);
        byte[] exact = new byte[65536];
        Arrays.fill(exact, (byte) 'x');
        System.arraycopy(prefix, 0, exact, 0, prefix.length);
        Files.write(sidecar(raster), exact);
        assertNotNull(RasterSidecarSupport.read(raster.toFile()));
        assertArrayEquals(exact, Files.readAllBytes(sidecar(raster)));
        byte[] tooLarge = Arrays.copyOf(exact, exact.length + 1);
        tooLarge[tooLarge.length - 1] = 'x';
        Files.write(sidecar(raster), tooLarge);
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        assertArrayEquals(tooLarge, Files.readAllBytes(sidecar(raster)));
    }

    @Test
    void invalidWriteBoundsCannotChangeOldFilesOrCreateANewDirectory() throws Exception {
        Path raster = directory.resolve("keep.tif");
        byte[] image = tiff();
        Files.write(raster, image);
        RasterSidecarSupport.write(raster.toFile(), validArea(), "EPSG:3857");
        byte[] original = Files.readAllBytes(sidecar(raster));
        int n = 0;
        for (Envelope invalid : List.of(new Envelope(Double.NaN, 1, 0, 1),
                new Envelope(0, Double.POSITIVE_INFINITY, 0, 1),
                new Envelope(0, 1, Double.NEGATIVE_INFINITY, 1),
                new Envelope(0, 0, 0, 1), new Envelope(0, 1, 0, 0),
                new Envelope(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1))) {
            assertThrows(IOException.class, () -> RasterSidecarSupport.write(raster.toFile(), invalid, "EPSG:3857"));
            assertArrayEquals(image, Files.readAllBytes(raster));
            assertArrayEquals(original, Files.readAllBytes(sidecar(raster)));
            Path absent = directory.resolve("not-created-" + n++).resolve("out.tif");
            assertThrows(IOException.class, () -> RasterSidecarSupport.write(absent.toFile(), invalid, "EPSG:3857"));
            assertFalse(Files.exists(absent.getParent()));
        }
    }

    @Test
    void writeRequiresAnExplicitCrsBeforeDirectoryCreationOrReplacement() throws Exception {
        Path raster = directory.resolve("declared.tif");
        RasterSidecarSupport.write(raster.toFile(), validArea(), "EPSG:3857");
        byte[] before = Files.readAllBytes(sidecar(raster));
        for (String crs : new String[] {null, "", " \t "}) {
            assertThrows(IOException.class, () -> RasterSidecarSupport.write(raster.toFile(), validArea(), crs));
            assertArrayEquals(before, Files.readAllBytes(sidecar(raster)));
            Path absent = directory.resolve("no-default-crs").resolve("out.tif");
            assertThrows(IOException.class, () -> RasterSidecarSupport.write(absent.toFile(), validArea(), crs));
            assertFalse(Files.exists(absent.getParent()));
        }
    }

    @Test
    void writerAccountsForEscapedByteSizeNotJustSourceCharacterCount() throws Exception {
        Path raster = directory.resolve("small.tif");
        RasterSidecarSupport.write(raster.toFile(), validArea(), "EPSG:3857");
        byte[] before = Files.readAllBytes(sidecar(raster));
        String oversizedAfterEscaping = "LOCAL:" + "\u00e9".repeat(12000);
        assertTrue(oversizedAfterEscaping.length() < RasterSidecarSupport.MAX_SIDECAR_BYTES);
        assertThrows(IOException.class, () -> RasterSidecarSupport.write(
                raster.toFile(), validArea(), oversizedAfterEscaping));
        assertArrayEquals(before, Files.readAllBytes(sidecar(raster)));
        Path absent = directory.resolve("too-large").resolve("out.tif");
        assertThrows(IOException.class, () -> RasterSidecarSupport.write(
                absent.toFile(), validArea(), "x".repeat(65537)));
        assertFalse(Files.exists(absent.getParent()));
    }

    @Test
    void optionalNullAndEmptyWriteArgumentsRemainNoOps() throws Exception {
        Path raster = directory.resolve("optional").resolve("not-created.tif");
        RasterSidecarSupport.write(null, validArea(), "EPSG:3857");
        RasterSidecarSupport.write(raster.toFile(), null, null);
        RasterSidecarSupport.write(raster.toFile(), new Envelope(), null);
        assertFalse(Files.exists(raster.getParent()));
        assertNull(RasterSidecarSupport.read(null));
    }

    @Test
    void missingEmptyOrDirectorySidecarIsUnavailableNotInventedMetadata() throws Exception {
        Path raster = directory.resolve("absent.tif");
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        Files.write(sidecar(raster), new byte[0]);
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        Files.delete(sidecar(raster));
        Files.createDirectory(sidecar(raster));
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        assertTrue(Files.isDirectory(sidecar(raster)));
    }

    @Test
    void unicodePathsEscapedPropertiesAndExtraLegacyFieldsRemainCompatible() throws Exception {
        Path raster = directory.resolve("r\u00edo con espacios.tif");
        Properties properties = validProperties();
        properties.setProperty("sourceCrs", "3857");
        properties.setProperty("nota", "M\u00e9todo previo: conservar\nsegunda linea");
        store(raster, properties);
        byte[] before = Files.readAllBytes(sidecar(raster));
        var metadata = RasterSidecarSupport.read(raster.toFile());
        assertNotNull(metadata);
        assertEquals("EPSG:3857", metadata.sourceCrs());
        assertEquals(validArea(), metadata.envelope());
        assertArrayEquals(before, Files.readAllBytes(sidecar(raster)));
    }

    private void assertRejected(Properties properties) throws IOException {
        Path raster = directory.resolve("invalid.tif");
        store(raster, properties);
        byte[] before = Files.readAllBytes(sidecar(raster));
        assertNull(RasterSidecarSupport.read(raster.toFile()));
        assertArrayEquals(before, Files.readAllBytes(sidecar(raster)));
        assertFalse(Files.exists(raster));
    }

    private static Envelope validArea() {
        return new Envelope(-12.125, 15.5, -45.75, 20.25);
    }

    private static Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "1");
        properties.setProperty("sourceCrs", "EPSG:3857");
        properties.setProperty("minX", "-12.125");
        properties.setProperty("maxX", "15.5");
        properties.setProperty("minY", "-45.75");
        properties.setProperty("maxY", "20.25");
        return properties;
    }

    private static Path sidecar(Path raster) {
        return RasterSidecarSupport.sidecarFile(raster.toFile()).toPath();
    }

    private static void store(Path raster, Properties properties) throws IOException {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        properties.store(encoded, "independent metadata fixture");
        Files.write(sidecar(raster), encoded.toByteArray());
    }

    private static byte[] tiff() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        try {
            image.getRaster().setSample(0, 0, 0, 17);
            image.getRaster().setSample(1, 1, 0, 254);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            assertTrue(ImageIO.write(image, "tif", output));
            return output.toByteArray();
        } finally {
            image.flush();
        }
    }
}
