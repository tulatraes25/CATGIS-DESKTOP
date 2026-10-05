package ar.com.catgis.renderer;

import ar.com.catgis.MapPanel;
import ar.com.catgis.Main;
import org.locationtech.jts.geom.Envelope;

import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;

/**
 * Renders map decorations (north arrow, scale bar, graticule, attribution)
 * directly onto the MapPanel's graphics context.
 * <p>
 * Activated by toggling in the View menu or toolbar.
 */
public class MapDecorationRenderer {

    // Configuration (could be made user-configurable)
    private boolean showNorthArrow = true;
    private boolean showScaleBar = true;
    private boolean showAttribution = true;
    private boolean showCoordinates = true;

    private static final Color DECORATION_BG = new Color(0, 0, 0, 40);
    private static final Color DECORATION_FG = new Color(255, 255, 255, 220);
    private static final Color DECORATION_TEXT = new Color(255, 255, 255, 200);
    private static final Color SCALE_BAR_COLOR = new Color(255, 255, 255, 200);
    private static final Color GRATICULE_COLOR = new Color(255, 255, 255, 30);

    private final DecimalFormat coordFormat = new DecimalFormat("#0.0000");
    private final DecimalFormat scaleFormat = new DecimalFormat("#,##0");

    // --- Public API ---

    public boolean isShowNorthArrow() { return showNorthArrow; }
    public void setShowNorthArrow(boolean v) { showNorthArrow = v; }
    public boolean isShowScaleBar() { return showScaleBar; }
    public void setShowScaleBar(boolean v) { showScaleBar = v; }
    public boolean isShowAttribution() { return showAttribution; }
    public void setShowAttribution(boolean v) { showAttribution = v; }
    public boolean isShowCoordinates() { return showCoordinates; }
    public void setShowCoordinates(boolean v) { showCoordinates = v; }

    // Live cursor coordinates (updated by MapPanel mouse listener)
    private volatile String cursorCoordinateText = null;

    /**
     * Update the cursor coordinate display text.
     * Called by MapPanel on mouse move.
     */
    public void setCursorCoordinate(String coordText) {
        this.cursorCoordinateText = coordText;
    }

    /**
     * Render all enabled decorations onto the given Graphics2D context.
     */
    public void render(Graphics2D g, int width, int height,
                       Envelope visibleBounds,
                       double scaleDenominator,
                       String crsDescription) {
        if (g == null || width <= 0 || height <= 0) return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (showNorthArrow) {
            renderNorthArrow(g, width, height);
        }
        if (showScaleBar) {
            renderScaleBar(g, width, height, visibleBounds, width, scaleDenominator);
        }
        if (showAttribution) {
            renderAttribution(g, width, height);
        }
        if (showCoordinates) {
            renderCoordinates(g, width, height);
        }
    }

    // --- North Arrow ---

    private void renderNorthArrow(Graphics2D g, int width, int height) {
        int x = width - 48;
        int y = 16;
        int size = 24;

        // Background circle
        g.setColor(DECORATION_BG);
        g.fillOval(x - 4, y - 4, size + 8, size + 8);

        // Arrow pointing up (North)
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Arrow shaft
        g.setColor(new Color(200, 200, 200));
        g.drawLine(x + size / 2, y + size - 2, x + size / 2, y + 2);

        // Arrow head (north pointing up)
        Path2D arrowHead = new Path2D.Double();
        arrowHead.moveTo(x + size / 2, y + 2);
        arrowHead.lineTo(x + size / 2 - 6, y + 10);
        arrowHead.lineTo(x + size / 2, y + 2);
        arrowHead.lineTo(x + size / 2 + 6, y + 10);
        g.setColor(new Color(220, 50, 50));
        g.fill(arrowHead);

        // "N" label
        g.setColor(DECORATION_TEXT);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String n = "N";
        int nx = x + size / 2 - fm.stringWidth(n) / 2;
        int ny = y + size + 14;
        g.drawString(n, nx, ny);
    }

    // --- Scale Bar ---

    private void renderScaleBar(Graphics2D g, int width, int height,
                                Envelope visibleBounds, int mapWidth,
                                double scaleDenominator) {
        if (visibleBounds == null) return;

        // Calculate scale bar length in meters
        double mapWidthMeters = visibleBounds.getWidth()
                * Math.cos(visibleBounds.centre().y * Math.PI / 180) * 111319.5;
        if (mapWidthMeters <= 0) return;

        double metersPerPixel = mapWidthMeters / mapWidth;

        // Choose a nice round number for the scale bar
        int[] niceLengths = {1, 2, 5, 10, 20, 50, 100, 200, 500,
                1000, 2000, 5000, 10000, 20000, 50000,
                100000, 200000, 500000, 1000000};
        double px = 0;
        double targetMeters = 0;
        int maxBarWidth = (int) (width * 0.3);

        for (int meters : niceLengths) {
            double pixels = meters / metersPerPixel;
            if (pixels <= maxBarWidth) {
                px = pixels;
                targetMeters = meters;
            } else {
                break;
            }
        }

        if (px <= 0) return;

        int barX = 16;
        int barY = height - 36;
        int barHeight = 6;

        g.setColor(DECORATION_BG);
        g.fillRoundRect(barX - 4, barY - 4, (int) px + 8, barHeight + 20, 6, 6);

        // Scale bar line
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(SCALE_BAR_COLOR);

        // Main bar
        g.drawLine(barX, barY, (int) (barX + px), barY);
        // Tick marks
        g.drawLine(barX, barY - 4, barX, barY + 4);
        g.drawLine((int) (barX + px), barY - 4, (int) (barX + px), barY + 4);

        // Label
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(DECORATION_TEXT);
        String label;
        if (targetMeters >= 1000) {
            label = scaleFormat.format(targetMeters / 1000) + " km";
        } else {
            label = scaleFormat.format(targetMeters) + " m";
        }
        FontMetrics fm = g.getFontMetrics();
        int labelX = barX + (int) (px / 2) - fm.stringWidth(label) / 2;
        g.drawString(label, labelX, barY + 16);
    }

    // --- Attribution ---

    private void renderAttribution(Graphics2D g, int width, int height) {
        String attribution = "CATGIS Desktop";
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(DECORATION_BG);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(attribution);
        int tx = 12;
        int ty = height - 10;
        g.fillRoundRect(tx - 4, ty - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 4, 4);
        g.setColor(DECORATION_TEXT);
        g.drawString(attribution, tx, ty);
    }

    // --- Coordinates ---

    private void renderCoordinates(Graphics2D g, int width, int height) {
        // Show live cursor coordinates (updated by MapPanel mouse listener)
        String coordText = cursorCoordinateText != null
                ? cursorCoordinateText
                : "Lat/Lon: ---, ---";
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(DECORATION_BG);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(coordText);
        int tx = width - tw - 16;
        int ty = height - 10;
        g.fillRoundRect(tx - 4, ty - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 4, 4);
        g.setColor(DECORATION_TEXT);
        g.drawString(coordText, tx, ty);
    }

    /**
     * Update coordinate display with actual coordinates.
     */
    public String formatCoordinate(double lon, double lat) {
        return coordFormat.format(lat) + ", " + coordFormat.format(lon);
    }
}
