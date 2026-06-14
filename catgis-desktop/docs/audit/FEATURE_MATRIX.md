# CATGIS Desktop -- Feature Matrix

Audit date: 2026-06-14 | Verified against C:\CATGIS\catgis-desktop\src\ar

Legend:
- **State**: estable, beta, experimental, placeholder
- **UI**: has user interface
- **Tests**: number of automated tests, "sintetico" = synthetic data, "real" = real-world dataset
- **Real Data**: verified against real datasets
- **Risks**: bajo, medio, alto, CRITICO
- **Manual Test**: requires manual testing

---

## Vector Data Formats

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| Shapefile read/write | estable | MapPanel, VectorLayerUtils, ShapefileData | si | 10+ tests | si, probado con datasets del IGN | bajo | Abrir SHP, editar atributos, guardar, reabrir |
| GeoPackage read | estable | SpatiaLiteLoader (geopkg driver) | si | 0 tests especificos | no verificado | medio | Abrir .gpkg con capas vectoriales |
| PostGIS read/write | estable | PostgisConnectionStore, PostgisWriteService, PostgisDataSourceAction, PostgisBrowserDialog | si | 2 (PostgisConnectionPresetTest, PostgisWriteServiceTest) | no, requiere servidor externo | medio | Conectar a PostgreSQL, cargar capa, editar, guardar |
| FlatGeobuf read | estable | FlatGeobufLoader (validateFile + magic check + Log4j) | si | 10 tests con datos sinteticos | si, .fgb de Natural Earth | bajo | Abrir .fgb, verificar atributos |
| SpatiaLite read | estable | SpatiaLiteLoader (spatialite driver) | si | 0 tests especificos | no verificado | medio | Abrir .sqlite con extension espacial |
| DXF import/export | beta | DxfExportEngine, CadEngine | si | 0 tests | parcial | medio | Importar DXF de AutoCAD, exportar capa a DXF |
| DWG import | experimental | DwgImportSupport (ODA Teigha externo) | si | 0 tests | parcial | ALTO (dependencia externa Windows-only) | Importar .dwg con ODA instalado |
| KML export | estable | KmlExportEngine | si | 0 tests | si | bajo | Exportar capa a KML, abrir en Google Earth |
| GPX export | estable | KmlExportEngine (mismo engine) | si | 0 tests | si | bajo | Exportar capa de puntos a GPX |
| CSV point import | estable | CsvDataSourceDialog, TablePointImportDialog | si | 0 tests | si | bajo | Importar CSV con columnas X,Y |
| New vector layer | estable | NewVectorLayerDialog, NewVectorLayerAction | si | 0 tests | N/A | bajo | Crear capa Point/Line/Polygon nueva |

## Raster Data Formats

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| GeoTIFF display | estable | RasterImageLoader, LocalRasterData, MapPanel | si | 3+ tests | si, DEMs IGN, Landsat, Sentinel-2 | bajo | Abrir .tif grande, probar modos preview/virtual/real |
| DEM hillshade | estable | TerrainHydrologyAnalysisService | si | 0 tests especificos | si, DEM de 30m | bajo | Generar hillshade con azimuth=315, altitud=45 |
| Spectral indices (NDVI, EVI, SAVI, BSI, NDWI, etc.) | estable | SpectralIndexDialog, computeIndex() | si | 0 tests | si, Sentinel-2 L2A | bajo | Calcular NDVI con bandas 4 y 8 |
| Raster calculator | estable | RasterCalculatorEngine, RasterCalculatorDialog, LabelExpressionEngine (215+ funciones) | si | 0 tests | si | bajo | Evaluar "(A+B)/2" con dos rasters |
| Raster colorizer | estable | RasterColorizer | si | 0 tests | si | bajo | Aplicar rampa de color a DEM monocanal |
| NoData gap filling | beta | RasterNoDataGapFilling | no | 0 tests | si, DEM con vacios | medio | Rellenar vacios en DEM SRTM |
| NetCDF read | beta | NetCdfLoader, ClimateOnlineDownloadDialog | si | 0 tests | si, ERA5, GCM datasets | medio | Abrir .nc con variables climaticas |
| GRIB read | experimental | GribLoader (placeholder, return null en varios paths) | si | 0 tests | no | ALTO | Abrir .grib de GFS |
| LAS/LiDAR read | experimental | LasReader (coordinate scaling, point format 2/3 con RGB) | no | 0 tests | parcial, .las de prueba | medio | Cargar .las y visualizar nube de puntos |

