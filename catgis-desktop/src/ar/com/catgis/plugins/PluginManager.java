package ar.com.catgis.plugins;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple plugin system for CATGIS.
 * Loads JAR files from a plugins directory.
 */
public final class PluginManager {

    private static final String PLUGINS_DIR = "plugins";
    private static final Map<String, PluginInfo> loadedPlugins = new LinkedHashMap<>();
    private static ClassLoader pluginClassLoader;

    private PluginManager() {}

    /**
     * Plugin information.
     */
    public record PluginInfo(String name, String version, String description, boolean enabled) {}

    /**
     * Initialize the plugin system.
     */
    public static void initialize() {
        File pluginsDir = new File(PLUGINS_DIR);
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        loadPlugins();
    }

    /**
     * Load all plugins from the plugins directory.
     */
    public static void loadPlugins() {
        File pluginsDir = new File(PLUGINS_DIR);
        if (!pluginsDir.exists()) return;

        List<URL> jarUrls = new ArrayList<>();
        File[] jars = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars != null) {
            for (File jar : jars) {
                try {
                    jarUrls.add(jar.toURI().toURL());
                    loadedPlugins.put(jar.getName(), new PluginInfo(
                            jar.getName().replace(".jar", ""),
                            "1.0",
                            "Plugin loaded from " + jar.getName(),
                            true));
                } catch (Exception ignored) {}
            }
        }

        if (!jarUrls.isEmpty()) {
            pluginClassLoader = new URLClassLoader(
                    jarUrls.toArray(new URL[0]),
                    PluginManager.class.getClassLoader());
        }
    }

    /**
     * Get all loaded plugins.
     */
    public static Map<String, PluginInfo> getPlugins() {
        return new LinkedHashMap<>(loadedPlugins);
    }

    /**
     * Get plugin class loader.
     */
    public static ClassLoader getPluginClassLoader() {
        return pluginClassLoader != null ? pluginClassLoader : PluginManager.class.getClassLoader();
    }

    /**
     * Enable/disable a plugin.
     */
    public static void setPluginEnabled(String pluginName, boolean enabled) {
        PluginInfo info = loadedPlugins.get(pluginName);
        if (info != null) {
            loadedPlugins.put(pluginName, new PluginInfo(
                    info.name(), info.version(), info.description(), enabled));
        }
    }
}
