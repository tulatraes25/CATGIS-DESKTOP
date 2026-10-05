# 6. Test Coverage by Feature

**Date:** 2026-06-14
**Repository:** `C:\CATGIS\catgis-desktop`
**Method:** File listing via `dir /s /b`, grep searches for class names, and selective file reads for test content.

> Verdict key -- **COVERED**: has tests exercising core behavior. **PARTIAL**: has tests but missing critical paths. **NONE**: zero tests for this feature.

---

## Shapefile (Read + Write)

**Existing tests:**
- `LayoutPipelineTest` -- exercises shapefile data through the layout rendering pipeline
- `GoldenImageTest` (9 tests) -- golden image comparison against shapefile-based map renders
- `ReleaseVectorInteropTest` + `ReleaseVectorInteropTestHelper` -- vector format interop with shapefile layers
- `ReleaseVectorOperationsTest` -- vector editing operations on shapefile data
- `FlatGeobufLoaderRealTest` (10 tests) -- input validation only; no shapefile context
- Edge: editing tests exercise `MapEditingEngine` + `EditingGeometryOperations` on shapefile layers

**What is tested:**
- Layout rendering with shapefile data (golden image comparison)
- Vector interop (read, basic geometry preservation)
- Input validation patterns (null, empty, non-existent files) -- FlatGeobuf only, not shapefile-specific

**What is NOT tested:**
- Shapefile read/write roundtrip (write expected attributes, close, re-open, verify)
- Attribute editing persistence (edit a field value, commit, re-read)
- CRS reprojection in shapefile context (read EPSG:4326, write to EPSG:32720, verify coordinates)
- Large shapefile performance (> 10K features)
- Corrupted .shp/.shx/.dbf handling
- `.cpg` encoding file detection and application

**Data used:** Synthetic polygons generated in test code. No real-world .shp files tracked in the repository.

**Verdict:** PARTIAL

**Recommended missing tests:**
1. `ShapefileRoundTripTest` -- create temp shapefile, write 3 features with attributes, close, reopen, assertEquals on attributes and geometry
2. `ShapefileCrsReprojectionTest` -- load a known-CRS shapefile, export with target CRS, verify reprojected coordinates
3. `ShapefileCorruptionTest` -- truncate .shx, verify clear error message, not silent NPE

---

## GeoPackage

**Existing tests:** NONE. No test file references geopkg or GeoPackage.

**What is tested:** Nothing. SpatiaLiteLoader is the entry point (uses GeoTools geopkg DataStore internally), but no test exercises a real `.gpkg` file or validates GeoPackage-specific behavior.

**Verdict:** NONE

**Recommended missing tests:**
1. `GeoPackageLoaderTest` -- create a minimal .gpkg with one layer, load, verify feature count and geometry type
2. `GeoPackageWriteTest` -- create features, export to .gpkg, verify file size > 0 and valid SQLite header

---

## PostGIS

**Existing tests:**
- `PostgisConnectionPresetTest` -- tests connection preset object (in-memory, no database)
- `PostgisWriteServiceTest` -- tests write service logic

**What is tested:**
- Connection preset serialization/deserialization (in-memory objects)
- Write service method signatures and basic error paths

**What is NOT tested:**
- Actual PostGIS connection (requires external database server)
- Feature read from PostGIS table
- Feature write to PostGIS table
- Connection pooling behavior (HikariCP)
- AES-256-GCM encryption/decryption roundtrip: `PostgisConnectionStore` has crypto methods (`deriveKey`, `obfuscate`, `deobfuscate` with AES/GCM/NoPadding), but **no `PostgisCryptoSupportTest` or similar test file exists** -- confirmed by glob search: zero files matching `**/PostgisCrypto*`
- Password storage and retrieval cycle
- Connection timeout and retry behavior
- Schema listing and table enumeration

**Verdict:** PARTIAL (in-memory tests exist; real DB integration is NONE; crypto has ZERO tests)

**Recommended missing tests:**
1. `PostgisCryptoRoundTripTest` -- obfuscate a password, deobfuscate, assertEquals with original
2. `PostgisCryptoKeyDerivationTest` -- verify deriveKey produces consistent output for same inputs
3. `PostgisCryptoMigrationTest` -- verify legacy XOR-encoded credentials are rejected or migrated
4. Manual: connect to a real PostGIS instance and verify feature display (see MANUAL_TEST_PLAN.md)

---

## pgRouting

**Existing tests:**
- `PgRoutingServiceTest` (5 tests) -- table name validation and availability check

