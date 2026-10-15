package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real PNG/JPG/PDF artifacts and fail-closed filesystem rollback contracts. */
class CatmapAtomicExportContractTest {

    @TempDir
    Path directory;

    private static LayoutModel sample() {
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("title", "MAPA CATGIS", 12, 14, 120, 20));
        return model;
    }

    private void noOwnedStages() throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            assertTrue(files.noneMatch(p -> p.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void realPngAndJpegRoundTripsPreserveDimensionsAndPixels() throws Exception {
        Path png = directory.resolve("mapa con espacios.png");
        Path jpg = directory.resolve("mapa con espacios.jpg");
        LayoutExportEngine.exportPng(sample(), png.toFile(), 72);
        LayoutExportEngine.exportJpg(sample(), jpg.toFile(), 72);

        BufferedImage decodedPng = ImageIO.read(png.toFile());
        BufferedImage decodedJpg = ImageIO.read(jpg.toFile());
        assertNotNull(decodedPng);
        assertNotNull(decodedJpg);
        assertEquals(841, decodedPng.getWidth());
        assertEquals(595, decodedPng.getHeight());
        assertEquals(decodedPng.getWidth(), decodedJpg.getWidth());
        assertEquals(decodedPng.getHeight(), decodedJpg.getHeight());
        assertEquals(0xFFFFFF, decodedPng.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0xFFFFFF, decodedJpg.getRGB(0, 0) & 0xFFFFFF);
        assertTrue(Files.size(png) > 100);
        assertTrue(Files.size(jpg) > 100);
        noOwnedStages();
    }

    @Test
    void realPdfParsesWithCorrectSinglePageAndVisibleText() throws Exception {
        Path pdf = directory.resolve("informe profesional con espacios.pdf");
        LayoutExportEngine.exportPdf(sample(), pdf.toFile(),
                new PdfExportOptions().dpi(72).title("CATMAP export integrity"));
        try (org.apache.pdfbox.pdmodel.PDDocument parsed =
                org.apache.pdfbox.Loader.loadPDF(pdf.toFile())) {
            assertEquals(1, parsed.getNumberOfPages());
            assertEquals(841.89f, parsed.getPage(0).getMediaBox().getWidth(), 0.6f);
            String extracted = new org.apache.pdfbox.text.PDFTextStripper().getText(parsed);
            assertTrue(extracted.contains("MAPA CATGIS"), "PDF must include actual vector text.");
        }
        noOwnedStages();
    }

    @Test
    void successfulReplacePublishesCompletePngJpgPdfWithoutLeftoverStages() throws Exception {
        Path png = directory.resolve("overwrite.png");
        Path jpg = directory.resolve("overwrite.jpg");
        Path pdf = directory.resolve("overwrite.pdf");
        Files.writeString(png, "OLD-PNG", StandardCharsets.UTF_8);
        Files.writeString(jpg, "OLD-JPG", StandardCharsets.UTF_8);
        Files.writeString(pdf, "OLD-PDF", StandardCharsets.UTF_8);
        LayoutExportEngine.exportPng(sample(), png.toFile(), 72);
        LayoutExportEngine.exportJpg(sample(), jpg.toFile(), 72);
        LayoutExportEngine.exportPdf(sample(), pdf.toFile(), 72);
        assertNotNull(ImageIO.read(png.toFile()));
        assertNotNull(ImageIO.read(jpg.toFile()));
        CatmapAtomicExport.verifyPdf(pdf, 297, 210);
        noOwnedStages();
    }

    @Test
    void unsupportedAtomicMoveLeavesPreviousImageByteIdentical() throws Exception {
        Path output = directory.resolve("precious mapa.png");
        byte[] original = "PREVIOUS_VALID_DOCUMENT_UNTOUCHED".getBytes(StandardCharsets.UTF_8);
        Files.write(output, original);
        IOException error = assertThrows(IOException.class,
                () -> LayoutExportEngine.exportPng(sample(), output.toFile(), 72,
                        (staged, target) -> {
                            throw new AtomicMoveNotSupportedException(
                                    staged.toString(), target.toString(),
                                    "simulated unsupported filesystem");
                        }));
        assertTrue(error.getMessage().contains("atomica"));
        assertArrayEquals(original, Files.readAllBytes(output));
        noOwnedStages();
    }

    @Test
    void writerAndValidatorErrorsCannotOverwritePreviousExport() throws Exception {
        Path output = directory.resolve("saved map.png");
        byte[] original = "UNMODIFIED_PREVIOUS_EXPORT".getBytes(StandardCharsets.UTF_8);
        Files.write(output, original);

        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                output.toFile(), staged -> {
                    Files.writeString(staged, "partially encoded", StandardCharsets.UTF_8);
                    throw new IOException("injected writer failure");
                }, staged -> {}));
        assertArrayEquals(original, Files.readAllBytes(output));
        noOwnedStages();

        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                output.toFile(),
                staged -> Files.writeString(staged, "not an actual PNG", StandardCharsets.UTF_8),
                staged -> CatmapAtomicExport.verifyImage(staged, "png", 4, 4)));
        assertArrayEquals(original, Files.readAllBytes(output));
        noOwnedStages();

        assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                output.toFile(),
                staged -> Files.writeString(staged, "%PDF-invalid", StandardCharsets.UTF_8),
                staged -> CatmapAtomicExport.verifyPdf(staged, 297, 210)));
        assertArrayEquals(original, Files.readAllBytes(output));
        noOwnedStages();
    }

    @Test
    void invalidTargetCannotBeOverwrittenAndNoStageIsCreated() throws Exception {
        Path directoryOutput = Files.createDirectory(directory.resolve("folder.jpg"));
        assertThrows(IOException.class,
                () -> LayoutExportEngine.exportJpg(sample(), directoryOutput.toFile(), 72));
        assertTrue(Files.isDirectory(directoryOutput));

        Path original = directory.resolve("original.png");
        byte[] untouched = "private document".getBytes(StandardCharsets.UTF_8);
        Files.write(original, untouched);
        Path symlink = directory.resolve("alias.png");
        try {
            Files.createSymbolicLink(symlink, original.getFileName());
        } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
            // Some Windows runners cannot create symlinks. Directory rejection
            // and unsupported-atomic tests are mandatory on every platform.
            noOwnedStages();
            return;
        }
        assertThrows(IOException.class,
                () -> LayoutExportEngine.exportPng(sample(), symlink.toFile(), 72));
        assertArrayEquals(untouched, Files.readAllBytes(original));
        assertTrue(Files.isSymbolicLink(symlink));
        noOwnedStages();
    }

    @Test
    void excessiveResolutionFailsBeforeAnyAllocationOrPublication() throws Exception {
        Path output = directory.resolve("huge output.jpg");
        byte[] original = "PREVIOUS_EXPORT".getBytes(StandardCharsets.UTF_8);
        Files.write(output, original);
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> LayoutExportEngine.exportJpg(sample(), output.toFile(), 1200));
        assertTrue(failure.getMessage().contains("pixeles"));
        assertArrayEquals(original, Files.readAllBytes(output));
        noOwnedStages();
    }

    @Test
    void noSilentNonAtomicFallbackOnGenericPublisher() throws Exception {
        Path output = directory.resolve("preserved.svg");
        byte[] original = "keep prior bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(output, original);
        IOException error = assertThrows(IOException.class, () -> CatmapAtomicExport.publish(
                output.toFile(),
                staged -> Files.writeString(staged, "COMPLETE", StandardCharsets.UTF_8),
                staged -> assertTrue(Files.size(staged) > 0),
                (staged, target) -> {
                    throw new AtomicMoveNotSupportedException(
                            staged.toString(), target.toString(), "unsupported");
                }));
        assertTrue(error.getMessage().contains("atomica"));
        assertArrayEquals(original, Files.readAllBytes(output));
        assertFalse(Files.exists(directory.resolve(".preserved.svg.part")));
        noOwnedStages();
    }
}
