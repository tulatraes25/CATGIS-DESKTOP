# CATGIS Desktop -- Large Classes Report

Audit date: 2026-06-14 | Line counts from `powershell Measure-Object -Line` on `src/ar/**/*.java`

Method: Line count includes blank lines and comments. Actual SLOC is ~15-20% lower.

---

## Top 30 Largest Files

| # | File | Lines | Primary Responsibility | Why Risky | Proposed Minimal Split |
|---|------|-------|----------------------|-----------|----------------------|
| 1 | `src/ar/com/catgis/MapLayoutComposerDialog.java` | 4139 | Layout composer main dialog -- UI construction, event wiring, export orchestration, template management | God dialog: UI, business logic, export, event handling all in one class. Hard to test. | Extract: `LayoutExportController` (export logic, 400 lines), `LayoutToolBar` (toolbar + actions, 500 lines), `LayoutTemplatePanel` (template management, 300 lines). Already underway with `LayoutController` (748 lines) extracted |
| 2 | `src/ar/com/catgis/MapPanel.java` | 3676 | Main map canvas -- rendering orchestration, tool dispatch, layer management, 12+ collaborators | Massive coupling surface. Mouse event handling, tool dispatch, rendering pipeline all co-located. 3676 lines is 35% over 2000-line aspirational goal | MapTool Strategy pattern already applied (commit b4807d4). Next: extract `MapRenderingPipeline` (orchestration), `MapInteractionHandler` (mouse/key events), `LayerVisibilityManager` (layer toggling) |
| 3 | `src/ar/com/catgis/LayersPanel.java` | 2096 | Layer tree, context menus, drag-drop reorder, visibility toggling, symbology quick-edit | Swing tree + data model + context menu builder + drag-drop all in one. Adding a new context menu action touches this file | Extract: `LayerTreeModel` (tree data model, 300 lines), `LayerContextMenuBuilder` (right-click menus, 400 lines), `LayerDragDropHandler` (DnD, 200 lines) |
| 4 | `src/ar/com/catgis/TerrainHydrologyAnalysisService.java` | 1980 | Hydrology processing -- hillshade, flow accumulation, watershed, stream network, contour generation | Multiple loosely-related algorithms in one service. Adding a new hydrology tool means touching this monolith | Split by algorithm: `HillshadeService`, `FlowAccumulationService`, `WatershedService`, `StreamNetworkService`, `ContourService` |
| 5 | `src/ar/com/catgis/analysis/vector/GeoprocessingAssistantDialog.java` | 1715 | Geoprocessing wizard UI -- buffer, clip, intersect, union, difference, dissolve, nearest neighbor | UI for 7 different operations in one class. Adding a new operation requires modifying this file | Split by operation: Extract `BufferPanel`, `ClipPanel`, `OverlayPanel` (intersect/union/difference), `DissolvePanel`, `NearestNeighborPanel` as composable JPanel components |
| 6 | `src/ar/com/catgis/MapEditingEngine.java` | 1687 | Vector editing coordination -- vertex operations, feature CRUD, undo/redo, geometry validation | Central editing coordinator that dispatches to concrete operations. High coupling to MapPanel state | Already partially refactored. Next: extract `EditSession` (transaction state), `VertexSnappingEngine` (tolerance + snap logic) |
| 7 | `src/ar/com/catgis/ExportVectorLayerAction.java` | 1674 | Export to DXF, KML, GPX, GeoJSON, CSV, Shapefile, etc. | Every export format lives in one action class. Adding a new format means touching this 1674-line file | Extract per-format: `DxfExportAction`, `KmlExportAction`, `GeoJsonExportAction`, `CsvExportAction`. Use ExportFormat interface to dispatch |
| 8 | `src/ar/com/catgis/LayoutPreviewPanel.java` | 1656 | Layout preview canvas -- rendering, mouse interaction, element selection, zoom/pan | Combines rendering and interaction handling. Already extracted from MapLayoutComposerDialog via pragmatic-inner-class-extraction pattern | Extract: `LayoutPreviewRenderer` (paint logic), `LayoutSelectionHandler` (mouse selection/drag) |
| 9 | `src/ar/com/catgis/catmap/Main.java` | 1628 | CATMAP standalone application main class -- UI startup, event wiring, layout model, export | Standalone app's main class is also its controller, model manager, and UI builder | Extract: `CatmapApp` (startup/bootstrap), `CatmapMenuBar` (menu construction), `CatmapLayoutModel` (model management) |
| 10 | `src/ar/com/catgis/ProRasterDerivedService.java` | 1616 | Raster-derived products -- hillshade, slope, aspect, TPI, TRI, roughness | Multiple terrain analysis algorithms in one service | Split: `HillshadeCalculator`, `SlopeCalculator`, `AspectCalculator`, `TerrainIndexCalculator` |
| 11 | `src/ar/com/catgis/AttributeTableWindow.java` | 1562 | Feature attribute table -- table UI, cell editing, sorting, filtering, column management | Swing table with editing, filtering, sorting all in one window class | Extract: `AttributeTableModel`, `AttributeSortFilterProxy`, `AttributeCellEditor` |
| 12 | `src/ar/com/catgis/DrainageExtractionService.java` | 1513 | Drainage network extraction -- flow direction, accumulation, stream delineation | Multiple hydrology sub-algorithms combined | Split: `FlowDirectionService`, `StreamDelineationService`, `BasinExtractionService` |
| 13 | `src/ar/com/catgis/layout/LayoutPageRenderer.java` | 1446 | PDF/PNG/SVG page rendering -- draws all layout elements to graphics context | Handles multiple output formats in one class | Extract: `PdfPageRenderer`, `PngPageRenderer`, `SvgPageRenderer` behind `PageRenderer` interface |
| 14 | `src/ar/com/catgis/LayerPropertiesDialog.java` | 1392 | Layer property editor -- symbology, labels, data source, rendering, metadata | Tabbed dialog that touches many subsystems | Extract per-tab: `SymbologyTab`, `LabelsTab`, `DataSourceTab`, `RenderingTab`, `MetadataTab` as JPanel components |
| 15 | `src/ar/com/catgis/CRSSelectorDialog.java` | 1284 | CRS selection -- search, browse, recent, details panel | Dialog with search + tree + details panel. UI logic for filtering and CRS metadata display | Extract: `CRSSearchPanel`, `CRSTreePanel`, `CRSDetailsPanel` |
| 16 | `src/ar/com/catgis/EditingGeometryOperations.java` | 1064 | Geometry operation primitives -- split, merge, explode, buffer(0), normalize, remove holes | Collection of static geometry utility methods. Pure functions on JTS geometries | Extract into stateless `GeometryOperations` utility class. Already mostly static -- low risk |
| 17 | `src/ar/com/catgis/MapRenderer.java` | 1054 | Map rendering -- draws layers, labels, grid, scale bar to Graphics2D | Rendering pipeline for all layer types in one class | Extract: `VectorLayerRenderer`, `RasterLayerRenderer`, `LabelRenderer`, `GridRenderer` |
| 18 | `src/ar/com/catgis/MainMenuBar.java` | 981 | Main menu bar -- File, Edit, View, Layer, Analysis, Tools, Help menus | Menu construction with localized strings and action wiring | Extract per-menu: `FileMenu`, `EditMenu`, `ViewMenu`, `LayerMenu`, `AnalysisMenu`, `ToolsMenu`, `HelpMenu` |
| 19 | `src/ar/com/catgis/CRSDefinitions.java` | 974 | CRS definitions catalog -- EPSG database, projection descriptions, metric queries | Large static data + lookup logic | Extract: `CRSDefinitionLoader` (data loading), `CRSQueryEngine` (search/lookup) |
| 20 | `src/ar/com/catgis/FloatingVectorEditToolbar.java` | 961 | Floating toolbar for vector editing -- vertex tools, feature tools, snapping, advanced | UI + tool dispatch + state management | Extract: `VertexEditTools` (move/add/remove), `FeatureEditTools` (merge/split), `SnapSettingsPanel` |
| 21 | `src/ar/com/catgis/LoadProjectAction.java` | 888 | Project loading -- XML parsing, feature reconstruction, symbology, validation, layer restoration | Does too much: parsing, validation, reconstruction, error handling | Extract: `ProjectXmlParser`, `FeatureReconstructor`, `LayerRestorer` |
| 22 | `src/ar/com/catgis/TopographicProfileDialog.java` | 870 | Topographic profile dialog -- DEM sampling, chart rendering, export | UI + chart drawing + data processing | Extract: `ProfileChartRenderer`, `ProfileDataSampler` |
| 23 | `src/ar/com/catgis/renderer/labels/LabelExpressionEngine.java` | 828 | Expression engine for labels -- 215+ functions, tokenizer, RPN evaluator | Already focused. 76 TODO stubs are deliberate expansion slots. Well-structured | No split needed now. Address 76 stubs before they become bugs |
| 24 | `src/ar/com/catgis/BooleanRiskService.java` | 817 | Boolean risk analysis -- raster overlay, logical operations, reclassification | Raster algebra with multiple operation types | Extract: `RiskOperation` subclasses per operation type |
| 25 | `src/ar/com/catgis/OpenFileAction.java` | 809 | File open action -- format detection, loader dispatch, layer addition | Does format detection + dispatch + UI feedback | Extract: `FormatDetector`, `LoaderDispatcher` |
| 26 | `src/ar/com/catgis/core/model/Layer.java` | 793 | Layer data model -- properties, style, visibility, CRS, data source | Core domain object that naturally grows. Well-structured | Monitor. If exceeds 1000 lines, split into `VectorLayer` and `RasterLayer` subclasses |
| 27 | `src/ar/com/catgis/TopographyWorkflowSupport.java` | 793 | Topography workflow support -- tool initialization, DEM preprocessing, workflow orchestration | Workflow coordination that touches multiple services | Extract: `WorkflowStep` interface with concrete steps |
| 28 | `src/ar/com/catgis/layout/LayoutController.java` | 748 | Layout controller -- mediates between model and view for CATMAP | Already extracted from MapLayoutComposerDialog. Focused on coordination | Good as-is. Continue extraction from the parent dialog |
| 29 | `src/ar/com/catgis/layout/LayoutTemplateManager.java` | 733 | Layout template management -- create, save, load, apply templates | Template CRUD with file I/O | Good as-is. Focused responsibility |
| 30 | `src/ar/com/catgis/BooleanRiskDialog.java` | 694 | Boolean risk dialog -- UI for risk factor configuration, layer selection, output settings | UI dialog for a specific analysis type | Extract: `RiskFactorPanel`, `RiskOutputPanel` |

