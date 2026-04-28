package ar.com.catgis;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

public final class ModuleRegistry {

    public static final String MODULE_CAD = "cad";
    public static final String MODULE_TOPOLOGY = "topology";
    public static final String MODULE_REPROJECT_EXPORT = "reproject-export";
    public static final String MODULE_CSV = "csv";
    public static final String MODULE_KML = "kml";
    public static final String MODULE_GEOPACKAGE = "geopackage";
    public static final String MODULE_CATSERVER = "catserver";
    public static final String MODULE_WFS = "wfs";
    public static final String MODULE_ONLINE_BASEMAPS = "online-basemaps";
    public static final String MODULE_LAYOUT_PRINT = "layout-print";
    public static final String MODULE_GEOPROCESS = "geoprocess";

    private static final Map<String, CatgisModule> MODULES = new LinkedHashMap<>();
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
    private static final Preferences MODULE_PREFS = Preferences.userNodeForPackage(ModuleRegistry.class).node("modules");
    private static boolean initialized = false;

    private ModuleRegistry() {
    }

    public static synchronized void initializeDefaults() {
        if (initialized) {
            return;
        }
        initialized = true;

        register(createCadModule());
        register(createTopologyModule());
        register(createReprojectExportModule());
        register(createCsvModule());
        register(createKmlModule());
        register(createGeoPackageModule());
        register(createCatserverModule());
        register(createWfsModule());
        register(createOnlineBasemapModule());
        register(createLayoutPrintModule());
        register(createGeoprocessModule());
    }

    public static List<CatgisModule> getModules() {
        return Collections.unmodifiableList(new ArrayList<>(MODULES.values()));
    }

    public static CatgisModule getModule(String id) {
        return MODULES.get(id);
    }

    public static boolean isModuleEnabled(String id) {
        CatgisModule module = getModule(id);
        return module != null && module.isEnabled();
    }

    public static void setModuleEnabled(String id, boolean enabled) {
        CatgisModule module = getModule(id);
        if (module == null) {
            return;
        }
        if (module.isEnabled() == enabled) {
            return;
        }
        module.setEnabled(enabled);
        persistModuleState(module);
        notifyListeners();
    }

