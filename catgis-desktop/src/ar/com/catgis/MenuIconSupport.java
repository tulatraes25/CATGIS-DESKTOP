package ar.com.catgis;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Container;
import java.util.Locale;

/**
 * Central icon assignment for menus and context menus. Keeps every actionable
 * item iconographic and consistent with the UI icon set; text-based mapping is
 * intentional because most menus are built inline.
 */
public final class MenuIconSupport {

    private MenuIconSupport() {}

    private static final class IconMenuItem extends JMenuItem {
        IconMenuItem(String text, Icon icon) {
            super(text, icon);
        }
    }

    private static final class IconCheckBoxMenuItem extends JCheckBoxMenuItem {
        IconCheckBoxMenuItem(String text, Icon icon) {
            super(text, icon);
        }
    }

    public static JMenuItem item(String text) {
        return new IconMenuItem(text, iconFor(text));
    }

    public static JCheckBoxMenuItem checkItem(String text) {
        return new IconCheckBoxMenuItem(text, iconFor(text));
    }

    public static Icon iconFor(String text) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (value.contains("cancelar")) return AppIcons.cancelIcon();
        if (value.contains("cerrar") && !value.contains("cerrar sesion")) return AppIcons.attrCloseIcon();
        if (value.contains("salir")) return AppIcons.exitIcon();
        if (value.contains("terminar") || value.contains("aplicar") || value.contains("confirmar")) {
            return AppIcons.finishIcon();
        }
        if (value.contains("guardar")) return AppIcons.saveIcon();
        if (value.contains("abrir")) return AppIcons.openIcon();
        if (value.contains("copiar")) return AppIcons.attrCopyIcon();
        if (value.contains("pegar")) return AppIcons.openIcon();
        if (value.contains("eliminar") || value.contains("borrar") || value.contains("quitar")) {
            return AppIcons.deleteIcon();
        }
        if (value.contains("medir") || value.contains("distancia")) return AppIcons.distanceIcon();
        if (value.contains("area") || value.contains("área")) return AppIcons.areaIcon();
        if (value.contains("pin")) return AppIcons.pointIcon();
        if (value.contains("zoom") || value.contains("vista")) return AppIcons.zoomInIcon();
        if (value.contains("vertice") || value.contains("vértice") || value.contains("editar")) {
            return AppIcons.attrEditIcon();
        }
        if (value.contains("agregar") || value.contains("crear") || value.contains("nueva")) {
            return AppIcons.addLayerIcon();
        }
        if (value.contains("propiedad") || value.contains("informacion")
                || value.contains("información") || value.contains("info")) {
            return AppIcons.propertiesIcon();
        }
        if (value.contains("exportar")) return AppIcons.exportIcon();
        if (value.contains("importar") || value.contains("tabla")) return AppIcons.importTableIcon();
        if (value.contains("crs") || value.contains("coordenada")) return AppIcons.crsIcon();
        if (value.contains("seleccionar") || value.contains("selección")) return AppIcons.selectIcon();
        if (value.contains("cuenca") || value.contains("drenaje") || value.contains("hidro")) {
            return AppIcons.hydrologyIcon();
        }
        if (value.contains("red") || value.contains("ruta")) return AppIcons.networkIcon();
        if (value.contains("calculo") || value.contains("cálculo") || value.contains("calculadora")) {
            return AppIcons.attrCalculatorIcon();
        }
        return AppIcons.genericLayerIcon();
    }

    /** Fill missing icons in an already-built popup/submenu tree, recursively. */
    static void apply(Container container) {
        if (container == null) return;
        for (Component component : container.getComponents()) {
            if (component instanceof JMenuItem item) {
                if (item.getIcon() == null) {
                    item.setIcon(iconFor(item.getText()));
                }
                if (item instanceof JMenu menu) {
                    apply(menu.getPopupMenu());
                }
            }
        }
    }
}
