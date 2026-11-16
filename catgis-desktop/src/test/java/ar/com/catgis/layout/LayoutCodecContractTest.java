package ar.com.catgis.layout;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The layout engine talks to CATMAP serialization only through the codec bridge. */
class LayoutCodecContractTest {

    @TempDir
    Path directory;

    @AfterEach
    void clearInstalls() {
        LayoutCodecs.reset();
    }

    @Test
    void lazyCatmapBridgeRoundTripsAnElementRecord() {
        LayoutLabel label = new LayoutLabel("lbl", "Hola CATMAP", 10, 20, 120, 14);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setColor(Color.BLUE);

        String record = LayoutCodecs.elements().encode(label);
        assertNotNull(record);
        assertTrue(record.startsWith("ELEMENT|LayoutLabel|"));

        LayoutElement decoded = LayoutCodecs.elements().decode(record);
        assertNotNull(decoded);
        assertEquals("Hola CATMAP", ((LayoutLabel) decoded).getText());
        assertEquals("lbl", decoded.getId());
        assertEquals(10.0, decoded.getBoundsMm().x, 1.0e-9);
        assertEquals(12.0, ((LayoutLabel) decoded).getFont().getSize2D(), 1.0e-9);
    }

    @Test
    void lazyCatmapBridgeRoundTripsAWholeDocument() throws Exception {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.PORTRAIT));
        model.addElement(new LayoutLabel("lbl", "Documento CATMAP", 12, 10, 200, 14));
        File file = directory.resolve("codec.catmap").toFile();

        LayoutCodecs.documents().save(model, file);
        LayoutModel loaded = LayoutCodecs.documents().load(file);

        assertEquals("A3", loaded.getPageSpec().presetName());
        assertEquals(PageOrientation.PORTRAIT, loaded.getPageSpec().orientation());
        assertEquals(1, loaded.getElements().size());
        assertEquals("Documento CATMAP",
                ((LayoutLabel) loaded.getElements().get(0)).getText());
    }

    @Test
    void undoSnapshotsKeepWorkingThroughTheBridge() {
        LayoutModel model = new LayoutModel();
        LayoutLabel label = new LayoutLabel("lbl", "Antes", 10, 10, 100, 14);
        model.addElement(label);
        model.saveSnapshot();
        label.setText("Despues");

        model.undo();

        assertEquals("Antes", ((LayoutLabel) model.getElements().get(0)).getText());
    }

    @Test
    void explicitCodecInstallOverridesTheLazyBridge() {
        LayoutCodecs.install(new LayoutElementCodec() {
            @Override
            public String encode(LayoutElement element) {
                return "CUSTOM";
            }

            @Override
            public LayoutElement decode(String record) {
                return new LayoutLabel("custom", record, 0, 0, 50, 10);
            }
        });
        assertEquals("CUSTOM", LayoutCodecs.elements().encode(
                new LayoutLabel("lbl", "x", 0, 0, 10, 10)));
        assertEquals("CUSTOM", ((LayoutLabel) LayoutCodecs.elements().decode("CUSTOM")).getText());
    }

    @Test
    void explicitDocumentCodecInstallIsUsedByTemplateManager() throws Exception {
        AtomicInteger saves = new AtomicInteger();
        LayoutCodecs.installDocument(new LayoutDocumentCodec() {
            @Override
            public void save(LayoutModel model, File file) {
                saves.incrementAndGet();
            }

            @Override
            public LayoutModel load(File file) {
                return new LayoutModel();
            }
        });
        LayoutTemplateManager.saveTemplate(
                directory.resolve("t.catmap").toFile(), new LayoutModel(),
                297.0, 210.0, "landscape");
        assertEquals(1, saves.get());
    }
}
