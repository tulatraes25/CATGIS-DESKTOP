# CATGIS SPEC/Kiro A0.7 - SVG raster-in-wrapper integrity (partial #99)

STATUS: SOURCE_CANDIDATE / NOT_CERTIFIED
Exact protected main baseline: 4d09113f07d9261b6433859e0e960a21e48c5c9d (PR #104 merged)
ISSUE: #99 - GIF? NO; ONLY SVG raster-in-SVG output.

## Objective and root cause

SvgExportEngine used PrintWriter directly on the final SVG file. PrintWriter can
suppress I/O errors and incomplete output may overwrite an existing asset.
The implementation embeds a single PNG image: it is not true vector artwork.
The current user-facing 'SVG' action did not explain that limitation.

## Requirements

1. Keep the existing raster-in-SVG file contract; do not invent vector feature.
2. Write a new SVG by checked UTF-8 Writer to a unique staging file in
   the same real parent directory; validate and atomically publish it using
   the certified shared CatmapAtomicExport helper from PR #104.
3. Verify namespace-aware well-formed XML with DTD, XXE and external XML
   entities disabled; accept only one SVG root, background rect and embedded
   data:image/png;base64 PNG with real decodable expected raster dimensions.
4. Reject scripts, foreign content and unsupported external links.
5. Bound raster rendering at 18M pixels (inherited), embedded PNG at 64 MiB,
   and SVG serialized file at 128 MiB. Do not silently reduce DPI.
6. On encoder failure, invalid XML/PNG, unsupported atomic move or unsafe target,
   preserve the original SVG byte-for-byte and remove CATMAP-owned staging.
7. The file chooser and completion message must describe the SVG as
   raster PNG embedded, NOT editable vector geometry.
8. Tests: actual embedded PNG pixels, re-export and replace, invalid base64,
   external entity/DOCTYPE, unexpected script, unsupported move, directory
   and symlink targets, excessive DPI, no remaining .part.

## Non-goals and residual risks

- SVG output is still raster in a wrapper. True vector typography, paths,
  scaling and layer preservation remain NOT_IMPLEMENTED.
- Existing export operation is synchronous on Swing EDT; large work/cancel
  and real native GUI responsiveness need separate acceptance.
- DOM XML verification has a bounded serialized-file size but may still use
  significant transient heap for embedded Base64; real memory profiling
  across Windows/Linux/Mac is NOT_VERIFIED.
- Atlas all-pages transaction (#96), physical SVG viewers and multi-process
  destination racing remain unverified; issue #99 and A0.7 remain OPEN.

## GitHub-first gates

- Verify exact candidate PR HEAD CI four jobs and Certification fifteen jobs.
- Authenticated previous main event=push CI/Certification on 4d09113f07d9261b6433859e0e960a21e48c5c9d.
- Normal SHA-constrained merge only after all gates SUCCESS; then require
  fresh event=push CI + Certification and independent post-main attestation.
- No local user PC/OpenCode tests until architecture gates permit.
