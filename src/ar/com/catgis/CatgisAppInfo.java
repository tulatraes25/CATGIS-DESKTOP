package ar.com.catgis;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.List;

public final class CatgisAppInfo {

    private static final String FALLBACK_VERSION = "1.0.0";
    private static final String RELEASE_STAGE = "Beta final";
    private static final int RELEASE_REVISION_COUNT = 18;
    private static final String FIRST_RELEASE_REVISION = "1.0.0.0";
    private static final String CURRENT_RELEASE_REVISION = "1.0.0.18";

    private CatgisAppInfo() {
    }

    public static String getApplicationName() {
        return "CATGIS Desktop";
    }

    public static String getDisplayVersion() {
        return getBaseVersion() + " " + RELEASE_STAGE + " (rev. " + RELEASE_REVISION_COUNT + ")";
    }

    public static String getBaseVersion() {
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

    public static String getCollaboratorLine() {
        return "Colaboradores de revisiones: Lic Daniel Warton y Geólogo Federico Sanchez.";
    }

    public static String getStatusLine() {
        return "Estado actual del release: beta final funcional sin firma digital de distribucion.";
    }

    public static String getFocusLine() {
        return I18n.t("Enfoque: trabajo GIS diario con una base moderna, integrada y mantenible sobre Windows.");
    }

    public static String getProfessionalNote() {
        return I18n.t("Proyecto en evolucion con foco en presentar una experiencia profesional, clara y confiable para cartografia y gestion espacial.");
    }

    public static String getRevisionCycleLine() {
        return "Ciclo de revisiones beta: " + FIRST_RELEASE_REVISION + " a " + CURRENT_RELEASE_REVISION
                + " (" + RELEASE_REVISION_COUNT + " revisiones).";
    }

    public static String getBetaFinalNote() {
        return "Esta build se considera beta final funcional. La firma digital del instalador queda como paso comercial posterior.";
    }

    public static List<String> getTechnologyLines() {
        return List.of(
                I18n.t("Java 21 y Gradle"),
                I18n.t("GeoTools 34 y JTS / LocationTech"),
                I18n.t("Shapefile, GeoJSON, KML, GeoPackage, WMS, WFS y PostGIS"),
                I18n.t("GeoTIFF, ArcGrid, raster process e ImageIO-Ext / GDAL para el bloque raster"),
                I18n.t("Apache PDFBox para salida cartografica y PDF"),
                I18n.t("Apache POI para hojas de calculo"),
                I18n.t("PostgreSQL JDBC y GT JDBC PostGIS para conectividad espacial"),
                I18n.t("JavaDBF y SODS para intercambio tabular y compatibilidad adicional"),
                I18n.t("FlatLaf, FlatLaf Extras e IntelliJ Themes para la interfaz"),
                I18n.t("JSVG para recursos SVG e iconografia"),
                I18n.t("Log4j 2 para logging y soporte tecnico de ejecucion")
        );
    }

    public static List<String> getComplementLines() {
        return List.of(
                I18n.t("OpenStreetMap y Esri World Imagery como mapas base integrados"),
                I18n.t("Soporte GeoPackage, WMS, WFS y PostGIS en la arquitectura actual"),
                I18n.t("CATSERVER como puerta de entrada de CATGIS para conectar servidores PostgreSQL/PostGIS"),
                I18n.t("Motor cartografico propio para layout, exportacion e impresion"),
                I18n.t("Snapping, CAD, geoprocesamiento basico y validacion topologica"),
                I18n.t("DEM online/local, recorte DEM, curvas, relieve e hidrologia preliminar integrados en el mismo flujo"),
                I18n.t("Suelos online y riesgo booleano preliminar integrados como bloques propios"),
                I18n.t("Centro de ayuda integrado con guía rápida y manual profesional 2026 embebido en PDF/DOCX"),
                I18n.t("Iconografia, splash, branding e identidad visual propios de CATGIS"),
                I18n.t("Compositor cartografico con leyenda, escala, norte, cartucho y exportacion"),
                I18n.t("Gestor de modulos y bloques tematicos para crecer sin rehacer el nucleo")
        );
    }

    public static List<String> getCreditsLines() {
        return List.of(
                getAuthorLine(),
                getCollaboratorLine(),
                getRevisionCycleLine(),
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
