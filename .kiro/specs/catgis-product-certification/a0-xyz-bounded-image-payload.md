# CATGIS — SPEC/Kiro A0.3/A0.4: bounded XYZ tile payloads and decode

Status: CANDIDATE / NOT_CERTIFIED
Parent: protected main `4a3d14a457b512857df265de89a05f57be9aeae6`
Branch: `quality/a0-xyz-bounded-image-decoding`
A0.7 and local GUI/runtime remain BLOCKED.

## Root cause, observed in production source

`OnlineTileCache.fetchTile` calls `readAllBytes(InputStream)`, accumulating the entire
HTTP response into an unbounded `ByteArrayOutputStream` before examining
`Content-Length`. An attacker, faulty server or unexpected large image can
exhaust JVM heap or starve the six online tile workers. The existing
`ImageIO.read` (network and disk cache) performs raster decompression before
checking image dimensions, enabling small compressed image bombs. This is an
independent resource-safety defect, not the provider-identity collision of #44.

## Objective and constraints

- Bound each compressed XYZ image response and cached disk file to 8 MiB.
- Reject a declared Content-Length exceeding this cap before reading data.
- Reject chunked/unknown-length responses as soon as they exceed the cap.
- Preserve complete-body checks for declared Content-Length and normal PNG/JPEG
  support; never persist or cache rejected/incomplete bytes.
- Inspect image reader width/height metadata before image decoding and reject
  width/height above 2048 pixels or pixel count above 4,194,304.
- Apply the same image metadata protections when reading an existing disk tile,
  including cached neighbor prefetch. Move neighbor disk existence/length checks
  into the existing worker executor rather than running them on Swing EDT.
- Keep network, filesystem probing, image decoding and repaint off Swing EDT;
  preserve foreground request deduplication, source-failure reporting and retry
  cooldown. Prefetch work must use a distinct pending-key set and must not block
  foreground image requests or suppress their repaint callbacks.
- No new dependencies, huge committed fixtures, remote services or GUI session.
- Do not claim a maximum wall-clock HTTP deadline: connect/read timeouts remain
  distinct and need further architecture review.

## Acceptance scenarios

1. Normal tiny PNG from a loopback HTTP server displays pixel-exactly and is
   cached on disk, then reloads without HTTP.
2. HTTP response with declared length >8 MiB is rejected without publishing a
   cache image, leaving a visible failure diagnostic.
3. Chunked/unknown-length response above 8 MiB is rejected after bounded input;
   does not create a disk tile and does not warm RAM.
4. PNG with misleadingly small compressed bytes but dimensions above 2048 is
   rejected before full raster decode, with no cache pollution.
5. Existing disk tile with excessive dimensions or on-disk length is never
   decoded into a large Java image; adjacent-tile prefetch must use the same
   bounded reader and prove pixel-exact memory warmup from a worker.
6. Error and failure paths preserve existing retry/backoff. Simulated
   prefetch-pending status for a tile must not prevent foreground getTile from
   fetching, caching and repainting it. Use only runtime fixtures and loopback HTTP.
7. Exact SHA CATGIS CI/Certification SUCCESS on Linux and Windows, real PostGIS,
   architecture and feature inventory gates. Merge and post-main CI are separate.

## Branch sequencing and known limitations

This tranche branches from the last integrated main to remain independent of
open #41 WCS and #44 provider-fingerprint changes. It must be reconciled onto
their eventual exact main SHA before merge; never force-update `main`.
The recent provider diagnostic API in #44 differs from main; tests here must
not silently depend on #44 or bypass its pending merge gate. The main push-run
attestation issue #42 still blocks full architectural closure.
