package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;

public record GpxImportResult(
        ShapefileData waypoints,
        ShapefileData tracks,
        ShapefileData routes
) {

    public ShapefileData get(GpxLayer.ContentKind kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case WAYPOINTS -> waypoints;
            case TRACKS -> tracks;
            case ROUTES -> routes;
        };
    }

    public boolean hasAnyData() {
        return hasFeatures(waypoints) || hasFeatures(tracks) || hasFeatures(routes);
    }

    public static boolean hasFeatures(ShapefileData data) {
        return data != null && data.getFeatures() != null && !data.getFeatures().isEmpty();
    }
}
