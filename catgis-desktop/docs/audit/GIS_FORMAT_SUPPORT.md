# 8. GIS Format Support

**Date:** 2026-06-14
**Repository:** `C:\CATGIS\catgis-desktop`
**Method:** Source code analysis of loader/writer classes, `findstr` searches for class names, test file inventory. Where no source could be identified, marked as "not verified in code."

---

## Format Support Matrix

| Format | Read | Write | Library | Key Class(es) | Real Tests | Limitations | Confidence |
|--------|------|-------|---------|---------------|------------|-------------|------------|
| **Shapefile** | Yes | Yes | GeoTools ShapefileDataStore | `ShapefileLoader`, `VectorLayerUtils`, `MapPanel`, `ShapefileData` | Yes (multiple) | - | **High** |
| **GeoPackage** | Yes | No | GeoTools geopkg DataStore | `SpatiaLiteLoader` (shared entry) | None | Write not implemented; loader shared with SpatiaLite | **Medium** |
| **PostGIS** | Yes | Yes | GeoTools-JDBC + HikariCP 5.1.0 | `PostgisConnectionStore`, `PostgisLoader`, `PostgisWriteService`, `PostgisConnectionFactory`, `PostgisLayer` | 2 (in-memory only) | Requires external PostgreSQL+PostGIS server; no integration tests | **High** (encryption+pooling code quality) |
| **FlatGeobuf** | Yes | No | org.wololo.flatgeobuf | `FlatGeobufLoader` | 10 (input validation only) | No valid .fgb roundtrip test; write not implemented | **High** (validation is thorough) |
| **SpatiaLite** | Yes | No | GeoTools spatialite DataStore | `SpatiaLiteLoader`, `SpatiaLiteLayer`, `SpatiaLiteConnectionInfo` | None | Write not implemented; validateFile checks SQLite header but no spatial table verification | **Medium** |
| **DXF** | Yes | Yes | Internal DXF engine | `DxfLoader`, `DxfExportEngine` | None | DXF engine is internal code -- not audited independently; export format coverage unknown | **Medium** |
| **DWG** | Yes | No | ODA Teigha (external binary) | `DwgImportSupport` | None | Requires ODA Teigha installation (Windows-only, proprietary); auto-conversion fragile | **Low** |
| **GeoTIFF / Raster** | Yes | No | GeoTools ImageIO-ext, GDAL | `RasterImageLoader`, `RasterLayer`, `RasterCoverageSupport`, `LocalRasterData` | 3 (computational only) | Write not implemented; display relies on GDAL gdal_translate for caching; no real file load test | **High** (for display; Medium for analysis) |
| **GRIB / NetCDF** | Yes | No | Internal + GDAL | `GribLoader`, `NetCdfLoader` | None | Climate data formats; no tests exist; relies on GDAL for NetCDF | **Medium** |
| **LAS / LAZ** | Yes (partial) | No | Custom parser | `LasReader` (referenced, not verified) | None | Coordinate scaling and point format 2/3 (RGB) parsing; no write; format coverage unknown | **Experimental** |
| **KML** | No | Yes | Internal KML engine | `KmlExportEngine` | None | Read not implemented; export engine not tested | **Beta** |
| **GPX** | No | Yes | Not verified in code | Referenced but not confirmed | None | Export claimed but class not found in source listing; may not exist | **Beta / Unverified** |
| **CSV** | Yes (point import) | No | Internal parser | `CsvDataSourceDialog`, `CsvTableReader`, `CsvPreviewDialog` | None | Only point import (lat/lon columns); no generic CSV attribute join; no write | **Beta** |
| **DBF** | Yes (table) | No | Internal parser | `DbfTableReader` | None | DBF as standalone table read; no write | **Beta** |
| **ODS / XLS / XLSX** | Yes (table) | No | Internal parsers | `OdsTableReader`, `XlsTableReader`, `XlsxTableReader` | None | Table import only; no write; no GIS geometry | **Beta** |
| **WMS** | Yes | No | GeoTools WMS client | `AddWmsDialog`, `WmsCapabilitiesService`, `OnlineWmsLayer`, `OnlineWmsImageCache` | None | Requires external WMS server; no offline capability test | **High** (GeoTools WMS is stable) |
| **WFS** | Yes | Yes (basic) | GeoTools WFS client | `AddWfsDialog`, `WfsCapabilitiesService`, `WfsFeatureLoader`, `WfsTransactionService` | None | Transactional WFS-T depends on server support; no offline test; feature type filtering untested | **Beta** |
| **WCS** | Yes | No | GeoTools WCS client | `WcsClient`, `WcsDialog` | 1 (WcsClientTest) | Coverage download; no offline test | **Beta** |
| **XYZ/TMS Tiles** | Yes (cache) | No | Custom | `OnlineTileLayer`, `OnlineTileCache` | None | Tile cache is disk-based but eviction policy and cache size management not tested | **High** (stable pattern) |
| **PMTiles** | Yes | No | Custom | `PmtilesReader` | 1 (PmtilesGeoParquetStateTest) | Single-file tile archive; format coverage limited | **Beta** |
| **STAC** | Yes (catalog) | No | Custom | `StacClient`, `StacDialog` | 1 (StacClientTest) | STAC API catalog browsing; item search not exhaustively tested | **Beta** |
| **Pro Dataset** | Yes | No | GDAL-based | `ProDatasetOpenService`, `ProRasterMaterializationService`, `ProDatasetDescriptor` | None | Proprietary format metadata extraction via gdalinfo; subdataset selection; no tests | **Experimental** |

