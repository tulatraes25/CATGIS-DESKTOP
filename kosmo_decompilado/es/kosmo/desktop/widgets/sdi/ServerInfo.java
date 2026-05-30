/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi;

public class ServerInfo {
    private String name;
    private String url;
    private String description;
    private String version;
    private boolean favourite;

    public ServerInfo(String serverName, String serverURL, String serverDescription, String serverVersion, boolean isFavourite) {
        this.name = serverName;
        this.url = serverURL;
        this.description = serverDescription;
        this.version = serverVersion;
        this.favourite = isFavourite;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavourite() {
        return this.favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return this.getName();
    }
}

