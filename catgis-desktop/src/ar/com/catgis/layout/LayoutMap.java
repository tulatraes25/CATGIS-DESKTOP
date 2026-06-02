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
    private boolean showGrid = false;
    private int gridCols = 3;
    private int gridRows = 3;
    private Color gridColor = new Color(0, 0, 0, 40);
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
        if (cachedImage == null || key != cacheKey) {
            cachedImage = captureMapImage(pw, ph);
            cacheKey = key;
        }

        if (cachedImage != null) {
            Graphics2D mg = (Graphics2D) g2.create();
            try {
                mg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                mg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Maintain aspect ratio: fit image within frame centered
                double imgW = cachedImage.getWidth(), imgH = cachedImage.getHeight();
                double scale = Math.min((double)pw / imgW, (double)ph / imgH);
                int drawW = (int)(imgW * scale), drawH = (int)(imgH * scale);
                int dx = px + (pw - drawW) / 2, dy = py + (ph - drawH) / 2;
                mg.drawImage(cachedImage, dx, dy, drawW, drawH, null);
            } finally {
                mg.dispose();
            }
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
        if (showGrid && gridCols > 0 && gridRows > 0) {
            g2.setColor(gridColor);
            g2.setStroke(new java.awt.BasicStroke(0.5f));
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

    private BufferedImage captureMapImage(int w, int h) {
        ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
        if (map == null) return null;
        try {
            double vx, vy, zf;
            if (ownExtent) {
                vx = ownViewMinX; vy = ownViewMinY; zf = ownZoomFactor;
            } else {
                vx = map.getViewMinX(); vy = map.getViewMinY(); zf = map.getZoomFactor();
            }
            if (zf <= 0) zf = 1;
            BufferedImage img = map.renderMapViewImage(vx, vy, zf);
            if (img == null) return null;
            if (img.getWidth() == w && img.getHeight() == h) return img;
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, w, h, null);
            g.dispose();
            return scaled;
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
        if (map != null) { ownViewMinX = map.getViewMinX(); ownViewMinY = map.getViewMinY(); ownZoomFactor = map.getZoomFactor(); ownExtent = true; }
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }
}
