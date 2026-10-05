# CATGIS Desktop -- Project Overview

Audit date: 2026-06-14
Source: C:\CATGIS\catgis-desktop (main repository)
Worktree: C:\Users\tulat\emdash\worktrees\CATGIS\emdash\wild-horses-wish-n92gr (frozen snapshot)

---

## What is CATGIS

CATGIS Desktop is a **Java Swing desktop GIS application** built on **GeoTools 34.0**. It is a single-developer project by Claudio Tula, licensed as **proprietary** (no open-source license). The application targets **Windows only** due to OSGeo4W, GDAL, and AutoCAD ODA dependencies.

### Scale (as of 2026-06-14)

| Metric | Count |
|--------|-------|
| Main Java source files | 480 |
| Total source characters | ~5.85 million |
| Estimated lines of Java | ~120,000 |
| Test files (main repo) | 96 |
| Test files (full suite in frozen worktree) | 25 |
| JUnit `@Test` annotations | ~647 (from prior audit) |

---

## Actually Implemented Features

### Vector Data

| Feature | Status | Notes |
|---------|--------|-------|
| Shapefile read/write | **Estable** | Full attribute editing, via GeoTools ShapefileDataStore |
| GeoPackage read | **Estable** | Via GeoTools DataStore (geopkg driver), loaded through SpatiaLiteLoader |
| PostGIS read/write | **Estable** | AES-256-GCM encrypted credentials via PBKDF2 (100k iterations), HikariCP 5.1.0 connection pooling by fingerprint |
| FlatGeobuf read | **Estable** | org.wololo.flatgeobuf, validateFile() with magic byte check, Log4j logging |
| SpatiaLite read | **Estable** | GeoTools DataStore with spatialite + geopkg module drivers |
| CSV point import | **Estable** | CsvDataSourceDialog, column mapping |
| DXF import/export | **Beta** | External DXF engine via DxfExportEngine |
| DWG import | **Experimental** | ODA Teigha external process (Windows-only), DwgImportSupport |
| KML/GPX export | **Estable** | KmlExportEngine |
| New vector layer creation | **Estable** | NewVectorLayerDialog, NewVectorLayerAction |

### Raster Data

| Feature | Status | Notes |
|---------|--------|-------|
| GeoTIFF / raster display | **Estable** | 3 modes: preview, virtual, real. RasterImageLoader |
| DEM hillshade | **Estable** | Horn (1981) algorithm, 3x3 neighborhood, azimuth/altitude lighting |
| Spectral indices | **Estable** | NDVI, EVI, SAVI, BSI, NDWI, etc. via SpectralIndexDialog |
| Raster calculator | **Estable** | 215+ LabelExpressionEngine functions, RasterCalculatorEngine |
| Raster colorizer | **Estable** | Single-band gradient interpolation |
| NoData gap filling | **Beta** | Iterative mean interpolation for DEMs |
| Terrain/hydrology | **Beta** | Flow accumulation, watershed, contour, viewshed, geomorphons (via WhiteboxTools) |
| NetCDF read | **Beta** | NetCdfLoader via GeoTools NetCDF plugin |
| GRIB read | **Experimental** | GribLoader -- placeholder, returns null on many paths |

### Web Services

| Feature | Status | Notes |
|---------|--------|-------|
| WMS client | **Beta** | AddWmsDialog, GetMap + GetFeatureInfo |
| WFS client | **Beta** | AddWfsDialog, basic GetFeature |
| WFS transactions | **Experimental** | Basic insert/update, WfsTransactionDialog |
| WCS client | **Alpha** | WcsDialog, minimal implementation |
| STAC catalog | **Alpha** | StacDialog, basic search |
| Online base maps | **Estable** | OnlineBaseMapAction, XYZ tile sources |
| Online DEM download | **Beta** | OnlineDemDownloadDialog |
| Online soil download | **Beta** | OnlineSoilDownloadDialog |
| Climate data download | **Beta** | ClimateOnlineDownloadDialog, CDS/ERA5 integration |

### Cartography & Layout

| Feature | Status | Notes |
|---------|--------|-------|
| CATMAP layout composer | **Beta** | A4 landscape default, PDF/PNG/SVG export, MapLayoutComposerDialog (4139 lines) |
| Layout export engine | **Beta** | LayoutPageRenderer, LayoutExportEngine |
| Golden image testing | **Estable** | 9 layout goldens, pixel-level comparison with per-channel delta tolerance |
| MapFrame rendering | **Beta** | Real map content in layout frames |
| Scale bar element | **Beta** | Configurable segments, denominator |
| Legend element | **Beta** | CatmapLegendEditorDialog, CatmapLegendItem |
| North arrow | **Estable** | Various styles |
| SLD import/export | **Experimental** | SldSupport, LayerSldStyleIO -- basic rules only, no full OGC SLD spec |

