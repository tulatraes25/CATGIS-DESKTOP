package ar.com.catgis.climate;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Swing component that renders a wind rose diagram.
 * Shows frequency distribution and average wind speed
 * across 16 cardinal directions.
 */
public class WindRoseRenderer extends JComponent {

    public static final String[] DIRECTIONS = {
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    };

    /** Angle in degrees for each direction (0° = N, 90° = E). */
    public static final double[] DIRECTION_ANGLES = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private static final DecimalFormat DF_PCT = new DecimalFormat("0.0");

    // Data
    private int[] frequency = new int[16];
    private double[] avgSpeed = new double[16];
    private int totalCount = 0;
    private double maxFrequency = 1;
    private double maxSpeed = 1;
    private boolean showFrequency = true;
    private boolean showSpeed = true;
    private boolean showLabels = true;
    private boolean showLegend = true;

    /**
     * Create a wind rose renderer with default (empty) data.
     */
    public WindRoseRenderer() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(new Color(255, 255, 255, 0)); // transparent background
    }

    /**
     * Set wind rose data from frequency and speed arrays.
     * @param freq frequency count for each of the 16 directions
     * @param speed average wind speed for each of the 16 directions
     */
    public void setData(int[] freq, double[] speed) {
        this.frequency = new int[16];
        this.avgSpeed = new double[16];
        this.totalCount = 0;
        this.maxFrequency = 1;
        this.maxSpeed = 1;

        for (int i = 0; i < 16 && i < freq.length; i++) {
            this.frequency[i] = Math.max(0, freq[i]);
            this.totalCount += this.frequency[i];
            this.avgSpeed[i] = i < speed.length ? Math.max(0, speed[i]) : 0;
        }

        this.maxFrequency = totalCount > 0 ? totalCount : 1;
        this.maxSpeed = 1;
        for (int i = 0; i < 16; i++) {
            if (avgSpeed[i] > maxSpeed) maxSpeed = avgSpeed[i];
        }
        if (maxSpeed <= 0) maxSpeed = 1;

        repaint();
    }

    public void setShowFrequency(boolean show) { this.showFrequency = show; repaint(); }
    public void setShowSpeed(boolean show) { this.showSpeed = show; repaint(); }
    public void setShowLabels(boolean show) { this.showLabels = show; repaint(); }
    public void setShowLegend(boolean show) { this.showLegend = show; repaint(); }

    public int getTotalCount() { return totalCount; }
    public double getMaxFrequency() { return maxFrequency; }
    public double getMaxSpeed() { return maxSpeed; }
    public int[] getFrequency() { return Arrays.copyOf(frequency, 16); }
    public double[] getAvgSpeed() { return Arrays.copyOf(avgSpeed, 16); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;

        // Calculate available space
        int legendWidth = showLegend ? 120 : 0;
        int maxRadius = Math.min(cx - 20, cy - 20);
        if (legendWidth > 0) {
            maxRadius = Math.min(maxRadius, w - legendWidth - 30);
        }

        if (maxRadius < 40) return;

        // Draw direction circle reference
        g2.setStroke(new BasicStroke(0.5f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawOval(cx - maxRadius, cy - maxRadius, maxRadius * 2, maxRadius * 2);
        // Inner circles for 25%, 50%, 75%, 100%
        if (showFrequency) {
            for (double pct : new double[]{0.25, 0.50, 0.75}) {
                int r = (int) (maxRadius * pct);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            }
        }

        // Draw direction lines
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(DIRECTION_ANGLES[i] - 90);
            int x2 = cx + (int) (maxRadius * Math.cos(angle));
            int y2 = cy + (int) (maxRadius * Math.sin(angle));

            g2.setColor(new Color(200, 200, 200));
            g2.drawLine(cx, cy, x2, y2);

            // Direction label
            if (showLabels) {
                int labelDist = maxRadius + 14;
                int lx = cx + (int) (labelDist * Math.cos(angle));
                int ly = cy + (int) (labelDist * Math.sin(angle));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String label = DIRECTIONS[i];
                lx -= fm.stringWidth(label) / 2;
                ly += fm.getAscent() / 2;
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(label, lx, ly);
            }
        }

        // Draw frequency petals (larger, lighter blue)
        if (showFrequency && totalCount > 0) {
            for (int i = 0; i < 16; i++) {
                double pct = frequency[i] / (double) maxFrequency;
                double radius = pct * maxRadius * 0.85;
                if (radius < 1) continue;

                double halfAngle = Math.toRadians(11.25); // half of 22.5 degrees
                double angle = Math.toRadians(DIRECTION_ANGLES[i] - 90);

                Path2D.Double petal = new Path2D.Double();
                petal.moveTo(cx, cy);
                double x1 = cx + radius * Math.cos(angle - halfAngle);
                double y1 = cy + radius * Math.sin(angle - halfAngle);
                petal.lineTo(x1, y1);
                // Curve to tip
                double tipX = cx + radius * 1.15 * Math.cos(angle);
                double tipY = cy + radius * 1.15 * Math.sin(angle);
                double x2 = cx + radius * Math.cos(angle + halfAngle);
                double y2 = cy + radius * Math.sin(angle + halfAngle);
                petal.quadTo(tipX, tipY, x2, y2);
                petal.closePath();

                // Color by frequency intensity
                float intensity = (float) (0.3 + 0.5 * pct);
                g2.setColor(new Color(0.1f, 0.3f + 0.4f * intensity, 0.8f, 0.35f));
                g2.fill(petal);
                g2.setColor(new Color(0.1f, 0.3f, 0.7f, 0.5f));
                g2.setStroke(new BasicStroke(0.8f));
                g2.draw(petal);
            }
        }

        // Draw speed petals (smaller, orange/red)
        if (showSpeed && maxSpeed > 0) {
            for (int i = 0; i < 16; i++) {
                double pct = avgSpeed[i] / maxSpeed;
                double radius = pct * maxRadius * 0.7;
                if (radius < 1) continue;

                double halfAngle = Math.toRadians(11.25);
                double angle = Math.toRadians(DIRECTION_ANGLES[i] - 90);

                Path2D.Double petal = new Path2D.Double();
                petal.moveTo(cx, cy);
                double x1 = cx + radius * Math.cos(angle - halfAngle);
                double y1 = cy + radius * Math.sin(angle - halfAngle);
                petal.lineTo(x1, y1);
                double tipX = cx + radius * 1.1 * Math.cos(angle);
                double tipY = cy + radius * 1.1 * Math.sin(angle);
                double x2 = cx + radius * Math.cos(angle + halfAngle);
                double y2 = cy + radius * Math.sin(angle + halfAngle);
                petal.quadTo(tipX, tipY, x2, y2);
                petal.closePath();

                float intensity = (float) (0.3 + 0.5 * pct);
                g2.setColor(new Color(1f, 0.5f - 0.3f * intensity, 0.1f, 0.4f));
                g2.fill(petal);
                g2.setColor(new Color(0.8f, 0.3f, 0.05f, 0.55f));
                g2.setStroke(new BasicStroke(0.7f));
                g2.draw(petal);
            }
        }

        // Legend
        if (showLegend) {
            int lx = w - legendWidth + 10;
            int ly = 20;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

            g2.setColor(new Color(0.1f, 0.3f, 0.7f, 0.5f));
            g2.fillRect(lx, ly, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(lx, ly, 14, 14);
            g2.drawString("Free. viento", lx + 18, ly + 12);

            ly += 22;
            g2.setColor(new Color(0.8f, 0.3f, 0.05f, 0.55f));
            g2.fillRect(lx, ly, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(lx, ly, 14, 14);
            g2.drawString("Velocidad media", lx + 18, ly + 12);

            ly += 22;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("N total: " + totalCount, lx, ly + 5);

            if (maxFrequency > 1) {
                ly += 14;
                g2.drawString("Max frec: " + DF_PCT.format(maxFrequency), lx, ly + 5);
            }
            if (maxSpeed > 0) {
                ly += 14;
                g2.drawString("Max vel: " + DF_PCT.format(maxSpeed) + " m/s", lx, ly + 5);
            }

            // Calm wind info
            int calmCount = frequency[0]; // Simplify: assume first direction
            if (totalCount > 0) {
                ly += 18;
                g2.drawString("Calmas: " + DF_PCT.format(calmCount * 100.0 / totalCount) + "%", lx, ly + 5);
            }
        }

        g2.dispose();
    }

    /**
     * Render this wind rose to a BufferedImage (for export or CATMAP).
     */
    public BufferedImage renderToImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Transparent background
        setSize(width, height);
        paintComponent(g2);

        g2.dispose();
        return image;
    }

    /**
     * Render wind rose to image with white background (for JPG/PDF).
     */
    public BufferedImage renderToImageOpaque(int width, int height) {
        BufferedImage image = renderToImage(width, height);
        BufferedImage opaque = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = opaque.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return opaque;
    }

    /**
     * Compute wind rose data from U/V wind component arrays.
     * @param uData U (eastward) wind component values
     * @param vData V (northward) wind component values
     */
    public void computeFromUVComponents(float[] uData, float[] vData) {
        int n = Math.min(uData != null ? uData.length : 0, vData != null ? vData.length : 0);
        if (n == 0) return;

        int[] dirCount = new int[16];
        double[] dirSpeedSum = new double[16];

        for (int i = 0; i < n; i++) {
            float u = uData[i];
            float v = vData[i];
            if (!Float.isFinite(u) || !Float.isFinite(v)) continue;
            if (Math.abs(u) < 0.01 && Math.abs(v) < 0.01) {
                // Calm wind, count as N
                dirCount[0]++;
                continue;
            }

            // Wind direction from components (meteorological convention)
            double dirDeg = Math.toDegrees(Math.atan2(-u, -v));
            if (dirDeg < 0) dirDeg += 360;

            double speed = Math.sqrt(u * u + v * v);

            // Find the nearest cardinal direction
            int dirIdx = (int) Math.round(dirDeg / 22.5) % 16;
            dirCount[dirIdx]++;
            dirSpeedSum[dirIdx] += speed;
        }

        double[] avgSpeed = new double[16];
        for (int i = 0; i < 16; i++) {
            if (dirCount[i] > 0) {
                avgSpeed[i] = dirSpeedSum[i] / dirCount[i];
            }
        }

        setData(dirCount, avgSpeed);
    }
}
