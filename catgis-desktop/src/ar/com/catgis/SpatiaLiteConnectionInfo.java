package ar.com.catgis;

/**
 * Connection parameters for SpatiaLite database.
 * SpatiaLite is a spatial extension for SQLite.
 */
public class SpatiaLiteConnectionInfo {

    private String filePath = "";
    private String tableName = "";
    private boolean rememberPassword = true;

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath != null ? filePath.trim() : ""; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName != null ? tableName.trim() : ""; }

    public boolean isRememberPassword() { return rememberPassword; }
    public void setRememberPassword(boolean rememberPassword) { this.rememberPassword = rememberPassword; }

    public boolean isCompleteForConnection() {
        return !filePath.isBlank();
    }

    public SpatiaLiteConnectionInfo copy() {
        SpatiaLiteConnectionInfo copy = new SpatiaLiteConnectionInfo();
        copy.filePath = this.filePath;
        copy.tableName = this.tableName;
        copy.rememberPassword = this.rememberPassword;
        return copy;
    }
}