### Vector Editing

| Feature | Status | Notes |
|---------|--------|-------|
| Move/add/remove vertices | **Estable** | EditingToolsWindow, FloatingVectorEditToolbar |
| Join/merge vertices | **Estable** | EditingGeometryOperations |
| Cut polygon | **Estable** | DrawingTools |
| Hole creation | **Estable** | Adjacent polygon editing |
| Merge/explode features | **Estable** | Multi-part geometry operations |
| Attribute editing | **Estable** | AttributeTableWindow, FieldCalculatorDialog |
| Copy/paste | **Estable** | CopyPasteHandler |
| Undo/redo | **Estable** | Snapshot-based via LayoutViewContext |
| Drawing tools | **Estable** | DrawingTools, DrawingToolManager |

### Geoprocessing

| Feature | Status | Notes |
|---------|--------|-------|
| Buffer | **Estable** | GeoprocessingService, GeoprocessingAssistantDialog |
| Clip | **Estable** | Vector clip operation |
| Intersect | **Estable** | JTS overlay operations |
| Union | **Estable** | JTS overlay operations |
| Difference | **Estable** | JTS overlay operations |
| Dissolve | **Estable** | Attribute-based merging |
| Nearest neighbor | **Beta** | Spatial index accelerated |
| Boolean risk analysis | **Beta** | BooleanRiskService, BooleanRiskDialog |

### Hydrology & Topography

| Feature | Status | Notes |
|---------|--------|-------|
| Flow accumulation | **Beta** | DrainageExtractionService, TerrainHydrologyAnalysisService |
| Watershed delineation | **Beta** | BasinFromOutletDialog |
| Contour generation | **Beta** | ContourGenerationService, ContourGenerationDialog |
| Viewshed | **Beta** | Ray-casting from DEM |
| Geomorphons | **Beta** | WhiteboxTools via ExternalToolService |
| Hydrologic conditioning | **Beta** | DEM fill, breach via WhiteboxTools |
| Topographic profiles | **Beta** | TopographicProfileDialog |
| DEM clipping | **Estable** | DemClipDialog |

### Project & Application

| Feature | Status | Notes |
|---------|--------|-------|
| Project save/load | **Beta** | .catgis XML format, CatmapSerializer |
| Batch processing | **Beta** | BatchProcessorDialog, batch templates with JSON save/load |
| CRS management | **Estable** | CRSSelectorDialog, CRSDefinitions (974 lines), CustomCrsDialog |
| Coordinate conversion | **Beta** | CoordinateConverterDialog |
| Notification system | **Estable** | Toast + modal dialogs, ~97% JOptionPane migrated to NotificationManager |
| Scripting console | **Experimental** | Python via javax.script.ScriptEngine, AnalysisConsoleDialog |
| Plugin system | **Experimental** | SPI ServiceLoader, sandbox URLClassLoader, directory-scan hot-reload |
| H3 hexagonal grid indexing | **Beta** | uber/h3-java, H3Service |
| Label expression engine | **Estable** | 215+ functions in LabelExpressionEngine (828 lines) |
| Rule-based symbology | **Beta** | RuleBasedSymbologyDialog |
| Categorized symbology | **Estable** | CategorizedSymbologyDialog |
| Graduated symbology | **Estable** | GraduatedSymbologyDialog |
| Proportional symbols | **Beta** | ProportionalSymbolsDialog |
| Field calculator | **Estable** | FieldCalculatorDialog with expression support |
| Query builder | **Estable** | QueryBuilderDialog |
| CAD integration | **Beta** | CadEngine, CadWorkflowSupport, CadGeoreferenceDialog, CadPlacementDialog |
| Topology validation | **Beta** | TopologyValidationDialog |
| LAS/LiDAR reader | **Experimental** | Coordinate scaling works, point format 2/3 with RGB |
| Flood scenario analysis | **Beta** | FloodScenarioDialog |
| Climate/environmental analysis | **Beta** | UnifiedAnalysisDialog, ClimateAreaAnalysisDialog, WindRoseDialog |
| Help center | **Estable** | HelpCenterDialog |

