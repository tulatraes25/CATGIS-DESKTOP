package ar.com.catgis;

import java.util.Locale;

public class PostgisConnectionInfo {

    private String host = "";
    private int port = 5432;
    private String database = "";
    private String schema = "public";
    private String user = "";
    private String password = "";
    private boolean rememberPassword = true;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host != null ? host.trim() : "";
    }

    public int getPort() {
        return port > 0 ? port : 5432;
    }

    public void setPort(int port) {
        this.port = port > 0 ? port : 5432;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database != null ? database.trim() : "";
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        String normalized = schema != null ? schema.trim() : "";
        this.schema = normalized.isBlank() ? "public" : normalized;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user != null ? user.trim() : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public boolean isRememberPassword() {
        return rememberPassword;
    }

    public void setRememberPassword(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
    }

    public boolean isCompleteForConnection() {
        return !host.isBlank() && getPort() > 0 && !database.isBlank() && !user.isBlank();
    }

    public String buildFingerprint() {
        return (host + "|" + getPort() + "|" + database + "|" + getSchema() + "|" + user)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public String buildDisplayLabel() {
        StringBuilder sb = new StringBuilder();
        if (!user.isBlank()) {
            sb.append(user).append("@");
        }
        sb.append(host.isBlank() ? "localhost" : host);
        sb.append(":").append(getPort());
        if (!database.isBlank()) {
            sb.append("/").append(database);
        }
        if (!getSchema().isBlank()) {
            sb.append(" [").append(getSchema()).append("]");
        }
        return sb.toString();
    }

    public PostgisConnectionInfo copy() {
        PostgisConnectionInfo copy = new PostgisConnectionInfo();
        copy.setHost(host);
        copy.setPort(getPort());
        copy.setDatabase(database);
        copy.setSchema(getSchema());
        copy.setUser(user);
        copy.setPassword(password);
        copy.setRememberPassword(rememberPassword);
        return copy;
    }
}