## Web Services

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| WMS client | beta | AddWmsDialog | si | 0 tests | parcial, servicios IGN/GeoServicios | medio | Conectar a WMS publico, GetMap, GetFeatureInfo |
| WFS client | beta | AddWfsDialog | si | 0 tests | parcial | medio | Conectar a WFS, cargar features |
| WFS transactions | experimental | WfsTransactionDialog | si | 0 tests | no | ALTO | Insertar/actualizar feature via WFS-T |
| WCS client | alpha | WcsDialog | si | 0 tests | no | ALTO | Conectar a WCS, descargar cobertura |
| STAC catalog | alpha | StacDialog | si | 0 tests | no | ALTO | Buscar imagenes en STAC API |
| Online base maps | estable | OnlineBaseMapAction | si | 0 tests | si | bajo | Agregar OpenStreetMap/Google basemap |
| Online DEM download | beta | OnlineDemDownloadDialog | si | 0 tests | si | medio | Descargar DEM de ALOS/COP30 |
| Online soil download | beta | OnlineSoilDownloadDialog, SoilGridsDownloadService | si | 1 (SoilGridsDownloadServiceTest) | si | medio | Descargar SoilGrids para area |
| Climate data download | beta | ClimateOnlineDownloadDialog, ClimateOnlineDownloadService | si | 0 tests | si, ERA5 CDS | medio | Descargar temperatura ERA5 mensual |

## Cartography & Layout (CATMAP)

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| Layout composer | beta | MapLayoutComposerDialog (4139 lineas), catmap/Main (1628) | si | 11 golden tests | si, layouts de prueba | medio | Crear layout A4, agregar mapa/leyenda/escala, exportar |
| PDF export | beta | LayoutPageRenderer, LayoutExportEngine | si | 0 tests especificos | si | medio | Exportar layout a PDF, verificar PDFBox metadata |
| PNG export | beta | LayoutPageRenderer | si | cubierto por golden tests | si | bajo | Exportar layout a PNG 300dpi |
| SVG export | beta | LayoutPageRenderer | si | 0 tests especificos | si | medio | Exportar layout a SVG, abrir en Inkscape |
| Golden image testing | estable | 9 golden PNGs en test resources | N/A | 9 tests | N/A | bajo | Ejecutar test suite de golden images |
| Scale bar element | beta | LayoutPageRenderer, MapLayoutComposerDialog | si | cubierto por golden tests | si | bajo | Configurar segmentos, denominador |
| Legend element | beta | CatmapLegendEditorDialog, CatmapLegendItem | si | cubierto por golden tests | si | medio | Agregar leyenda desde capas del proyecto |
| North arrow | estable | LayoutPageRenderer | si | cubierto por golden tests | si | bajo | Seleccionar estilo de flecha norte |
| SLD import/export | experimental | SldSupport, LayerSldStyleIO (684 lineas) | si | 0 tests | no | ALTO (solo reglas basicas, no full OGC SLD) | Exportar estilo a SLD, importar SLD de QGIS |

