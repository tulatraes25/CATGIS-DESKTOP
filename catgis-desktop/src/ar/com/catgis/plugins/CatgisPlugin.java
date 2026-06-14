package ar.com.catgis.plugins;

import javax.swing.*;
import java.util.List;

/**
 * Service Provider Interface (SPI) for CATGIS plugins.
 * <p>
 * Plugins implement this interface and register via
 * {@code META-INF/services/ar.com.catgis.plugins.CatgisPlugin}
 * or jar manifest {@code Plugin-Class} entry.
 * </p>
 * <p>
 * Lifecycle: {@code onEnable()} → (plugin runs) → {@code onDisable()} → (gc)
 * </p>
 * <p>
 * <strong>Security:</strong> Plugins run with full application privileges.
 * They can read/write files, access the network, and execute arbitrary code.
 * CATGIS does not sandbox or restrict plugin code. Only install plugins
 * from trusted sources.
 * </p>
 */
public interface CatgisPlugin {

    /** Plugin name shown in the UI. */
    String getName();

    /** Plugin version string. */
    String getVersion();

    /** Called when the plugin is loaded and enabled. */
    void onEnable();

    /** Called when the plugin is disabled or unloaded. */
    void onDisable();

    /**
     * Optional: provide a configuration panel.
     * Return null if no configuration is needed.
     */
    default JPanel getConfigPanel() { return null; }

    /**
     * Optional: provide menu items to add to CATGIS menus.
     * Return null or empty list if none.
     */
    default List<javax.swing.JMenuItem> getMenuItems() { return java.util.List.of(); }

    /**
     * Optional: provide toolbar actions.
     */
    default List<javax.swing.Action> getToolbarActions() { return java.util.List.of(); }
}
