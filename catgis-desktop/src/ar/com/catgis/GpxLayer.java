package ar.com.catgis;

public class GpxLayer extends Layer implements ReadOnlyVectorLayerSource {

    public enum ContentKind {
        WAYPOINTS("Waypoints", "POINT"),
        TRACKS("Tracks", "LINE"),
        ROUTES("Routes", "LINE");

        private final String label;
        private final String geometryFamily;

        ContentKind(String label, String geometryFamily) {
            this.label = label;
            this.geometryFamily = geometryFamily;
        }

        public String getLabel() {
            return label;
        }

        public String getGeometryFamily() {
            return geometryFamily;
        }

        public static ContentKind fromValue(String value) {
            if (value != null) {
                for (ContentKind kind : values()) {
                    if (kind.name().equalsIgnoreCase(value.trim())) {
                        return kind;
                    }
                }
            }
            return WAYPOINTS;
        }
    }

    private ContentKind contentKind;

    public GpxLayer(String name, String path, ContentKind contentKind) {
        super(name, path, "[GPX]");
        setContentKind(contentKind);
        setSourceCRS("EPSG:4326");
    }

    public ContentKind getContentKind() {
        return contentKind;
    }

    public void setContentKind(ContentKind contentKind) {
        this.contentKind = contentKind != null ? contentKind : ContentKind.WAYPOINTS;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getReadOnlyReason() {
        return "La capa proviene de un archivo GPX. Exportala a un formato editable si necesitas modificarla.";
    }
}