**What is tested:**
- `isValidTableName()` accepts normal names (`edges`, `public.roads`, `schema_01.my_edges`)
- `isValidTableName()` rejects SQL injection (`edges; DROP TABLE users;--`, `edges' OR '1'='1`, etc.)
- `isValidTableName()` rejects special chars (spaces, `@`, empty string)
- `isAvailable()` returns false for invalid connection

**What is NOT tested:**
- Actual pgRouting execution (requires pgrouting extension installed on PostGIS)
- Edge table column validation (`source`, `target`, `cost` columns must exist)
- Shortest path computation result correctness
- Driving distance computation
- Invalid edge table name that passes `isValidTableName()` but doesn't exist in the database
- Timeout handling for long-running queries

**Verdict:** PARTIAL -- SQL injection prevention is tested. Actual routing is not.

**Recommended missing tests:**
1. Manual: connect to PostGIS with pgrouting, load a known road network, compute shortest path between two nodes, verify distance and path geometry

---

## Raster / GeoTIFF

**Existing tests:**
- `RasterCalculatorEngineTest` -- raster calculator expression engine
- `RasterCoverageSupportReprojectionOrientationTest` -- verifies raster orientation/pixel-is-area metadata behavior
- `RasterColorizerTest` -- color ramp and symbology test

**What is tested:**
- Expression engine parsing and computation (NDVI, band math)
- Pixel-is-area and image orientation metadata from GeoTools Coverage
- Color ramp application

**What is NOT tested:**
- Actual GeoTIFF file load (all tests use in-memory or mock data)
- Large raster performance (> 100 MB GeoTIFF)
- Multi-band raster handling (4+ bands)
- NoData value handling in computations
- CRS reprojection of rasters (gdalwarp call path)
- BigTIFF support
- Cloud-optimized GeoTIFF (COG) HTTP range reads
- `RasterImageLoader.java` conversion pipeline (gdal_translate + gdaladdo)

**Verdict:** PARTIAL

**Recommended missing tests:**
1. `GeoTiffLoadTest` -- create a small synthetic GeoTIFF (10x10 pixels, 1 band), load, verify pixel values at known coordinates
2. `RasterNoDataTest` -- GeoTIFF with NoData = -9999, run calculator, verify NoData pixels are excluded
3. `RasterReprojectionServiceTest` -- mock GdalSupport.resolve to return a controlled gdalwarp path, verify command composition

---

## DEM / Hillshade

**Existing tests:**
- `HillshadeGeneratorTest` -- hillshade computation

**What is tested:**
- Basic hillshade algorithm (Horn 1981, 3x3 neighborhood, azimuth/altitude parameters)

**What is NOT tested:**
- Hillshade on real DEM data (all tests use synthetic raster)
- Hillshade from large DEMs (> 5000x5000 pixels)
- Flat terrain edge case (all pixels same elevation = no hillshade variation)
- Hillshade with extreme sun angles (azimuth 0, altitude 90)

**Verdict:** PARTIAL

**Recommended missing tests:**
1. `HillshadeFlatTerrainTest` -- constant elevation DEM, verify output is uniform gray (no variation)
2. `HillshadeExtremeAnglesTest` -- altitude=0 (horizon), verify no division by zero

---

## WMS / WFS

**Existing tests:** NONE. WMS: `AddWmsDialog`, `WmsCapabilitiesService`, `OnlineWmsImageCache` exist as source -- zero tests. WFS: `AddWfsDialog`, `WfsCapabilitiesService`, `WfsTransactionService` exist -- zero tests.

**Verdict:** NONE -- requires external server, but capability XML parsing and URL construction can be unit tested

**Recommended missing tests:**
1. `WmsCapabilitiesXmlParseTest` -- feed a static GetCapabilities XML, verify layer names and CRS list extracted correctly
2. `WmsTileUrlConstructionTest` -- verify BBOX parameter calculation for a given extent and zoom level
3. `WfsDescribeFeatureTypeParseTest` -- feed a static XML response, verify attribute names and types extracted

---

## DXF / DWG

**Existing tests:** NONE. `DxfExportEngine`, `DxfLoader`, `DwgImportSupport` exist as source -- zero tests.

**Verdict:** NONE -- DXF engine is internal and testable; DWG requires external ODA Teigha

**Recommended missing tests:**
1. `DxfExportRoundTripTest` -- export a known JTS geometry to DXF string, verify ENTITIES section contains expected LINE/POLYLINE
2. `DxfLoaderTest` -- feed a minimal DXF string, verify JTS Geometry produced
3. `DwgImportSupportResolutionTest` -- test `resolveDwgReference()` with a mock file structure (sidecar DXF present, absent, converter available, unavailable)

---

