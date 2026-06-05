package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class LayoutMap implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;

    private transient BufferedImage cachedImage;
    private transient long cacheKey;
    private transient int cachedWidthPx = -1;
    private transient int cachedHeightPx = -1;
    private transient long lastRenderTimeNanos = 0;
    private transient MapFrameRenderer independentRenderer;
    private transient MapFrameViewport viewport;
    // Cached constants to avoid per-render allocation
    private static final java.awt.Color PLACEHOLDER_BG = new java.awt.Color(0xE8EBF0);
    private static final java.awt.Color PLACEHOLDER_BORDER = new java.awt.Color(0xB0B8C4);
    private static final java.awt.Color PLACEHOLDER_TEXT = new java.awt.Color(0x8B95A5);
    private static final java.awt.Font PLACEHOLDER_FONT = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14);
    private static final java.awt.Font GRID_LABEL_FONT = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7);
    private boolean showGrid = false;
    private int gridCols = 3;
    private int gridRows = 3;
    private Color gridColor = new Color(0, 0, 0, 80);
    private float gridLineWidth = 0.5f;
    private boolean showGridLabels = false;
    // Grid distance mode
    private boolean gridByDistance = false;
    private double gridIntervalX = 100;   // meters or degrees
    private double gridIntervalY = 100;
    private String gridUnit = "m";
    private double gridOffsetX = 0;
    private double gridOffsetY = 0;
    // Scale control
    private double targetScaleDenominator = 0;
    // Frame style (ArcMap-like)
    private Color frameColor = new Color(0, 0, 0, 0); // transparent = no frame
    private float frameWidth = 1f;
    private int frameCornerRadius = 0;
    // Independent extent
    private boolean ownExtent = false;
    private double ownViewMinX, ownViewMinY, ownZoomFactor = 1;
    private boolean showIndicator = false; // For inset maps: show indicator rectangle

    public LayoutMap(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id;
        this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public void setName(String n) { name = n; }
    @Override public Rectangle2D.Double getBoundsMm() { return boundsMm; }
    @Override public void setBoundsMm(double x, double y, double w, double h) { boundsMm.setRect(x, y, w, h); }
    @Override public int getZOrder() { return zOrder; }
    @Override public void setZOrder(int z) { this.zOrder = z; }
    @Override public boolean isVisible() { return visible; }
    @Override public void setVisible(boolean v) { this.visible = v; }
    @Override public boolean isLocked() { return locked; }
    @Override public void setLocked(boolean l) { this.locked = l; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean s) { this.selected = s; }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        int px = ctx.mmToPxInt(boundsMm.x);
        int py = ctx.mmToPxInt(boundsMm.y);
        int pw = ctx.mmToPxInt(boundsMm.width);
        int ph = ctx.mmToPxInt(boundsMm.height);

        if (pw < 10 || ph < 10) return;

        long key = computeCacheKey();
        long now = System.nanoTime();
        // Re-render if: no cache, key changed, size changed, or more than 500ms elapsed (live update)
        boolean stale = cachedImage == null || key != cacheKey || cachedWidthPx != pw || cachedHeightPx != ph
                || (now - lastRenderTimeNanos > 500_000_000L);
        if (stale) {
            if (shouldPreferMainMapComposite()) {
                cachedImage = captureMapImage(pw, ph);
                if (cachedImage == null) {
                    cachedImage = renderIndependent(pw, ph);
                }
            } else {
                cachedImage = renderIndependent(pw, ph);
                if (cachedImage == null) {
                    cachedImage = captureMapImage(pw, ph);
                }
            }
            // If still null, create a placeholder
            if (cachedImage == null) {
                cachedImage = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = cachedImage.createGraphics();
                g.setColor(PLACEHOLDER_BG);
                g.fillRect(0, 0, pw, ph);
                g.setColor(PLACEHOLDER_BORDER);
                g.setStroke(new java.awt.BasicStroke(1f));
                g.drawRect(2, 2, pw - 4, ph - 4);
                g.setColor(PLACEHOLDER_TEXT);
                g.setFont(PLACEHOLDER_FONT);
                String msg = "Mapa del proyecto";
                int tw = g.getFontMetrics().stringWidth(msg);
                g.drawString(msg, (pw - tw) / 2, ph / 2);
                g.dispose();
            }
            cacheKey = key;
            cachedWidthPx = pw;
            cachedHeightPx = ph;
            lastRenderTimeNanos = now;
        }

        if (cachedImage != null) {
            Graphics2D mg = (Graphics2D) g2.create();
            try {
                mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                mg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                mg.setClip(px, py, pw, ph);
                mg.drawImage(cachedImage, px, py, null);
            } finally {
                mg.dispose();
            }
        } else {
            // Placeholder when no map content available (preview without project)
            g2.setColor(PLACEHOLDER_BG);
            g2.fillRect(px, py, pw, ph);
            g2.setColor(PLACEHOLDER_BORDER);
            g2.setStroke(new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 4f}, 0f));
            g2.drawRect(px + 2, py + 2, pw - 4, ph - 4);
            g2.setColor(PLACEHOLDER_TEXT);
            g2.setFont(PLACEHOLDER_FONT);
            String msg = "Mapa";
            int tw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, px + (pw - tw) / 2, py + ph / 2);
        }
        // Frame border (ArcMap-style)
        if (frameColor.getAlpha() > 0 && frameWidth > 0) {
            g2.setColor(frameColor);
            g2.setStroke(new java.awt.BasicStroke(frameWidth));
            if (frameCornerRadius > 0) {
                g2.drawRoundRect(px, py, pw, ph, frameCornerRadius, frameCornerRadius);
            } else {
                g2.drawRect(px, py, pw, ph);
            }
        }
        if (showGrid) {
            renderGrid(g2, px, py, pw, ph);
        }
    }

    private void renderGrid(Graphics2D g2, int px, int py, int pw, int ph) {
        g2.setColor(gridColor);
        g2.setStroke(new java.awt.BasicStroke(gridLineWidth, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

        if (gridByDistance && gridIntervalX > 0 && gridIntervalY > 0) {
            // CRS-based grid with coordinate labels
            double mapMinX = ownExtent ? ownViewMinX : 0;
            double mapMinY = ownExtent ? ownViewMinY : 0;
            double mapMaxX = ownExtent ? ownViewMinX + pw * ownZoomFactor : pw;
            double mapMaxY = ownExtent ? ownViewMinY + ph * ownZoomFactor : ph;

            // Determine if geographic (degrees) or projected (meters)
            boolean isGeographic = isGeographicCRS();

            // Vertical lines (X axis / Longitude)
            double startX = Math.ceil((mapMinX - gridOffsetX) / gridIntervalX) * gridIntervalX + gridOffsetX;
            for (double wx = startX; wx <= mapMaxX; wx += gridIntervalX) {
                if (wx < mapMinX) continue;
                double ratio = (wx - mapMinX) / (mapMaxX - mapMinX);
                int gx = px + (int) (ratio * pw);
                if (gx < px || gx > px + pw) continue;
                g2.drawLine(gx, py, gx, py + ph);
                if (showGridLabels) {
                    drawGridLabel(g2, formatGridCoordinate(wx, isGeographic), gx, py + ph - 2, true);
                }
            }

            // Horizontal lines (Y axis / Latitude)
            double startY = Math.ceil((mapMinY - gridOffsetY) / gridIntervalY) * gridIntervalY + gridOffsetY;
            for (double wy = startY; wy <= mapMaxY; wy += gridIntervalY) {
                if (wy < mapMinY) continue;
                double ratio = (wy - mapMinY) / (mapMaxY - mapMinY);
                int gy = py + ph - (int) (ratio * ph);
                if (gy < py || gy > py + ph) continue;
                g2.drawLine(px, gy, px + pw, gy);
                if (showGridLabels) {
                    drawGridLabel(g2, formatGridCoordinate(wy, isGeographic), px + 2, gy - 2, false);
                }
            }
        } else if (gridCols > 0 && gridRows > 0) {
            // Simple subdivision grid
            for (int i = 1; i < gridCols; i++) {
                int gx = px + (pw * i) / gridCols;
                g2.drawLine(gx, py, gx, py + ph);
            }
            for (int i = 1; i < gridRows; i++) {
                int gy = py + (ph * i) / gridRows;
                g2.drawLine(px, gy, px + pw, gy);
            }
        }
    }

    private boolean isGeographicCRS() {
        ar.com.catgis.Project proj = ar.com.catgis.CatgisDesktopApp.currentProject;
        if (proj == null) return false;
        String crs = proj.getProjectCRS();
        if (crs == null) return false;
        // EPSG codes for geographic CRS are typically in range 4000-4999
        // WGS84 is 4326, that's the most common
        return crs.contains("4326") || crs.contains("4269") || crs.contains("4283");
    }

    private String formatGridCoordinate(double value, boolean isGeographic) {
        if (isGeographic) {
            // Format as DMS (degrees, minutes, seconds)
            return formatDMS(value);
        }
        // Format as meters with appropriate precision
        if (Math.abs(value) >= 10000) return String.format("%.0f m", value);
        if (Math.abs(value) >= 100) return String.format("%.0f m", value);
        if (Math.abs(value) >= 10) return String.format("%.1f m", value);
        return String.format("%.2f m", value);
    }

    private String formatDMS(double decimalDegrees) {
        boolean negative = decimalDegrees < 0;
        double abs = Math.abs(decimalDegrees);
        int degrees = (int) abs;
        double minutesFull = (abs - degrees) * 60;
        int minutes = (int) minutesFull;
        double seconds = (minutesFull - minutes) * 60;
        String dir = negative ? (decimalDegrees >= 0 ? "E" : "W") : (decimalDegrees >= 0 ? "E" : "W");
        return String.format("%d°%02d'%04.1f\"%s", degrees, minutes, seconds, dir);
    }

    private void drawGridLabel(Graphics2D g2, String text, int x, int y, boolean horizontal) {
        java.awt.Font prev = g2.getFont();
        g2.setFont(GRID_LABEL_FONT);
        java.awt.FontMetrics fm = g2.getFontMetrics();
        g2.setColor(gridColor);
        if (horizontal) {
            int tw = fm.stringWidth(text);
            g2.drawString(text, x - tw / 2, y - 1);
        } else {
            g2.drawString(text, x + 1, y - 1);
        }
        g2.setFont(prev);
    }

    private String formatGridValue(double value, double interval) {
        if (interval >= 1000) return String.format("%.0f", value);
        if (interval >= 100) return String.format("%.0f", value);
        if (interval >= 10) return String.format("%.1f", value);
        return String.format("%.2f", value);
    }

    private long computeCacheKey() {
        long key = 31;
        if (ownExtent) {
            key = key * 31 + Double.doubleToLongBits(ownViewMinX);
            key = key * 31 + Double.doubleToLongBits(ownViewMinY);
            key = key * 31 + Double.doubleToLongBits(ownZoomFactor);
        } else {
            ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
            if (map != null) {
                key = key * 31 + Double.doubleToLongBits(map.getViewMinX());
                key = key * 31 + Double.doubleToLongBits(map.getViewMinY());
                key = key * 31 + Double.doubleToLongBits(map.getZoomFactor());
            }
        }
        ar.com.catgis.Project proj = ar.com.catgis.CatgisDesktopApp.currentProject;
        if (proj != null && proj.getLayers() != null) {
            for (ar.com.catgis.Layer layer : proj.getLayers()) {
                if (layer == null) continue;
                key = key * 31 + (layer.isVisible() ? 1 : 0);
                key = key * 31 + (layer.getName() != null ? layer.getName().hashCode() : 0);
            }
        }
        return key;
    }

    private boolean shouldPreferMainMapComposite() {
        ar.com.catgis.Project proj = ar.com.catgis.CatgisDesktopApp.currentProject;
        if (proj == null || proj.getLayers() == null) {
            return false;
        }
        for (ar.com.catgis.Layer layer : proj.getLayers()) {
            if (layer == null || !layer.isVisible()) {
                continue;
            }
            if (layer instanceof ar.com.catgis.RasterLayer
                    || layer instanceof ar.com.catgis.OnlineTileLayer
                    || layer instanceof ar.com.catgis.OnlineWmsLayer) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to render using the independent MapFrameRenderer.
     * Returns null if not available (falls back to MapPanel-based rendering).
     */
    private BufferedImage renderIndependent(int w, int h) {
        syncViewportToSource(w, h);
        if (independentRenderer == null) {
            independentRenderer = new MapFrameRenderer(viewport);
        }
        // Set indicator extent for inset maps
        if (showIndicator) {
            MapFrameViewport mainViewport = new MapFrameViewport();
            mainViewport.fitFromMainMap();
            independentRenderer.setIndicatorExtent(mainViewport);
        } else {
            independentRenderer.setIndicatorExtent(null);
        }
        try {
            return independentRenderer.render(w, h, 96);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the independent viewport for this map frame.
     * Creates one if it doesn't exist yet.
     */
    public MapFrameViewport getViewport() {
        if (viewport == null) {
            viewport = new MapFrameViewport();
            if (ownExtent) {
                viewport.fitToExtent(ownViewMinX, ownViewMinY,
                        ownViewMinX + 1000, ownViewMinY + 1000);
            } else {
                viewport.fitFromMainMap();
            }
        }
        return viewport;
    }

    /**
     * Get the independent renderer for this map frame.
     */
    public MapFrameRenderer getIndependentRenderer() {
        if (independentRenderer == null) {
            independentRenderer = new MapFrameRenderer(getViewport());
        }
        return independentRenderer;
    }

    /**
     * Force re-render by invalidating cache.
     */
    public void invalidateRenderCache() {
        cachedImage = null;
        cacheKey = 0;
    }

    private void syncViewportToSource(int w, int h) {
        if (viewport == null) {
            viewport = new MapFrameViewport();
        }

        if (ownExtent) {
            double effectiveZoom = ownZoomFactor > 0 ? ownZoomFactor : 1d;
            viewport.fitToExtent(
                    ownViewMinX,
                    ownViewMinY,
                    ownViewMinX + w * effectiveZoom,
                    ownViewMinY + h * effectiveZoom
            );
            return;
        }

        // Step 1: Try to sync from main map (fitFromMainMap now checks
        // if the view actually contains useful data, otherwise falls back)
        if (!viewport.fitFromMainMap()) {
            // Step 2: Fallback to project visible layers
            viewport.fitFromProjectLayers();
        }
    }

    private BufferedImage captureMapImage(int w, int h) {
        ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
        if (map == null) return null;
        try {
            double vx, vy, zf;
            if (ownExtent) {
                vx = ownViewMinX; vy = ownViewMinY; zf = ownZoomFactor;
            } else {
                // Use MapPanel's current view
                vx = map.getViewMinX();
                vy = map.getViewMinY();
                zf = map.getZoomFactor();
            }
            if (zf <= 0) zf = 1;

            // Calculate correct zoom to fit the view into the layout frame
            // MapPanel's zoomFactor is pixels-per-world-unit
            // We need to adjust for the layout frame size
            double mapWidth = map.getWidth();
            double mapHeight = map.getHeight();
            if (mapWidth > 0 && mapHeight > 0) {
                // Scale factor to fit MapPanel view into layout frame
                double scaleX = w / mapWidth;
                double scaleY = h / mapHeight;
                double fitScale = Math.min(scaleX, scaleY);
                // Keep the original zoom but scale for the layout frame
                zf = zf * fitScale;
            }

            return map.renderMapViewImage(vx, vy, zf, w, h);
        } catch (Exception ex) {
            return null;
        }
    }

    public void setShowGrid(boolean g) { this.showGrid = g; }
    public boolean isShowGrid() { return showGrid; }
    public int getGridCols() { return gridCols; }
    public void setGridCols(int c) { this.gridCols = c; }
    public int getGridRows() { return gridRows; }
    public void setGridRows(int r) { this.gridRows = r; }
    public Color getGridColor() { return gridColor; }
    public void setGridColor(Color c) { if (c != null) this.gridColor = c; }
    public float getGridLineWidth() { return gridLineWidth; }
    public void setGridLineWidth(float w) { gridLineWidth = Math.max(0.1f, w); }
    public boolean isShowGridLabels() { return showGridLabels; }
    public void setShowGridLabels(boolean b) { showGridLabels = b; }
    public boolean isGridByDistance() { return gridByDistance; }
    public void setGridByDistance(boolean b) { this.gridByDistance = b; }
    public double getGridIntervalX() { return gridIntervalX; }
    public void setGridIntervalX(double v) { this.gridIntervalX = Math.max(0.001, v); }
    public double getGridIntervalY() { return gridIntervalY; }
    public void setGridIntervalY(double v) { this.gridIntervalY = Math.max(0.001, v); }
    public String getGridUnit() { return gridUnit; }
    public void setGridUnit(String u) { this.gridUnit = u; }
    public double getGridOffsetX() { return gridOffsetX; }
    public void setGridOffsetX(double v) { this.gridOffsetX = v; }
    public double getGridOffsetY() { return gridOffsetY; }
    public void setGridOffsetY(double v) { this.gridOffsetY = v; }
    public double getTargetScaleDenominator() { return targetScaleDenominator; }
    public void setTargetScaleDenominator(double d) { this.targetScaleDenominator = Math.max(0, d); }
    public Color getFrameColor() { return frameColor; }
    public void setFrameColor(Color c) { if (c != null) frameColor = c; }
    public float getFrameWidth() { return frameWidth; }
    public void setFrameWidth(float w) { frameWidth = Math.max(0, w); }
    public int getFrameCornerRadius() { return frameCornerRadius; }
    public void setFrameCornerRadius(int r) { frameCornerRadius = Math.max(0, r); }
    public boolean isOwnExtent() { return ownExtent; }
    public void setOwnExtent(boolean b) { this.ownExtent = b; }
    public double getOwnViewMinX() { return ownViewMinX; }
    public void setOwnViewMinX(double v) { ownViewMinX = v; }
    public double getOwnViewMinY() { return ownViewMinY; }
    public void setOwnViewMinY(double v) { ownViewMinY = v; }
    public double getOwnZoomFactor() { return ownZoomFactor; }
    public void setOwnZoomFactor(double v) { ownZoomFactor = v; }
    public void captureFromMainMap() {
        ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
        if (map != null) {
            ownViewMinX = map.getViewMinX();
            ownViewMinY = map.getViewMinY();
            ownZoomFactor = map.getZoomFactor();
            ownExtent = true;
            viewport = null;
            independentRenderer = null;
            invalidateRenderCache();
        }
    }
    public boolean isShowIndicator() { return showIndicator; }
    public void setShowIndicator(boolean b) { showIndicator = b; }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }
}
