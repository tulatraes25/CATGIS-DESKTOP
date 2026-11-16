package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.QgisQptImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Registry/QPT templates apply to the standalone layout with their sheet geometry. */
class TemplateApplicationServiceContractTest {

    @TempDir
    Path directory;

    @Test
    void catalogExposesEveryCategoryAndTemplate() {
        List<TemplateApplicationService.TemplateEntry> templates =
                TemplateApplicationService.templates();
        assertTrue(templates.size() >= 70, "expected the full registry catalog");
        Map<String, List<TemplateApplicationService.TemplateEntry>> grouped =
                TemplateApplicationService.byCategory();
        assertTrue(grouped.size() >= 10, "expected every category to be browseable");
        int groupedCount = grouped.values().stream().mapToInt(List::size).sum();
        assertEquals(templates.size(), groupedCount);
        assertTrue(templates.stream().allMatch(entry ->
                entry.key() != null && !entry.key().isBlank()
                        && entry.displayName() != null));
    }

    @Test
    void applyingATemplateReplacesTheLayoutAndInfersItsSheet() throws Exception {
        String key = TemplateApplicationService.templates().get(0).key();
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("old", "Viejo", 10, 10, 100, 20));

        TemplateApplicationService.applyTemplate(model, key);

        assertFalse(model.getElements().isEmpty());
        assertTrue(model.getElements().stream().noneMatch(element -> "old".equals(element.getId())));
        assertEquals(PageOrientation.LANDSCAPE, model.getPageSpec().orientation());
        assertEquals("A4", model.getPageSpec().presetName());
    }

    @Test
    void unknownTemplateFailsClosed() {
        LayoutModel model = new LayoutModel();
        assertThrows(java.io.IOException.class,
                () -> TemplateApplicationService.applyTemplate(model, "NO_EXISTE"));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateApplicationService.applyTemplate(model, " "));
    }

    @Test
    void applyingAQptImportsElementsAndUsesItsPage() throws Exception {
        File qpt = directory.resolve("plantilla.qpt").toFile();
        Files.writeString(qpt.toPath(), "<Composer>"
                + "<ComposerLabel id='lbl1' x='10' y='20' width='100' height='30'>"
                + "<labelText>Hola QGIS</labelText></ComposerLabel>"
                + "</Composer>", StandardCharsets.UTF_8);

        LayoutModel model = new LayoutModel();
        TemplateApplicationService.applyQpt(model, qpt);

        assertEquals(1, model.getElements().size());
        LayoutElement element = model.getElements().get(0);
        assertTrue(element instanceof LayoutLabel);
        assertEquals("Hola QGIS", ((LayoutLabel) element).getText());
        assertEquals(LayoutPageSpec.A4_LANDSCAPE, model.getPageSpec());
    }

    @Test
    void qptWithoutSupportedElementsKeepsTheCurrentLayout() throws Exception {
        File qpt = directory.resolve("vacio.qpt").toFile();
        Files.writeString(qpt.toPath(), "<Layout></Layout>", StandardCharsets.UTF_8);
        LayoutModel model = new LayoutModel();
        model.addElement(new LayoutLabel("keep", "Se queda", 10, 10, 100, 20));

        assertThrows(java.io.IOException.class,
                () -> TemplateApplicationService.applyQpt(model, qpt));
        assertEquals(1, model.getElements().size());
        assertEquals("keep", model.getElements().get(0).getId());
    }

    @Test
    void corruptQptFailsClosed() throws Exception {
        File qpt = directory.resolve("roto.qpt").toFile();
        Files.writeString(qpt.toPath(), "esto no es XML", StandardCharsets.UTF_8);
        assertThrows(java.io.IOException.class,
                () -> TemplateApplicationService.applyQpt(new LayoutModel(), qpt));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateApplicationService.applyQpt(new LayoutModel(), null));
    }

    @Test
    void portraitQptPageIsConvertedToTheDocumentContract() {
        QgisQptImporter.ImportResult result = new QgisQptImporter.ImportResult();
        result.pageWidthMm = 210.0;
        result.pageHeightMm = 297.0;
        result.orientation = "portrait";

        LayoutPageSpec spec = TemplateApplicationService.pageSpecFromQpt(result);
        assertEquals(PageOrientation.PORTRAIT, spec.orientation());
        assertEquals("A4", spec.presetName());
        assertEquals(210.0, spec.widthMm(), 0.001);
        assertEquals(297.0, spec.heightMm(), 0.001);
        assertNotNull(spec);
    }
}
