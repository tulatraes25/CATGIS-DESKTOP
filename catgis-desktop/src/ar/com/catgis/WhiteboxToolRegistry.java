package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of available WhiteboxTools geoprocessing tools.
 * Tools are categorized by domain (terrain, hydrology, LiDAR, etc.)
 */
public final class WhiteboxToolRegistry {

    private WhiteboxToolRegistry() {}

    public record ToolInfo(String id, String name, String category, String description,
                           List<ParamInfo> params) {}

    public record ParamInfo(String name, String label, String type, boolean required, String description) {}

    public static List<ToolInfo> getTools() {
        List<ToolInfo> tools = new ArrayList<>();

        // Terrain Analysis
        tools.add(new ToolInfo("Slope", "Pendiente", "Terreno",
                "Calcula la pendiente (grados) desde un DEM",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de pendiente"))));

        tools.add(new ToolInfo("Aspect", "Aspecto", "Terreno",
                "Calcula el aspecto (direccion de pendiente) desde un DEM",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de aspecto"))));

        tools.add(new ToolInfo("Hillshade", "Sombreado", "Terreno",
                "Genera sombreado de relieve desde un DEM",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de sombreado"),
                       new ParamInfo("altitude", "Altitud", "NUMBER", false, "Altitud del sol (0-90, default 45)"),
                       new ParamInfo("azimuth", "Azimut", "NUMBER", false, "Azimut del sol (0-360, default 315)"))));

        tools.add(new ToolInfo("Curvature", "Curvatura", "Terreno",
                "Calcula la curvatura de un DEM",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de curvatura"))));

        tools.add(new ToolInfo("TerrainRuggednessIndex", "Rugosidad del terreno", "Terreno",
                "Calcula el indice de rugosidad topografica (TRI)",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster TRI"))));

        tools.add(new ToolInfo("Viewshed", "Visibilidad", "Terreno",
                "Calcula areas visibles desde un punto observador",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de visibilidad"),
                       new ParamInfo("obs_x", "X observador", "NUMBER", true, "Coordenada X del observador"),
                       new ParamInfo("obs_y", "Y observador", "NUMBER", true, "Coordenada Y del observador"),
                       new ParamInfo("obs_elev", "Elevacion observador", "NUMBER", false, "Elevacion del observador"),
                       new ParamInfo("max_dist", "Distancia maxima", "NUMBER", false, "Distancia maxima de visibilidad"))));

        tools.add(new ToolInfo("MultidirectionalHillshade", "Sombreado multidireccional", "Terreno",
                "Genera sombreado con multiples direcciones de iluminacion",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de sombreado"))));

        tools.add(new ToolInfo("ConvergenceIndex", "Indice de convergencia", "Terreno",
                "Calcula el indice de convergencia del terreno",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de convergencia"))));

        // Hydrology
        tools.add(new ToolInfo("D8FlowAccumulation", "Acumulacion de flujo D8", "Hidrologia",
                "Calcula la acumulacion de flujo usando D8",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de acumulacion"),
                       new ParamInfo("out_type", "Tipo salida", "TEXT", false, "cars/hillslope (default cars)"))));

        tools.add(new ToolInfo("D8Pointer", "Puntero D8", "Hidrologia",
                "Calcula la direccion de flujo usando D8",
                List.of(new ParamInfo("input", "DEM de entrada", "RASTER", true, "Raster de elevacion"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de direccion"))));

        tools.add(new ToolInfo("Watershed", "Cuenca de drenaje", "Hidrologia",
                "Calcula cuencas de drenaje desde un DEM",
                List.of(new ParamInfo("d8_pntr", "Puntero D8", "RASTER", true, "Raster de direccion D8"),
                       new ParamInfo("pour_pts", "Puntos vertedero", "VECTOR", true, "Puntos de salida"),
                       new ParamInfo("output", "Salida", "VECTOR", true, "Poligonos de cuenca"))));

        tools.add(new ToolInfo("StreamOrder", "Orden de corrientes", "Hidrologia",
                "Calcula el orden Strahler de las corrientes",
                List.of(new ParamInfo("d8_pntr", "Puntero D8", "RASTER", true, "Raster de direccion D8"),
                       new ParamInfo("streams", "Corrientes", "RASTER", true, "Raster de corrientes"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster de orden"))));

        // Raster Analysis
        tools.add(new ToolInfo("IdwInterpolation", "Interpolacion IDW", "Raster",
                "Interpolacion por distancia inversa al cuadrado",
                List.of(new ParamInfo("input", "Puntos de entrada", "VECTOR", true, "Capa de puntos"),
                       new ParamInfo("field", "Campo", "TEXT", true, "Campo con valores"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster interpolado"),
                       new ParamInfo("cell_size", "Tamanio de celda", "NUMBER", false, "Tamanio de celda"))));

        tools.add(new ToolInfo("Reclassify", "Reclasificar", "Raster",
                "Reclasifica un raster basado en reglas de valor",
                List.of(new ParamInfo("input", "Raster de entrada", "RASTER", true, "Raster a reclasificar"),
                       new ParamInfo("output", "Salida", "RASTER", true, "Raster reclasificado"),
                       new ParamInfo("assign_vals", "Valores", "TEXT", true, "Lista de valores"))));

        // Vector Analysis
        tools.add(new ToolInfo("BufferAnalysis", "Buffer vectorial", "Vector",
                "Genera areas de buffer alrededor de entidades vectoriales",
                List.of(new ParamInfo("input", "Capa de entrada", "VECTOR", true, "Capa vectorial"),
                       new ParamInfo("output", "Salida", "VECTOR", true, "Capa de buffer"),
                       new ParamInfo("distance", "Distancia", "NUMBER", true, "Distancia del buffer"))));

        // LiDAR
        tools.add(new ToolInfo("LidarInfo", "Info LiDAR", "LiDAR",
                "Muestra informacion de un archivo LiDAR",
                List.of(new ParamInfo("input", "Archivo LiDAR", "FILE", true, "Archivo LAS/LAZ"))));

        return tools;
    }

    public static List<ToolInfo> getToolsByCategory(String category) {
        return getTools().stream()
                .filter(t -> t.category().equals(category))
                .toList();
    }

    public static List<String> getCategories() {
        return getTools().stream()
                .map(ToolInfo::category)
                .distinct()
                .sorted()
                .toList();
    }
}
