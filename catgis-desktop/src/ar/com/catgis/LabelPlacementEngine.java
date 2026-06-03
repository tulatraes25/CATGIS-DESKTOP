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
    private record Candidate(int x, int y, String description) {}

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
        int gap = 6; // gap between point and label

        return switch (mode) {
            case POINT_ABOVE -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY - gap + offY, "above")
            );
            case POINT_BELOW -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "below")
            );
            case POINT_LEFT -> List.of(
                    new Candidate(anchorX - tw - gap + offX, anchorY + offY, "left")
            );
            case POINT_RIGHT -> List.of(
                    new Candidate(anchorX + gap + offX, anchorY + offY, "right")
            );
            case POINT_CENTER -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "center")
            );
            case LINE_CENTER -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY - ascent / 2 + offY, "line_center")
            );
            case LINE_FOLLOW -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY - ascent - gap + offY, "follow_above"),
                    new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "follow_below"),
                    new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "follow_center")
            );
            case POLYGON_CENTROID, POLYGON_INTERIOR -> List.of(
                    new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "polygon_center"),
                    new Candidate(anchorX - tw / 2 + offX, anchorY - ascent - gap + offY, "polygon_above"),
                    new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "polygon_below")
            );
            case AUTO -> {
                if ("POINT".equals(geomType) || "MULTIPOINT".equals(geomType)) {
                    yield List.of(
                            new Candidate(anchorX + gap + offX, anchorY + offY, "auto_right"),
                            new Candidate(anchorX - tw - gap + offX, anchorY + offY, "auto_left"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY - gap + offY, "auto_above"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "auto_below"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "auto_center")
                    );
                } else if ("LINESTRING".equals(geomType) || "MULTILINESTRING".equals(geomType)) {
                    yield List.of(
                            new Candidate(anchorX - tw / 2 + offX, anchorY - ascent - gap + offY, "auto_line_above"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "auto_line_center"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "auto_line_below")
                    );
                } else {
                    // Polygon or unknown
                    yield List.of(
                            new Candidate(anchorX - tw / 2 + offX, anchorY + offY, "auto_poly_center"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY - ascent - gap + offY, "auto_poly_above"),
                            new Candidate(anchorX - tw / 2 + offX, anchorY + ascent + gap + offY, "auto_poly_below")
                    );
                }
            }
        };
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
