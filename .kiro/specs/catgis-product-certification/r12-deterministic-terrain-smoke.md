# SPEC/Kiro #178 — deterministic real Terrain Tiles smoke

STATUS: CANDIDATE / NOT_CERTIFIED
BASE_SHA: `2ba764847705ccc40209f359db228b40c094c006`

## Root cause
Runtime Identity PR #177 run 38991724943, macOS Intel JDK21 job
117275435141: one external-network test failed with
HttpConnectTimeoutException. EmergencyPerfSmokeTest called AWS Terrarium
directly, despite being part of the mandatory native OS/JDK test matrix.

## Invariants
- Production download API stays bound to the existing fixed HTTPS
  Terrarium provider. Never trust arbitrary user-selected tile endpoints.
- Internal package-private downloadFromTileBase introduces a test seam,
  allowing a loopback HTTP server to exercise actual HTTP tile retrieval.
- Synthetic genuine PNG encodes +42 m, test verifies real GeoTIFF pixel,
  georeferencing and RasterImageLoader re-open as well as exact tile
  request count and nonnull CRS envelope.
- Temporary output is isolated per test and server stops in finally.
- No external Internet requirement and no skipped 'mock PASS'.
- This changes the test reliability boundary only, not provider fallback,
  geometry, HTTP limits or atomic publication policy.

## Certification
Run exact-head CI, full Certification, Runtime Identity 8 OS/JDK,
GeoParquet metadata, live-main attestation, then merge SHA-guarded and
repeat post-main checks. Issue #178 remains OPEN until post-main evidence.
Public DEM external availability is separate observational telemetry;
this synthetic fixture does not prove external provider uptime.
#161 atomic multi-file TIFF/sidecar publication remains OPEN.
