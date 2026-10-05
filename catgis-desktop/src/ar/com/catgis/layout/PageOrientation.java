package ar.com.catgis.layout;

/**
 * Page orientation for layout composition.
 */
public enum PageOrientation {
    LANDSCAPE("Horizontal"),
    PORTRAIT("Vertical");

    private final String label;

    PageOrientation(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
