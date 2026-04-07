package ar.com.catgis;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.List;

public final class CatgisAppInfo {

    private static final String FALLBACK_VERSION = "1.0";

    private CatgisAppInfo() {
    }

    public static String getApplicationName() {
        return "CATGIS Desktop";
    }

    public static String getDisplayVersion() {
        String packageVersion = CatgisAppInfo.class.getPackage() != null
                ? CatgisAppInfo.class.getPackage().getImplementationVersion()
                : null;
        if (packageVersion != null && !packageVersion.isBlank()) {
            return packageVersion;
        }
        return FALLBACK_VERSION;
    }

    public static String getTagline() {
        return I18n.t("GIS de escritorio para edicion, analisis visual, datos remotos y composicion cartografica.");
    }

    public static String getAuthorLine() {
        return I18n.t("Creado por Lic Claudio Alejandro Tula - Licenciado en proteccion y saneamiento ambiental.");
    }

    public static String getStatusLine() {
        return I18n.t("Estado actual: desarrollo activo, orientado a trabajo GIS tecnico, servicios web geograficos y mapas finales.");
    }

    public static String getFocusLine() {
        return I18n.t("Enfoque: trabajo GIS diario con una base moderna, integrada y mantenible sobre Windows.");
    }

    public static String getProfessionalNote() {
        return I18n.t("Proyecto en evolucion con foco en presentar una experiencia profesional, clara y confiable para cartografia y gestion espacial.");
    }

    public static List<String> getTechnologyLines() {
        return List.of(
                I18n.t("Java 21 y Gradle"),
                I18n.t("GeoTools 34 y JTS / LocationTech"),
                I18n.t("Shapefile, GeoJSON, KML, CSV y GeoPackage"),
                I18n.t("WMS, WFS, PostGIS y mapas base online"),
                I18n.t("Apache PDFBox para salida cartografica y PDF"),
                I18n.t("Apache POI para hojas de calculo"),
                I18n.t("PostgreSQL JDBC y GT JDBC PostGIS para conectividad espacial"),
                I18n.t("JavaDBF y SODS para intercambio tabular y compatibilidad adicional"),
                I18n.t("FlatLaf, FlatLaf Extras e IntelliJ Themes para la interfaz"),
                I18n.t("JSVG para recursos SVG e iconografia")
        );
    }

    public static List<String> getComplementLines() {
        return List.of(
                I18n.t("OpenStreetMap y Esri World Imagery como mapas base integrados"),
                I18n.t("Soporte GeoPackage, WMS, WFS y PostGIS en la arquitectura actual"),
                I18n.t("Motor cartografico propio para layout, exportacion e impresion"),
                I18n.t("Snapping, CAD, geoprocesamiento basico y validacion topologica"),
                I18n.t("Iconografia, splash, branding e identidad visual propios de CATGIS"),
                I18n.t("Compositor cartografico con leyenda, escala, norte, cartucho y exportacion"),
                I18n.t("Gestor de modulos y bloques tematicos para crecer sin rehacer el nucleo")
        );
    }

    public static List<String> getCreditsLines() {
        return List.of(
                getAuthorLine(),
                I18n.t("Direccion y criterio funcional orientados a cartografia, analisis visual y flujo GIS de escritorio."),
                I18n.t("Construido sobre un ecosistema de librerias GIS, PDF, UI y conectividad espacial integradas dentro de CATGIS."),
                getProfessionalNote()
        );
    }

    public static ImageIcon getApplicationIcon(int size) {
        List<Image> images = AppBranding.getApplicationIconImages();
        if (images == null || images.isEmpty()) {
            return null;
        }
        Image best = images.get(images.size() - 1);
        Image scaled = best.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
