package ar.com.catgis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Basic label placement and collision engine for CATGIS.
 * <p>
 * Resolves placement position per geometry type, detects bounding-box
 * collisions, respects layer priority, and returns only labels that
 * should be rendered.
 * </p>
 */
public final class LabelPlacementEngine {

    private LabelPlacementEngine() {}

    /**
     * A resolved label ready to render.
     */
    public record ResolvedLabel(
            String text,
            int drawX,
            int drawY,
            int textWidth,
            int textHeight,
            int priority,
            Layer layer
    ) {}

    /**
     * Bounding box used for collision detection.
     */
    private static final class BBox {
        int x, y, w, h;
        BBox(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean intersects(BBox o) {
            return x < o.x + o.w && x + w > o.x && y < o.y + o.h && y + h > o.y;
        }
    }

    /**
     * Candidate position for a label.
     */
    private record Candidate(int x, int y, String description, double score) {}

    /**
     * Compute all label positions for a layer, apply collision detection,
     * and return only the labels that should be rendered.
     *
     * @param g2             the graphics context (for font metrics)
     * @param layer          the layer being labeled
     * @param labelTexts     list of [text, screenX, screenY, geometryType] per feature
     * @param existingBoxes  collision boxes from other layers as [x, y, w, h] arrays (mutated with accepted labels)
     * @return list of resolved labels to render
     */
    public static List<ResolvedLabel> resolveLabels(
            Graphics2D g2,
            Layer layer,
            List<Object[]> labelTexts,
            List<int[]> existingBoxes) {

        List<ResolvedLabel> result = new ArrayList<>();
        if (labelTexts == null || labelTexts.isEmpty()) return result;

        boolean collisionAvoid = layer.isLabelCollisionAvoid();
        int priority = layer.getLabelPriority();
        Layer.LabelPlacementMode mode = layer.getLabelPlacementMode();

        // Build font for metrics
        int fontStyle = Font.PLAIN;
        if (layer.isLabelBold()) fontStyle |= Font.BOLD;
        if (layer.isLabelItalic()) fontStyle |= Font.ITALIC;
        Font font = new Font(layer.getLabelFontFamily(), fontStyle, layer.getLabelFontSize());
        Font prevFont = g2.getFont();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Sort by priority (lower number = higher priority = rendered first)
        List<Object[]> sorted = new ArrayList<>(labelTexts);
        sorted.sort(Comparator.comparingInt(a -> {
            Object p = a.length > 4 ? a[4] : priority;
            return p instanceof Integer ? (Integer) p : priority;
        }));

        List<BBox> usedBoxes = new ArrayList<>();
        for (int[] arr : existingBoxes) {
            if (arr != null && arr.length >= 4) usedBoxes.add(new BBox(arr[0], arr[1], arr[2], arr[3]));
        }

        for (Object[] entry : sorted) {
            String text = (String) entry[0];
            int anchorX = (int) entry[1];
            int anchorY = (int) entry[2];
            String geomType = entry.length > 3 ? (String) entry[3] : "POINT";

            if (text == null || text.isBlank()) continue;

            int tw = fm.stringWidth(text);
            int th = fm.getHeight();
            int ascent = fm.getAscent();

            // Get candidate positions based on mode and geometry
            List<Candidate> candidates = getCandidates(mode, geomType, anchorX, anchorY, tw, th, ascent, layer);

            // Try each candidate, pick first non-colliding
            ResolvedLabel placed = null;
            for (Candidate c : candidates) {
                BBox box = new BBox(c.x, c.y - ascent, tw + 8, th + 4);
                if (!collisionAvoid || !intersectsAny(box, usedBoxes)) {
                    placed = new ResolvedLabel(text, c.x, c.y, tw, th, priority, layer);
                    usedBoxes.add(box);
                    break;
                }
            }

            // If all candidates collide, use the best one anyway (better some overlap than no label)
            if (placed == null && !candidates.isEmpty()) {
                Candidate fallback = candidates.get(0);
                placed = new ResolvedLabel(text, fallback.x, fallback.y, tw, th, priority, layer);
                usedBoxes.add(new BBox(fallback.x, fallback.y - ascent, tw + 8, th + 4));
            }

            if (placed != null) {
                result.add(placed);
            }
        }

        g2.setFont(prevFont);
        existingBoxes.clear();
        for (BBox b : usedBoxes) existingBoxes.add(new int[]{b.x, b.y, b.w, b.h});
        return result;
    }