## FlatGeobuf

**Existing tests:**
- `FlatGeobufLoaderRealTest` (10 tests)

**What is tested:**
1. Non-existent file throws `UnsupportedFormatException`
2. Non-.fgb file throws
3. Empty .fgb file throws
4. Null file throws
5. String path delegates to File overload
6. Bad magic bytes (0x00...) throws
7. Correct magic "gbfg" but invalid header throws
8. `validateFile()` for null
9. `validateFile()` for non-.fgb extension
10. `validateFile()` for non-existent file

**What is NOT tested:**
- Valid .fgb file roundtrip (load, verify feature count, geometry type, attributes)
- Geometry preservation (Point, LineString, Polygon, Multi*)
- Attribute type handling (string, int, double, date)
- Large .fgb file (> 1000 features)
- Streaming read (not loading entire file into memory)
- CRS extraction from .fgb header

**Data:** All synthetic -- no real .fgb files exist in the repository or test resources.

**Verdict:** PARTIAL -- input validation is thorough; actual data loading is untested

**Recommended missing tests:**
1. `FlatGeobufRoundTripTest` -- generate a .fgb with 10 features, load, verify count + first feature geometry/attributes
2. `FlatGeobufGeometryTypesTest` -- roundtrip Point, LineString, Polygon, MultiPolygon separately
3. `FlatGeobufCrsExtractionTest` -- verify EPSG code extracted from .fgb header

---

## SpatiaLite

**Existing tests:** NONE. Zero test files matching `**/SpatiaLite*`.

**What is tested:** Nothing. `SpatiaLiteLoader` has `validateFile()` that checks SQLite header magic, but no roundtrip test.

**Verdict:** NONE

**Recommended missing tests:**
1. `SpatiaLiteLoaderTest` -- create a minimal .sqlite with one spatial table, load, verify feature count
2. `SpatiaLiteValidateFileTest` -- verify non-SQLite file is rejected, SQLite file without spatial tables is rejected

---

## LAS / LiDAR

**Existing tests:** NONE. Zero test files for LAS/LiDAR.

**Verdict:** NONE

**Recommended missing tests:**
1. `LasCoordinateScalingTest` -- known scale/offset, verify world coordinate calculation from raw int32
2. `LasHeaderParsingTest` -- parse a minimal LAS 1.2 header, verify point count and scale factors
3. `LasPointFormatTest` -- verify format 2 (RGB) and format 3 (GPS time) parsing

---

## KML / GPX

**Existing tests:** NONE. Export engines exist (`KmlExportEngine`). Zero tests.

**Verdict:** NONE

**Recommended missing tests:**
1. `KmlExportTest` -- export a known Point geometry, verify `<Placemark>` and `<Point>` elements in output XML
2. `GpxExportTest` -- export a known track (LineString), verify `<trkpt>` elements with lat/lon

---

## CSV

**Existing tests:** NONE. `CsvDataSourceDialog`, `CsvTableReader` exist -- zero tests.

**Verdict:** NONE

**Recommended missing tests:**
1. `CsvPointImportTest` -- CSV with lat/lon columns, verify point layer created with correct coordinate order
2. `CsvEncodingTest` -- CSV in UTF-8 with BOM and Latin1, verify correct column detection

---

## H3

**Existing tests:** NONE. `HexagonalGridTest` exists but tests a custom grid implementation, not uber/h3 library integration.

**Verdict:** NONE

**Recommended missing tests:**
1. `H3IndexRoundTripTest` -- lat/lon to H3 index, verify cell boundary polygon, verify resolution
2. `H3KRingTest` -- compute k-ring of neighbors, verify count matches expected for resolution

---

## SLD

**Existing tests:**
- `SldSupportTest` -- SLD import/export

**What is tested:** Basic SLD export/import roundtrip

**What is NOT tested:**
- Complex styling rules (multiple rules per layer, scale-dependent rules)
- External graphic symbols
- TextSymbolizer with LabelPlacement
- SLD validation against OGC schema

**Verdict:** PARTIAL

---

## CATMAP / Layout

**Existing tests:**
- `layout/` package: 31 test files covering Cartouche, EdgeCases, Elements, ExportSettings, Expression, FeatureTests, Frame, ImageAndGuideLine, Integration, Label, Legend, LegendReorder, Map, Model, ModelAdvanced, MultiMap, MultiPage, NorthArrowAndTable, Pipeline, Render, RenderContext, ScaleBar, ShapeTests, Stress, TableData, TemplateManager, Utils, QgisQptImporter, QptImporterAdvanced, Atlas, Completion
- `catmap/` package: `GoldenImageTest` (9 tests), `CatmapVisualTest` (2 tests), `PdfExportOptionsTest`
- `LayoutPipelineTest` -- end-to-end pipeline

