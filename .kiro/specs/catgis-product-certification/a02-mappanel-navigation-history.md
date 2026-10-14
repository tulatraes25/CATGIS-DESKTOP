# CATGIS SPEC/Kiro A0.2 — MapPanel navigation and viewport state

Status: CANDIDATE / NOT_CERTIFIED
Issue: #80
Baseline main: `fe7e90d5b96aa684c224caf2efaabb752c0194ec` (post PR #77 certified; WCS PR #78/#79 untouched).
Branch: `quality/a02-mappanel-zoom-pan-history-post77`

## Evidence / root cause
- MapPanel zoomIn/zoomOut pre-stores the view; MapViewController does the
  same internally and neither stores the post-zoom view. Navigation back/next
  can revisit identical stale samples instead of returning to the zoomed map.
- MouseHandler performs pan by updating live fields and syncing controller,
  whereas MoveTool release only records the pre-drag state; the new position
  is absent from view history.
- MapViewController initializes size to 800×600. Component resize previously
  notified only the status bar; resizing to another size before zoom could
  anchor at a stale center or incorrect cursor y-world coordinate.
- The closed but unmerged PR #3 explored pan preview; it is NOT production
  evidence on today's protected main and is excluded from this narrow fix.

## Requirements / non-goals
- A0.2 behavioral gate: preserve old and new distinct valid views; deduplicate
  identical history samples; invalidate forward history when a new zoom
  branches from an earlier view; never create history on an immobile click.
- Keep viewport center fixed around Zoom In/Out in the actual resized
  MapPanel; preserve zoom-wheel world anchor after resizing.
- Keep class responsibilities and cross-component pathways unchanged beyond
  history/resize sync; no imagery/cache modifications, no new GUI features.
- Do not claim this eliminates pan lag: MouseHandler still executes
  snapping/status checks and paintComponent still renders full layer scene.
  That optimization requires causal frame-time baselines and a separate PR.

## Regression contract
- Swing EDT test: zoomIn from 320×200 panel; prior/next restores exact
  minXY/zoom and does not reveal phantom duplicate states.
- Branch test: previous then new zoomOut invalidates next redo state.
- Wheel test: world coordinate under cursor is preserved after panel resize.
- Actual MouseHandler+MoveTool drag: pre/post XY persisted, previous/next
  roundtrip in both directions; stationary click adds no history.
- Existing MapPanel export exception rollback, golden map rendering and
  render-order tests continue without changes.

## Merge gate
Exact-head CATGIS CI, full Certification and predecessor-main push
attestation; controlled expected-SHA merge then post-main push CI,
Certification and authenticated transfer. Mark A0.2 PARTIAL until full
MapPanel raster/vector/selection/pan preview scientific/runtime coverage.
Do not request local OpenCode/Windows GUI tests while A0.7 is OPEN.