    public static void addChangeListener(Runnable listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    private static void register(CatgisModule module) {
        if (module != null) {
            loadPersistedState(module);
            MODULES.put(module.getId(), module);
        }
    }

    private static void loadPersistedState(CatgisModule module) {
        if (module == null || module.getId() == null || module.getId().isBlank()) {
            return;
        }
        module.setEnabled(MODULE_PREFS.getBoolean(module.getId(), module.isEnabled()));
    }

    private static void persistModuleState(CatgisModule module) {
        if (module == null || module.getId() == null || module.getId().isBlank()) {
            return;
        }
        MODULE_PREFS.putBoolean(module.getId(), module.isEnabled());
    }

    private static void notifyListeners() {
        for (Runnable listener : LISTENERS) {
            try {
                listener.run();
            } catch (Exception ignored) {
            }
        }
        CatgisDesktopApp.syncFloatingVectorEditToolbar();
    }

    private static CatgisModule createCadModule() {
        CatgisModule module = new CatgisModule(
                MODULE_CAD,
                "Herramientas CAD",
                ModuleCategory.CAD,
                "Formaliza el bloque de edicion geometrica ya existente como un modulo CAD nativo: snapping, continuacion, rectangulo, vertices, merge y explode.",
                "Herramientas CAD / Herramientas avanzadas",
                true,
                true
        );

        module.addAction(new CatgisModuleAction("cad-snap", "Alternar SNAP", "Activa o desactiva el snapping de vertices y segmentos.",
                AppIcons.selectIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.setSnapEnabled(!CatgisDesktopApp.mapPanel.isSnapEnabled());
                CatgisDesktopApp.syncFloatingVectorEditToolbar();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> true));
        module.addAction(new CatgisModuleAction("cad-continue-line", "Continuar linea", "Continua una linea existente desde uno de sus extremos.",
                AppIcons.lineIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableContinueLineMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureCount() > 0));
        module.addAction(new CatgisModuleAction("cad-rectangle", "Rectangulo", "Dibuja un rectangulo poligonal real.",
                AppIcons.rectangleIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawRectangleMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> true));
        module.addAction(new CatgisModuleAction("cad-circle", "Circulo", "Dibuja un circulo a partir de centro y radio.",
                AppIcons.circleIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawCircleMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> true));
        module.addAction(new CatgisModuleAction("cad-circle-3p", "Circulo por 3 puntos", "Construye un circulo usando tres puntos sobre la circunferencia.",
                AppIcons.circleThreePointsIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.enableDrawCircleThreePointMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> true));
        module.addAction(new CatgisModuleAction("cad-add-vertex", "Agregar vertice", "Inserta vertices sobre la entidad en edicion.",
                AppIcons.addVertexIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateAddVertexMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-remove-vertex", "Quitar vertice", "Elimina vertices de la entidad en edicion.",
                AppIcons.removeVertexIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateRemoveVertexMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-grow-area", "Aumentar superficie", "Expande poligonos por distancia.",
                AppIcons.increaseAreaIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.increaseSelectedPolygonArea();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-shrink-area", "Disminuir superficie", "Contrae poligonos por distancia.",
                AppIcons.decreaseAreaIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.decreaseSelectedPolygonArea();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-extend-line", "Extender linea", "Extiende la linea seleccionada desde uno de sus extremos.",
                AppIcons.extendLineIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateExtendLineMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-shorten-line", "Acortar linea", "Acorta la linea seleccionada desde uno de sus extremos.",
                AppIcons.shortenLineIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateShortenLineMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-parallel", "Paralela", "Genera una linea paralela o desplazamiento lateral a partir de una linea seleccionada.",
                AppIcons.parallelIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activateParallelLineMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-perpendicular", "Perpendicular", "Genera una linea perpendicular a partir de un tramo de referencia.",
                AppIcons.perpendicularIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.activatePerpendicularLineMode();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureRef() != null));
        module.addAction(new CatgisModuleAction("cad-merge", "Unir elementos", "Fusiona entidades seleccionadas cuando es valido.",
                AppIcons.saveIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.mergeSelectedFeatures();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.canMergeSelectedFeatures()));
        module.addAction(new CatgisModuleAction("cad-explode", "Explotar entidades", "Convierte multipartes en entidades separadas.",
                AppIcons.exportIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.explodeSelectedFeatures();
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.canExplodeSelectedFeatures()));
        module.addAction(new CatgisModuleAction("cad-copy-edit-layer", "Copiar seleccion a capa editable", "Duplica la seleccion sobre la capa actualmente editable.",
                AppIcons.attrAssignIcon(), () -> {
            if (CatgisDesktopApp.mapPanel != null) {
                Layer layer = CatgisDesktopApp.mapPanel.getEditingLayerRef();
                if (layer != null) {
                    CatgisDesktopApp.mapPanel.prepareLayerForEditing(layer);
                    CatgisDesktopApp.mapPanel.copySelectedFeaturesToEditingLayer();
                } else {
                    JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), "No hay una capa editable activa.");
                }
            }
        }, ModuleActionPlacement.EDIT_TOOLBAR, () -> CatgisDesktopApp.mapPanel != null && CatgisDesktopApp.mapPanel.getSelectedFeatureCount() > 0));