    private static List<Candidate> getCandidates(
            Layer.LabelPlacementMode mode, String geomType,
            int anchorX, int anchorY, int tw, int th, int ascent,
            Layer layer) {

        int offX = layer.getLabelOffsetX();
        int offY = layer.getLabelOffsetY();
        int gap = 6;
        int halfW = tw / 2;

        // Generate all possible positions with scores (lower = better)
        List<Candidate> candidates = new ArrayList<>();

        switch (mode) {
            case POINT_ABOVE -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - gap + offY, "above", 0));
                candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "right", 1));
                candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left", 2));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "below", 3));
            }
            case POINT_BELOW -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "below", 0));
                candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "right", 1));
                candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left", 2));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - gap + offY, "above", 3));
            }
            case POINT_LEFT -> {
                candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left", 0));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - gap + offY, "above", 1));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "below", 2));
                candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "right", 3));
            }
            case POINT_RIGHT -> {
                candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "right", 0));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - gap + offY, "above", 1));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "below", 2));
                candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left", 3));
            }
            case POINT_CENTER -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "center", 0));
            }
            case LINE_CENTER -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent / 2 + offY, "line_center", 0));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent - gap + offY, "line_above", 1));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "line_below", 2));
            }
            case LINE_FOLLOW -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent - gap + offY, "follow_above", 0));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "follow_center", 1));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "follow_below", 2));
            }
            case POLYGON_CENTROID, POLYGON_INTERIOR -> {
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "poly_center", 0));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent - gap + offY, "poly_above", 1));
                candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "poly_below", 2));
                candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "poly_right", 3));
                candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "poly_left", 4));
            }
            case AUTO -> {
                if ("POINT".equals(geomType) || "MULTIPOINT".equals(geomType)) {
                    candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "right", 0));
                    candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left", 1));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY - gap + offY, "above", 2));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "below", 3));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "center", 4));
                    // Diagonal positions for better displacement
                    candidates.add(new Candidate(anchorX + gap + offX, anchorY - gap + offY, "right_above", 5));
                    candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY - gap + offY, "left_above", 6));
                    candidates.add(new Candidate(anchorX + gap + offX, anchorY + ascent + gap + offY, "right_below", 7));
                    candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + ascent + gap + offY, "left_below", 8));
                } else if ("LINESTRING".equals(geomType) || "MULTILINESTRING".equals(geomType)) {
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent - gap + offY, "line_above", 0));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "line_center", 1));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "line_below", 2));
                    candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "line_right", 3));
                    candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "line_left", 4));
                } else {
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + offY, "poly_center", 0));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY - ascent - gap + offY, "poly_above", 1));
                    candidates.add(new Candidate(anchorX - halfW + offX, anchorY + ascent + gap + offY, "poly_below", 2));
                    candidates.add(new Candidate(anchorX + gap + offX, anchorY + offY, "poly_right", 3));
                    candidates.add(new Candidate(anchorX - tw - gap + offX, anchorY + offY, "poly_left", 4));
                }
            }
        }

        // Sort by score (lower = better position)
        candidates.sort(Comparator.comparingDouble(Candidate::score));
        return candidates;
    }

    private static boolean intersectsAny(BBox box, List<BBox> boxes) {
        for (BBox b : boxes) {
            if (box.intersects(b)) return true;
        }
        return false;
    }

    /**
     * Resolve geometry type string from a JTS geometry class name.
     */
    public static String resolveGeometryType(Class<?> geometryClass) {
        if (geometryClass == null) return "POINT";
        String name = geometryClass.getSimpleName();
        if (name.contains("Point")) return "POINT";
        if (name.contains("MultiPoint")) return "MULTIPOINT";
        if (name.contains("LineString")) return "LINESTRING";
        if (name.contains("MultiLineString")) return "MULTILINESTRING";
        if (name.contains("Polygon")) return "POLYGON";
        if (name.contains("MultiPolygon")) return "MULTIPOLYGON";
        return "POINT";
    }
}
