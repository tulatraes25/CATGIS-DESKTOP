package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

public class PostgisLayer extends Layer implements ReadOnlyVectorLayerSource {

    private String host = "";
    private int port = 5432;
    private String databaseName = "";
    private String schemaName = "public";
    private String userName = "";
    private String typeName = "";
    private String tableName = "";
    private String geometryTypeLabel = "";
    private boolean readOnly = true;

    public PostgisLayer(String name) {
        super(name, "", "POSTGIS");
    }

    public void setConnectionInfo(PostgisConnectionInfo info) {
        if (info == null) {
            return;
        }
        setHost(info.getHost());
        setPort(info.getPort());
        setDatabaseName(info.getDatabase());
        setSchemaName(info.getSchema());
        setUserName(info.getUser());
    }

    public PostgisConnectionInfo toConnectionInfo() {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost(host);
        info.setPort(port);
        info.setDatabase(databaseName);
        info.setSchema(schemaName);
        info.setUser(userName);
        return info;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host != null ? host.trim() : "";
        refreshPath();
    }

    public int getPort() {
        return port > 0 ? port : 5432;
    }

    public void setPort(int port) {
        this.port = port > 0 ? port : 5432;
        refreshPath();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName != null ? databaseName.trim() : "";
        refreshPath();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        String normalized = schemaName != null ? schemaName.trim() : "";
        this.schemaName = normalized.isBlank() ? "public" : normalized;
        refreshPath();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName != null ? userName.trim() : "";
        refreshPath();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName != null ? typeName.trim() : "";
        refreshPath();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName != null ? tableName.trim() : "";
        refreshPath();
    }

    public String getGeometryTypeLabel() {
        return geometryTypeLabel;
    }

    public void setGeometryTypeLabel(String geometryTypeLabel) {
        this.geometryTypeLabel = geometryTypeLabel != null ? geometryTypeLabel.trim() : "";
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
        return "La capa PostGIS actual está en modo solo lectura. Podés consultarla, verla en tabla y exportarla, pero no editarla hasta volver a cargarla en modo editable.";
    }

    private void refreshPath() {
        StringBuilder sb = new StringBuilder("postgis://");
        if (!userName.isBlank()) {
            sb.append(userName).append("@");
        }
        sb.append(host.isBlank() ? "localhost" : host);
        sb.append(":").append(getPort());
        if (!databaseName.isBlank()) {
            sb.append("/").append(databaseName);
        }
        if (!schemaName.isBlank() || !tableName.isBlank() || !typeName.isBlank()) {
            sb.append("/");
            if (!schemaName.isBlank()) {
                sb.append(schemaName);
            }
            if (!tableName.isBlank()) {
                if (!schemaName.isBlank()) {
                    sb.append(".");
                }
                sb.append(tableName);
            } else if (!typeName.isBlank()) {
                if (!schemaName.isBlank()) {
                    sb.append(".");
                }
                sb.append(typeName);
            }
        }
        setPath(sb.toString());
    }
}
