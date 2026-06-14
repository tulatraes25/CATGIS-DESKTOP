package ar.com.catgis.layout;

import ar.com.catgis.AppContext;
import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.CatgisLogger;
import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.CatmapLayoutItem;
import ar.com.catgis.CategorizedSymbology;
import ar.com.catgis.CategoryStyleRule;
import ar.com.catgis.GraduatedSymbology;
import ar.com.catgis.GraduatedRangeRule;
import ar.com.catgis.MapLayoutComposerDialog;
import ar.com.catgis.MapPanel;
import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator for the layout subsystem.
 * Coordinates model, canvas, selection, toolbar, and element list.
 *
 * Also houses the business logic extracted from MapLayoutComposerDialog:
 * element CRUD, legend population, scale computations, snapshot capture,
 * layout rendering, undo/redo, and utility methods.
 */
public class LayoutController {

    private final LayoutModel model;
    private LayoutInteractionState interactionState;
    private LayoutSnapshot snapshot;
    private CanvasRenderer canvasRenderer;
    private LayoutSelectionManager selectionManager;
    private ElementListPanel elementListPanel;
    private ElementInspectorPanel inspectorPanel;

    public LayoutController(LayoutModel model) {
        this.model = model;
    }

    // --- Wiring setters ---

    public void setCanvasRenderer(CanvasRenderer cr) { canvasRenderer = cr; }
    public void setSelectionManager(LayoutSelectionManager sm) { selectionManager = sm; }
    public void setElementListPanel(ElementListPanel elp) { elementListPanel = elp; }
    public void setInspectorPanel(ElementInspectorPanel ip) { inspectorPanel = ip; }
    public void setInteractionState(LayoutInteractionState is) { interactionState = is; }
    public LayoutInteractionState getInteractionState() { return interactionState; }
    public void setSnapshot(LayoutSnapshot s) { snapshot = s; }
    public LayoutSnapshot getSnapshot() { return snapshot; }

    // --- Model accessors ---

    public LayoutModel getModel() { return model; }
    public CanvasRenderer getCanvasRenderer() { return canvasRenderer; }
    public LayoutSelectionManager getSelectionManager() { return selectionManager; }

    // =========================================================================
    // Element operations
    // =========================================================================

    public void addMapFrame() { addDefaultElement("map"); }
    public void addLegend() { addDefaultElement("legend"); }
    public void addScaleBar() { addDefaultElement("scale"); }
    public void addNorthArrow() { addDefaultElement("north"); }
    public void addText() { addDefaultElement("text"); }
    public void addImage() { addDefaultElement("image"); }

    /** Stub — real implementation lives in MapLayoutComposerDialog for now (creates Swing UI). */
    private void addDefaultElement(String type) {
    }

    public void deleteSelected() {
        LayoutElement sel = model.getSelected();
        if (sel != null) {
            model.removeElement(sel.getId());
            refreshAll();
        }
    }

    public void duplicateSelected() {
        LayoutElement sel = model.getSelected();
        if (sel != null) {
            duplicateLayoutElement(sel);
            refreshAll();
        }
    }

    // =========================================================================
    // Element CRUD (extracted from MapLayoutComposerDialog)
    // =========================================================================