## Vector Editing

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| Vertex editing (move/add/remove) | estable | EditingToolsWindow, FloatingVectorEditToolbar, EditingGeometryOperations | si | 0 tests | si | bajo | Mover vertice, agregar en segmento, eliminar |
| Join vertices | estable | EditingGeometryOperations | si | 0 tests | si | bajo | Seleccionar 2 vertices, unir |
| Cut polygon | estable | DrawingTools, DrawingToolManager | si | 0 tests | si | bajo | Dibujar linea de corte, dividir poligono |
| Hole creation | estable | EditingGeometryOperations | si | 0 tests | si | bajo | Crear agujero en poligono existente |
| Adjacent polygon | estable | EditingGeometryOperations | si | 0 tests | si | bajo | Digitalizar poligono adyacente con borde comun |
| Merge features | estable | EditingGeometryOperations | si | 0 tests | si | bajo | Seleccionar 2 poligonos, merge |
| Explode multi-part | estable | EditingGeometryOperations | si | 0 tests | si | bajo | Explode MULTIPOLYGON a POLYGON individuales |
| Attribute table | estable | AttributeTableWindow (1562 lineas) | si | 0 tests | si | bajo | Abrir tabla, editar celda, guardar |
| Field calculator | estable | FieldCalculatorDialog | si | 0 tests | si | bajo | Calcular area, concatenar campos |
| Copy/paste | estable | CopyPasteHandler | si | 0 tests | si | bajo | Copiar feature, pegar en misma/otra capa |
| Undo/redo | estable | MapEditingEngine, LayoutViewContext (snapshots) | si | 0 tests | si | bajo | Editar, deshacer, rehacer |

## Geoprocessing

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| Buffer | estable | GeoprocessingService, GeoprocessingAssistantDialog | si | 0 tests | si | bajo | Buffer de 100m a capa de rios |
| Clip | estable | GeoprocessingService (JTS overlay) | si | 0 tests | si | bajo | Clip de cobertura de suelo por area de estudio |
| Intersect | estable | GeoprocessingService | si | 0 tests | si | bajo | Interseccion de dos capas de poligonos |
| Union | estable | GeoprocessingService | si | 0 tests | si | bajo | Union de dos capas |
| Difference | estable | GeoprocessingService | si | 0 tests | si | bajo | Diferencia entre capas |
| Dissolve | estable | GeoprocessingService | si | 0 tests | si | bajo | Dissolve por atributo de categoria |
| Nearest neighbor | beta | GeoprocessingService | si | 0 tests | si | medio | Calcular distancia al vecino mas cercano |
| Boolean risk | beta | BooleanRiskService, BooleanRiskDialog | si | 2 tests | si | medio | Combinar capas de riesgo con operadores logicos |

## Hydrology & Topography

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| DEM hillshade | estable | TerrainHydrologyAnalysisService | si | 0 tests | si | bajo | Ver arriba |
| Contour generation | beta | ContourGenerationService, ContourGenerationDialog | si | 1 (ContourThresholdControlTest) | si, DEM IGN | medio | Generar curvas cada 10m |
| Flow accumulation | beta | DrainageExtractionService (1513 lineas), TerrainHydrologyAnalysisService (1980) | si | 0 tests | si, DEM de cuenca | medio | Calcular acumulacion de flujo |
| Watershed delineation | beta | BasinFromOutletDialog | si | 1 (BatchPourPointResultTest) | si | medio | Delimitar cuenca desde punto de salida |
| Viewshed | beta | ViewshedRayCasting | no | 0 tests | si | medio | Calcular cuenca visual desde punto |
| Geomorphons | beta | WhiteboxTools via ExternalToolService | si | 0 tests | si, DEM de 30m | medio | Clasificar formas de terreno |
| Hydrologic conditioning | beta | WhiteboxTools via ExternalToolService (fill, breach) | no | 0 tests | si | medio | Rellenar sumideros en DEM |
| Topographic profile | beta | TopographicProfileDialog, TopographicProfileTool | si | 0 tests | si | medio | Trazar perfil sobre DEM |
| DEM clip | estable | DemClipDialog, DemClipService | si | 1 (ClippedDemRoundTripAlignmentTest) | si | bajo | Recortar DEM por poligono |

