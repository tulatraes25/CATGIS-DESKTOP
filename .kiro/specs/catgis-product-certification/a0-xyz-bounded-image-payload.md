# CATGIS — SPEC/Kiro A0.3/A0.4: bounded XYZ tile payloads and decode

Status: CANDIDATE / NOT_CERTIFIED
Parent: protected main `943940bfb7603bfff9e9e0843963508b6cdb1560`
Branch: `quality/a0-xyz-cache-safety-consolidated-post41`
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
- Bound the **total** decoded-memory tile cache to 128 MiB of pixel backing
  buffers, in addition to the existing 512-entry count ceiling. Evict the
  least recently used images when crossing either limit.
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
7. Byte-budget regression proves that ordinary decoded LRU entries are evicted
   by pixel backing bytes even when fewer than 512 entries exist, including
   access-order preservation and images larger than an artificial test budget.
8. Exact SHA CATGIS CI/Certification SUCCESS on Linux and Windows, real PostGIS,
   architecture and feature inventory gates. Merge and post-main CI are separate.

## Branch sequencing and known limitations

This is a **consolidated post-PR41 replay** of former #44 provider-identity
and #45 bounded-memory candidates onto protected main
`943940bfb7603bfff9e9e0843963508b6cdb1560`.
The diagnostics contract now accepts the complete immutable source, not only
the mutable ID; test fixtures follow the versioned SHA-256 disk namespace.
Both requirements must pass on the exact combined SHA before any merge.
PR #41 WCS transport was already merged, but WCS scientific payload validation
remains an independent OPEN issue #43.

Attestation of the pre-PR41 main SHA via GitHub Actions was established by PR
#46, CI push run `38321642515` and Certification push run `38321642463`.
The post-PR41 main push evidence and this consolidated candidate's exact-SHA
certification must be independently verified before integration. No local
GUI/OpenCode checks are authorized before A0.7 closure.
