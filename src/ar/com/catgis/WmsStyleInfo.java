package ar.com.catgis;

public class WmsStyleInfo {
    private final String name;
    private final String title;

    public WmsStyleInfo(String name, String title) {
        this.name = name != null ? name : "";
        this.title = title != null ? title : "";
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        if (title != null && !title.isBlank() && name != null && !name.isBlank()) {
            return title + " (" + name + ")";
        }
        if (title != null && !title.isBlank()) {
            return title;
        }
        return name != null && !name.isBlank() ? name : "(default)";
    }
}
