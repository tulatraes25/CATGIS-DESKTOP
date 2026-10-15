# CATGIS CATMAP — SPEC/Kiro A0.3 Atlas page isolation and safe PNG export

Status: CODE_CANDIDATE / NOT_CERTIFIED
Parent main: `ae79a773f1f5acc81f1fa459d57ee131827f4452` (PR #97 merged)
Primary issue: #96
Related integrity issues: #95, #98, #99
Feature freeze: bug fixes and safety guardrails only. C:\catgis untouched.

## Source evidence / root cause

Prior `catmap/AtlasEngine.generatePages(template, pages, dpi)`
called `applyPageOverrides(template, page)` directly. This wrote
`LayoutLabel.setText`, `LayoutMap.setOwnExtent`, view X/Y and zoom into
elements still owned by the open editor. A/B/A generation mutated the
editor template to the last page's values and could contaminate future
outputs. The method called `setOwnViewMinX` twice. It also allocated
every page BufferedImage in memory with arbitrary DPI. Its disk export
wrote each PNG directly to the final output filename without atomic
staging or a guard against silent overwrites.

## Tranche A acceptance implemented

1. Atlas-only changes are made to `LayoutLabel.copyForAtlas()` and
   `LayoutMap.copyForAtlas()` instances with independent field state.
   Preserve font/color/halo/group and map CRS-relevant grid, layer keys,
   frame, extent, scale, and preview settings. Independent MapFrame
   renderers/viewport caches are never shared with the source map.
   No Atlas-specific edit is applied to the original model.
2. Each page starts from original map/label state, independent of prior
   page override. A/B/A image hashes must match and A vs B differ.
   Original title/subtitle, map X/Y/zoom and assigned layers remain
   unchanged after successful and failed generation.
3. Reject incomplete/malformed page extent values rather than write
   NaN/null. Require finite X/Y and positive zoom together.
4. Reject invalid and excessive DPI before allocating the raster;
   bound one page to 96 MiB (RGBA estimate) and the in-memory
   `generatePages` batch to 256 MiB. Never multiply pixel counts in
   signed int arithmetic.
5. `generateAndSave` renders one page at a time, validates real PNG
   decoding and dimensions, stages in the actual destination directory,
   fsyncs staged bytes, and publishes via ATOMIC_MOVE. Do not silently
   overwrite an already exported atlas PNG. Reject traversal, symlinked
   output directory, and control characters.
6. Real headless Java/JUnit tests: A/B/A stable rendering and open
   template unchanged; invalid extent and huge batch rejected;
   streamed genuine decodable PNG with names/spaces; existing output
   unchanged; atomic publication failure injection leaves no .part;
   traversal name fails without creating directories.
7. CATMAP Standalone's atlas dialog previously offered `PNG` and `PDF`
   but unconditionally called `AtlasEngine.generateAndSave(...)` producing
   PNG. Remove the deceptive PDF atlas choice, showing truthful PNG-only
   output until actual PDF batch export is implemented and certified.

## Out of scope / residuals

- Other `LayoutElement` subtypes do not receive page overrides;
  their existing renderers are reused, and thread-safety of their
  caches is **not** asserted. The mutable derived frame state of
  map elements is isolated.
- The stored map preview `BufferedImage` is shared read-only between
  map copies; callers must not concurrently mutate the pixel buffer.
- External modifications between exists-check and ATOMIC_MOVE can
  race; no per-directory cross-process lock or physical crash test.
  Batch is per-page atomic, **not all pages as one transaction**;
  completed earlier pages remain if a later page fails.
- Actual CATMAP Standalone GUI usability and native atlas workflow execution,
  signed .app launch, print/PDF export and scientific map extent
  accuracy beyond the tiny tests are not physically verified.
  Read-through raster/vector data and NoData require end-to-end runs.
- Follow-up #98 multi-page persistence and #99 PDF/SVG honesty remain
  OPEN. Do not claim entire issue #96 resolved without the full
  professional UI and physical native acceptance.

## GitHub certification gates

Require exact source SHA candidate CATGIS CI 4/4 and full Certification
15 mandatory jobs (Windows, Linux, both Macs, native GDAL/PostGIS).
Obtain authenticated **post-PR #97** event=push main CI and
Certification on `ae79a773f1f5acc81f1fa459d57ee131827f4452`.
Only after those pass, mark ready, merge by expected head SHA and
authenticate fresh post-main CI and Certification. Until then
`ATLAS_PAGE_ISOLATION=NOT_CERTIFIED`. A0.7 OPEN.