## Project & Application Infrastructure

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| Project save/load (.catgis XML) | beta | CatmapSerializer, SaveProjectAction, LoadProjectAction (888 lineas) | si | 5 release tests | si | medio (CatmapSerializer tiene 2 silent parse failures) | Guardar proyecto con capas+simbolos, cerrar, reabrir |
| Batch processing | beta | BatchProcessorDialog, BatchProcessor | si | 0 tests | si | medio | Ejecutar batch de geoprocesos |
| CRS selection | estable | CRSSelectorDialog (1284 lineas), CRSDefinitions (974 lineas) | si | 1 (CRSDefinitionsCatalogTest) | si | bajo | Buscar EPSG, asignar CRS a capa |
| Coordinate conversion | beta | CoordinateConverterDialog, CoordinateTransformSupport | si | 0 tests | si | medio | Convertir coordenadas entre sistemas |
| Notification system | estable | NotificationManager (Toast + modal) | si | 0 tests | N/A | bajo | Verificar notificaciones sin bloqueo |
| Scripting console | experimental | AnalysisConsoleDialog (Python via ScriptEngine) | si | 0 tests | N/A | medio | Ejecutar script Python en consola |
| Plugin system | experimental | PluginManager (SPI ServiceLoader + sandbox URLClassLoader) | no | 0 tests | N/A | ALTO (ClassLoader permite cargar clases arbitrarias) | Cargar plugin desde directorio |
| H3 hexagonal grid | beta | H3Service (uber/h3-java) | no | 0 tests | no | medio | Generar grilla H3 para area |
| Field calculator | estable | FieldCalculatorDialog | si | 1 (ReleaseFieldCalculatorTest) | si | bajo | Ver arriba |
| Query builder | estable | QueryBuilderDialog | si | 0 tests | si | bajo | Filtrar features con expresion CQL |
| Symbology (categorized) | estable | CategorizedSymbologyDialog | si | 0 tests | si | bajo | Asignar colores por categoria |
| Symbology (graduated) | estable | GraduatedSymbologyDialog | si | 0 tests | si | bajo | Asignar tamanos por rango |
| Symbology (rule-based) | beta | RuleBasedSymbologyDialog | si | 0 tests | si | medio | Crear reglas con expresiones |
| Proportional symbols | beta | ProportionalSymbolsDialog | si | 0 tests | si | medio | Simbolos proporcionales a atributo |
| Label expression engine | estable | LabelExpressionEngine (828 lineas, 215+ funciones, 76 stubs TODO) | si | 0 tests | si | bajo | Evaluar expresion de etiqueta |
| CAD integration | beta | CadEngine, CadWorkflowSupport, CadGeoreferenceDialog | si | 0 tests | parcial | medio | Georreferenciar DWG importado |
| Topology validation | beta | TopologyValidationDialog | si | 0 tests | si | medio | Validar gaps/slivers en capa |
| Flood scenario | beta | FloodScenarioDialog, FloodScenarioGeoTiffExportTest | si | 1 test | si | medio | Simular inundacion desde DEM |
| Climate analysis | beta | UnifiedAnalysisDialog, ClimateAreaAnalysisDialog, WindRoseDialog | si | 0 tests | si | medio | Analisis climatico multitemporal |
| Help center | estable | HelpCenterDialog | si | 0 tests | N/A | bajo | Abrir ayuda |

## pgRouting

| Feature | State | Main Files | UI | Tests | Real Data | Risks | Manual Test |
|---------|-------|-----------|-----|-------|-----------|-------|-------------|
| pgRouting (shortest path, etc.) | experimental | PgRoutingService (SQL injection risk in edge table name interpolation) | no | 0 tests | no, requiere servidor externo con pgRouting instalado | CRITICO (inyeccion SQL) | NO USAR con datos no confiables sin fix |

---

## Summary

| State | Count |
|-------|-------|
| estable | 36 |
| beta | 27 |
| experimental | 8 |
| alpha | 2 |
| placeholder | 1 |

**Total features documented**: 74

### Risk Distribution

| Risk Level | Features |
|------------|----------|
| CRITICO | 1 (pgRouting SQL injection) |
| ALTO | 5 (DWG import, GRIB read, WFS-T, WCS, STAC, Plugin system, SLD) |
| medio | 22 |
| bajo | 27 |

### Test Coverage Gaps

- **80% of features** lack automated tests with real datasets
- FlatGeobuf has 10 synthetic-data tests (good)
- CATMAP has 11 golden image tests (good)
- PostGIS: 2 tests but require external server
- Shapefile: 10+ tests via release suite
- Project save/load: 5 release round-trip tests
- Most geoprocessing, hydrology, web services, and editing features have **zero automated tests**
