package ar.com.catgis.layout;

import java.awt.*;
import static ar.com.catgis.layout.TemplateRegistry.Category;

/**
 * Massive template catalog auto-registers all templates with TemplateRegistry.
 * Each template is defined with its key, display name, and category.
 * Builders are in LayoutTemplateManager.
 */
public final class TemplateCatalog {

    static {
        // === TECNICAS (A4) ===
        reg("A4_TECNICO", "Tecnica · A4 · Leyenda derecha", Category.TECNICAS);
        reg("A4_TECNICO_INFERIOR", "Tecnica · A4 · Leyenda inferior", Category.TECNICAS);
        reg("A4_TECNICO_CARTUCHO", "Tecnica · A4 · Cartucho inferior", Category.TECNICAS);
        reg("A4_TECNICO_LIMPIA", "Tecnica · A4 · Limpia (sin cartucho)", Category.TECNICAS);
        reg("A4_TECNICO_MAPA_UBICACION", "Tecnica · A4 · Mapa de ubicacion", Category.TECNICAS);
        reg("A4_TECNICO_DOBLE_MAPA", "Tecnica · A4 · Doble mapa", Category.TECNICAS);
        reg("A4_TECNICO_VERTICAL", "Tecnica · A4 · Vertical", Category.TECNICAS);
        reg("A4_VERTICAL", "Tecnica · A4 · Vertical institucional", Category.TECNICAS);
        // A3
        reg("A3_TECNICO", "Tecnica · A3 · Leyenda derecha", Category.TECNICAS);
        reg("A3_TECNICO_INFERIOR", "Tecnica · A3 · Leyenda inferior", Category.TECNICAS);
        reg("A3_TECNICO_LIMPIA", "Tecnica · A3 · Limpia", Category.TECNICAS);
        reg("A3_TECNICO_DOBLE_MAPA", "Tecnica · A3 · Doble mapa", Category.TECNICAS);

        // === AMBIENTALES (A4) ===
        reg("A4_AMBIENTAL", "Ambiental · A4 · Estandar", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_LEYENDA_LATERAL", "Ambiental · A4 · Leyenda lateral", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_CARTUCHO", "Ambiental · A4 · Cartucho amplio", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_SATELITAL", "Ambiental · A4 · Satelital", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_VEGETACION", "Ambiental · A4 · Vegetacion", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_IMPACTO", "Ambiental · A4 · Impacto", Category.AMBIENTALES);
        reg("A4_AMBIENTAL_MONITOREO", "Ambiental · A4 · Monitoreo", Category.AMBIENTALES);
        reg("A4_HIDROLOGIA", "Ambiental · A4 · Hidrologia", Category.AMBIENTALES);
        reg("A4_MUESTREO", "Ambiental · A4 · Muestreo", Category.AMBIENTALES);
        // A3
        reg("A3_AMBIENTAL", "Ambiental · A3 · Estandar", Category.AMBIENTALES);
        reg("A3_AMBIENTAL_IMPACTO", "Ambiental · A3 · Impacto ambiental", Category.AMBIENTALES);
        reg("A3_AMBIENTAL_SATELITAL", "Ambiental · A3 · Satelital", Category.AMBIENTALES);

        // === CATASTRALES (A4) ===
        reg("A4_CATASTRAL", "Catastral · A4 · Estandar", Category.CATASTRALES);
        reg("A4_PARCELARIO", "Catastral · A4 · Parcelario con tabla", Category.CATASTRALES);
        reg("A4_URBANO", "Catastral · A4 · Urbano", Category.CATASTRALES);
        reg("A4_CATASTRAL_NOMENCLATURA", "Catastral · A4 · Nomenclatura", Category.CATASTRALES);
        reg("A4_CATASTRAL_ZONIFICACION", "Catastral · A4 · Zonificacion", Category.CATASTRALES);
        reg("A4_CATASTRAL_ACCESOS", "Catastral · A4 · Parcelas + accesos", Category.CATASTRALES);
        // A3
        reg("A3_CATASTRAL", "Catastral · A3 · Estandar", Category.CATASTRALES);
        reg("A3_PARCELARIO", "Catastral · A3 · Parcelario con tabla", Category.CATASTRALES);
        reg("A3_CATASTRAL_INSTITUCIONAL", "Catastral · A3 · Institucional", Category.CATASTRALES);

        // === TOPOGRAFIA (A4) ===
        reg("A4_TOPOGRAFIA", "Topografia · A4 · Curvas de nivel", Category.TOPOGRAFIA);
        reg("A4_TOPOGRAFIA_RELIEVE", "Topografia · A4 · Relieve + hillshade", Category.TOPOGRAFIA);
        reg("A4_TOPOGRAFIA_PERFIL", "Topografia · A4 · Con perfil", Category.TOPOGRAFIA);
        reg("A4_TOPOGRAFIA_DRENAJE", "Topografia · A4 · Con drenaje", Category.TOPOGRAFIA);
        reg("A4_TOPOGRAFIA_GRILLA", "Topografia · A4 · Grilla fuerte", Category.TOPOGRAFIA);
        // A3
        reg("A3_TOPOGRAFIA", "Topografia · A3 · Curvas de nivel", Category.TOPOGRAFIA);
        reg("A3_TOPOGRAFIA_INSTITUCIONAL", "Topografia · A3 · Institucional", Category.TOPOGRAFIA);

        // === HIDROLOGIA (A4) ===
        reg("A4_HIDRO_CUENCAS", "Hidrologia · A4 · Cuencas", Category.HIDROLOGIA);
        reg("A4_HIDRO_ESCORRENTIA", "Hidrologia · A4 · Escorrentia", Category.HIDROLOGIA);
        reg("A4_HIDRO_DRENAJE", "Hidrologia · A4 · Drenaje", Category.HIDROLOGIA);
        reg("A4_HIDRO_INUNDACION", "Hidrologia · A4 · Inundacion", Category.HIDROLOGIA);
        reg("A4_HIDRO_TABLA", "Hidrologia · A4 · Con tabla", Category.HIDROLOGIA);
        // A3
        reg("A3_HIDROLOGIA", "Hidrologia · A3 · Cuencas", Category.HIDROLOGIA);
        reg("A3_HIDRO_TECNICA", "Hidrologia · A3 · Tecnica", Category.HIDROLOGIA);

        // === INFRAESTRUCTURA (A4) ===
        reg("A4_REFERENCIA", "Infraestructura · A4 · Referencia", Category.INFRAESTRUCTURA);
        reg("A4_ACCESIBILIDAD", "Infraestructura · A4 · Accesibilidad", Category.INFRAESTRUCTURA);
        reg("A4_EMPLAZAMIENTO", "Infraestructura · A4 · Emplazamiento", Category.INFRAESTRUCTURA);
        reg("A4_INFRAESTRUCTURA", "Infraestructura · A4 · General", Category.INFRAESTRUCTURA);
        reg("A4_CAMINOS", "Infraestructura · A4 · Caminos", Category.INFRAESTRUCTURA);
        reg("A4_DUCTOS", "Infraestructura · A4 · Ductos", Category.INFRAESTRUCTURA);
        reg("A4_LOCACIONES", "Infraestructura · A4 · Locaciones", Category.INFRAESTRUCTURA);
        reg("A4_RED_LINEAL", "Infraestructura · A4 · Red lineal", Category.INFRAESTRUCTURA);
        reg("A4_POZOS", "Infraestructura · A4 · Pozos / Sondeos", Category.INFRAESTRUCTURA);
        // A3
        reg("A3_INFRAESTRUCTURA", "Infraestructura · A3 · General", Category.INFRAESTRUCTURA);

        // === SATELITALES (A4) ===
        reg("A4_SATELITAL", "Satelital · A4 · Estandar", Category.SATELITALES);
        reg("A4_SATELITAL_TITULO", "Satelital · A4 · Titulo minimo", Category.SATELITALES);
        reg("A4_SATELITAL_INSTITUCIONAL", "Satelital · A4 · Institucional", Category.SATELITALES);
        reg("A4_SATELITAL_COMPARATIVA", "Satelital · A4 · Comparativa", Category.SATELITALES);
        reg("A4_SATELITAL_OVERLAY", "Satelital · A4 · Overlay tecnico", Category.SATELITALES);
        // A3
        reg("A3_SATELITAL", "Satelital · A3 · Estandar", Category.SATELITALES);
        reg("A3_SATELITAL_INSTITUCIONAL", "Satelital · A3 · Institucional", Category.SATELITALES);

        // === INSTITUCIONAL (A4) ===
        reg("A4_INSTITUCIONAL", "Institucional · A4 · Sobria", Category.INSTITUCIONAL);
        reg("A4_INSTITUCIONAL_LOGO", "Institucional · A4 · Con logo", Category.INSTITUCIONAL);
        reg("A4_INSTITUCIONAL_PORTADA", "Institucional · A4 · Portada", Category.INSTITUCIONAL);
        reg("A4_INSTITUCIONAL_RESUMEN", "Institucional · A4 · Mapa resumen", Category.INSTITUCIONAL);
        // A3
        reg("A3_PRESENTACION", "Institucional · A3 · Presentacion", Category.INSTITUCIONAL);
        reg("A3_INSTITUCIONAL_GRAN_MAPA", "Institucional · A3 · Gran mapa", Category.INSTITUCIONAL);

        // === PERFILES (A4) ===
        reg("A4_PERFIL", "Perfil · A4 · Altimetrico con tabla", Category.PERFILES);
        reg("A4_PERFIL_PROGRESIVAS", "Perfil · A4 · Progresivas", Category.PERFILES);
        reg("A4_PERFIL_MAPA", "Perfil · A4 · Mapa + perfil", Category.PERFILES);
        reg("A4_PERFIL_PUNTOS", "Perfil · A4 · Tabla de puntos", Category.PERFILES);
        // A3
        reg("A3_PERFIL", "Perfil · A3 · Altimetrico", Category.PERFILES);
        reg("A3_PERFIL_COMPLETO", "Perfil · A3 · Mapa + perfil + tabla", Category.PERFILES);

        // === REFERENCIA (A4) ===
        reg("A4_REFERENCIA_GENERAL", "Referencia · A4 · General", Category.REFERENCIA);
        reg("A4_REFERENCIA_REGIONAL", "Referencia · A4 · Regional", Category.REFERENCIA);
        reg("A4_REFERENCIA_LOCAL", "Referencia · A4 · Local", Category.REFERENCIA);
        reg("A4_REFERENCIA_INSET", "Referencia · A4 · Con inset", Category.REFERENCIA);
        // A3
        reg("A3_REFERENCIA", "Referencia · A3 · General", Category.REFERENCIA);
        reg("A3_REFERENCIA_REGIONAL", "Referencia · A3 · Regional", Category.REFERENCIA);
    }

    private static void reg(String key, String display, TemplateRegistry.Category cat) {
        TemplateRegistry.register(key, display, cat);
    }

    private TemplateCatalog() {}
}