**What is tested:**
- Label rendering, shape rendering, scalebar, north arrow, legend, cartouche
- Layout pipeline (serialize, render, export)
- Map preview and export to image
- Golden image comparison (pixel-level regression)
- Template management (save, load, apply)
- Multi-page and atlas generation
- QGIS QPT import
- Stress testing (rapid operations)
- PDF export options

**What is NOT tested:**
- `LayoutImage` element rendering
- `LayoutTable` element rendering with dynamic data
- Grid/graticule rendering (`LayoutGraticule`)
- PDF output comparison against reference PDF (only pixel golden images)
- Template application with real project data
- `LayoutAtlas` with > 10 pages
- Performance: layout with 20+ map frames

**Verdict:** COVERED -- strongest test suite in the project, 31+ test classes

**Recommended missing tests:**
1. `LayoutImageGoldenTest` -- golden image test with a LayoutImage element
2. `LayoutTableRenderTest` -- table element with known data, verify cell content pixel positions
3. `LayoutGraticuleRenderTest` -- golden image with grid lines at known intervals

---

## Plugins

**Existing tests:** NONE. `PluginManager`, `CatgisPlugin` interface, SPI-based discovery exist in source -- zero tests.

**Verdict:** NONE

**Recommended missing tests:**
1. `PluginManagerDiscoveryTest` -- create a temp JAR with META-INF/services, verify plugin is discovered
2. `PluginSandboxClassLoaderTest` -- load a plugin class, verify it cannot access internal packages
3. `PluginHotReloadTest` -- register plugin, modify manifest, verify reload picks up changes

---

## Scripting

**Existing tests:**
- `ScriptEngineTest` (3 tests) -- missing file, inline code execution, Python script execution (conditional on Python being installed)

**What is tested:**
- `executeScript()` with non-existent file returns failure
- `executeCode("print('hello')")` produces expected output
- `executeScript()` with a temp .py file (only runs if Python is on PATH)

**What is NOT tested:**
- Timeout enforcement (script that runs > 30 seconds)
- stdin piping (passing input to script)
- Virtual environment detection
- CATGIS context injection (environment variables)
- Script that produces an error (non-zero exit code)
- `ScriptConsoleDialog` UI rendering

**Verdict:** PARTIAL

**Recommended missing tests:**
1. `ScriptEngineTimeoutTest` -- script with `time.sleep(60)`, verify timeout kills process within 35s
2. `ScriptEngineStdinTest` -- script that reads stdin, pipe "hello", verify output
3. `ScriptEngineErrorTest` -- script that calls `sys.exit(1)`, verify `result.success()` is false

---

## Proyectos (.catgis save/load)

**Existing tests:**
- `ProjectSerializationTest` -- project file serialization
- `ReleaseProjectRoundTripTest` -- project roundtrip with release testing
- `ProjectOperationalCrsRoundTripTest` -- CRS roundtrip
- `ProjectOperationalCrs32719AlignmentTest` -- specific CRS alignment test
- `ProjectBooleanRiskRoundTripTest` -- boolean risk layer roundtrip
- `ProjectContourOperationalCrsRoundTripTest` -- contour CRS roundtrip
- `ProjectFloodOperationalCrsRoundTripTest` -- flood scenario CRS roundtrip

**What is tested:**
- Project save/load cycle with various layer types
- CRS preservation in project files
- Boolean risk layer roundtrip
- Contour and flood scenario operational CRS

**What is NOT tested:**
- `CatmapSerializer` (the .catgis format specifically) -- the serializer is in `src/ar/com/catgis/catmap/CatmapSerializer.java`, 0 tests reference it
- Silent parse failures (malformed project file produces unclear error)
- Large project file (50+ layers) performance
- Project file backward compatibility (v1 format loaded by current code)

**Verdict:** COVERED for data model roundtrip; PARTIAL for .catgis format specifically

**Recommended missing tests:**
1. `CatmapSerializerRoundTripTest` -- serialize a minimal project, deserialize, verify all layers restored
2. `CatmapSerializerCorruptionTest` -- truncate project file at various points, verify clear error message, not silent NPE

---

## Edicion vectorial

**Existing tests:**
- Editing is exercised indirectly through layout and vector operation tests
- `EditingGeometryOperations` methods are pure JTS functions used by `MapEditingEngine`

**What is tested:** Pure geometry operations (buffer, split, merge) via JTS. No isolated unit tests for the editing pipeline.

