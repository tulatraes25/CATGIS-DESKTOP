package ar.com.catgis;

import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WfsFeatureTypeInfo {

    private final String name;
    private final String title;
    private String defaultCrs = "";
    private final Set<String> crsCodes = new LinkedHashSet<>();
    private Envelope geographicBounds;

    public WfsFeatureTypeInfo(String name, String title) {
        this.name = name != null ? name : "";
        this.title = title != null ? title : "";
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultCrs() {
        return defaultCrs;
    }

    public void setDefaultCrs(String defaultCrs) {
        this.defaultCrs = normalizeCrs(defaultCrs);
        if (!this.defaultCrs.isBlank()) {
            addCrs(this.defaultCrs);
        }
    }

    public List<String> getCrsCodes() {
        return new ArrayList<>(crsCodes);
    }

    public void addCrs(String crs) {
        String normalized = normalizeCrs(crs);
        if (!normalized.isBlank()) {
            crsCodes.add(normalized);
        }
    }

    public Envelope getGeographicBounds() {
        return geographicBounds != null ? new Envelope(geographicBounds) : null;
    }

    public void setGeographicBounds(Envelope geographicBounds) {
        this.geographicBounds = geographicBounds != null ? new Envelope(geographicBounds) : null;
    }

    public String getDisplayLabel() {
        String text = title != null && !title.isBlank() ? title : name;
        if (name != null && !name.isBlank() && !name.equalsIgnoreCase(text)) {
            return text + " [" + name + "]";
        }
        return text;
    }

    private String normalizeCrs(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return CRSDefinitions.normalizeCode(value.trim().toUpperCase(Locale.ROOT));
    }
}
