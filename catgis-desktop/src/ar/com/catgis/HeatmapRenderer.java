package ar.com.catgis;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Renders point density heatmaps.
 * Converts point features into a colored density surface.
 */
public final class HeatmapRenderer {

    private HeatmapRenderer() {}

    /**
     * Render a heatmap from point coordinates.
     *
     * @param points      List of (x, y) screen coordinates
     * @param width       Output image width in pixels
     * @param height      Output image height in pixels
     * @param radius      Blur radius in pixels (affects smoothness)
     * @param opacity     Overall opacity (0.0 - 1.0)
     * @return BufferedImage with heatmap overlay (TYPE_INT_ARGB)
     */
    public static BufferedImage renderHeatmap(
            List<Point2D> points, int width, int height, int radius, float opacity) {

        if (points == null || points.isEmpty() || width <= 0 || height <= 0) {
            return new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        }

        // Step 1: Build density buffer at potentially reduced resolution for performance
        int scale = Math.max(1, radius / 20);
        int sw = (width + scale - 1) / scale;
        int sh = (height + scale - 1) / scale;
        float sr = (float) radius / scale;
        float[][] density = new float[sh][sw];

        // Accumulate density using Gaussian-like falloff
        int kernelSize = (int) Math.ceil(sr * 3);
        float[] kernel = buildGaussianKernel(kernelSize);

        for (Point2D pt : points) {
            int cx = (int) (pt.getX() / scale);
            int cy = (int) (pt.getY() / scale);
            if (cx < 0 || cy < 0 || cx >= sw || cy >= sh) continue;

            // Add weighted density around each point
            for (int dy = -kernelSize; dy <= kernelSize; dy++) {
                int ky = cy + dy;
                if (ky < 0 || ky >= sh) continue;
                float wy = kernel[Math.abs(dy)];
                if (wy < 0.01f) continue;

                for (int dx = -kernelSize; dx <= kernelSize; dx++) {
                    int kx = cx + dx;
                    if (kx < 0 || kx >= sw) continue;
                    float w = wy * kernel[Math.abs(dx)];
                    if (w < 0.01f) continue;
                    density[ky][kx] += w;
                }
            }
        }

        // Find max density for normalization
        float maxDensity = 0.01f;
        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                if (density[y][x] > maxDensity) maxDensity = density[y][x];
            }
        }

        // Step 2: Render with color ramp
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, Math.max(0f, Math.min(1f, opacity))));

            for (int sy = 0; sy < sh; sy++) {
                for (int sx = 0; sx < sw; sx++) {
                    float v = density[sy][sx] / maxDensity;
                    if (v < 0.01f) continue;

                    // Color ramp: transparent blue → blue → cyan → yellow → red
                    Color c = heatColor(v);
                    g2.setColor(c);

                    int px = sx * scale;
                    int py = sy * scale;
                    g2.fillRect(px, py, scale, scale);
                }
            }
        } finally {
            g2.dispose();
        }

        return image;
    }

    /**
     * Build a Gaussian kernel array.
     * kernel[i] = exp(-i² / (2*σ²)) where σ = kernelSize/3
     */
    private static float[] buildGaussianKernel(int size) {
        if (size <= 0) return new float[]{1f};
        float sigma = size / 3f;
        float[] k = new float[size + 1];
        float sum = 0;
        for (int i = 0; i <= size; i++) {
            k[i] = (float) Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += (i == 0 ? k[i] : 2 * k[i]);
        }
        // Normalize
        for (int i = 0; i <= size; i++) k[i] /= sum;
        return k;
    }

    /**
     * Color ramp: transparent → blue → cyan → yellow → red.
     * Returns a color with alpha = v * 200 (semi-transparent)
     */
    private static Color heatColor(float v) {
        int r, g, b;
        if (v < 0.25f) {
            // Transparent → blue
            float t = v / 0.25f;
            r = 0;
            g = (int) (t * 100);
            b = (int) (128 + t * 127);
        } else if (v < 0.5f) {
            // Blue → cyan
            float t = (v - 0.25f) / 0.25f;
            r = 0;
            g = (int) (100 + t * 155);
            b = 255;
        } else if (v < 0.75f) {
            // Cyan → yellow
            float t = (v - 0.5f) / 0.25f;
            r = (int) (t * 255);
            g = 255;
            b = (int) ((1 - t) * 255);
        } else {
            // Yellow → red
            float t = Math.min(1f, (v - 0.75f) / 0.25f);
            r = 255;
            g = (int) ((1 - t) * 255);
            b = 0;
        }
        int alpha = Math.min(200, (int) (v * 200));
        return new Color(r, g, b, alpha);
    }
}