**What is NOT tested:**
- Undo/redo stack (`UndoRedoManager`)
- Editing with snapping enabled (`SnapManager`, `SnapContext` has tests)
- Commit + rollback of edit transactions
- Concurrent editing detection
- Attribute editing while geometry is being modified

**Verdict:** PARTIAL -- geometry ops are covered via JTS; editing workflow is untested

**Recommended missing tests:**
1. `MapEditingEngineUndoTest` -- create point, undo, verify point removed, redo, verify point restored
2. `EditingGeometryOperationsVertexTest` -- add vertex to existing LineString, verify new coordinate count

---

## Geoprocesamiento

**Existing tests:**
- `GeoprocessingService` and `GeoprocessingAssistantDialog` exist in source
- `ReleaseVectorOperationsTest` likely covers some operations

**What is tested:** Basic vector operations via release testing

**What is NOT tested:** Isolated unit tests for buffer, clip, intersect, union, difference, dissolve with known geometries and expected results

**Verdict:** PARTIAL

**Recommended missing tests:**
1. `GeoprocessingBufferTest` -- point geometry, buffer(100), verify resulting circle radius
2. `GeoprocessingClipTest` -- polygon clipped by smaller polygon, verify result geometry is subset
3. `GeoprocessingDissolveTest` -- two adjacent polygons, dissolve, verify one output polygon

---

## Hidrologia

**Existing tests:**
- `TerrainHydrologyAnalysisServiceViewshedTest` -- viewshed from DEM
- `FloodScenarioComparatorBatchTest` -- flood scenario comparison
- `FloodScenarioGeoTiffExportTest` -- flood GeoTIFF export
- `BatchPourPointResultTest` -- pour point batch results
- `DemClipServiceTest` -- DEM clipping
- `ClippedDemRoundTripAlignmentTest` -- clipped DEM alignment
- `ContourThresholdControlTest` -- contour threshold

**What is tested:**
- Viewshed computation
- Flood scenario operations
- DEM clipping
- Contour generation parameters
- Pour point batch processing

**What is NOT tested:**
- WhiteboxTools integration (requires external WhiteboxTools binary -- `ExternalToolServiceTest` only tests CLI wrapper, not actual tool execution)
- Flow accumulation / watershed delineation
- Stream network extraction
- Hydrologic conditioning (fill sinks, breach depressions)

**Verdict:** PARTIAL -- computational hydrology is tested; WhiteboxTools external integration is untested

**Recommended missing tests:**
1. `WhiteboxFillSinksTest` -- mock WhiteboxTools path, verify command composition for FillDepressions
2. `WatershedDelineationTest` -- synthetic DEM with known pour point, verify watershed polygon

---

## Summary Table

| Feature | Tests Exist | Coverage Level | Biggest Gap |
|---------|-------------|----------------|-------------|
| Shapefile | Multiple | PARTIAL | No roundtrip test |
| GeoPackage | 0 | NONE | Everything |
| PostGIS | 2 (in-memory) | PARTIAL | No crypto tests, no real DB |
| pgRouting | 5 | PARTIAL | No real routing test |
| Raster/GeoTIFF | 3 | PARTIAL | No real file load test |
| DEM/Hillshade | 1 | PARTIAL | No flat terrain edge case |
| WMS/WFS | 0 | NONE | No capability parse test |
| DXF/DWG | 0 | NONE | DXF engine is testable |
| FlatGeobuf | 10 | PARTIAL | No valid .fgb roundtrip |
| SpatiaLite | 0 | NONE | Everything |
| LAS/LiDAR | 0 | NONE | Everything |
| KML/GPX | 0 | NONE | Export is testable |
| CSV | 0 | NONE | Import is testable |
| H3 | 0 | NONE | Library integration |
| SLD | 1 | PARTIAL | Complex styling |
| CATMAP/Layout | 31+ | COVERED | LayoutImage, Table, Grid |
| Plugins | 0 | NONE | SPI discovery, sandbox |
| Scripting | 3 | PARTIAL | Timeout, stdin, errors |
| Proyectos | 7 | COVERED (data) / PARTIAL (format) | .catgis serializer |
| Edicion vectorial | Indirect | PARTIAL | Undo/redo workflow |
| Geoprocesamiento | Indirect | PARTIAL | Isolated geometry ops |
| Hidrologia | 7 | PARTIAL | WhiteboxTools integration |

**Overall:** 3 fully covered, 11 partially covered, 8 with no tests. Strongest area: CATMAP/Layout. Weakest area: GIS format import/export (SpatiaLite, GeoPackage, DXF/DWG, LAS, KML/GPX, CSV all zero).
