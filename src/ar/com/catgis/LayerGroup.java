package ar.com.catgis;

public class LayerGroup {

    private String name;
    private boolean visible = true;
    private boolean expanded = true;

    public LayerGroup(String name) {
        setName(name);
    }

    public LayerGroup(LayerGroup other) {
        if (other == null) {
            this.name = "Grupo";
            return;
        }
        this.name = other.name;
        this.visible = other.visible;
        this.expanded = other.expanded;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        String trimmed = name != null ? name.trim() : "";
        this.name = trimmed.isBlank() ? "Grupo" : trimmed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public String toString() {
        return name;
    }
}
