# CATGIS SPEC/Kiro — A0.7 P1 WCS total transfer deadline and cancellation

Status: CANDIDATE / NOT_CERTIFIED
Issue: #86 (tranche B)
Parent main: `57e68821ffdc47a83881d2e386e03d433cf78676` (PR #90 merged, post-main certification must be proven before next merge)
Feature freeze: transfer reliability only; no new GIS features or workstation changes.

## Root cause

`WcsClient.streamCoverageVersion` previously used HTTP connect timeout 10s
and read timeout 60s, each bounding a single connect/read, **not the entire
request**. A response sending one byte within each read timeout could
consume resources until the full (otherwise bounded) encoded-byte budget
was exhausted. Broad WCS 2.0 exception catch would also start WCS 1.0
fallback after a thread interruption unless cancellation was checked first.

## Requirements and implementation

1. One monotonic `System.nanoTime()` deadline shared across WCS 2.0.1,
   WCS 1.0.0, staged TIFF validation and atomic publication. Production
   default 600 seconds (10 minutes); explicit Java property
   `catgis.wcs.maxDownloadSeconds` accepts whole seconds 1–7200.
   Bad configuration fails before staging. Package-private seam permits
   sub-second test budgets, no global property mutation.
2. Bound connect and HTTP read timeout to remaining total budget. Check
   deadline and interruption before network, after response headers, before
   and after each streamed chunk, after decoding, and before final rename.
3. A single daemon watchdog periodically checks elapsed monotonic time
   and worker interruption and calls `HttpURLConnection.disconnect()`
   to abort an active blocking read/response-code call. It is cancelled
   and the HTTP connection disconnected on every return/failure.
4. Treat overall deadline expiry and worker interruption as terminal
   `InterruptedIOException`: **never** retry WCS v1 after v2 cancellation
   or deadline. Ordinary recoverable HTTP v2 errors continue to use
   existing fallback. Keep shared byte budget and staging cleanup.
5. Preserve previous raster byte-for-byte on error; no orphaned
   `.catgis-wcs-*.part`, permanent interprocess lease still held
   until operation cleanup; successful GeoTIFF transfer remains supported.
6. Test real loopback HTTP fixtures with chunked slow trickle across
   read timeouts and 350ms total deadline; cancelled-before-request
   worker; interrupted in-flight blocked read with bounded join and no
   fallback; successful raster plus fallback covered by existing tests.
   Validate Windows/Linux/macOS Intel + ARM and native GDAL/PostGIS CI.
7. Do not merge until candidate exact-HEAD CI and 15-job Certification,
   authenticated `main` event=push CI/Certification for the parent,
   expected-HEAD normal merge, and fresh post-main CI/Certification
   plus authenticated attestation all succeed.

## Residuals: #86/#87/#43/#91 and A0.7 remain open

- This is a **transport watchdog** and publication safety deadline, not a
  guarantee that an uncooperative JDK/native TIFF decoder or a stalled
  filesystem can always be preempted exactly at the time boundary.
- Concurrent WCS operations may each use the configured byte/time budget;
  global concurrency/quota and interrupted GUI user controls are separate.
- UI-layer cancel button and post-download raster decode on Swing EDT
  remain issue #91; core worker interruption is covered here.
- TIFF NoData/whole-image scientific validity, memory decompression
  limits and malicious external filesystem races remain open (#43/#87).
- No local GUI acceptance, Windows OSGeo4W/JNI or release certification
  is claimed by these CI tests.

## Decision

`WCS_DEADLINE_TRANCHE=NOT_CERTIFIED` until exact post-main evidence is
complete. A0.7 OPEN.
