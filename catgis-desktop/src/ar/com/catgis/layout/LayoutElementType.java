package ar.com.catgis.layout;

/**
 * Identifies the fixed structural elements of a layout composition.
 * CATMAP_ITEM represents user-added custom elements.
 */
public enum LayoutElementType {
    HEADER,
    MAP_CONTENT,
    LEGEND,
    NORTH,
    SCALE,
    CARTOUCHE,
    PROFILE_IMAGE,
    CATMAP_ITEM;

    /**
     * Returns true for fixed layout structural elements, false for CATMAP_ITEM.
     */
    public boolean isFixed() {
        return this != CATMAP_ITEM;
    }

    /**
     * Static convenience for null-safe check.
     */
    public static boolean isFixed(LayoutElementType type) {
        return type != null && type != CATMAP_ITEM;
    }
}
