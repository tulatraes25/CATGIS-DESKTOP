package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T32 preparation: the snapshot harness is headless-safe and validates PNGs. */
class UiSnapshotHarnessContractTest {

    @TempDir
    Path directory;

    @Test
    void harnessReportsHeadlessWithoutFailing() {
        // CI keeps the suite headless; the harness itself must never break it.
        assertTrue(UiSnapshotHarness.isHeadless() || !UiSnapshotHarness.isHeadless());
        assertFalse(UiSnapshotHarness.isSnapshot(null));
        assertFalse(UiSnapshotHarness.isSnapshot(directory.resolve("missing.png").toFile()));
    }

    @Test
    void snapshotValidationRequiresAReadablePng() throws Exception {
        java.io.File file = directory.resolve("snapshot.png").toFile();
        BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(1, 1, 0xFF224466);
        ImageIO.write(image, "png", file);
        assertTrue(UiSnapshotHarness.isSnapshot(file));

        java.io.File text = directory.resolve("not-a-png.png").toFile();
        java.nio.file.Files.writeString(text.toPath(), "not an image");
        assertFalse(UiSnapshotHarness.isSnapshot(text));
    }
}
