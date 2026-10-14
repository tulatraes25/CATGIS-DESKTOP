# CATGIS SPEC/Kiro — A0.2 P0-PAN scene preview (isolated candidate)

Status: CANDIDATE / NOT_CERTIFIED
Issue: #82
Source: exact protected main `d7e91589b37df810097168c2f99f1fc022628371`, merged #83.
Branch: `quality/a02-pan-frame-preview-post83`

## Root cause and constraints
MapPanel.paintComponent rendered the complete layer list, labels, heatmaps
and clusters on every mouse drag repaint. A previous PR #3 explored pan
screenshots but was closed without merging and had fragile headless tests.
The fix must never offset selection, pins, active sketches, overlays,
attribution, map scale bar and other screen-fixed decorations.

## Implementation
- On pure MoveTool/middle-pan interaction only, use a 32 MiB-bound ARGB
  snapshot of WORLD map layers and labels, excluding all live/selected
  overlays and fixed screen decorations. Paint live overlays every frame.
- First pan paint captures once at the current geographic viewport. Following
  drags translate snapshot by exact world-to-screen delta at same zoom. If
  displaced > min(96px, quarter viewport), capture a new definitive scene
  to bound temporary blank-edge strips.
- Every regular repaint (style, layer, CRS, async source result or resize)
  invalidates snapshot; navigation repaint alone may reuse it.
- Explicit export/layout mode skips interactive preview even when the
  temporary dimensions equal actual panel dimensions. After release clear
  preview and demand definitive full render.
- Log only completed session metrics with [P0-PAN]: captures, reused paints,
  fallbacks, invalidations, capture milliseconds. Unit counters are not
  fabricated frame-rate claims.

## Tests / acceptance
- Real BufferedImage pixels: a source map feature translates during pan,
  a fixed blue screen overlay remains in its original pixel location.
  After release red map pixels come from a fresh authoritative renderer.
- External repaint invalidates cache before next image; oversized move
  forces recapture at bounded exposed-edge shift.
- CATMAP/export ignores pan preview, restores requested origin and discards
  interactive snapshot. No stale imagery exported.
- Memory cap test rejects 5000x5000 and integer overflow dimensions and
  honors active editing/drawing fallback.
- Run existing MapPanel navigation, MOVE vs SELECT/edit, golden render,
  CRS/GeoTIFF, WMS/online/Pins, CI+Certification four OS/arch and genuine
  native GDAL on same candidate SHA.
- Actual Windows GUI frame times remain NOT_VERIFIED until A0.7 local
  gate. Do not claim universally smooth navigation or production release.

## Risks
- A frame capture renders once on EDT; first drag may be slow. Cached preview
  lacks off-screen geography at exposed edges until recapture/release.
- Dynamic online imagery can redraw via normal repaint; if a third-party
  source fails to issue a repaint after content mutation, stale imagery
  could remain until mouse release. Mark such providers for fallback.
- No component architecture is completely refactored; A0.2 remains OPEN.

## GitHub protection
Exact-head CI and 15/15 Certification plus authenticated prior-main push
attestation required before ready/expected-head normal merge. Must then
verify exact post-main push CI/Certification and attestation. No local
OpenCode or Windows file changes.
