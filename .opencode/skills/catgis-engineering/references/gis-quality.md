# GIS scientific validity and release gates

## Evidence levels
UNIT CONTRACT ≠ REAL NATIVE INTEGRATION ≠ PACKAGE STRUCTURE ≠ GUI RUNTIME ≠ PROFESSIONAL ACCEPTANCE.

## GIS checks
- Raster: generated small genuine AAIGrid/GeoTIFF; TIFF metadata AND actual pixels, band count, NoData, envelope, CRS/EPSG axis policy, EPSG:4326/3857 transforms. Reject fake nonempty binary payloads.
- GDAL tiles: classic gdal2tiles defaults to TMS; CATGIS expects XYZ north origin. At zoom z: TMS y = (2^z - 1) - XYZ y. Validate real 256-px PNG, requested z/x/y, nontransparent source pixels and correct geographic hemisphere; probe installed modern gdal raster tile or official osgeo_utils Python utilities.
- WCS: before atomic publication verify format/signature, dimensions, GeoTIFF geokeys, CRS/BBOX, bands/pixel integrity as required. Bound memory; do not full-decode multi-GB files. On fail preserve destination and clear owned staging.
- WMS/WFS: XXE-safe namespace-aware XML, version negotiation, CRS axis order, HTTP and pagination/error paths.
- PostGIS: real isolated PostgreSQL/PostGIS service read/write/schema/pooling; no personal or production data.
- Vector/projects: import/export roundtrip with real tiny fixtures, invalid geometry, edit/undo/redo, safe save-close-reopen and cancellation.

## OS-independent and platform tests
Windows: JDK17 full suite + jpackage contract; separately native OSGeo4W/GDAL/JNI, actual Swing GUI and install E2E.
Linux: suite, image package, installed apt GDAL CLI test; separately GNOME/KDE/GUI and JNI.
Mac Intel and ARM: own native .app/architecture suite + Homebrew GDAL real test; separately GUI, native dylib/JNI, signing/notarization.

## Safety
- Bounded network payload, image decode, concurrent cache memory by bytes, subprocess timeout/log capture.
- Never block EDT with disk/network/long GDAL. ProcessBuilder argv, approved absolute binaries, no command shell.
- Stage GIS outputs next to final on same filesystem, validate and atomically publish with rollback; prevent symlink traversal, partial overwrite and concurrent collision.
- Generate fixtures in CI temp, no binary fixture dumps or GDAL installers in GitHub.

Do not declare A0.7 release-ready until exact-main CI, real OS and GUI, full project workflows (vector/raster/analysis/CATMAP/PNG/PDF) and residual issue gates are observed.