    /**
     * Duplicate a layout element, creating a copy offset by 5 mm in X and Y.
     * Each subtype is handled with its own constructor.
     */
    public void duplicateLayoutElement(LayoutElement src) {
        if (src instanceof LayoutMap) { LayoutMap m = new LayoutMap("map-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); m.setZOrder(model.nextZ()); m.setName(src.getName() + " copia"); model.addElement(m); return; }
        if (src instanceof LayoutLegend) { LayoutLegend l = new LayoutLegend("legend-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); l.setZOrder(model.nextZ()); l.setName(src.getName() + " copia"); l.setAutoHeight(true); l.setItems(((LayoutLegend)src).getItems()); model.addElement(l); return; }
        if (src instanceof LayoutNorthArrow) { LayoutNorthArrow n = new LayoutNorthArrow("north-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); n.setZOrder(model.nextZ()); n.setName(src.getName() + " copia"); model.addElement(n); return; }
        if (src instanceof LayoutScaleBar) { LayoutScaleBar s = new LayoutScaleBar("scale-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); s.setZOrder(model.nextZ()); s.setName(src.getName() + " copia"); model.addElement(s); return; }
        if (src instanceof LayoutLabel) { LayoutLabel l = new LayoutLabel("lbl-" + System.currentTimeMillis(), ((LayoutLabel)src).getText(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); l.setZOrder(model.nextZ()); l.setName(src.getName() + " copia"); model.addElement(l); return; }
        if (src instanceof LayoutImage) { LayoutImage i = new LayoutImage("img-" + System.currentTimeMillis(), null, src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); i.setZOrder(model.nextZ()); i.setName(src.getName() + " copia"); model.addElement(i); return; }
        if (src instanceof LayoutTable) { LayoutTable t = new LayoutTable("table-" + System.currentTimeMillis(), src.getBoundsMm().x + 5, src.getBoundsMm().y + 5, src.getBoundsMm().width, src.getBoundsMm().height); t.setZOrder(model.nextZ()); t.setName(src.getName() + " copia"); model.addElement(t); return; }
    }

    public LayoutElement findElementByType(Class<?> type) {
        for (LayoutElement el : model.getElements()) if (type.isInstance(el)) return el;
        return null;
    }

    /**
     * Counts elements whose name starts with the given prefix.
     * Returns the next available suffix number (max existing + 1).
     */
    public int countOfType(String prefix) {
        int c = 1;
        for (LayoutElement e : model.getElements()) {
            if (e.getName() != null && e.getName().startsWith(prefix)) {
                try { int v = Integer.parseInt(e.getName().substring(prefix.length()).trim()); c = Math.max(c, v + 1); } catch (Exception ignored) { c++; }
            }
        }
        return c;
    }

    public LayoutElement findElementById(String id) {
        for (LayoutElement el : model.getElements()) if (el.getId().equals(id)) return el;
        return null;
    }

    public LayoutElement findEl(String id) { for (LayoutElement e : model.getElements()) if (e.getId().equals(id)) return e; return null; }

    public LayoutLabel findLayoutLabelByName(String expected) {
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutLabel label) {
                String name = label.getName();
                if (name != null && name.equalsIgnoreCase(expected)) {
                    return label;
                }
            }
        }
        return null;
    }

    public boolean hasLayoutElement(Class<? extends LayoutElement> type) {
        for (LayoutElement el : model.getElements()) {
            if (type.isInstance(el)) {
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // Alignment
    // =========================================================================

    /**
     * Align selected (non-locked) elements by mode:
     * 0=left, 1=center-h, 2=right, 3=top, 4=center-v, 5=bottom.
     */
    public void alignElements(int mode) {
        List<LayoutElement> sel = new ArrayList<>();
        for (LayoutElement el : model.getElements()) if (el.isSelected() && !el.isLocked()) sel.add(el);
        if (sel.size() < 2) return;
        pushUndoGroup(sel);
        double minX = sel.stream().mapToDouble(e -> e.getBoundsMm().x).min().orElse(0);
        double maxX = sel.stream().mapToDouble(e -> e.getBoundsMm().x + e.getBoundsMm().width).max().orElse(0);
        double minY = sel.stream().mapToDouble(e -> e.getBoundsMm().y).min().orElse(0);
        double maxY = sel.stream().mapToDouble(e -> e.getBoundsMm().y + e.getBoundsMm().height).max().orElse(0);
        for (LayoutElement el : sel) {
            double x = el.getBoundsMm().x, y = el.getBoundsMm().y, w = el.getBoundsMm().width, h = el.getBoundsMm().height;
            switch (mode) {
                case 0: x = minX; break;
                case 1: x = (minX + maxX - w) / 2; break;
                case 2: x = maxX - w; break;
                case 3: y = minY; break;
                case 4: y = (minY + maxY - h) / 2; break;
                case 5: y = maxY - h; break;
            }
            el.setBoundsMm(x, y, w, h);
        }
    }

    // =========================================================================
    // Legend population
    // =========================================================================

    public void populateLegendFromProject(LayoutLegend legend) {
        legend.getItems().clear();
        if (ctxProject() == null || ctxProject().getLayers() == null) return;
        for (Layer layer : ctxProject().getLayers()) {
            if (layer == null || !layer.isVisible()) continue;
            String name = layer.getName();
            if (name == null) continue;
            if (LayoutLegend.isBasemapName(name)) continue;
            if (layer instanceof OnlineTileLayer || layer instanceof OnlineWmsLayer) continue;

            String gtype = resolveGeometryType(layer);

            boolean hasGraduated = addGraduatedLegendItems(legend, layer, gtype);
            if (hasGraduated) continue;

            boolean hasCategorized = addCategorizedLegendItems(legend, layer, gtype);
            if (hasCategorized) continue;

            Color c = resolveLayerColor(layer);
            LayoutLegend.LegendItem item = new LayoutLegend.LegendItem(name, c, gtype);
            item.catalogSymbolId = layer.getCatalogSymbolId();
            item.pointSymbolStyle = layer.getPointSymbolStyle();
            item.lineSymbolStyle = layer.getLineSymbolStyle();
            item.polygonFillStyle = layer.getPolygonFillStyle();
            item.strokeColor = layer.getBorderColor() != null ? layer.getBorderColor() : layer.getLineColor();
            legend.getItems().add(item);
        }
    }

    public boolean addGraduatedLegendItems(LayoutLegend legend, Layer layer, String gtype) {
        GraduatedSymbology sym = null;
        if (gtype.contains("POINT")) sym = layer.getPointGraduatedSymbology();
        else if (gtype.contains("LINE")) sym = layer.getLineGraduatedSymbology();
        else if (gtype.contains("POLYGON")) sym = layer.getPolygonGraduatedSymbology();
        if (sym == null || !sym.isConfigured()) return false;

        for (GraduatedRangeRule rule : sym.getRules()) {
            LayoutLegend.LegendItem item = new LayoutLegend.LegendItem(rule.getLabel(), rule.getPrimaryColor(), gtype);
            item.strokeColor = rule.getSecondaryColor() != null ? rule.getSecondaryColor() : rule.getPrimaryColor().darker();
            if ("POINT".equals(gtype)) {
                item.pointSymbolStyle = layer.getPointSymbolStyle();
                item.catalogSymbolId = rule.getCatalogSymbolId();
            } else if ("LINE".equals(gtype)) {
                item.lineSymbolStyle = rule.getLineStyle();
                item.strokeColor = rule.getSecondaryColor() != null ? rule.getSecondaryColor() : rule.getPrimaryColor();
            } else {
                item.polygonFillStyle = rule.getPolygonFillStyle();
            }
            legend.getItems().add(item);
        }
        return true;
    }

    public boolean addCategorizedLegendItems(LayoutLegend legend, Layer layer, String gtype) {
        CategorizedSymbology sym = null;
        if (gtype.contains("POINT")) sym = layer.getPointCategorizedSymbology();
        else if (gtype.contains("LINE")) sym = layer.getLineCategorizedSymbology();
        else if (gtype.contains("POLYGON")) sym = layer.getPolygonCategorizedSymbology();
        if (sym == null || !sym.isConfigured()) return false;

        for (CategoryStyleRule rule : sym.getRules().values()) {
            String label = rule.getValue();
            if (label.isEmpty()) label = "(sin valor)";
            LayoutLegend.LegendItem item = new LayoutLegend.LegendItem(label, rule.getPrimaryColor(), gtype);
            item.strokeColor = rule.getSecondaryColor() != null ? rule.getSecondaryColor() : rule.getPrimaryColor().darker();
            if ("POINT".equals(gtype)) {
                item.pointSymbolStyle = rule.getPointSymbolStyle();
                item.catalogSymbolId = rule.getCatalogSymbolId();
            } else if ("LINE".equals(gtype)) {
                item.lineSymbolStyle = rule.getLineStyle();
                item.strokeColor = rule.getSecondaryColor() != null ? rule.getSecondaryColor() : rule.getPrimaryColor();
            } else {
                item.polygonFillStyle = rule.getPolygonFillStyle();
            }
            legend.getItems().add(item);
        }
        return true;
    }

    public Color resolveLayerColor(Layer layer) {
        if (layer.getPointColor() != null && !layer.getPointColor().equals(Color.BLUE)) return layer.getPointColor();
        if (layer.getLineColor() != null && !layer.getLineColor().equals(Color.RED)) return layer.getLineColor();
        if (layer.getFillColor() != null) return layer.getFillColor();
        return new Color(0x1976D2);
    }

    public String resolveGeometryType(Layer layer) {
        ShapefileData data = ctxMapPanel() != null ? ctxMapPanel().getShapefileData(layer) : null;
        if (data == null) return "VECTOR";
        String family = VectorLayerUtils.resolveGeometryFamily(data);
        return family != null ? family : "VECTOR";
    }

    // =========================================================================
    // Scale / Viewport computations
    // =========================================================================

    public Double parseScaleDenominator(String value) {
        String text = safeTrim(value);
        if (text.isBlank()) {
            return null;
        }
        int colonIndex = text.indexOf(':');
        if (colonIndex >= 0 && colonIndex < text.length() - 1) {
            text = text.substring(colonIndex + 1);
        }
        text = text.replaceAll("[^0-9]", "");
        if (text.isBlank()) {
            return null;
        }
        try {
            double denominator = Double.parseDouble(text);
            return denominator > 0 ? denominator : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String formatScaleDenominator(double denominator) {
        if (denominator <= 0) {
            return "Escala no disponible";
        }
        return "1:" + new DecimalFormat("#,##0").format(Math.round(denominator));
    }

    public double estimateMapScale() {
        try {
            MapPanel mp = ctxMapPanel();
            if (mp != null) {
                double denom = mp.getCurrentScaleDenominator();
                if (denom > 0) return denom;
            }
        } catch (Exception ignored) { CatgisLogger.warn("LayoutController: operation failed", ignored); }
        return 10000;
    }

    public double estimateRepresentativeScaleMeters(int mapPixelWidth) {
        if (ctxMapPanel() == null || mapPixelWidth <= 0) {
            return 0;
        }

        double zoomFactor = Math.max(ctxMapPanel().getZoomFactor(), 0.000001d);
        double projectWidth = Math.max(1d, mapPixelWidth / zoomFactor);
        String projectCrs = ctxProject() != null ? ctxProject().getProjectCRS() : "";
        if (projectCrs == null) {
            projectCrs = "";
        }
        projectCrs = CRSDefinitions.normalizeCode(projectCrs);

        if (isGeographic(projectCrs)) {
            double centerLat = ctxMapPanel().getViewMinY()
                    + (Math.max(1, ctxMapPanel().getHeight()) / 2d) / zoomFactor;
            double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerLat));
            metersPerDegreeLon = Math.max(1d, Math.abs(metersPerDegreeLon));
            return projectWidth * metersPerDegreeLon;
        }
        return projectWidth;
    }

    public boolean isGeographic(String projectCrs) {
        return "EPSG:4326".equalsIgnoreCase(projectCrs)
                || "EPSG:4258".equalsIgnoreCase(projectCrs)
                || "EPSG:4269".equalsIgnoreCase(projectCrs)
                || "EPSG:4674".equalsIgnoreCase(projectCrs)
                || "EPSG:4190".equalsIgnoreCase(projectCrs)
                || "EPSG:4221".equalsIgnoreCase(projectCrs);
    }

    public double computeCurrentScaleDenominator(LayoutSettings settings) {
        if (settings == null || snapshot == null) {
            return 0d;
        }
        syncHardcodedLayoutFlagsFromModel();
        Dimension previewSize = settings.pageSize().pixelSize(settings.orientation(), MapLayoutComposerDialog.PREVIEW_RENDER_DPI);
        LayoutRenderResult result = LayoutPageRenderer.renderResult(
                settings,
                snapshot,
                previewSize.width,
                previewSize.height,
                interactionState,
                MapLayoutComposerDialog.PREVIEW_RENDER_DPI
        );
        return result.exactScaleDenominator();
    }

    // =========================================================================
    // Utility methods
    // =========================================================================

    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String formatDistance(double meters) {
        if (meters <= 0) {
            return "Escala no disponible";
        }
        if (meters >= 1000d) {
            return new DecimalFormat("#,##0.## km").format(meters / 1000d);
        }
        return new DecimalFormat("#,##0 m").format(meters);
    }

    public String currentProjectName() {
        if (ctxProject() == null || ctxProject().getName() == null || ctxProject().getName().isBlank()) {
            return "Proyecto actual";
        }
        return ctxProject().getName();
    }

    public String currentProjectCrs() {
        return CRSDefinitions.getLabelForCode(currentProjectCrsCode());
    }

    public String currentProjectCrsCode() {
        String code = ctxProject() != null ? ctxProject().getProjectCRS() : "";
        return CRSDefinitions.normalizeCode(code != null ? code : "");
    }

    public String defaultTitle() {
        return currentProjectName();
    }

    public String defaultSubtitle() {
        return "Vista cartografica generada desde la vista actual";
    }

    public String defaultFooter() {
        return "Generado en CATGIS | " + MapLayoutComposerDialog.FOOTER_DATE.format(LocalDateTime.now());
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public String getTypeIcon(LayoutElement el) {
        if (el instanceof LayoutMap) return "\uD83D\uDDFA";
        if (el instanceof LayoutLegend) return "\uD83D\uDCCB";
        if (el instanceof LayoutNorthArrow) return "\uD83E\uDDED";
        if (el instanceof LayoutScaleBar) return "\uD83D\uDCCF";
        if (el instanceof LayoutImage) return "\uD83D\uDDBC";
        if (el instanceof LayoutEllipse) return "\u2B55";
        if (el instanceof LayoutLine) return "\u2795";
        if (el instanceof LayoutRectangle) return "\u25AD";
        if (el instanceof LayoutTable) return "\uD83D\uDCCA";
        if (el instanceof LayoutCartouche) return "\uD83D\uDCC4";
        if (el instanceof LayoutGraticule) return "\uD83D\uDCC8";
        if (el instanceof LayoutLabel) return "\uD83D\uDCDD";
        return "\u25A1";
    }

    public String extractNameFromDisplay(String display) {
        if (display == null) return "";
        String s = display.replaceAll("^\u25C9 |^\u25CB |\uD83D\uDD12 |\uD83D\uDD13 |^\\> |^  ", "");
        s = s.replaceAll("\\s*\\(oculto\\)|\\s*\\(bloq\\)", "");
        return s.trim();
    }

    // =========================================================================
    // Snapshot / Undo-Redo
    // =========================================================================

    public LayoutSnapshot captureSnapshot() {
        if (ctxMapPanel() == null) {
            return new LayoutSnapshot(
                    new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB),
                    visibleLayers(),
                    currentProjectName(),
                    currentProjectCrs(),
                    currentProjectCrsCode(),
                    "Escala no disponible",
                    0,
                    0,
                    0,
                    1d,
                    1200,
                    800
            );
        }

        int mapWidth = Math.max(ctxMapPanel().getWidth(), 1200);
        int mapHeight = Math.max(ctxMapPanel().getHeight(), 800);
        BufferedImage mapImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mapImage.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, mapWidth, mapHeight);
            ctxMapPanel().paint(g2);
        } finally {
            g2.dispose();
        }
        mapImage = trimOuterWhitespace(mapImage);

        double scaleMeters = estimateRepresentativeScaleMeters(mapWidth);
        return new LayoutSnapshot(
                mapImage,
                visibleLayers(),
                currentProjectName(),
                currentProjectCrs(),
                currentProjectCrsCode(),
                formatDistance(scaleMeters),
                scaleMeters,
                ctxMapPanel().getViewMinX(),
                ctxMapPanel().getViewMinY(),
                Math.max(ctxMapPanel().getZoomFactor(), 0.000001d),
                Math.max(1, ctxMapPanel().getWidth()),
                Math.max(1, ctxMapPanel().getHeight())
        );
    }

    public void pushUndo(LayoutElement el, boolean isDelete) {
        model.saveSnapshot();
    }

    public void pushUndoGroup(List<LayoutElement> elements) {
        model.saveSnapshot();
    }

    public void undo() {
        if (model.canUndo()) {
            model.undo();
            refreshAll();
        }
    }

    public void redo() {
        if (model.canRedo()) {
            model.redo();
            refreshAll();
        }
    }

    public BufferedImage trimOuterWhitespace(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 2 || height <= 2) {
            return image;
        }

        int left = 0;
        int right = width - 1;
        int top = 0;
        int bottom = height - 1;

        while (left < right && isNearWhiteColumn(image, left)) {
            left++;
        }
        while (right > left && isNearWhiteColumn(image, right)) {
            right--;
        }
        while (top < bottom && isNearWhiteRow(image, top)) {
            top++;
        }
        while (bottom > top && isNearWhiteRow(image, bottom)) {
            bottom--;
        }

        if (left <= 0 && right >= width - 1 && top <= 0 && bottom >= height - 1) {
            return image;
        }
        int croppedWidth = Math.max(1, right - left + 1);
        int croppedHeight = Math.max(1, bottom - top + 1);
        if (croppedWidth < width / 4 || croppedHeight < height / 4) {
            return image;
        }
        BufferedImage cropped = image.getSubimage(left, top, croppedWidth, croppedHeight);
        BufferedImage copy = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D copyGraphics = copy.createGraphics();
        try {
            copyGraphics.drawImage(cropped, 0, 0, null);
        } finally {
            copyGraphics.dispose();
        }
        return copy;
    }

    public boolean isNearWhiteColumn(BufferedImage image, int x) {
        for (int y = 0; y < image.getHeight(); y += Math.max(1, image.getHeight() / 120)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    public boolean isNearWhiteRow(BufferedImage image, int y) {
        for (int x = 0; x < image.getWidth(); x += Math.max(1, image.getWidth() / 160)) {
            if (!isNearWhite(image.getRGB(x, y))) {
                return false;
            }
        }
        return true;
    }

    public boolean isNearWhite(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        if (alpha < 10) {
            return true;
        }
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return red >= 245 && green >= 245 && blue >= 245;
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    /**
     * Renders all layout elements into the supplied Graphics2D context.
     * Handles selection visuals, hover highlights, and resize handles.
     */
    public void drawLayoutModelOverlay(Graphics2D g2, LayoutSettings settings, int pageX, int pageY, double scale) {
        if (model.size() == 0) return;
        double dpi = settings.dpi();
        PageSizePreset ps = settings.pageSize();
        double wMm = ps.widthMm;
        double hMm = ps.heightMm;
        if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
        LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.PREVIEW, dpi, wMm, hMm);
        for (LayoutElement el : model.getVisibleElementsSortedByZ()) {
            if (el instanceof LayoutScaleBar) {
                double mapScale = estimateMapScale();
                for (LayoutElement m : model.getElements()) {
                    if (m instanceof LayoutMap && ((LayoutMap)m).isOwnExtent()) {
                        double zoom = ((LayoutMap)m).getOwnZoomFactor();
                        if (zoom > 0) mapScale = Math.max(100, estimateMapScale() * zoom / Math.max(((LayoutMap)m).getOwnZoomFactor(), 1));
                    }
                }
                ((LayoutScaleBar) el).setMapScaleDenominator(Math.max(100, mapScale));
            }
            Graphics2D g2el = (Graphics2D) g2.create();
            try {
                g2el.translate(pageX, pageY);
                g2el.scale(scale, scale);
                el.render(g2el, ctx);
                int px = ctx.mmToPxInt(el.getBoundsMm().x);
                int py = ctx.mmToPxInt(el.getBoundsMm().y);
                int pw = ctx.mmToPxInt(el.getBoundsMm().width);
                int ph = ctx.mmToPxInt(el.getBoundsMm().height);

                // Hover highlight (if passed from previewPanel)
                if (el.isSelected()) {
                    g2el.setColor(new Color(0x1976D2));
                    g2el.setStroke(new BasicStroke(el.isLocked() ? 1f : 2f));
                    g2el.drawRect(px, py, pw, ph);
                    if (!el.isLocked()) {
                        int hs = 7;
                        int[][] positions = {{px-hs, py-hs}, {px+pw/2-hs/2, py-hs}, {px+pw-hs, py-hs},
                            {px-hs, py+ph/2-hs/2}, {px+pw-hs, py+ph/2-hs/2},
                            {px-hs, py+ph-hs}, {px+pw/2-hs/2, py+ph-hs}, {px+pw-hs, py+ph-hs}};
                        for (int[] p : positions) {
                            g2el.setColor(Color.WHITE);
                            g2el.fillRect(p[0], p[1], hs, hs);
                            g2el.setColor(new Color(0x1976D2));
                            g2el.setStroke(new BasicStroke(1.2f));
                            g2el.drawRect(p[0], p[1], hs, hs);
                        }
                        String nm = el.getName();
                        if (nm != null && !nm.isEmpty()) {
                            g2el.setFont(new Font("SansSerif", Font.PLAIN, 8));
                            int lw = g2el.getFontMetrics().stringWidth(nm);
                            g2el.setColor(new Color(0x1976D2));
                            g2el.fillRect(px, py - 14, lw + 6, 14);
                            g2el.setColor(Color.WHITE);
                            g2el.drawString(nm, px + 3, py - 2);
                        }
                    }
                }
            } finally { g2el.dispose(); }
        }
    }

    /**
     * Render the full layout into a BufferedImage for export.
     */
    public BufferedImage renderLayout(LayoutSettings settings, Dimension size) {
        syncHardcodedLayoutFlagsFromModel();
        BufferedImage base = LayoutPageRenderer.render(settings, snapshot, size.width, size.height, interactionState, settings.dpi());
        if (model.size() > 0) {
            Graphics2D g2 = base.createGraphics();
            try {
                PageSizePreset ps = settings.pageSize();
                double wMm = ps.widthMm, hMm = ps.heightMm;
                if (settings.orientation() == PageOrientation.LANDSCAPE) { double tmp = wMm; wMm = hMm; hMm = tmp; }
                LayoutRenderContext ctx = new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, settings.dpi(), wMm, hMm);
                for (LayoutElement el : model.getVisibleElementsSortedByZ()) {
                    if (el instanceof LayoutScaleBar) {
                        ((LayoutScaleBar) el).setMapScaleDenominator(Math.max(100, estimateMapScale()));
                    }
                    el.render(g2, ctx);
                }
            } finally { g2.dispose(); }
        }
        return base;
    }

    // =========================================================================
    // Sync
    // =========================================================================

    /**
     * Model-level portion of sync: updates interactionState flags based on which
     * elements exist in the model. The UI checkbox update (legendCheck) is handled
     * by the caller.
     */
    public void syncHardcodedLayoutFlagsFromModel() {
        boolean hasHeaderLabels = false;
        boolean hasCartoucheLabels = false;
        for (LayoutElement el : model.getElements()) {
            if (el instanceof LayoutLabel) {
                String n = el.getName();
                if (n != null && (n.equals("Titulo") || n.startsWith("Titulo ") || n.equals("Subtitulo"))) {
                    hasHeaderLabels = true;
                }
                if (n != null && (n.equals("Empresa") || n.equals("Cartografo") || n.equals("Estudio") || n.startsWith("Datos"))) {
                    hasCartoucheLabels = true;
                }
            }
        }
        if (hasHeaderLabels) {
            interactionState.setElementVisible(LayoutElementType.HEADER, false);
        }
        if (hasLayoutElement(LayoutLegend.class)) {
            interactionState.setElementVisible(LayoutElementType.LEGEND, false);
        }
        if (hasLayoutElement(LayoutNorthArrow.class)) {
            interactionState.setElementVisible(LayoutElementType.NORTH, false);
        }
        if (hasLayoutElement(LayoutScaleBar.class)) {
            interactionState.setElementVisible(LayoutElementType.SCALE, false);
        }
        if (hasLayoutElement(LayoutCartouche.class)) {
            interactionState.setElementVisible(LayoutElementType.CARTOUCHE, false);
        }
        if (hasCartoucheLabels) {
            interactionState.setElementVisible(LayoutElementType.CARTOUCHE, false);
        }
        if (hasLayoutElement(LayoutMap.class)) {
            interactionState.setElementVisible(LayoutElementType.MAP_CONTENT, false);
        }
    }

    public void persistCatmapItems(List<CatmapLayoutItem> items) {
        if (ctxProject() == null) {
            return;
        }
        ctxProject().setCatmapItems(items);
        CatgisDesktopApp.markProjectDirty();
    }

    // =========================================================================
    // Refresh / UI notification
    // =========================================================================

    public void refreshAll() {
        if (elementListPanel != null) elementListPanel.refresh();
        if (inspectorPanel != null) inspectorPanel.refresh();
    }

    /**
     * Notify that the selection changed from the element list.
     */
    public void onListSelectionChanged() {
        if (elementListPanel == null) return;
        LayoutElement sel = elementListPanel.getSelectedElement();
        if (sel != null && selectionManager != null) {
            selectionManager.select(sel);
        }
        if (inspectorPanel != null) inspectorPanel.refresh();
    }

    /**
     * Notify that a canvas click occurred at mm coordinates.
     */
    public void onCanvasClicked(double xMm, double yMm) {
        if (selectionManager == null) return;
        LayoutElement hit = selectionManager.hitTest(xMm, yMm);
        if (hit != null) {
            selectionManager.select(hit);
        } else {
            selectionManager.clearSelection();
        }
        refreshAll();
    }

    // --- Export ---

    public void exportPdf() {
        // Delegated to concrete implementation
    }

    // =========================================================================
    // Context helpers (delegate to MapLayoutComposerDialog statics)
    // =========================================================================

    private static Project ctxProject() {
        return MapLayoutComposerDialog.ctxProject();
    }

    private static MapPanel ctxMapPanel() {
        return MapLayoutComposerDialog.ctxMapPanel();
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    public List<Layer> visibleLayers() {
        List<Layer> visible = new ArrayList<>();
        if (ctxProject() == null || ctxProject().getLayers() == null) {
            return visible;
        }
        for (Layer layer : ctxProject().getLayers()) {
            if (layer != null && isProjectLayerEffectivelyVisible(layer)) {
                visible.add(layer);
            }
        }
        return visible;
    }

    public boolean isProjectLayerEffectivelyVisible(Layer layer) {
        if (layer == null) {
            return false;
        }
        if (ctxProject() != null) {
            return ctxProject().isLayerEffectivelyVisible(layer);
        }
        return layer.isVisible();
    }
}
