package ar.com.catgis;

import javax.swing.JComponent;
import javax.swing.UIManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public final class I18n {

    private static final Preferences PREFS = Preferences.userNodeForPackage(I18n.class);
    private static final String LANGUAGE_KEY = "ui.language";
    private static final String BUNDLE_NAME = "ar.com.catgis.CatgisMessages";
    private static final String INSTALL_DEFAULTS_FILE = "catgis-defaults.properties";

    private static Language currentLanguage = Language.SPANISH;
    private static ResourceBundle bundle;

    private I18n() {
    }

    public static void initialize() {
        setLanguage(resolveInitialLanguage(), false);
    }

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(Language language) {
        setLanguage(language, true);
    }

    private static void setLanguage(Language language, boolean persist) {
        currentLanguage = language != null ? language : Language.SPANISH;

        Locale locale = currentLanguage.toLocale();
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(locale);

        bundle = currentLanguage == Language.ENGLISH
                ? ResourceBundle.getBundle(BUNDLE_NAME, locale)
                : null;

        applySwingDefaults();

        if (persist) {
            PREFS.put(LANGUAGE_KEY, currentLanguage.code);
        }
    }

    public static String t(String sourceText) {
        if (sourceText == null) {
            return "";
        }
        if (currentLanguage == Language.SPANISH || bundle == null) {
            return sourceText;
        }
        try {
            return bundle.getString(sourceText);
        } catch (MissingResourceException ignored) {
            return englishFallback(sourceText);
        }
    }

    /**
     * Generate English from Spanish when no explicit translation exists.
     * Handles common GIS patterns, accents, and punctuation.
     */
    private static String englishFallback(String spanish) {
        if (spanish == null || spanish.isBlank()) return spanish;
        String s = spanish.trim();

        // Handle ellipsis suffix
        boolean ellipsis = s.endsWith("...");
        if (ellipsis) s = s.substring(0, s.length() - 3);

        // Handle trailing punctuation
        char last = s.charAt(s.length() - 1);
        String suffix = "";
        if (last == ':' || last == '?') {
            suffix = "" + (last == '?' ? '?' : ':');
            s = s.substring(0, s.length() - 1).trim();
        }

        // Common one-word translations
        String result = quickTranslate(s);
        if (result != null) {
            return result + (ellipsis ? "..." : "") + suffix;
        }

        // Word-by-word translation with common GIS terms
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            String translated = translateWord(w);
            if (sb.length() > 0) sb.append(' ');
            sb.append(translated);
        }
        return sb.toString() + (ellipsis ? "..." : "") + suffix;
    }

    private static String quickTranslate(String s) {
        switch (s) {
            // Common UI
            case "Aceptar": return "OK";
            case "Cancelar": return "Cancel";
            case "Cerrar": return "Close";
            case "Guardar": return "Save";
            case "Buscar": return "Search";
            case "Eliminar": return "Delete";
            case "Abrir": return "Open";
            case "Cargar": return "Load";
            case "Exportar": return "Export";
            case "Importar": return "Import";
            case "Aplicar": return "Apply";
            case "Restablecer": return "Reset";
            case "Actualizar": return "Update";
            case "Refrescar": return "Refresh";
            case "Deshacer": return "Undo";
            case "Rehacer": return "Redo";
            case "Seleccionar": return "Select";
            case "Anadir": return "Add";
            case "Quitar": return "Remove";
            case "Mover": return "Move";
            case "Copiar": return "Copy";
            case "Cortar": return "Cut";
            case "Pegar": return "Paste";
            case "Si": return "Yes";
            case "No": return "No";
            case "Listo": return "Ready";
            case "Ayuda": return "Help";
            // Labels
            case "Nombre": return "Name";
            case "Descripcion": return "Description";
            case "Archivo": return "File";
            case "Capa": return "Layer";
            case "Capas": return "Layers";
            case "Mapa": return "Map";
            case "Tabla": return "Table";
            case "Vista": return "View";
            case "Datos": return "Data";
            case "Proyecto": return "Project";
            case "Formato": return "Format";
            case "Fuente": return "Source";
            case "Ruta": return "Path";
            case "Valor": return "Value";
            case "Color": return "Color";
            case "Estilo": return "Style";
            case "Simbolo": return "Symbol";
            case "Etiqueta": return "Label";
            case "Leyenda": return "Legend";
            case "Escala": return "Scale";
            case "Version": return "Version";
            case "Autor": return "Author";
            case "Licencia": return "License";
            case "Titulo": return "Title";
            case "Fecha": return "Date";
            case "Hora": return "Time";
            case "Error": return "Error";
            case "Advertencia": return "Warning";
            case "Analisis": return "Analysis";
            case "Resultado": return "Result";
            case "Configuracion": return "Settings";
            case "Propiedades": return "Properties";
            case "Herramientas": return "Tools";
            case "Ventana": return "Window";
            case "Editar": return "Edit";
            case "Nuevo": return "New";
            // GIS terms
            case "Raster": return "Raster";
            case "Vector": return "Vector";
            case "Geometria": return "Geometry";
            case "Poligono": return "Polygon";
            case "Linea": return "Line";
            case "Punto": return "Point";
            case "Vertice": return "Vertex";
            case "Superficie": return "Area";
            case "Perfil": return "Profile";
            case "Cuenca": return "Basin";
            case "Drenaje": return "Drainage";
            case "Pendiente": return "Slope";
            case "Aspecto": return "Aspect";
            case "Suelo": return "Soil";
            case "Hidrologia": return "Hydrology";
            case "Topografia": return "Topography";
            case "Cartografia": return "Cartography";
            case "Imagen": return "Image";
            case "Banda": return "Band";
            case "Pixel": return "Pixel";
            case "Celda": return "Cell";
            case "Grilla": return "Grid";
            case "Mosaico": return "Mosaic";
            case "Recorte": return "Clip";
            case "Buffer": return "Buffer";
            case "Interseccion": return "Intersection";
            case "Union": return "Union";
            case "Diferencia": return "Difference";
            case "Disolver": return "Dissolve";
            case "Contorno": return "Contour";
            case "Curva": return "Curve";
            case "Nivel": return "Level";
        }
        return null;
    }

    private static String translateWord(String w) {
        if (w.isEmpty()) return w;
        // Preserve casing
        boolean allUpper = w.equals(w.toUpperCase(java.util.Locale.ROOT));
        boolean firstUpper = Character.isUpperCase(w.charAt(0));
        String lower = w.toLowerCase(java.util.Locale.ROOT);

        String translated = switch (lower) {
            case "de" -> "of";
            case "del" -> "of the";
            case "en" -> "in";
            case "el" -> "the";
            case "la" -> "the";
            case "los" -> "the";
            case "las" -> "the";
            case "un" -> "a";
            case "una" -> "a";
            case "para" -> "for";
            case "por" -> "by";
            case "con" -> "with";
            case "sin" -> "without";
            case "sobre" -> "on";
            case "entre" -> "between";
            case "desde" -> "from";
            case "hasta" -> "to";
            case "hacia" -> "toward";
            case "como" -> "as";
            case "mas" -> "more";
            case "menos" -> "less";
            case "muy" -> "very";
            case "poco" -> "little";
            case "mucho" -> "much";
            case "todo" -> "all";
            case "cada" -> "each";
            case "otro" -> "other";
            case "mismo" -> "same";
            case "aqui" -> "here";
            case "alli" -> "there";
            case "ahora" -> "now";
            case "luego" -> "then";
            case "siempre" -> "always";
            case "nunca" -> "never";
            case "tambien" -> "also";
            case "solo" -> "only";
            case "casi" -> "almost";
            case "nombre" -> "name";
            case "archivo" -> "file";
            case "capa" -> "layer";
            case "mapa" -> "map";
            case "datos" -> "data";
            case "vista" -> "view";
            case "tabla" -> "table";
            case "valor" -> "value";
            case "tipo" -> "type";
            case "modo" -> "mode";
            case "estado" -> "state";
            case "tamano" -> "size";
            case "color" -> "color";
            case "forma" -> "shape";
            case "texto" -> "text";
            case "linea" -> "line";
            case "punto" -> "point";
            case "area" -> "area";
            case "zona" -> "zone";
            case "region" -> "region";
            case "borde" -> "border";
            case "centro" -> "center";
            case "inicio" -> "start";
            case "fin" -> "end";
            case "norte" -> "north";
            case "sur" -> "south";
            case "este" -> "east";
            case "oeste" -> "west";
            case "alto" -> "high";
            case "bajo" -> "low";
            case "grande" -> "large";
            case "pequeno" -> "small";
            case "largo" -> "long";
            case "corto" -> "short";
            case "ancho" -> "wide";
            case "angosto" -> "narrow";
            case "rapido" -> "fast";
            case "lento" -> "slow";
            case "fuerte" -> "strong";
            case "debil" -> "weak";
            case "duro" -> "hard";
            case "blando" -> "soft";
            case "ligero" -> "light";
            case "pesado" -> "heavy";
            case "simple" -> "simple";
            case "complejo" -> "complex";
            case "facil" -> "easy";
            case "dificil" -> "hard";
            case "claro" -> "clear";
            case "oscuro" -> "dark";
            case "nuevo" -> "new";
            case "viejo" -> "old";
            case "bueno" -> "good";
            case "malo" -> "bad";
            case "mejor" -> "better";
            case "peor" -> "worse";
            case "primero" -> "first";
            case "ultimo" -> "last";
            case "anterior" -> "previous";
            case "siguiente" -> "next";
            case "actual" -> "current";
            case "final" -> "final";
            case "total" -> "total";
            case "parcial" -> "partial";
            case "completo" -> "complete";
            case "disponible" -> "available";
            case "activo" -> "active";
            case "inactivo" -> "inactive";
            case "visible" -> "visible";
            case "oculto" -> "hidden";
            case "abierto" -> "open";
            case "cerrado" -> "closed";
            case "seleccionado" -> "selected";
            case "marcado" -> "marked";
            case "filtrado" -> "filtered";
            case "ordenado" -> "sorted";
            case "agrupado" -> "grouped";
            case "uso" -> "usage";
            case "salida" -> "output";
            case "entrada" -> "input";
            case "consulta" -> "query";
            case "busqueda" -> "search";
            case "resultado" -> "result";
            case "informe" -> "report";
            case "mensaje" -> "message";
            case "alerta" -> "alert";
            case "dialogo" -> "dialog";
            case "panel" -> "panel";
            case "barra" -> "bar";
            case "boton" -> "button";
            case "menu" -> "menu";
            case "lista" -> "list";
            case "arbol" -> "tree";
            case "pestana" -> "tab";
            case "ventana" -> "window";
            case "marco" -> "frame";
            case "campo" -> "field";
            case "etiqueta" -> "label";
            case "icono" -> "icon";
            case "imagen" -> "image";
            case "grafico" -> "graphic";
            case "figura" -> "figure";
            case "esquema" -> "diagram";
            case "plano" -> "plan";
            case "croquis" -> "sketch";
            case "proyecto" -> "project";
            case "tarea" -> "task";
            case "trabajo" -> "work";
            case "sesion" -> "session";
            case "usuario" -> "user";
            case "equipo" -> "team";
            case "grupo" -> "group";
            case "categoria" -> "category";
            case "etapa" -> "stage";
            case "fase" -> "phase";
            case "paso" -> "step";
            case "nivel" -> "level";
            case "grado" -> "degree";
            case "indice" -> "index";
            case "codigo" -> "code";
            case "clave" -> "key";
            case "token" -> "token";
            case "perfil" -> "profile";
            case "cuenta" -> "account";
            case "rol" -> "role";
            case "permiso" -> "permission";
            case "acceso" -> "access";
            case "control" -> "control";
            case "gestion" -> "management";
            case "administracion" -> "administration";
            case "sistema" -> "system";
            case "servicio" -> "service";
            case "proceso" -> "process";
            case "operacion" -> "operation";
            case "funcion" -> "function";
            case "metodo" -> "method";
            case "clase" -> "class";
            case "objeto" -> "object";
            case "instancia" -> "instance";
            case "modelo" -> "model";
            case "motor" -> "engine";
            case "nucleo" -> "core";
            case "base" -> "base";
            case "capa de" -> "layer of";
            default -> {
                // Strip accents and keep as-is (GIS terms often shared)
                String stripped = lower
                        .replace("\u00E1", "a").replace("\u00E9", "e")
                        .replace("\u00ED", "i").replace("\u00F3", "o")
                        .replace("\u00FA", "u").replace("\u00F1", "n")
                        .replace("\u00FC", "u");
                // If the word is a known GIS term, keep it
                yield stripped;
            }
        };

        if (allUpper) return translated.toUpperCase(java.util.Locale.ROOT);
        if (firstUpper) return Character.toUpperCase(translated.charAt(0)) + translated.substring(1);
        return translated;
    }

    public static String format(String sourcePattern, Object... arguments) {
        return MessageFormat.format(t(sourcePattern), arguments);
    }

    public static String languageMenuLabel() {
        return currentLanguage == Language.ENGLISH ? "Language" : "Idioma";
    }

    public static String languageSelectionLabel(Language language) {
        if (language == null) {
            return "";
        }
        return language == Language.SPANISH ? "Espa\u00F1ol" : "English";
    }

    private static Language resolveInitialLanguage() {
        String explicitProperty = System.getProperty("catgis.ui.language");
        if (explicitProperty != null && !explicitProperty.isBlank()) {
            return Language.fromCode(explicitProperty);
        }

        String storedLanguage = PREFS.get(LANGUAGE_KEY, null);
        if (storedLanguage != null && !storedLanguage.isBlank()) {
            return Language.fromCode(storedLanguage);
        }

        String installDefault = loadInstallDefaultLanguage();
        if (installDefault != null && !installDefault.isBlank()) {
            return Language.fromCode(installDefault);
        }

        return Language.SPANISH;
    }

    private static String loadInstallDefaultLanguage() {
        Path defaultsPath = resolveInstallDefaultsPath();
        if (defaultsPath == null || !Files.exists(defaultsPath)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(defaultsPath)) {
            properties.load(input);
            return properties.getProperty(LANGUAGE_KEY);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Path resolveInstallDefaultsPath() {
        String jpackageAppPath = System.getProperty("jpackage.app-path");
        if (jpackageAppPath != null && !jpackageAppPath.isBlank()) {
            Path executablePath = Paths.get(jpackageAppPath);
            if (executablePath.getParent() != null) {
                return executablePath.getParent().resolve("app").resolve(INSTALL_DEFAULTS_FILE);
            }
        }

        try {
            Path location = Paths.get(I18n.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path baseDir = Files.isDirectory(location) ? location : location.getParent();
            if (baseDir != null) {
                return baseDir.resolve(INSTALL_DEFAULTS_FILE);
            }
        } catch (Exception ignored) { CatgisLogger.warn("I18n: operation failed", ignored); }

        return null;
    }

    private static void applySwingDefaults() {
        UIManager.put("OptionPane.okButtonText", t("Aceptar"));
        UIManager.put("OptionPane.cancelButtonText", t("Cancelar"));
        UIManager.put("OptionPane.yesButtonText", t("Si"));
        UIManager.put("OptionPane.noButtonText", t("No"));

        UIManager.put("FileChooser.openButtonText", t("Abrir"));
        UIManager.put("FileChooser.saveButtonText", t("Guardar"));
        UIManager.put("FileChooser.cancelButtonText", t("Cancelar"));
        UIManager.put("FileChooser.updateButtonText", t("Actualizar"));
        UIManager.put("FileChooser.helpButtonText", t("Ayuda"));
        UIManager.put("FileChooser.directoryOpenButtonText", t("Abrir"));
        UIManager.put("FileChooser.lookInLabelText", t("Buscar en:"));
        UIManager.put("FileChooser.saveInLabelText", t("Guardar en:"));
        UIManager.put("FileChooser.fileNameLabelText", t("Nombre:"));
        UIManager.put("FileChooser.filesOfTypeLabelText", t("Tipo de archivo:"));
        UIManager.put("FileChooser.openDialogTitleText", t("Abrir"));
        UIManager.put("FileChooser.saveDialogTitleText", t("Guardar"));
        UIManager.put("FileChooser.upFolderToolTipText", t("Subir un nivel"));
        UIManager.put("FileChooser.homeFolderToolTipText", t("Inicio"));
        UIManager.put("FileChooser.newFolderToolTipText", t("Nueva carpeta"));
        UIManager.put("FileChooser.listViewButtonToolTipText", t("Vista de lista"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", t("Vista de detalles"));
    }

    public enum Language {
        SPANISH("es"),
        ENGLISH("en");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        private Locale toLocale() {
            return Locale.forLanguageTag(code);
        }

        private static Language fromCode(String code) {
            if (ENGLISH.code.equalsIgnoreCase(code)) {
                return ENGLISH;
            }
            return SPANISH;
        }
    }
}
