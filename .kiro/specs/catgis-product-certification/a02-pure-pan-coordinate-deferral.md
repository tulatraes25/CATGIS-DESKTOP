# CATGIS SPEC/Kiro — A0.2 P0-PAN Tranche A (coordinate and snap deferral)

Status: CANDIDATE / NOT_CERTIFIED
Issue: #82, depends on PR #81
Protected parent main: `4858a7d80b886ff94b6199a2e3e8cc375d2fe12a`
Branch: `quality/a02-pure-pan-coordinate-deferral-post81`

## Source-verified hot path
MouseHandler.mouseDragged always invokes updateStatusCoordinates (which may
perform expensive CRS reprojection and geographic formatting) and
updateHoverAndSnap BEFORE checking the pan cases. When the pointer is
merely navigating the map these operations are not needed per delta.
MapPanel.paintComponent still re-renders all layers for repaint: this
tranche does NOT remove full-scene render cost or claim GUI smoothness.

## Minimal controlled change
- Only for active middle-pan OR plain active MoveTool mouse drag with no
  CAD, editing, pin, selection box, profile, drawing or measurement, skip
  coordinate/status/snap work during repeated mouseDragged calls.
- Update real view coordinates and repaint using the already-working
  direct pan math; do not change geometry/CRS or screen decorations.
- On corresponding mouseReleased update status and snap preview ONCE,
  then preserve normal tool release and navigation history behavior
  from PR #81.
- Keep every non-navigation drag path as before; do not modify WMS/XYZ,
  raster output, print/CATMAP export, production-native tools or fonts.

## Regression tests
- Six consecutive real left MoveTool drag events cause zero status/snap
  updates until release, then exactly one each, with committed undo view.
- Real middle-button pan has same deferred update and retains history.
- SELECT box drag and featureEditMode continue updating status/snap on drag.
- Ordinary hover continues updating status/snap normally.
- Existing MapPanelNavigationHistoryContractTest remains green, as do
  golden rendering, vector/raster, packaging and native GIS suites.

## Residual
This is a low-risk CPU-work reduction, NOT snapshot pan or frame cache.
Every pan still requests a repaint, which draws full vector/raster/labels.
Review prior unmerged PR #3 only for design. Follow-up tranche B must
prove pixel/overlay/CRS invalidation and bound snapshot memory before a
preview cache can be considered. Runtime GUI/frame timings remain
NOT_VERIFIED until A0.7 is closed.

## GitHub gate
Do NOT merge while parent `main` post-#81 push CI/Certification are
running, nor before exact-head CI+Certification and authenticated
previous-main attestation all SUCCESS. Normal expected-SHA merge and
post-main push CI/Certification/attestation required. No local Windows
tests or user data changes.
