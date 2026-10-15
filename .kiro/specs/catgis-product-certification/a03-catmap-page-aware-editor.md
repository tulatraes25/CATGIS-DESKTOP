# CATGIS / CATMAP SPEC Kiro — Active-page edit and undo isolation

STATUS: SOURCE_CANDIDATE / NOT_CERTIFIED
Issue: #102
Exact baseline: `2266223f1f8b4c2dd09a853c86bbb4cafc2a79f5` (PR #101 merged).
A0.7: OPEN. GitHub-first. Do not modify or delete `C:\catgis`.

## Objective

A user editing a CATMAP multipage layout must never accidentally edit
page zero while viewing page two, undo another page's work, or interact
with a stale preview model after New/Open. Correct model/editor
consistency, not a new GIS feature.

## Evidence / confirmed root causes

1. `LayoutModel` stored page lists and active-page index, but
   `addElement/removeElement/getElements/getVisibleElementsSortedByZ`,
   hit-testing, selection, z-order, grouping, translation and
   `saveSnapshot/undo/redo` accessed page-0 `elements` and one
   shared history regardless of active page. #101's new v2
   serializer now correctly persists all pages, exposing this
   source-level editor inconsistency.
2. CATMAP Standalone `Main` had no visible page navigation despite
   the page-aware document model, so multipage workflows were not
   accessible through ordinary editor controls.
3. `newLayout` assigned a brand-new model but kept the old
   `LayoutPreviewPanel` whose model field is final.
   `loadLayoutFile` attempted to cast a Frame content-pane child
   to JSplitPane, even though `createMainPanel` returns JPanel
   installed in BorderLayout.CENTER. Both paths could produce a stale
   preview or fail.
4. Mouse dragging moved elements directly without a before-drag
   history snapshot. Undo could not reliably reverse a drag, and
   a stale selected drag target could survive page switching.

## Requirements / implementation

- All standard edit, visible-layer, hit-test, selection, size, z-order,
  grouping and group translation operations act on the active page.
  Guard reordering and grouping against element references from
  another page: reject before touching any user data.
- Per-page independent undo/redo history of up to 100 snapshots.
  Switching pages preserves each history; undo/redo on B must not
  change page A. Existing single-page semantics remain.
- Keep `getPageElements(index)` snapshot independent for #101
  serializer. Retain existing clamped `setCurrentPage` contract for
  compatibility, but reject invalid indices in the CATMAP UI.
- CATMAP Standalone menu `Páginas` supports Add, Previous and Next,
  visible current-page status, safety check for v2's maximum 200 pages.
  Menu switching also clears the prior drag/selection and invalidates
  preview render.
- A single preview-rebinding helper creates a new LayoutPreviewPanel
  when New/Open swaps LayoutModel and sets the existing scroll viewport
  directly, without invalid casting of nested Swing containers.
- First actual mouse-drag event captures a single before-drag snapshot;
  release and page switch reset drag state. Undo/redo clears stale
  selected element references after restoring new objects.
- Tests use actual CATMAP LayoutLabel/Map and real `.catmap` output;
  verify page-scoped add/delete/undo/redo/grouping/hit-testing,
  page switch and v2 save-reopen, synthetic real Swing mouse drag
  on EDT and undo of moved element with page 0 unaffected.
  CI Checkstyle + 4 jobs and 15 native/system Certification jobs
  required on exact HEAD.

## Non-goals and residuals

- Other CATGIS Composer UI and export options may have different
  page navigation semantics; physical Windows/Linux/macOS GUI and
  project persistence must still be validated.
- Direct mutation through legacy `getCurrentPageElements().add`
  bypasses snapshot recording. The serializer intentionally uses
  it to load v2 efficiently and remains compatible. A later
  package-private/internal builder API can tighten it.
- Advanced editing properties may invalidate the cached preview only
  on repaint rather than on every property change. Audit separately.
- Snapshot format still inherits element subset serialization from v1;
  group IDs and complex styles are not roundtrip certified. Prevent
  cross-page contamination now; expand history coverage separately
  without inventing a perfect undo claim.
- Printing and PDF/PNG/SVG exports #99 remain open; full native GUI
  runtime/Windows OSGeo4W/JNI, signed apps, performance and A0.7 OPEN.
- Merge only after this PR exact-head CI 4/4 + Certification 15/15 and
  authenticated prior-main event=push CI+Certification for
  `2266223f1f8b4c2dd09a853c86bbb4cafc2a79f5`.
  Merge normal with expected_head_sha; then independently check
  new main event=push CI/Certification and attestation.

## Exact-head failure and correction

Initial PR #103 HEAD `30a36830ff8997f0f7daf4d1932a63e66cbe3822`
failed the Source Feature Surface Inventory job in Certification
`38720114262` (job `116372410579`). The Java CI also reported the
same strict `SupplementalVisibleCommandInventoryCertificationTest`.
The source was correct but the registered UI command inventory had not
yet recorded the new `Agregar página`, `Página anterior` and
`Página siguiente` actions. All three actions are real and wired to
nonempty callbacks; they cannot simply be hidden from certification.

Updated `docs/quality/SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv` and
its exact-count regression contract in the same PR, from 201/191/147
occurrences/rows/unique labels to 204/194/150. No test was skipped,
suppressed or relaxed; a fresh SHA CI and Certification is mandatory.
The old run may be cancelled as superseded by source updates; never
claim its failures were infrastructure flakes.

## Second independent semantic-certification gate

After registering the three visible commands, the second exact-head
source candidate failed `Visible Command Inventory` in Certification
`38720495125` job `116374021036`. The original semantic test
`SupplementalCommandSemanticCertificationTest` still expected 191
supplemental commands and 74 ACTION rows; adding valid UI commands
requires explicit semantic traceability, not just a count update.

The branch now adds one honest `ACTION` semantic mapping each for
CATMAP page-add, previous and next, matching RUNTIME_PENDING entries
with SUPPLEMENTAL_COMMAND provenance in
`SEMANTIC_FEATURE_MATRIX.tsv`. Semantic contract counts are now
194 rows, 77 ACTIONs, 68 distinct ACTION feature IDs. Existing
ALIAS_EXISTING, CONTROL, DISPLAY_ONLY, DISABLED_UNSUPPORTED and the
three deliberately disabled CATMAP sync commands are unchanged.

No source command disabled, no test ignored, no A0.7 runtime status
promoted. A new exact SHA CI + all mandatory Certification gates must
start and pass before any merge. Prior SHA failures remain recorded.

Until then: `CATMAP_PAGE_EDITOR=NOT_CERTIFIED`.
