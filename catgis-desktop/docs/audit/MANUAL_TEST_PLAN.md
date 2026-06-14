# 9. Manual Test Plan

**Date:** 2026-06-14
**Purpose:** Step-by-step manual tests for a human tester to validate CATGIS Desktop functionality. Designed for execution on a Windows machine with OSGeo4W installed.

---

## Prerequisites

### Required Software
- **Java 17+** (verify: `java -version`)
- **OSGeo4W** installed at `C:\OSGeo4W` or `C:\OSGeo4W64` (verify: `C:\OSGeo4W\bin\gdalinfo.exe --version`)
- **CATGIS Desktop** built and launchable (verify: `.\probar-catgis.bat` launches the app)

### Optional (for advanced tests)
- **PostgreSQL 14+ with PostGIS 3+** running and accessible
- **Python 3.8+** (for scripting tests)
- **WhiteboxTools** (for hydrology tests, install to `%USERPROFILE%\.catgis\tools\`)
- **ODA Teigha** (for DWG import tests)

### Test Data Preparation
1. Download a known Shapefile: e.g., Argentina provinces from IGN or Natural Earth countries
2. Locate or download a GeoTIFF DEM (e.g., SRTM 30m tile)
3. Locate or download a FlatGeobuf file (or generate one with GDAL: `ogr2ogr -f FlatGeobuf output.fgb input.shp`)

---

## Test Cases

### 1. Application Launch

**Steps:**
1. Close all instances of CATGIS
2. Run `probar-catgis.bat` (or launch the built application)
3. Wait for splash screen to disappear

**Expected:** Application window opens with menu bar, main toolbar, and empty map canvas. No error dialogs.

**If it fails:** Check Java version, check `build.gradle` dependencies, check `CatgisLogger` output.

---

### 2. Load Shapefile

**Steps:**
1. Click `File > Open` or the Open toolbar button
2. Navigate to a known .shp file (e.g., Argentina provinces)
3. Select the .shp file and click Open

**Expected:**
- Layer appears in the Layers panel (left sidebar)
- Features render on the map canvas within 3 seconds
- Layer name matches the file name (or the layer name from the shapefile)

**Verify:**
- Zoom in/out with mouse wheel -- features scale smoothly
- Pan by dragging -- map moves with cursor
- Identify: click on a feature -- popup shows attributes

**If it fails:** Check that the .shp has all companion files (.shx, .dbf, .prj). Check `CatgisLogger` for GeoTools DataStore errors.

---

### 3. Shapefile Attribute Table

**Steps:**
1. Right-click the loaded layer in the Layers panel
2. Select "Open Attribute Table" (or equivalent)
3. Sort by a numeric column (click column header)
4. Select a row -- verify the corresponding feature highlights on the map

**Expected:**
- Table opens in a new window/dialog
- All columns and rows displayed
- Sorting works (ascending/descending toggle)
- Selection syncs between table and map

**If it fails:** Check `AttributeTableWindow.java` for column type handling. Numeric sort vs string sort on numeric columns is a common bug.

---

### 4. Vector Editing

**Steps:**
1. Create a new vector layer: `Layer > New Vector Layer`, choose Polygon, set CRS to EPSG:4326
2. Activate editing mode (pencil icon or `Edit > Toggle Editing`)
3. Draw a polygon by clicking 5+ points on the map, double-click to finish
4. Add a second polygon
5. Select the first polygon, move a vertex (drag vertex handle)
6. Undo the vertex move (`Edit > Undo` or Ctrl+Z)
7. Redo (`Edit > Redo` or Ctrl+Y)
8. Select both polygons, merge them (`Edit > Merge Selected Features`)
9. Delete a feature: select it, press Delete key
10. Undo the deletion
11. Save edits (`Edit > Save Edits`)

**Expected:**
- Polygon draws with fill color
- Vertex dragging works (vertex highlights, geometry updates in real-time)
- Undo reverts the last operation
- Redo re-applies the undone operation
- Merge produces a single feature (multi-polygon if features are non-adjacent)
- Delete removes the feature from display
- Undo restores the deleted feature
- Save commits changes to the layer

**Verify:**
- After save, close the layer and reopen it -- all edits persist
- Vertex count is correct after merge (sum of both polygon vertices)

**If it fails:** Check `MapEditingEngine`, `EditingGeometryOperations`, `UndoRedoManager`. Vertex dragging issues may be in `MouseHandler` or `MapInteractionHandler`.

---

### 5. Save and Reopen Project

**Steps:**
1. With at least 2 layers loaded (shapefile + new vector), click `File > Save Project`
2. Save as `test_manual.catgis`
3. Close CATGIS completely
4. Relaunch CATGIS
5. Click `File > Open Project`
6. Select `test_manual.catgis`

**Expected:**
- All layers restored with their original names
- Map canvas shows the same extent (zoom level and center)
- Layer order preserved (drawing order)
- Symbology preserved (colors, line widths, fill patterns)
- The new vector layer still has its features

**Verify:**
- Open attribute table of each layer -- data is intact
- Zoom to layer extent for each layer -- bounds are correct

**If it fails:** Check `ProjectSerializer` / `ProjectDeserializer` / `CatmapSerializer`. CRS mismatch is a common cause of layers appearing but not rendering (wrong projection).

---

### 6. CATMAP Layout Export to PDF

**Steps:**
1. Click the "Abrir CATMAP" button in the CartographyToolbar (or `View > Layout Composer`)
2. In the layout composer, add a Map element (drag from Map Frame tool)
3. Add a Scale Bar element
4. Add a North Arrow element
5. Add a Legend element
6. Add a Title (Label element) with text "Manual Test Map"
7. Arrange elements on the page
8. Click `Export > PDF`
9. Set output path and click Save

**Expected:**
- PDF file is created and is > 0 bytes
- Open the PDF in a viewer -- all elements render at correct positions
- Map frame shows the features from the main view
- Scale bar shows correct distances
- North arrow points up
- Legend shows layer names and symbols
- Title text is readable

**Verify:**
- Print the PDF or zoom to 200% -- elements are not pixelated
- Measure scale bar against a known distance on the map

**If it fails:** Check `LayoutExportEngine`, `PdfExportOptions`. Missing fonts can cause label rendering failures.

---

### 7. PostGIS Connection (if available)

**Prerequisites:** PostgreSQL + PostGIS running, a database with spatial tables.

**Steps:**
1. Click `Layer > Add PostGIS Layer` (or the PostGIS toolbar button)
2. In the connection dialog:
   - Host: localhost
   - Port: 5432
   - Database: your_db_name
   - User: your_user
   - Password: your_password
3. Click Connect
4. In the table list, select a spatial table
5. Click Add

**Expected:**
- Connection succeeds without error
- List of spatial tables populates
- Layer loads and renders on the map
- Feature count displayed in status bar

**Verify:**
- Zoom to layer extent -- all features visible
- Identify a feature -- attributes displayed correctly
- Close and reopen -- connection preset is saved (credentials encrypted)

**If it fails:** Check PostgreSQL is running (`pg_isready`), PostGIS extension is installed (`SELECT PostGIS_Version()`), pg_hba.conf allows the connection. Check `PostgisConnectionStore` for AES decryption errors.

---

### 8. Load DEM / Generate Hillshade

**Steps:**
1. Click `File > Open` and select a GeoTIFF DEM file
2. Wait for raster to load (may take a few seconds for large DEMs)
3. Open the hillshade dialog: `Analysis > Terrain > Hillshade`
4. Set azimuth=315, altitude=45, leave Z-factor=1
5. Click Generate
6. When complete, the hillshade layer should appear

**Expected:**
- Original DEM renders as grayscale elevation
- Hillshade renders with realistic terrain shading
- Hillshade correctly aligns with DEM (no offset)
- Shadows correspond to NW light source (azimuth 315)

**Verify:**
- Toggle hillshade on/off -- terrain becomes flat
- Change azimuth to 135 (SE light) -- shadows reverse direction
- Compare with known hillshade of same area (QGIS or GRASS)

**If it fails:** Check `HillshadeGenerator.java` for the Horn algorithm implementation. Ensure DEM has valid elevation values (not all NoData).

---

### 9. Geoprocessing: Buffer and Clip

**Steps:**
1. Load a polygon shapefile (e.g., provinces)
2. Select one feature with the Select tool
3. Open geoprocessing: `Analysis > Geoprocessing > Buffer`
4. Set distance to 10000 (10 km, depends on CRS units), click Run
5. Verify a new buffer layer appears (should be larger than the original)
6. Open geoprocessing: `Analysis > Geoprocessing > Clip`
7. Input layer: the buffer layer
8. Clip layer: the original shapefile
9. Click Run

**Expected:**
- Buffer layer renders as a thicker outline around the selected feature
- Buffer distance is visually correct (measure with measurement tool if available)
- Clip result contains only features within the buffer boundary
- No features outside the buffer remain

**Verify:**
- Buffer feature count should match input feature count (1 feature)
- Clip result feature count may be less than or equal to buffer count

**If it fails:** Check `GeoprocessingService.java`. CRS mismatch between layers can cause incorrect buffer distances.

---

### 10. Load FlatGeobuf File

**Prerequisites:** A valid .fgb file (generate with GDAL: `ogr2ogr -f FlatGeobuf output.fgb input.shp`).

**Steps:**
1. `File > Open`, select a .fgb file
2. Wait for load to complete (`CatgisLogger` shows "FlatGeobuf header: geometry=..." in debug output)
3. Note the "FlatGeobuf loaded: file.fgb → N features" log message

**Expected:**
- Layer appears in panel
- Features render on map
- Attribute table shows data
- Status bar or toast notification shows feature count

**Verify:**
- Feature count matches expected count from source
- Geometry type is correct (Point, Line, Polygon)
- CRS is correctly detected from the .fgb header
- `validateFile()` was called before load (visible in debug log)

**If it fails:** Check `FlatGeobufLoader.java` — magic bytes constant is `0x67666267`. The `validateFile()` method checks magic, header size bounds, and header parseability. Invalid files immediately return `ValidationResult.invalid()` with a Spanish error message.

---

### 11. Error Handling: Corrupt Files

**Steps:**
1. Create a text file named `fake.fgb` with content "this is not a flatgeobuf file"
2. Try to open it with `File > Open`
3. Create an empty file named `empty.shp`
4. Try to open it
5. Rename a .txt file to `wrongext.shp`
6. Try to open it
7. **New:** Create a valid SQLite header file (16 bytes "SQLite format 3\0" + padding) with `.spatialite` extension but no spatial tables — try to open it
8. **New:** Create a corrupt `.catgis` layout file with invalid numbers in element positions — open it in CATMAP

**Expected (all cases):**
- Clear error message displayed (toast notification or modal dialog, not a stack trace in the UI)
- Application does not crash or freeze
- No layer added to the panel
- The error message explains what went wrong (e.g., "El archivo no es FlatGeobuf válido (magic incorrecto)", "El archivo no es una base de datos SQLite válida")
- **Case 7:** `SpatiaLiteLoader.validateFile()` returns valid (SQLite header correct) but warns "sin tablas espaciales detectadas"
- **Case 8:** `CatmapSerializer` loads the layout but `CatgisLogger.warn` shows "valor decimal invalido" or "tamanio invalido" for the corrupt fields

**Verify:**
- After each error, the application remains responsive
- Check `CatgisLogger` output for specific warning messages
- No silent fallback to default values without logging
- **Case 7:** The file IS a valid SQLite database, so it should NOT show an error — it should open with a warning about no spatial tables
- **Case 8:** Elements with corrupt positions appear at (0,0) but other valid elements load normally
- Can continue to load valid files after the error

**If it fails:** Check `UnsupportedFormatException` handling in each loader. Silent catch blocks that suppress stack traces are the primary risk.


---

### 11b. Load DXF File (minimal)

**Prerequisites:** A valid .dxf file (minimal fixture included in `DxfRealTest.java` constants).

**Steps:**
1. `File > Open`, select a .dxf file
2. Wait for load

**Expected:**
- Layer appears in panel with geometry features
- At minimum, one LINE entity is rendered
- Import does not crash on minimal valid DXF

**Verify:**
- Feature count ≥ 1
- Geometry type is LineString
- Works with both ASCII DXF (our fixture) and binary DXF (if available)

**If it fails:** Check `DxfLoader.java` — the loader parses DXF text format. Binary DXF files require AutoCAD libraries.

**Pendiente por fixture:** Full DXF with layers, text, blocks, polylines.


---

### 11c. SpatiaLite Validation

**Prerequisites:** None — uses synthetic SQLite headers.

**Steps:**
1. Create a file with valid SQLite header (16 bytes "SQLite format 3\0") but no spatial tables, with `.spatialite` extension
2. Try to open it in CATGIS

**Expected:**
- `SpatiaLiteLoader.validateFile()` returns valid (SQLite header is correct)
- Message: "Base de datos SQLite válida, sin tablas espaciales detectadas"
- No crash, no empty layer added

**Verify:**
- Non-SQLite file (plain text) → `validateFile()` returns invalid with message "no es una base de datos SQLite válida"
- File < 100 bytes → rejected with "demasiado pequeño"
- Null file → "no especificado"

**If it fails:** Check `SpatiaLiteLoader.isSqliteDatabase()` — reads full 16 bytes, checks `startsWith("SQLite format 3")`.


---

### 11d. GRIB Error Logging

**Steps:**
1. Load a GRIB file with unsupported data structure (or a file that triggers the `copyToNDJavaArray` reflection path)
2. Check `CatgisLogger` output

**Expected:**
- Application does not crash
- Raster image is displayed (even if synthetic)
- **Critical:** `CatgisLogger.error` message appears: "GribLoader: fallo al leer datos GRIB via reflection, la imagen mostrada sera sintetica (no representa datos reales)"

**Verify:**
- The log message IS present when GRIB data extraction fails
- The synthetic image is visually distinguishable from real data
- User is not misled into thinking synthetic data is real


---

### 11e. CATMAP Atomic Save

**Prerequisites:** A CATMAP layout with at least 1 element.

**Steps:**
1. Open CATMAP layout composer
2. Add a label element
3. Save layout as `atomic_test.catmap`
4. Save layout again (same file)
5. Check directory for files

**Expected:**
- First save: `atomic_test.catmap` created, no `.tmp` file remaining
- Second save: `atomic_test.catmap.bak` created (backup of previous version)
- No `.tmp` file lingering (it was atomically renamed)

**Verify:**
- If app crashes during save (simulate by killing process during write), `.tmp` file exists and original `.catmap` is intact
- Backup file content matches the version before the second save


---

### 11f. Plugin Security Advisory

**Steps:**
1. Launch CATGIS
2. Check `CatgisLogger` output (or log file)

**Expected:**
- Log message: "PluginManager: los plugins se ejecutan con los mismos permisos que la aplicacion — solo instalar plugins de fuentes confiables."

**Verify:**
- Message appears at startup even if `plugins/` directory is empty
- `CatgisPlugin.java` Javadoc contains the security warning
- No mention of "sandbox" anywhere in PluginManager Javadoc or code comments


---

### 11g. Raster Memory After Project Close

**Steps:**
1. Load a large GeoTIFF (>50 MB if available, or any GeoTIFF)
2. Note memory usage in Task Manager
3. Close project (`File > New Project` or `File > Close Project`)
4. Reload the same GeoTIFF
5. Close project again
6. Repeat steps 3-5 three times
7. Note memory usage each time

**Expected:**
- Memory usage does NOT grow unboundedly with each cycle
- Heap should stabilize after GC (may need to wait 30s after each close)
- No `OutOfMemoryError`

**Verify:**
- `MapPanel.clearAllLayers()` is called on project close → disposes all `LocalRasterData`
- `MapPanel.removeLayer()` disposes individual raster when removed from legend
- If memory grows asymptotically, check `LocalRasterData.dispose()` → `image.flush()` is called

**If it fails:** Check that `clearAllLayers()` iterates `rasterLayers.values()` and calls `data.dispose()` on each before clearing the map.

---

### 12. WMS: Connect to Public Service

**Steps:**
1. Click `Layer > Add WMS Layer`
2. Enter URL: `https://ows.terrestris.de/osm/service` (public OSM WMS)
3. Click Connect
4. In the layer list, select "OSM-WMS"
5. Set image format to `image/png`
6. Click Add

**Expected:**
- Connection succeeds, layer list populates
- OSM tiles render on the map
- Zoom in/out fetches new tiles (near-instant for cached tiles, few seconds for new)

**Verify:**
- Tiles align with vector layers (no offset)
- Panning works smoothly
- No tile gaps or missing tiles at any zoom level

**If it fails:** Check network connectivity. WMS URL may be down. Try alternative: `https://demo.mapserver.org/cgi-bin/wms` or GeoServer demo.

---

### 13. Stress Test: Multiple Layers + Rapid Zoom

**Steps:**
1. Load 5+ shapefiles simultaneously (or load them one by one)
2. Load a DEM/GeoTIFF
3. Load a WMS layer
4. Rapidly zoom in/out using mouse wheel (10+ scrolls in 2 seconds)
5. Rapidly pan across large distances (drag map edge to edge repeatedly)
6. Open attribute table while map is rendering
7. Switch between layers in the legend while rendering

**Expected:**
- UI remains responsive (no multi-second freezes)
- Rendering completes for the final zoom level (does not queue up all intermediate zooms)
- No `OutOfMemoryError`
- No blank/white map canvas
- Attribute table opens within 2 seconds

**Verify:**
- Check memory usage in Task Manager -- should not grow unboundedly
- After rapid zoom, let it settle and verify features render at correct final zoom

**If it fails:** Check `MapRenderer` and `MapRenderingPipeline` for render cancellation on new requests. Check `BufferedImage` disposal in `RasterImageLoader`.

---

### 14. Scripting Console (if Python is installed)

**Steps:**
1. Open scripting console: `View > Script Console` (or equivalent menu)
2. Type: `print("CATGIS manual test")` and execute
3. Type: `import sys; print(sys.version)` and execute
4. Create a temp .py file with content: `print("Hello from file")`
5. Load and execute the file

**Expected:**
- Inline code produces expected output in console
- Python version is 3.x
- File execution works and produces output

**Verify:**
- Console shows both stdout and stderr
- If script has an error, the error message is clear (not a generic "execution failed")

**If it fails:** Check Python installation. Verify `CATGIS_PYTHON_PATH` is not set to a non-existent path. Check `ScriptEngine.resolvePython()`.

---

## Test Completion Checklist

| # | Test | Pass/Fail | Notes |
|---|------|-----------|-------|
| 1 | Application Launch | | |
| 2 | Load Shapefile | | |
| 3 | Attribute Table | | |
| 4 | Vector Editing | | |
| 5 | Save/Reopen Project | | |
| 6 | CATMAP PDF Export | | |
| 7 | PostGIS Connection | | |
| 8 | DEM Hillshade | | |
| 9 | Buffer + Clip | | |
| 10 | FlatGeobuf Load | | |
| 10b | DXF Load | | Pendiente por fixture |
| 10c | SpatiaLite Validation | | Usa header sintético |
| 11 | Error Handling | | |
| 11d | GRIB Error Logging | | Pendiente por fixture GRIB |
| 11e | CATMAP Atomic Save | | |
| 11f | Plugin Security Advisory | | Verificar logs |
| 11g | Raster Memory | | Necesita GeoTIFF >50MB |
| 12 | WMS Connection | | |
| 13 | Stress Test | | |
| 14 | Scripting Console | | |

---

## Known Test Data Sources

- **Natural Earth:** https://www.naturalearthdata.com/downloads/ -- free cultural and physical vector data
- **SRTM DEM:** https://srtm.csi.cgiar.org/ -- 30m global DEM
- **IGN Argentina:** https://www.ign.gob.ar/ -- official Argentina geodata
- **Terrestris OSM WMS:** https://ows.terrestris.de/osm/service -- public OSM WMS
- **GDAL sample data:** included with OSGeo4W at `C:\OSGeo4W\share\gdal\`
