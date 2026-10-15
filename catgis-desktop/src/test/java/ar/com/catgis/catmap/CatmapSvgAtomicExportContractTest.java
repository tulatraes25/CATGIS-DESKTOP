package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real embedded-raster SVG integration and atomic rollback contracts.
 * No assertion misrepresents an embedded PNG as vector geometry.
 */
class CatmapSvgAtomicExportContractTest {

    @TempDir
    Path dir;

    private static LayoutModel sample(String title) {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("title", title, 10, 10, 170, 25));
        return model;
    }

    private void assertNoOwnedStages() throws IOException {
        try (Stream<Path> paths = Files.list(dir)) {
            assertTrue(paths.noneMatch(p -> p.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void emittedSvgIsXmlWithDecodableEmbeddedPngNotVectorText() throws Exception {
        Path svg = dir.resolve("plan cartografico con espacios.svg");
        SvgExportEngine.exportSvg(sample("Mapa profesional"), svg.toFile(), 72);

        SvgExportEngine.verifyRasterSvg(svg, 841, 595);
        String xml = Files.readString(svg, StandardCharsets.UTF_8);
        assertTrue(xml.contains("<svg xmlns=\"http://www.w3.org/2000/svg\""));
        assertTrue(xml.contains("viewBox=\"0 0 297.0 210.0\""));
        assertTrue(xml.contains("href=\"data:image/png;base64,"));
        assertTrue(xml.contains("</svg>"));
        assertTrue(!xml.contains("<text"), "No true vector-text claim for this SVG.");

        String prefix = "data:image/png;base64,";
        int start = xml.indexOf(prefix);
        assertTrue(start >= 0);
        start += prefix.length();
        int end = xml.indexOf('"', start);
        byte[] embedded = Base64.getDecoder().decode(xml.substring(start, end));
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(embedded));
        assertNotNull(decoded);
        assertEquals(841, decoded.getWidth());
        assertEquals(595, decoded.getHeight());
        assertEquals(0xFFFFFF, decoded.getRGB(0, 0) & 0xFFFFFF);
        assertTrue(Files.size(svg) > 100);
        assertNoOwnedStages();
    }

    @Test
    void secondSuccessfulExportReplacesOnlyCompletedSvg() throws Exception {
        Path svg = dir.resolve("layout validado.svg");
        SvgExportEngine.exportSvg(sample("First"), svg.toFile(), 72);
        byte[] first = Files.readAllBytes(svg);
        SvgExportEngine.exportSvg(sample("Second different title"), svg.toFile(), 72);
        SvgExportEngine.verifyRasterSvg(svg, 841, 595);
        assertTrue(!java.util.Arrays.equals(first, Files.readAllBytes(svg)));
        assertNoOwnedStages();
    }

    @Test
    void unsupportedAtomicMovePreservesPreviousCompleteSvgByteForByte()
            throws Exception {
        Path svg = dir.resolve("plano previo.svg");
        SvgExportEngine.exportSvg(sample("Previous title"), svg.toFile(), 72);
        byte[] original = Files.readAllBytes(svg);

        IOException thrown = assertThrows(IOException.class, () ->
                SvgExportEngine.exportSvg(sample("New title"), svg.toFile(), 72,
                        (stage, target) -> {
                            throw new AtomicMoveNotSupportedException(
                                    stage.toString(), target.toString(), "synthetic failure");
                        }));
        assertTrue(thrown.getMessage().contains("atomica"));
        assertArrayEquals(original, Files.readAllBytes(svg));
        SvgExportEngine.verifyRasterSvg(svg, 841, 595);
        assertNoOwnedStages();
    }

    @Test
    void malformedOrExternalSvgCannotReplacePreviousDocument() throws Exception {
        Path svg = dir.resolve("document protected.svg");
        SvgExportEngine.exportSvg(sample("Retain"), svg.toFile(), 72);
        byte[] old = Files.readAllBytes(svg);

        String bogusPngSvg = "<?xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\""
                + " width=\"297.0mm\" height=\"210.0mm\" viewBox=\"0 0 297.0 210.0\">"
                + "<rect width=\"297.0\" height=\"210.0\" fill=\"white\"/>"
                + "<image href=\"data:image/png;base64,AA==\"/></svg>";
        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                svg.toFile(),
                staged -> Files.writeString(staged, bogusPngSvg, StandardCharsets.UTF_8),
                staged -> SvgExportEngine.verifyRasterSvg(staged, 841, 595)));
        assertArrayEquals(old, Files.readAllBytes(svg));
        assertNoOwnedStages();

        String maliciousDoctype = "<?xml version=\"1.0\"?><!DOCTYPE svg ["
                + "<!ENTITY leak SYSTEM \"file:///etc/passwd\">]>"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\">&leak;</svg>";
        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                svg.toFile(),
                staged -> Files.writeString(staged, maliciousDoctype, StandardCharsets.UTF_8),
                staged -> SvgExportEngine.verifyRasterSvg(staged, 841, 595)));
        assertArrayEquals(old, Files.readAllBytes(svg));
        assertNoOwnedStages();

        String unexpectedScript = "<?xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\""
                + " width=\"297.0mm\" height=\"210.0mm\" viewBox=\"0 0 297.0 210.0\">"
                + "<rect width=\"297.0\" height=\"210.0\" fill=\"white\"/>"
                + "<image href=\"data:image/png;base64,AA==\"/><script>bad</script></svg>";
        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                svg.toFile(),
                staged -> Files.writeString(staged, unexpectedScript, StandardCharsets.UTF_8),
                staged -> SvgExportEngine.verifyRasterSvg(staged, 841, 595)));
        assertArrayEquals(old, Files.readAllBytes(svg));
        assertNoOwnedStages();
    }

    @Test
    void invalidOrOversizedDpiCannotReplaceExistingSvg() throws Exception {
        Path svg = dir.resolve("high resolution.svg");
        SvgExportEngine.exportSvg(sample("Retain"), svg.toFile(), 72);
        byte[] previous = Files.readAllBytes(svg);
        assertThrows(IllegalArgumentException.class, () ->
                SvgExportEngine.exportSvg(sample("Too big"), svg.toFile(), 1200));
        assertArrayEquals(previous, Files.readAllBytes(svg));
        assertNoOwnedStages();
    }

    @Test
    void directoryAndSymlinkAreRejectedWithoutTouchingVictim() throws Exception {
        Path folder = Files.createDirectory(dir.resolve("folder.svg"));
        assertThrows(IOException.class, () ->
                SvgExportEngine.exportSvg(sample("No write"), folder.toFile(), 72));
        Path existing = dir.resolve("previous.svg");
        SvgExportEngine.exportSvg(sample("Keep"), existing.toFile(), 72);
        byte[] old = Files.readAllBytes(existing);
        Path linked = dir.resolve("symlink.svg");
        try {
            Files.createSymbolicLink(linked, existing.getFileName());
        } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
            assertNoOwnedStages();
            return;
        }
        assertThrows(IOException.class, () ->
                SvgExportEngine.exportSvg(sample("No write"), linked.toFile(), 72));
        assertArrayEquals(old, Files.readAllBytes(existing));
        assertTrue(Files.isSymbolicLink(linked));
        assertNoOwnedStages();
    }
}
