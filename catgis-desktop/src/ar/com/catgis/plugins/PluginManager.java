package ar.com.catgis.plugins;

import ar.com.catgis.MapPanel;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Plugin system for CATGIS with SPI-based plugin discovery.
 * <p>
 * Plugins are JAR files placed in the 'plugins' directory. Each plugin
 * must implement {@link CatgisPlugin} and declare it via META-INF/services
 * or a manifest entry.
 * </p>
 * <p>
 * Supports: hot-reload (rescan directory), enable/disable, sandbox classloader.
 * </p>
 */
public final class PluginManager {

    private static final String PLUGINS_DIR = "plugins";
    private static final Map<String, PluginInfo> loadedPlugins = new LinkedHashMap<>();
    private static final List<CatgisPlugin> activePlugins = new CopyOnWriteArrayList<>();
    private static ClassLoader pluginClassLoader;
    private static long lastScanTime;

    private PluginManager() {}

    public record PluginInfo(String name, String version, String description,
                              boolean enabled, String mainClass) {}

    // ─── Lifecycle ─────────────────────────────────────────────────────

    /**
     * Initialize the plugin system. Called at startup.
     */
    public static void initialize() {
        File dir = new File(PLUGINS_DIR);
        if (!dir.exists()) dir.mkdirs();
        loadPlugins();
    }

    /**
     * Scan for new/removed plugins (hot-reload).
     */
    public static void scanForChanges() {
        File dir = new File(PLUGINS_DIR);
        if (!dir.exists()) return;

        long lastMod = dir.lastModified();
        if (lastMod == lastScanTime) return;
        lastScanTime = lastMod;

        // Shutdown removed plugins
        Set<String> currentJars = new HashSet<>();
        File[] jars = dir.listFiles((d, n) -> n.endsWith(".jar"));
        if (jars != null) {
            for (File jar : jars) currentJars.add(jar.getName());
        }

        for (String name : new ArrayList<>(loadedPlugins.keySet())) {
            if (!currentJars.contains(name)) {
                unloadPlugin(name);
            }
        }

        // Load new plugins
        loadPlugins();
    }

    // ─── Loading ──────────────────────────────────────────────────────

    private static void loadPlugins() {
        File dir = new File(PLUGINS_DIR);
        if (!dir.exists()) return;

        List<URL> jarUrls = new ArrayList<>();
        File[] jars = dir.listFiles((d, n) -> n.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            String name = jar.getName();
            if (loadedPlugins.containsKey(name)) continue; // already loaded

            try {
                // Read manifest for plugin metadata
                String mainClass = null;
                String version = "1.0";
                String description = "";
                try (JarFile jf = new JarFile(jar)) {
                    Manifest mf = jf.getManifest();
                    if (mf != null) {
                        mainClass = mf.getMainAttributes().getValue("Plugin-Class");
                        if (mainClass == null) {
                            mainClass = mf.getMainAttributes().getValue("Main-Class");
                        }
                        String v = mf.getMainAttributes().getValue("Plugin-Version");
                        if (v != null) version = v;
                        String d = mf.getMainAttributes().getValue("Plugin-Description");
                        if (d != null) description = d;
                    }
                }

                URL jarUrl = jar.toURI().toURL();
                jarUrls.add(jarUrl);
                loadedPlugins.put(name, new PluginInfo(
                        name.replace(".jar", ""),
                        version,
                        description.isEmpty() ? "Plugin: " + name : description,
                        true,
                        mainClass));
            } catch (Exception e) {
                loadedPlugins.put(name, new PluginInfo(
                        name.replace(".jar", ""), "error",
                        "Failed to load: " + e.getMessage(), false, null));
            }
        }

        // Create classloader with all JARs
        if (!jarUrls.isEmpty()) {
            // Isolate plugins in their own classloader (sandbox)
            URLClassLoader oldLoader = pluginClassLoader instanceof URLClassLoader ucl ? ucl : null;
            pluginClassLoader = new URLClassLoader(
                    jarUrls.toArray(new URL[0]),
                    PluginManager.class.getClassLoader());

            // Instantiate plugins via SPI
            for (URL url : jarUrls) {
                try {
                    instantiatePlugin(url);
                } catch (Exception ignored) {}
            }

            // Close old loader
            if (oldLoader != null) {
                try { oldLoader.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static void instantiatePlugin(URL jarUrl) {
        try {
            // Try ServiceLoader (SPI) approach
            ServiceLoader<CatgisPlugin> loader = ServiceLoader.load(
                    CatgisPlugin.class, pluginClassLoader);
            for (CatgisPlugin plugin : loader) {
                if (!activePlugins.contains(plugin)) {
                    activePlugins.add(plugin);
                    plugin.onEnable();
                }
            }
        } catch (Exception ignored) {}
    }

    private static void unloadPlugin(String jarName) {
        PluginInfo info = loadedPlugins.remove(jarName);
        if (info != null && info.mainClass() != null) {
            // Notify plugins of shutdown
            for (CatgisPlugin p : activePlugins) {
                if (p.getClass().getName().equals(info.mainClass())) {
                    p.onDisable();
                    activePlugins.remove(p);
                    break;
                }
            }
        }
    }

    // ─── Access ───────────────────────────────────────────────────────

    /**
     * Get all loaded plugins (enabled and disabled).
     */
    public static Map<String, PluginInfo> getPlugins() {
        return new LinkedHashMap<>(loadedPlugins);
    }

    /**
     * Get active (enabled) plugins.
     */
    public static List<CatgisPlugin> getActivePlugins() {
        return new ArrayList<>(activePlugins);
    }

    /**
     * Get plugin class loader.
     */
    public static ClassLoader getPluginClassLoader() {
        return pluginClassLoader != null ? pluginClassLoader
                : PluginManager.class.getClassLoader();
    }

    /**
     * Enable/disable a plugin.
     */
    public static void setPluginEnabled(String name, boolean enabled) {
        PluginInfo info = loadedPlugins.get(name);
        if (info != null) {
            loadedPlugins.put(name, new PluginInfo(
                    info.name(), info.version(), info.description(),
                    enabled, info.mainClass()));

            // Notify plugin
            for (CatgisPlugin p : activePlugins) {
                String className = p.getClass().getName();
                if (className.equals(info.mainClass())
                        || name.contains(p.getClass().getSimpleName())) {
                    if (enabled) p.onEnable();
                    else p.onDisable();
                    break;
                }
            }
        }
    }

    /**
     * Shutdown all plugins.
     */
    public static void shutdown() {
        for (CatgisPlugin p : activePlugins) {
            try { p.onDisable(); } catch (Exception ignored) {}
        }
        activePlugins.clear();
        loadedPlugins.clear();
    }
}
