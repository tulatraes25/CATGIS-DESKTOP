package ar.com.catgis.layout;

import java.util.*;

/**
 * Central registry for all CATMAP layout templates.
 * Templates are organized by category for easy browsing.
 */
public final class TemplateRegistry {

    public enum Category {
        TECNICAS("Tecnicas"),
        AMBIENTALES("Ambientales"),
        CATASTRALES("Catastrales"),
        TOPOGRAFIA("Topografia / Relieve"),
        HIDROLOGIA("Hidrologia"),
        INFRAESTRUCTURA("Infraestructura / Campo"),
        SATELITALES("Satelitales"),
        INSTITUCIONAL("Presentacion / Institucional"),
        PERFILES("Perfiles / Tablas"),
        REFERENCIA("Referencia / Ubicacion");

        private final String label;
        Category(String l) { label = l; }
        public String getLabel() { return label; }
    }

    public static final List<Entry> ALL = new ArrayList<>();
    private static final Map<String, Entry> BY_KEY = new LinkedHashMap<>();
    private static final Map<Category, List<Entry>> BY_CATEGORY = new LinkedHashMap<>();

    public static void register(String key, String displayName, Category category) {
        Entry e = new Entry(key, displayName, category);
        ALL.add(e);
        BY_KEY.put(key, e);
        BY_CATEGORY.computeIfAbsent(category, k -> new ArrayList<>()).add(e);
    }

    public static Entry get(String key) { return BY_KEY.get(key); }
    public static List<Entry> getAll() {
        ensureLoaded();
        return ALL;
    }

    private static volatile boolean loaded = false;
    private static void ensureLoaded() {
        if (!loaded) {
            try { Class.forName("ar.com.catgis.layout.TemplateCatalog"); } catch (Exception ignored) {}
            loaded = true;
        }
    }
    public static List<Entry> getByCategory(Category cat) { return BY_CATEGORY.getOrDefault(cat, Collections.emptyList()); }
    public static Map<Category, List<Entry>> getAllByCategory() { return BY_CATEGORY; }

    public static class Entry {
        public final String key;
        public final String displayName;
        public final Category category;
        Entry(String k, String d, Category c) { key = k; displayName = d; category = c; }
    }
}
