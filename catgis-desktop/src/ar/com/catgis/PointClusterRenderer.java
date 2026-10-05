package ar.com.catgis;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Groups nearby point features into clusters with count labels.
 * Uses a simple grid-based approach for performance.
 */
public final class PointClusterRenderer {

    private PointClusterRenderer() {}

    /**
     * Cluster data for a single cluster.
     */
    public record Cluster(int x, int y, int count) {}

    /**
     * Cluster points from a collection using grid-based spatial grouping.
     *
     * @param points List of (x, y) screen coordinates
     * @param clusterRadiusPx Radius in pixels for grouping
     * @return List of clusters
     */
    public static List<Cluster> clusterPoints(List<Point2D> points, int clusterRadiusPx) {
        if (points == null || points.isEmpty()) return Collections.emptyList();

        int cellSize = Math.max(1, clusterRadiusPx);

        // Grid: cell key → list of points
        Map<Long, List<Point2D>> grid = new HashMap<>();

        for (Point2D pt : points) {
            int gx = (int) Math.floor(pt.getX() / cellSize);
            int gy = (int) Math.floor(pt.getY() / cellSize);
            long key = ((long) gx << 32) | (gy & 0xFFFFFFFFL);
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(pt);
        }

        // Build clusters from grid cells
        List<Cluster> clusters = new ArrayList<>();
        for (List<Point2D> cellPoints : grid.values()) {
            int cx = 0, cy = 0;
            for (Point2D p : cellPoints) {
                cx += p.getX();
                cy += p.getY();
            }
            int avgX = cx / cellPoints.size();
            int avgY = cy / cellPoints.size();
            clusters.add(new Cluster(avgX, avgY, cellPoints.size()));
        }

        return clusters;
    }

    /**
     * Render clusters onto a BufferedImage overlay.
     */
    public static BufferedImage renderClusters(List<Cluster> clusters, int width, int height) {
        BufferedImage img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Cluster c : clusters) {
                // Size proportional to count (log scale)
                int radius = Math.max(12, (int) (Math.log(c.count() + 1) * 8));
                radius = Math.min(60, radius);

                // Color based on density
                Color fill;
                if (c.count() < 10) fill = new Color(59, 130, 246, 180);     // blue
                else if (c.count() < 100) fill = new Color(245, 158, 11, 180); // amber
                else fill = new Color(220, 38, 38, 180);                       // red

                // Draw circle
                g2.setColor(fill);
                g2.fillOval(c.x() - radius, c.y() - radius, radius * 2, radius * 2);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawOval(c.x() - radius, c.y() - radius, radius * 2, radius * 2);

                // Draw count label
                String label = String.valueOf(c.count());
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, radius / 2)));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(label);
                int th = fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(label, c.x() - tw / 2, c.y() + th / 3);
            }
        } finally {
            g2.dispose();
        }
        return img;
    }
}
