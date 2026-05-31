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
                mg.drawImage(cachedImage, px, py, pw, ph, null);
            } finally {
                mg.dispose();
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
        ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
        if (map != null) {
            key = key * 31 + Double.doubleToLongBits(map.getViewMinX());
            key = key * 31 + Double.doubleToLongBits(map.getViewMinY());
            key = key * 31 + Double.doubleToLongBits(map.getZoomFactor());
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
            BufferedImage img = map.renderMapViewImage(
                map.getViewMinX(), map.getViewMinY(), map.getZoomFactor());
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
    public void setGridCols(int c) { this.gridCols = c; }
    public void setGridRows(int r) { this.gridRows = r; }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }
}