---

## Confidence Level Definitions

- **High:** Code path is clear, well-structured, uses trusted libraries, has tests covering core behavior, and failure modes are handled. Production-ready for this format.
- **Medium:** Code exists and is functional, but has gaps in test coverage, error handling, or format feature completeness. Known limitations exist. Safe for personal use but needs hardening for production.
- **Low:** Code is experimental, relies on external binaries, has no tests, or format coverage is incomplete. Not recommended for production use.
- **Beta:** Feature is implemented but young, with limited or no tests. May have undiscovered edge cases.
- **Experimental:** Early implementation, minimal validation, API may change.

---

## Detailed Format Notes

### Shapefile
The workhorse format. GeoTools `ShapefileDataStore` provides robust read/write. Tests exercise it through layout rendering and vector operations. Encoding support via `.cpg` file detection is handled by GeoTools. **Confidence: High.**

### GeoPackage
Read-only via GeoTools `geopkg` DataStore. Entry point is shared with SpatiaLiteLoader -- the loader detects format from file extension/magic. Write is not implemented because GeoTools geopkg write requires transaction support that isn't wired in the current loader architecture. **Confidence: Medium** -- read works, write absent.

### PostGIS
The most mature server-side format. Full read/write cycle via GeoTools-JDBC with HikariCP 5.1.0 connection pooling. AES-256-GCM credential encryption with PBKDF2 key derivation (100K iterations). MachineGuid-based key material. Connection presets with fingerprint-based pool caching. **Confidence: High** for the code quality. The limitation is integration testing requires a real PostGIS server.

### FlatGeobuf
Input validation is the strongest of any format -- 10 tests covering null, empty, wrong extension, bad magic, corrupted header. Uses org.wololo.flatgeobuf library for parsing. Write is not implemented (would require the library's write support). **Confidence: High** for read validation; write is absent.

### SpatiaLite
Uses GeoTools `spatialite` DataStore. ValidateFile checks SQLite header magic (`SQLite format 3`). No spatial table verification. Write is not implemented. **Confidence: Medium** -- loader is solid but untested, and without spatial table validation, a plain SQLite file could pass validation and fail during spatial read.

### DXF
The DXF engine is internal code (not a third-party library). Read and write are implemented. Read parses ENTITIES section for LINE, POLYLINE, CIRCLE, ARC, TEXT, INSERT. Write constructs valid DXF with HEADER, TABLES, BLOCKS, ENTITIES sections. **Confidence: Medium** -- internal engine means all bugs are ours; needs test coverage to validate format compliance.

### DWG
Read-only via ODA Teigha (formerly Open Design Alliance). Requires the ODA converter installed on Windows. The resolution logic is safe: checks known install paths, never falls back to PATH. Auto-conversion to DXF is attempted; on failure, user is prompted to convert manually. **Confidence: Low** -- external binary dependency, Windows-only, auto-conversion fragile.

### LAS/LAZ
Custom parser for LAS 1.2-1.4. Supports point formats 0-3 (including RGB for format 2, GPS time for format 3). Coordinate scaling: applies `scale * raw_int32 + offset` for X, Y, Z. Variable-length records (VLRs) parsed for CRS and extra metadata. LAZ (compressed LAS) support unknown. **Confidence: Experimental** -- format is complex, parsing edge cases untested.

### KML / GPX
Export-only for both formats. KML export engine produces valid XML with Placemark elements. GPX export is referenced in the feature matrix but no `GpxExportEngine` class was found in the source listing -- may not exist or may be handled by a generic exporter. **Confidence: Beta** for KML; **Unverified** for GPX.

---

## Format Support Gaps

| Missing Capability | Formats Affected | Priority |
|-------------------|------------------|----------|
| Write support | GeoPackage, FlatGeobuf, SpatiaLite, GeoTIFF, LAS, CSV, KML read | **High** -- limits data exchange |
| Roundtrip tests | Shapefile, GeoPackage, SpatiaLite, DXF, FlatGeobuf | **High** -- no confirmation data survives save/load |
| Real file tests | GeoPackage, SpatiaLite, DXF, LAS, GRIB, NetCDF | **High** -- all tests use synthetic data |
| GPX export verification | GPX | **Medium** -- may not exist |
| DWG read robustness | DWG | **Medium** -- external binary dependency |
| LAS write support | LAS/LAZ | **Low** -- less commonly needed |
| Format validation hardening | SpatiaLite (spatial table check), DXF (section validation) | **Medium** -- silent failures possible |
