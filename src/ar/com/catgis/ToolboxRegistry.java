package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ToolboxRegistry {

    private static final List<ToolboxAlgorithm> ALGORITHMS = new ArrayList<>();
    private static boolean initialized = false;

    private ToolboxRegistry() {
    }

    public static synchronized void initializeDefaults() {
        if (initialized) {
            return;
        }
        initialized = true;

        register(new ToolboxAlgorithm(
                "buffer",
                "Buffer",
                "Proximidad",
                "Genera areas de influencia alrededor de una capa vectorial.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_BUFFER
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addParameter(new ToolboxParameter("distance", "Distancia", ToolboxParameterType.DISTANCE, true, "Distancia del buffer"))
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "dissolve",
                "Dissolve",
                "Agregacion",
                "Agrega entidades por geometria completa o por un campo comun.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_DISSOLVE
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addParameter(new ToolboxParameter("field", "Campo", ToolboxParameterType.TEXT, false, "Campo opcional de agrupacion"))
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "clip",
                "Clip",
                "Superposicion",
                "Recorta la capa A usando una mascara poligonal B.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_CLIP
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addInput(ToolboxInputType.VECTOR_POLYGON)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "intersection",
                "Interseccion",
                "Superposicion",
                "Calcula la interseccion espacial entre dos capas.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_INTERSECTION
        ).addInput(ToolboxInputType.VECTOR_POLYGON)
                .addInput(ToolboxInputType.VECTOR_POLYGON)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "merge",
                "Merge",
                "Combinacion",
                "Combina entidades de dos capas del mismo tipo geometrico sin hacer overlay.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_MERGE
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addInput(ToolboxInputType.VECTOR_ANY)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "difference",
                "Diferencia",
                "Superposicion",
                "Resta una mascara poligonal B sobre la capa A.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_DIFFERENCE
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addInput(ToolboxInputType.VECTOR_POLYGON)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "spatial_join",
                "Spatial Join",
                "Relacion espacial",
                "Conserva la geometria de A y agrega atributos desde B por primera coincidencia o resumen.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_SPATIAL_JOIN
        ).addInput(ToolboxInputType.VECTOR_ANY)
                .addInput(ToolboxInputType.VECTOR_ANY)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));

        register(new ToolboxAlgorithm(
                "union",
                "Union geometrica",
                "Superposicion",
                "Parte dos capas poligonales en piezas de overlay y conserva atributos de ambas.",
                ToolboxOutputType.VECTOR_LAYER,
                GeoprocessingAssistantDialog.OP_UNION
        ).addInput(ToolboxInputType.VECTOR_POLYGON)
                .addInput(ToolboxInputType.VECTOR_POLYGON)
                .addParameter(new ToolboxParameter("output", "Salida", ToolboxParameterType.OUTPUT_NAME, true, "Nombre de la nueva capa")));
    }

    private static void register(ToolboxAlgorithm algorithm) {
        if (algorithm != null) {
            ALGORITHMS.add(algorithm);
        }
    }

    public static List<ToolboxAlgorithm> getAlgorithms() {
        initializeDefaults();
        return Collections.unmodifiableList(ALGORITHMS);
    }

    public static ToolboxAlgorithm getAlgorithm(String id) {
        initializeDefaults();
        for (ToolboxAlgorithm algorithm : ALGORITHMS) {
            if (algorithm != null && algorithm.getId().equalsIgnoreCase(id)) {
                return algorithm;
            }
        }
        return null;
    }
}
