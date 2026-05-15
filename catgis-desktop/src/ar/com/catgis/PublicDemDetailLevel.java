package ar.com.catgis;

public enum PublicDemDetailLevel {
    FAST("Rapida", 8, 5),
    BALANCED("Equilibrada", 11, 6),
    DETAILED("Mas detalle", 13, 7);

    private final String label;
    private final int preferredMaxZoom;
    private final int preferredMinZoom;

    PublicDemDetailLevel(String label, int preferredMaxZoom, int preferredMinZoom) {
        this.label = label;
        this.preferredMaxZoom = preferredMaxZoom;
        this.preferredMinZoom = preferredMinZoom;
    }

    public int preferredMaxZoom() {
        return preferredMaxZoom;
    }

    public int preferredMinZoom() {
        return preferredMinZoom;
    }

    @Override
    public String toString() {
        return I18n.t(label);
    }
}
