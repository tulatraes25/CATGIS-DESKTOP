package ar.com.catgis.catmap.workspace;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Headless model of the CATMAP data workspace (REQ-PROD-10): project layers with
 * symbology and a bounded list of jobs/results. UI panels render this model; the
 * ranking/rows stay unit-testable.
 */
public final class DataWorkspaceModel {

    /** One project/data layer row with a deterministic symbology colour. */
    public record LayerRow(String name, String kind, boolean visible,
                           int featureCount, String crs, Color symbol) {
        public String describe() {
            StringBuilder text = new StringBuilder(name);
            text.append(" · ").append(kind);
            if (featureCount > 0) {
                text.append(" · ").append(featureCount).append(" entidades");
            }
            if (crs != null && !crs.isBlank()) {
                text.append(" · ").append(crs);
            }
            if (!visible) {
                text.append(" · oculta");
            }
            return text.toString();
        }
    }

    /** One job/result entry (analysis or export run). */
    public record JobResult(String kind, String status, String detail) {
        public String describe() {
            return "[" + status + "] " + kind + " — " + detail;
        }
    }

    public static final int MAX_JOBS = 50;

    private final List<JobResult> jobs = new ArrayList<>();

    /** Rows for the project layers (never null; empty for a null project). */
    public static List<LayerRow> rows(Project project) {
        if (project == null || project.getLayers() == null) {
            return List.of();
        }
        List<LayerRow> rows = new ArrayList<>();
        for (Layer layer : project.getLayers()) {
            if (layer == null) {
                continue;
            }
            rows.add(new LayerRow(
                    safeName(layer),
                    kindOf(layer),
                    layer.isVisible(),
                    Math.max(0, layer.getFeatureCount()),
                    layer.getSourceCRS() == null ? "" : layer.getSourceCRS(),
                    symbolOf(safeName(layer))));
        }
        return rows;
    }

    /** Deterministic symbology colour derived from the layer name. */
    public static Color symbolOf(String name) {
        if (name == null || name.isBlank()) {
            return new Color(0x6B7280);
        }
        int hash = Math.abs(name.toLowerCase(Locale.ROOT).hashCode());
        int red = 60 + (hash & 0x7F);
        int green = 60 + ((hash >> 7) & 0x7F);
        int blue = 60 + ((hash >> 14) & 0x7F);
        return new Color(red, green, blue);
    }

    private static String safeName(Layer layer) {
        String name = layer.getName();
        return name == null || name.isBlank() ? "(sin nombre)" : name;
    }

    private static String kindOf(Layer layer) {
        String type = layer.getType();
        if (type == null || type.isBlank()) {
            return "Capa";
        }
        String lower = type.toLowerCase(Locale.ROOT);
        if (lower.contains("raster") || lower.contains("tif") || lower.contains("dem")) {
            return "Raster";
        }
        if (lower.contains("postgis")) {
            return "PostGIS";
        }
        if (lower.contains("wms") || lower.contains("wfs")) {
            return "Servicio OGC";
        }
        return "Vectorial";
    }

    /** Register a completed/failed job; older entries are dropped past the cap. */
    public synchronized void addJob(String kind, String status, String detail) {
        jobs.add(new JobResult(
                blankTo(kind, "Trabajo"),
                blankTo(status, "INFO"),
                blankTo(detail, "")));
        while (jobs.size() > MAX_JOBS) {
            jobs.remove(0);
        }
    }

    public synchronized List<JobResult> jobs() {
        return List.copyOf(jobs);
    }

    public synchronized void clearJobs() {
        jobs.clear();
    }

    public synchronized int jobCount() {
        return jobs.size();
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
