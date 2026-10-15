# CATGIS SPEC/Kiro A0.7 - Fail-closed CATMAP PNG/JPG/PDF exports (partial #99)

STATUS: SOURCE_CANDIDATE / NOT_CERTIFIED
Issue: #99
Exact main baseline: 9dc74c96350148267f99754638a1d0ffc7082fae (PR #103 merged).
Local workstation: untouched; architecture A0.7 remains OPEN.

## Objective and source evidence

LayoutExportEngine.exportPng/exportJpg previously wrote with ImageIO.write directly
to finalFile; exportPdf used PDDocument.save(finalFile). A failed encoder or
mid-write exception could truncate an existing professional export.
This is a source-level risk, not a report of actual customer data loss.

## Requirements and boundaries

1. Stage a unique .part in the destination's real parent directory.
   Preserve the prior target until encoding and independent decoder checks pass.
2. Validate parent existence; reject symlink/directory/nonregular output.
   Recheck the target immediately before publication.
3. Publish only with ATOMIC_MOVE + REPLACE_EXISTING; unsupported atomic move
   must fail closed with actionable message, never retry nonatomically.
4. Delete only CATMAP-owned unique staging on any error.
5. PNG/JPG: correct format, dimensions, and three sampled decoded real pixels.
   PDF: parseable PDFBox document with one page and matching media box,
   plus vector text proven by small fixture.
6. Check ImageIO.write boolean; bound A4 raster allocations at 18M pixels.
7. Preserve export behavior, metadata and legacy filenames.
8. Cross-platform Java17 GitHub tests for actual valid files and failure modes.

## Design

New CatmapAtomicExport has three injectable seams: stage writer, real decoder
validator, and atomic mover. LayoutExportEngine uses it for PNG/JPG/PDF.
PDF rendering is preserved but writes only to stage. Image checks sample
bounded 1x1 pixel regions and do NOT establish complete image integrity.

## Acceptance and certification

- PR HEAD CI four jobs + Certification fifteen jobs SUCCESS.
- Independently authenticate predecessor main push CI/Certification at
  9dc74c96350148267f99754638a1d0ffc7082fae.
- Normal expected-head merge, then fresh main push CI + Certification
  and authenticated post-merge attestation.
- No local PC/OpenCode changes; no skipped test accepted as success.
- Issue #99 and A0.7 stay open.

## Explicit residual risks and next gates

- SvgExportEngine still embeds raster PNG inside an SVG wrapper and
  directly writes the output. It is not a true vector SVG. Separate gate.
- AtlasEngine (#96) needs multi-output transactional semantics and isolation.
- Filesystem parent directory durability after power cut, multi-process
  destination exclusion and last-instant alias/reparse races unproven.
- UI export remains synchronous on Swing EDT; native GUI and physical
  PDF typography/page fidelity beyond small fixture unverified.
- Multiplatform packaging, native GDAL and GUI are separate evidence layers.
