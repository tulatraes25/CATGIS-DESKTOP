package ar.com.catgis.catmap;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.LayoutTemplateManager;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import ar.com.catgis.layout.QgisQptImporter;
import ar.com.catgis.layout.TemplateRegistry;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Applies CATMAP templates (registry catalog or QGIS {@code .qpt}) to a layout
 * model, including the sheet geometry defined by the template.
 */
public final class TemplateApplicationService {

    private TemplateApplicationService() {}

    /** One catalog entry, ready for the standalone gallery. */
    public record TemplateEntry(String key, String displayName, String categoryKey,
                                String categoryLabel) {}

    /** Gallery selection: a registry template key or an imported QPT file. */
    public record TemplateSelection(String templateKey, File qptFile) {}

    /** Full catalog ordered by category. */
    public static List<TemplateEntry> templates() {
        List<TemplateEntry> result = new ArrayList<>();
        for (TemplateRegistry.Entry entry : TemplateRegistry.getAll()) {
            result.add(new TemplateEntry(entry.key, entry.displayName,
                    entry.category.name(), entry.category.getLabel()));
        }
        return result;
    }

    /** Catalog grouped by category label, preserving registry order. */
    public static Map<String, List<TemplateEntry>> byCategory() {
        Map<String, List<TemplateEntry>> grouped = new LinkedHashMap<>();
        for (TemplateEntry entry : templates()) {
            grouped.computeIfAbsent(entry.categoryLabel(), key -> new ArrayList<>()).add(entry);
        }
        return grouped;
    }

    /**
     * Replace the layout with a registry template and adopt the sheet geometry
     * the template was designed for (A4 landscape for the current catalog).
     */
    public static void applyTemplate(LayoutModel model, String key) throws IOException {
        Objects.requireNonNull(model, "CATMAP layout model");
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("CATMAP: template key is required.");
        }
        if (TemplateRegistry.get(key) == null) {
            throw new IOException("CATMAP: plantilla desconocida: " + key);
        }
        LayoutTemplateManager.applyTemplate(key, model);
        model.setPageSpec(inferPageSpec(model.getElements()));
    }

    /**
     * Replace the layout with a QGIS {@code .qpt} template, adopting its page
     * size and orientation. The model is only modified when the QPT yields at
     * least one supported element.
     */
    public static void applyQpt(LayoutModel model, File qptFile) throws IOException {
        Objects.requireNonNull(model, "CATMAP layout model");
        if (qptFile == null) {
            throw new IllegalArgumentException("CATMAP: QPT file is required.");
        }
        QgisQptImporter.ImportResult result;
        try {
            result = QgisQptImporter.importQpt(qptFile);
        } catch (Exception invalid) {
            throw new IOException("CATMAP: no se pudo importar el QPT: " + qptFile.getName(),
                    invalid);
        }
        if (result == null || result.imported.isEmpty()) {
            throw new IOException("CATMAP: el QPT no contiene elementos soportados.");
        }
        for (LayoutElement element : new ArrayList<>(model.getElements())) {
            model.removeElement(element.getId());
        }
        for (LayoutElement element : result.imported) {
            model.addElement(element);
        }
        model.setPageSpec(pageSpecFromQpt(result));
    }

    /** Page geometry declared by the QPT (millimetres and orientation). */
    static LayoutPageSpec pageSpecFromQpt(QgisQptImporter.ImportResult result) {
        PageOrientation orientation = "portrait".equalsIgnoreCase(result.orientation)
                ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE;
        double effectiveWidth = result.pageWidthMm;
        double effectiveHeight = result.pageHeightMm;
        double portraitWidth = orientation == PageOrientation.LANDSCAPE
                ? effectiveHeight : effectiveWidth;
        double portraitHeight = orientation == PageOrientation.LANDSCAPE
                ? effectiveWidth : effectiveHeight;
        return LayoutPageSpec.fromPortraitDimensions(portraitWidth, portraitHeight, orientation);
    }

    /**
     * Smallest standard landscape preset that contains every element (the
     * registry catalog is designed for A4 landscape); A3 is the fallback.
     */
    static LayoutPageSpec inferPageSpec(List<LayoutElement> elements) {
        double maxX = 0;
        double maxY = 0;
        for (LayoutElement element : elements) {
            Rectangle2D.Double bounds = element.getBoundsMm();
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }
        for (PageSizePreset preset : new PageSizePreset[]{
                PageSizePreset.A4, PageSizePreset.A3, PageSizePreset.TABLOID,
                PageSizePreset.LETTER, PageSizePreset.LEGAL, PageSizePreset.A5}) {
            double width = Math.max(preset.widthMm, preset.heightMm);
            double height = Math.min(preset.widthMm, preset.heightMm);
            if (maxX <= width + 2.0 && maxY <= height + 2.0) {
                return LayoutPageSpec.preset(preset, PageOrientation.LANDSCAPE);
            }
        }
        return LayoutPageSpec.preset(PageSizePreset.A3, PageOrientation.LANDSCAPE);
    }
}
