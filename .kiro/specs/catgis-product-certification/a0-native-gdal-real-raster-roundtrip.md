# CATGIS — SPEC/Kiro native GDAL real raster integration across Unix architectures

Status: CANDIDATE / NOT_CERTIFIED
Exact source base: protected main `ef40a3d0b751b7beba8c06e7dd25059692f28db4`
Branch: `quality/a0-real-native-gdal-cli-macos-linux`
Related: #64 GDAL external executables; A0.7 platform runtime OPEN.

## Evidence/problem

PR #65 resolved only *finding* GDAL's `gdal_translate`, `gdalwarp`
and `gdalinfo` binaries on Windows/Linux/macOS. Simulated executable
resolution tests do not establish whether real installed native GDAL can
decode elevation data, produce a georeferenced GeoTIFF, export scientific
pixel values, or reproject CRS on Apple Silicon, Intel Macs and Linux.

## Requirements

1. Add a physically executable, local-only GDAL integration test tagged
   `native-gdal`. Provision GDAL on each GitHub OS with apt/brew, record
   version and explicit absolute `CATGIS_GDAL_BIN`; do not add system binaries
   to the repository or generated application distribution.
2. Create a 3x2 synthetic Arc/Info ASCII grid with known elevation cells
   1, 2, 3, 4, 5, 6 and EPSG:4326 geospatial extent. Execute the **actual**
   GDAL binaries through CATGIS's `GdalSupport.resolve` and Java
   `ProcessBuilder(List<String>)`. Enforce finite command timeout.
3. Convert AAIGrid -> GeoTIFF, verify TIFF magic, raster size (3x2),
   WGS84 georeference; convert GeoTIFF -> XYZ, compare all six elevation
   cells, ensuring the scientific data survives transcoding.
4. Execute a real `gdalwarp` reprojection EPSG:4326 -> EPSG:3857, verify
   output exists and metadata advertises EPSG:3857. Set PROJ_NETWORK=OFF
   so test never depends on a remote coordinate grid server.
5. Add a Gradle `nativeGdalIntegrationTest` task selecting only
   `@Tag("native-gdal")`. Exclude that tag from the standard `test`
   task so it doesn't consume its <=15 reviewed skip budget or require
   GDAL installation on normal Java CI, Windows/headless desktops or users.
6. Add a new Certification matrix with three **separately required** jobs:
   Linux Ubuntu, macOS Intel x86_64 and macOS Apple Silicon arm64. Each
   provisions GDAL and executes the tagged test with an explicit opt-in
   environment marker, asserts XML shows *exactly 1 test, 0 failures/errors/
   skips*, and fails hard if binary install or conversion fails.
7. No uploaded GDAL packages, raster images or large Gradle caches; only
   record OS/architecture, version, known-cell result and files SHA
   metadata to the Actions log/summary. Keep dependency fetch bounded.
8. Add all three required native-GDAL job names to the authenticated
   Main Push Attestation gate **conditionally on the exact main SHA's
   workflow configuration**, so candidate PRs can attest previous main
   without claiming its nonexistent future jobs.
9. Existing Windows/Linux full suites and app packages, macOS Intel/ARM
   full suites and .app packages, PostGIS real integration and architecture
   must remain successful on the **same PR HEAD SHA**, then post-main
   attestation must verify exact new merge SHA.

## Scientific & security invariants

- Use synthetically generated no-personal-data rasters only; do not touch
  desktop files, system configuration or an existing geodatabase.
- GDAL executable names are from a strict allowlist and resolved to
  absolute existing executable files; no arbitrary `PATH` discovery
  at runtime, no shell interpolation, no command-string execution.
- Restrict GDAL commands to single argument-list processes and never
  transfer Java heap-backed tile data across external process boundaries.
- Jobs must run real installed GDAL: a silent skip or mocked executable
  is a release-gating failure.

## Explicit remaining limits

- Windows *real* native OSGeo4W pipeline is not provisioned in this tranche:
  only Windows Java executable-resolution contracts and app packaging are
  independently certified. Windows GDAL GUI and JNI remain separate gates.
- Successful native Unix CLI tests do NOT certify libgdalalljni `.so/.dylib`
  loading, all GeoTIFF compression/tile modes, large rasters, exact
  WCS scientific envelope/NoData constraints, native GUI, Mac codesigning/
  notarization, or Linux distribution compatibility.
- GDAL Homebrew/APT network provisioning and packages can change over time;
  capture full GDAL version on each run and fail if dependencies regress.
- A0.7 and #64 must remain OPEN until those remaining gaps are addressed.
