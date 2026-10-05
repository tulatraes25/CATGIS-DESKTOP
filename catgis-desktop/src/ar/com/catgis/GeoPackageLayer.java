package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

public class GeoPackageLayer extends Layer implements ReadOnlyVectorLayerSource {

    private String tableName = "";
    private String identifier = "";
    private String description = "";
    private String geometryTypeLabel = "";
    private boolean readOnly = true;

    public GeoPackageLayer(String name, String path) {
        super(name, path, "GEOPACKAGE");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName != null ? tableName : "";
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public String getGeometryTypeLabel() {
        return geometryTypeLabel;
    }

    public void setGeometryTypeLabel(String geometryTypeLabel) {
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel : "";
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getReadOnlyReason() {
        return "La capa GeoPackage cargada en esta etapa funciona en modo lectura.";
    }
}