---

## Class Size Distribution

| Size Range | Count | % of 480 |
|------------|-------|----------|
| >4000 lines | 1 | 0.2% |
| 3000-3999 | 1 | 0.2% |
| 2000-2999 | 1 | 0.2% |
| 1500-1999 | 8 | 1.7% |
| 1000-1499 | 7 | 1.5% |
| 500-999 | 17 | 3.5% |
| 200-499 | ~80 | ~17% |
| <200 | ~365 | ~76% |

**76% of classes are under 200 lines** -- the distribution is healthy overall. The top 30 classes (6.3% of files) contain the bulk of the complexity.

---

## Extraction Priority (by risk x effort)

| Priority | Class | Lines | Risk | Effort | Extracted Classes |
|----------|-------|-------|------|--------|-------------------|
| P1 | MapLayoutComposerDialog | 4139 | High -- god dialog | Medium (already underway) | LayoutExportController, LayoutToolBar, LayoutTemplatePanel |
| P1 | MapPanel | 3676 | High -- coupling hub | High (12+ collaborators) | MapRenderingPipeline, MapInteractionHandler, LayerVisibilityManager |
| P1 | ExportVectorLayerAction | 1674 | Medium -- export monolith | Low | Per-format export actions (Dxf/Kml/GeoJson/Csv) |
| P2 | GeoprocessingAssistantDialog | 1715 | Medium -- 7 ops in one | Medium | BufferPanel, ClipPanel, OverlayPanel, etc. |
| P2 | LayersPanel | 2096 | Medium | Medium | LayerTreeModel, LayerContextMenuBuilder, LayerDragDropHandler |
| P2 | TerrainHydrologyAnalysisService | 1980 | Medium -- algorithm monolith | Medium | Per-algorithm services |
| P3 | AttributeTableWindow | 1562 | Low | Low | AttributeTableModel, AttributeSortFilterProxy |
| P3 | LayoutPageRenderer | 1446 | Low | Medium | Format-specific renderers |
| P3 | EditingGeometryOperations | 1064 | Low -- already static | Low | Rename/restructure, already good candidate for stateless utility |

---

## Progress Since Prior Audit

The numbers below reflect refactoring completed before this audit (vs. frozen worktree snapshot numbers):

| File | Frozen Worktree | Current | Reduction |
|------|----------------|---------|-----------|
| MapLayoutComposerDialog | 4496 | 4139 | -357 (-7.9%) |
| MapPanel | 4310 | 3676 | -634 (-14.7%) |
| MapEditingEngine | 1925 | 1687 | -238 (-12.4%) |
| LayersPanel | 2313 | 2096 | -217 (-9.4%) |
| LayoutPreviewPanel | 1768 | 1656 | -112 (-6.3%) |
| MapRenderer | 1256 | 1054 | -202 (-16.1%) |

These reductions are real and significant -- the MapTool Strategy pattern extraction from MapPanel (-634 lines), LayoutController extraction from MapLayoutComposerDialog (-357 lines), and MapRenderer factorization (-202 lines) represent meaningful architectural improvements.