        return module;
    }

    private static CatgisModule createTopologyModule() {
        CatgisModule module = new CatgisModule(
                MODULE_TOPOLOGY,
                "Comprobaciones de topologia",
                ModuleCategory.VALIDATION,
                "Valida capas vectoriales buscando geometrias invalidas, duplicados, extremos colgantes, superposiciones y huecos simples.",
                "Generar fichero de comprobaciones de topologia",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("topology-open", "Validar capa", "Ejecuta comprobaciones topologicas sobre una capa vectorial.",
                AppIcons.propertiesIcon(), TopologyValidationDialog::open, ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        return module;
    }

    private static CatgisModule createReprojectExportModule() {
        CatgisModule module = new CatgisModule(
                MODULE_REPROJECT_EXPORT,
                "Exportacion reproyectada",
                ModuleCategory.EXPORT,
                "Exporta una nueva capa vectorial reproyectada a un CRS destino en Shapefile, GeoJSON o KML.",
                "Exportar capa reproyectada",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("reproject-export-open", "Exportar capa reproyectada", "Abre el asistente de exportacion reproyectada.",
                AppIcons.exportIcon(), ExportReprojectedLayerDialog::open, ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        return module;
    }

    private static CatgisModule createCsvModule() {
        CatgisModule module = new CatgisModule(
                MODULE_CSV,
                "Tablas externas",
                ModuleCategory.DATA_SOURCE,
                "Carga CSV, XLSX, XLS, ODS o DBF como tabla externa o como capa espacial de puntos con eleccion de X/Y y CRS.",
                "Tablas externas",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("csv-open", "Cargar tabla externa", "Abre una tabla externa y decide si se carga como tabla o como capa espacial.",
                AppIcons.importTableIcon(), CsvDataSourceAction::openCsvDataSource, ModuleActionPlacement.MODULE_MENU, () -> true));
        return module;
    }

    private static CatgisModule createKmlModule() {
        CatgisModule module = new CatgisModule(
                MODULE_KML,
                "Origen de datos KML / KMZ",
                ModuleCategory.DATA_SOURCE,
                "Formaliza la carga KML y KMZ como modulo visible y separado del cargador general.",
                "Origen de datos KML / KMZ",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("kml-open", "Abrir KML / KMZ", "Carga un archivo KML o KMZ como capa vectorial del proyecto.",
                AppIcons.openIcon(), KmlDataSourceAction::openKmlDataSource, ModuleActionPlacement.MODULE_MENU, () -> true));
        return module;
    }

    private static CatgisModule createGeoPackageModule() {
        CatgisModule module = new CatgisModule(
                MODULE_GEOPACKAGE,
                "Origen de datos GeoPackage",
                ModuleCategory.DATA_SOURCE,
                "Carga capas vectoriales internas de un archivo GeoPackage como fuente de datos nativa del proyecto.",
                "GeoPackage / fuentes de datos espaciales locales",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("geopackage-open", "Abrir GeoPackage...", "Inspecciona un archivo GeoPackage y permite cargar una o varias capas internas.",
                AppIcons.openIcon(), GeoPackageDataSourceAction::openGeoPackageDataSource, ModuleActionPlacement.MODULE_MENU, () -> true));
        return module;
    }

    private static CatgisModule createCatserverModule() {
        CatgisModule module = new CatgisModule(
                MODULE_CATSERVER,
                "CATSERVER",
                ModuleCategory.DATA_SOURCE,
                "CATSERVER es la puerta de entrada de CATGIS para conectar, listar y cargar capas desde cualquier servidor PostgreSQL/PostGIS.",
                "Conexion CATSERVER a servidores PostgreSQL/PostGIS",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("catserver-open", "Conectar CATSERVER...", "Abre CATSERVER con host, puerto, base y schema editables para listar capas y agregarlas al proyecto.",
                AppIcons.projectIcon(), PostgisDataSourceAction::openCatserverBrowser, ModuleActionPlacement.MODULE_MENU, () -> true));
        module.addAction(new CatgisModuleAction("catserver-export", "Enviar capa a CATSERVER...", "Toma la capa vectorial seleccionada y la escribe en el servidor PostgreSQL/PostGIS configurado en CATSERVER.",
                AppIcons.exportIcon(), PostgisDataSourceAction::exportSelectedLayerToPostgis, ModuleActionPlacement.MODULE_MENU,
                () -> CatgisDesktopApp.layersPanel != null
                        && CatgisDesktopApp.layersPanel.getSelectedLayer() != null
                        && !(CatgisDesktopApp.layersPanel.getSelectedLayer() instanceof RasterLayer)));
        return module;
    }

    private static CatgisModule createWfsModule() {
        CatgisModule module = new CatgisModule(
                MODULE_WFS,
                "Origen de datos WFS",
                ModuleCategory.DATA_SOURCE,
                "Carga servicios WFS como capas vectoriales reales en modo lectura, separadas de WMS y reutilizando la tabla y el render vectorial existente.",
                "Servicios vectoriales WFS",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("wfs-open", "Agregar WFS...", "Conecta un servicio WFS, lista feature types y carga una capa vectorial remota en modo lectura.",
                AppIcons.tableIcon(), AddWfsAction::openDialog, ModuleActionPlacement.MODULE_MENU, () -> true));
        return module;
    }

    private static CatgisModule createLayoutPrintModule() {
        CatgisModule module = new CatgisModule(
                MODULE_LAYOUT_PRINT,
                "Composicion cartografica",
                ModuleCategory.COMPOSITION,
                "Genera una salida cartografica simple del mapa actual con titulo, cuadro de mapa, escala, norte y exportacion o impresion basica.",
                "Impresion / composicion cartografica",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("layout-open", "Abrir compositor...", "Abre el compositor de mapas para generar una salida cartografica exportable o imprimible.",
                AppIcons.projectIcon(), MapLayoutComposerDialog::open, ModuleActionPlacement.MODULE_MENU, () -> CatgisDesktopApp.mapPanel != null));
        return module;
    }

    private static CatgisModule createGeoprocessModule() {
        CatgisModule module = new CatgisModule(
                MODULE_GEOPROCESS,
                "Geoprocesamiento",
                ModuleCategory.GEOPROCESSING,
                "Bloque unico de geoprocesamiento de CATGIS: asistente, toolbox interno y operaciones vectoriales clave sin duplicar UI.",
                "Asistente para operaciones de geoprocesamiento",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("geoprocess-open", "Abrir asistente", "Abre el asistente de geoprocesamiento basico.",
                AppIcons.attrCalculatorIcon(), GeoprocessingAssistantDialog::open, ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-toolbox", "Abrir toolbox", "Abre el toolbox interno de algoritmos registrados del modulo Geoprocesamiento.",
                AppIcons.toolboxIcon(), ToolboxDialog::open, ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-buffer", "Buffer", "Abre el asistente listo para Buffer.",
                AppIcons.areaIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_BUFFER), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-dissolve", "Dissolve", "Abre el asistente listo para Dissolve.",
                AppIcons.attrRefreshIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_DISSOLVE), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-clip", "Clip", "Abre el asistente listo para Clip.",
                AppIcons.cutIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_CLIP), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-intersection", "Interseccion", "Abre el asistente listo para Interseccion.",
                AppIcons.selectIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_INTERSECTION), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-merge", "Merge", "Abre el asistente listo para Merge.",
                AppIcons.attrAssignIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_MERGE), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-difference", "Diferencia", "Abre el asistente listo para Diferencia.",
                AppIcons.removeIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_DIFFERENCE), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-spatial-join", "Spatial Join", "Abre el asistente listo para Spatial Join.",
                AppIcons.attrCopyIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_SPATIAL_JOIN), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        module.addAction(new CatgisModuleAction("geoprocess-union", "Union geometrica", "Abre el asistente listo para una union poligonal con atributos de ambas capas.",
                AppIcons.propertiesIcon(), () -> GeoprocessingAssistantDialog.openForOperation(GeoprocessingAssistantDialog.OP_UNION), ModuleActionPlacement.MODULE_MENU, () -> !VectorLayerUtils.getVectorLayers().isEmpty()));
        return module;
    }

    private static CatgisModule createOnlineBasemapModule() {
        CatgisModule module = new CatgisModule(
                MODULE_ONLINE_BASEMAPS,
                "Conexiones online",
                ModuleCategory.ONLINE_MAPS,
                "Accesos directos online acotados a OSM, Esri y WFS para flujo estable y simple.",
                "OSM / Esri / WFS",
                false,
                true
        );
        module.addAction(new CatgisModuleAction("online-basemap-osm", "OpenStreetMap", "Agrega o activa OpenStreetMap como mapa base.",
                AppIcons.basemapIcon(), () -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_OSM), ModuleActionPlacement.MODULE_MENU, () -> true));
        module.addAction(new CatgisModuleAction("online-basemap-esri", "Esri World Imagery", "Agrega o activa Esri World Imagery como capa de fondo.",
                AppIcons.imageryIcon(), () -> OnlineBaseMapAction.addBaseMap(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY), ModuleActionPlacement.MODULE_MENU, () -> true));
        module.addAction(new CatgisModuleAction("online-basemap-wfs", "Agregar WFS...", "Abre el dialogo para conectar un servicio WFS via GetCapabilities.",
                AppIcons.tableIcon(), AddWfsAction::openDialog, ModuleActionPlacement.MODULE_MENU, () -> true));
        return module;
    }
}