---

## Incomplete / Experimental Features

| Feature | Status | Risk |
|---------|--------|------|
| pgRouting | Experimental | SQL injection risk in edge table name interpolation |
| Plugin SPI hot-reload | Experimental | Sandbox URLClassLoader allows arbitrary class loading from plugin directory |
| SLD import/export | Experimental | Basic rules only, no full OGC SLD spec |
| WFS transactions | Experimental | Basic insert/update only |
| 3D terrain rendering | Experimental | Placeholder |
| Label placement/collision | Basic | No advanced conflict resolution |
| LAS/LiDAR reader | Experimental | Coordinate scaling works, point format 2/3 with RGB |
| Label expression slots | 76 TODO stubs | Expansion slots, not bugs |
| GRIB loader | Experimental | Placeholder, returns null on many paths |

---

## External Dependencies

| Dependency | Role | Resolution |
|------------|------|------------|
| OSGeo4W (GDAL, WhiteboxTools, gdal2tiles.py) | Raster processing, external tools | GdalSupport.resolve() -- checks specific install paths + CATGIS_OSGEO4W env var, **never PATH fallback** |
| AutoCAD ODA libraries | DWG import | DwgImportSupport -- external process with validated paths |
| PostgreSQL/PostGIS | Optional, PostGIS layers + pgRouting | JDBC 42.7.5 driver |
| Java 17+ | Runtime | Verified in build.gradle |
| GeoTools 34.0 | GIS framework | DataStore, rendering, CRS, symbology |
| JTS | Geometry engine | Overlay operations, spatial predicates |
| PDFBox 3.0.3 | PDF export | Layout export, metadata/watermark support |
| FlatLaf | Swing Look & Feel | Modern UI theme |
| Log4j2 | Logging | CatgisLogger wrapper |
| JUnit 5.10.2 | Testing | Jupiter API |
| uber/h3-java | H3 grid indexing | Hexagonal discrete global grid |
| org.wololo/flatgeobuf | FlatGeobuf I/O | Streaming read, magic byte validation |
| HikariCP 5.1.0 | Connection pooling | PostGIS DataStore pooling by fingerprint |
| WhiteboxTools | Geomorphons, hydrology | External process via ExternalToolService |

---

## Architecture Notes

- **Pattern**: Monolithic Swing application with growing service extraction
- **Package**: `ar.com.catgis` -- flat main package with subpackages for `analysis`, `data`, `layout`, `renderer`, `climate`, `catmap`, `core`, `ui`
- **MapTool Strategy pattern**: Extracted MapTool interface + 3 concrete tools (commit b4807d4), replaced string-based dispatch
- **Notification migration**: ~97% of JOptionPane calls migrated to NotificationManager (Toast + modal)
- **Credentials**: XOR obfuscation replaced with AES-256-GCM + PBKDF2 (100k iterations)
- **Connection pooling**: HikariCP injected into GeoTools PostGIS DataStore, cached by connection fingerprint
- **External processes**: Centralized resolution via GdalSupport.resolve() with absolute paths, never PATH fallback
- **Plugin loader**: SPI ServiceLoader with sandbox URLClassLoader (experimental -- classloader allows arbitrary class loading)

---

## Key Files (Top 15 by Line Count)

| File | Lines | Responsibility |
|------|-------|----------------|
| MapLayoutComposerDialog.java | 4139 | Layout composer main dialog |
| MapPanel.java | 3676 | Main map canvas + 12+ collaborators |
| LayersPanel.java | 2096 | Layer tree and management |
| TerrainHydrologyAnalysisService.java | 1980 | Hydrology processing service |
| GeoprocessingAssistantDialog.java | 1715 | Geoprocessing wizard UI |
| MapEditingEngine.java | 1687 | Vector editing coordination |
| ExportVectorLayerAction.java | 1674 | Export to various formats |
| LayoutPreviewPanel.java | 1656 | Layout preview canvas |
| catmap/Main.java | 1628 | CATMAP standalone main |
| ProRasterDerivedService.java | 1616 | Raster-derived products |
| AttributeTableWindow.java | 1562 | Feature attribute table |
| DrainageExtractionService.java | 1513 | Drainage network extraction |
| LayoutPageRenderer.java | 1446 | PDF/PNG/SVG page rendering |
| LayerPropertiesDialog.java | 1392 | Layer property editor |
| CRSSelectorDialog.java | 1284 | CRS selection and search |
