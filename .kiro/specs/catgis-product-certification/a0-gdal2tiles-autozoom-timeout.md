# CATGIS — SPEC/Kiro GDAL2Tiles automatic zoom and process timeout

Status: STAGED SOURCE TESTS / NOT_CERTIFIED
Based on protected main `ef40a3d0b751b7beba8c06e7dd25059692f28db4`
Branch: `quality/a0-gdal2tiles-autozoom-bounded-process`
Do not merge until the real GDAL CLI #66 tranche is certified and this branch
is replayed onto the exact then-current protected-main commit.

## Root defects (code confirmed)

1. In `Gdal2TilesService.generateTiles`, automatic zoom (`zoomMax <= 0`)
   built `-z` followed by a synthesized zoom range, then replaced and
   removed only the range. The `-z` flag was left alone, so GDAL interpreted
   the next `--resampling` option as its argument or rejected the command.
2. The service read the subprocess stdout stream **to EOF BEFORE** calling
   `Process.waitFor(600 seconds)`. A stalled external tool holding stdout
   open bypassed the timeout entirely. Output was appended to an unbounded
   StringBuilder, risking large heap growth for verbose geoprocessing runs.
3. There were no deterministic command-boundary/automatic-zoom tests.

## Correction

- Introduce package-private, pure `buildCommand` returning immutable argument
  lists. Fixed zoom supplies exactly one `-z min-max`; auto zoom omits `-z`
  entirely and retains the reviewed bilinear/4-worker policy.
- Reject negative minimum or inverted fixed zoom; preserve raster and output
  paths containing spaces as **single** ProcessBuilder arguments, no shell.
- Use already-reviewed `ExternalToolService.collectProcess` to drain stdout
  concurrently with a hard timeout and a 1 MiB output cap. Keep a worker-only
  usage contract; do not block Swing EDT.
- No new external dependencies, GDAL binaries or user-data manipulation.
- Treat the existing Python `-m gdal2tiles` module and environment detection
  as a separate availability/version issue; do not silently assert that GDAL
  Python bindings are installed by merely detecting Python.

## Acceptance

- Five deterministic tests for auto-zoom, explicit zoom, equal zoom bounds,
  argument boundaries with spaces, and negative/invalid parameters.
- CATGIS CI and Certification Windows/Linux/macOS Intel/ARM, runtime-bundled
  app images, PostGIS, architecture and (after #66) native GDAL jobs must pass
  on exact head and again on protected-main push merge SHA.
- The 600s timeout must actually apply even if GDAL keeps stdout open,
  and memory must remain bounded by the shared process collector.
- Actual gdal2tiles Python module generation is not yet certified by these
  command construction tests; A0.7 OPEN.
