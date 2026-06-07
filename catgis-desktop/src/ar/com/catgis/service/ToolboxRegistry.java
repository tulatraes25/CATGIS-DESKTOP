package ar.com.catgis.service;
import ar.com.catgis.analysis.vector.GeoprocessingAssistantDialog;
import ar.com.catgis.service.ToolboxAlgorithm;

import ar.com.catgis.ToolboxParameterType;
import ar.com.catgis.ToolboxParameter;
import ar.com.catgis.ToolboxOutputType;
import ar.com.catgis.ToolboxInputType;
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

        register(new ToolboxAlgorithm("voronoi","Poligonos de Voronoi","Proximidad","Genera poligonos de Thiessen a partir de puntos.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_VORONOI).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("symdiff","Diferencia simetrica","Superposicion","Calcula la parte exclusiva de cada capa sin las intersecciones.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_SYM_DIFF).addInput(ToolboxInputType.VECTOR_POLYGON).addInput(ToolboxInputType.VECTOR_POLYGON).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("multi_buffer","Buffer multiple","Proximidad","Genera multiples anillos de buffer concentricos.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_MULTI_BUFFER).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("distance","Distancia",ToolboxParameterType.DISTANCE,true,"Distancia entre anillos")).addParameter(new ToolboxParameter("rings","Anillos",ToolboxParameterType.TEXT,false,"Cantidad de anillos (default 3)")).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("smooth","Suavizar","Generalizacion","Suaviza geometrias preservando topologia.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_SMOOTH).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("tolerance","Tolerancia",ToolboxParameterType.DISTANCE,true,"Tolerancia de suavizado")).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("poly_to_line","Poligonos a lineas","Conversion","Extrae los anillos de poligonos como lineas.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_POLY_TO_LINE).addInput(ToolboxInputType.VECTOR_POLYGON).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("line_to_poly","Lineas a poligonos","Conversion","Convierte lineas cerradas en poligonos usando Polygonizer.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_LINE_TO_POLY).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("min_bounding","Geometria envolvente minima","Generalizacion","Calcula el circulo/rectangulo/diametro minimo envolvente.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_MIN_BOUNDING).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("type","Tipo",ToolboxParameterType.TEXT,false,"circle, diameter, rectangle, envelope")).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
        register(new ToolboxAlgorithm("nearest","Vecino mas cercano","Estadisticas","Calcula la distancia minima entre entidades de una capa.",ToolboxOutputType.REPORT,GeoprocessingAssistantDialog.OP_NEAREST).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre del reporte")));
        register(new ToolboxAlgorithm("delaunay","Triangulacion de Delaunay","Proximidad","Genera una red de triangulos a partir de puntos.",ToolboxOutputType.VECTOR_LAYER,GeoprocessingAssistantDialog.OP_DELAUNAY).addInput(ToolboxInputType.VECTOR_ANY).addParameter(new ToolboxParameter("output","Salida",ToolboxParameterType.OUTPUT_NAME,true,"Nombre de la nueva capa")));
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
