package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WmsLayerInfo {
    private final String name;
    private final String title;
    private final int depth;
    private final Set<String> crsCodes = new LinkedHashSet<>();
    private final List<WmsStyleInfo> styles = new ArrayList<>();
    private final Map<String, Envelope> boundingBoxes = new LinkedHashMap<>();
    private Envelope geographicBounds;

    public WmsLayerInfo(String name, String title, int depth) {
        this.name = name != null ? name : "";
        this.title = title != null ? title : "";
        this.depth = Math.max(0, depth);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getDepth() {
        return depth;
    }

    public Set<String> getCrsCodes() {
        return Collections.unmodifiableSet(crsCodes);
    }

    public List<WmsStyleInfo> getStyles() {
        return Collections.unmodifiableList(styles);
    }

    public Map<String, Envelope> getBoundingBoxes() {
        return Collections.unmodifiableMap(boundingBoxes);
    }

    public Envelope getGeographicBounds() {
        return geographicBounds != null ? new Envelope(geographicBounds) : null;
    }

    public void addCrs(String crs) {
        if (crs != null && !crs.isBlank()) {
            crsCodes.add(crs.trim().toUpperCase());
        }
    }

    public void addStyle(WmsStyleInfo style) {
        if (style != null) {
            styles.add(style);
        }
    }

    public void putBoundingBox(String crs, Envelope envelope) {
        if (crs != null && !crs.isBlank() && envelope != null && !envelope.isNull()) {
            boundingBoxes.put(crs.trim().toUpperCase(), new Envelope(envelope));
        }
    }

    public void setGeographicBounds(Envelope geographicBounds) {
        this.geographicBounds = geographicBounds != null ? new Envelope(geographicBounds) : null;
    }

    public String getDisplayLabel() {
        String label = (title != null && !title.isBlank()) ? title : name;
        if (depth <= 0) {
            return label;
        }
        return "  ".repeat(depth) + label;
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
