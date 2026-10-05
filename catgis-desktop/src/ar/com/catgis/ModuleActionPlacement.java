package ar.com.catgis;

public enum ModuleActionPlacement {
    MODULE_MENU("Menu Modulos"),
    MAIN_TOOLBAR("Barra principal"),
    EDIT_TOOLBAR("Barra de edicion"),
    EXISTING_UI("UI existente");

    private final String displayName;

    ModuleActionPlacement(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
