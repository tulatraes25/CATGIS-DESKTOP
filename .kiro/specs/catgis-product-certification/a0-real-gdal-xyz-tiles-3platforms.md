# CATGIS — SPEC/Kiro GDAL tile generation real scientific validation

Status: CANDIDATE / NOT_CERTIFIED
Base protected main: `b2312a3ab34004a1cca68a068711f49a56115c89` (after #68)
Branch: `quality/a0-gdal2tiles-real-xyz-roundtrip-post68`
Related: #67 Python GDAL 3.13 modernization; #64 platform GDAL capability; A0.7 OPEN.

## Why this tranche exists

PR #68 certified only the GDAL tile ProcessBuilder *argument construction*
and bounded output collection, NOT actual Python GDAL availability,
geographic tile placement or data output. Critically, classic
`osgeo_utils.gdal2tiles` defaults to TMS (south-oriented Y), whereas
CATGIS's XYZ cache expects north-oriented Y. Without explicit `--xyz`,
valid-looking tiles can appear in the wrong hemisphere. Modern
`gdal raster tile` introduced in GDAL 3.11 defaults to XYZ and supports
explicit `--convention xyz`; Mac Intel/ARM native GDAL 3.13.3 already
certified on three-platform pipelines (#66).

## Requirements and experimental design

- Independently run a *real end-to-end* synthetic GeoTIFF -> PNG slippy
  map tile exercise in the existing three native GDAL CI jobs (Linux,
  macOS Intel, macOS ARM), with real system GDAL and no binary fixtures.
- Input: ASCII grid generated under JUnit @TempDir, 16x16 cells on
  EPSG:4326 (lon -90..-58, lat 50..82), converted to Byte GeoTIFF using
  the already-verified native `gdal_translate`.
- Request zoom 2 only. Assert generated PNG images really decode
  to 256x256, nonempty data, and **north-oriented XYZ Y=0..1**
  (not TMS Y=2..3). Validate output file shapes and reject unexpected
  zoom levels. Never upload PNGs or GeoTIFF files to Actions artifacts.
- For the legacy GDAL Python backend, require `--xyz` explicitly in
  the real ProcessBuilder command and its pure command tests, because the
  official classic gdal2tiles defaults to TMS Y orientation. Keep this option
  mandatory for fixed and automatically chosen zoom levels.
- This is explicitly a *discovery* gate: first verify whether
  ScriptEngine-selected Python actually imports `osgeo_utils.gdal2tiles`
  on every native runner; do not trust mere `python --version`.
  If not available on GDAL 3.13 macOS, implement probed/allowlisted
  modern `gdal raster tile` backend with strict ProcessBuilder argv,
  not an unreviewed PyPI module or arbitrary PATH shell execution.
- Terminate subprocesses under bounded collector; preserve user files
  on failure and never loosen JVM memory/EDT limits.
- The existing `GdalNativeCliRoundTripIntegrationTest` remains required.
  Native-GDAL CI count must increase from 1 to 2 and both tests must PASS,
  0 failures/errors/skips on all three provisioned platforms.
- All remaining CI/Certification jobs unchanged and required:
  Windows/Linux full tests & app images, both Mac CPU variants and apps,
  PostGIS real, architecture inventories, source safety and storage.
- Authenticate preceding protected-main push workflows by exact SHA
  before merge, followed by exact new-main push workflows and the new
  native-GDAL 2-test obligation. Keep issue #67 and A0.7 open until
  actual tile formats, NoData and GUI are independently proven.

## Stop conditions

If a platform lacks a versioned tile backend or produces TMS instead
of XYZ, fail the integration test and DO NOT claim production readiness.
If CI shows GDAL's CLI flags differ by version, investigate root cause
and adjust the backend by capability probes, not by globally bypassing
or skipping tests.
