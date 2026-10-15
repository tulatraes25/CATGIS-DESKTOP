# CATGIS CATMAP SPEC/Kiro — Multipage document and PNG payload safety

Status: SOURCE_CANDIDATE / NOT_CERTIFIED
Issue: #98
Base: `43744678c8c7f232c3c9d58360c00f160a2ac4da` (PR #100 just merged)
Feature freeze: compatibility and data-integrity repair only.
User workstation: do not modify or delete C:\catgis.

## Root cause confirmed in source

1. `LayoutModel` retains a `pages` list and `currentPage`,
   but `CatmapSerializer.save` used only `getElements()` (first
   page) and `load` created exactly one page. Additional content was
   silently absent when reopening.
2. For non-null `LayoutImage`, the serializer caught all exceptions
   from `ImageIO.write` and wrote empty `IMAGE_DATA`, then reported
   successful save: unnoticed omission of images.
3. Non-empty corrupted PNG payloads were silently converted to
   blank `LayoutImage` during loading. Large malicious encoded images
   could allocate memory without a dimension/budget check.

## Design / requirements

- Keep exact `# CATMAP Layout v1` for a one-page layout and tolerate
  existing v1 lines. No breaking migration of old saved projects.
- Use explicit `# CATMAP Layout v2`, `PAGE_COUNT`,
  `CURRENT_PAGE`, `PAGE_BEGIN=index` and `PAGE_END=index` for
  multi-page layouts. Serialize elements on every page, including
  empty pages, preserving their order and active page selection.
- A malformed v2 page order, missing boundary, unknown element,
  corrupt nonempty embedded image, excessive count, and illegal
  page index are terminal IOException with no partial model.
- Save all pages without changing current editor page. Add
  `getPageElements(index)` snapshot, preserving existing in-memory API.
- A non-null embedded image must produce a genuine PNG; test encoder
  return flag, nonempty signature/IHDR and bounded encoded bytes and
  dimensions. Encoding failure throws IOException and, under the
  existing CATMAP atomic staging, leaves prior .catmap and .bak intact.
- Restrict file reads to 128 MiB, 200 pages, 25,000 elements,
  24 MiB encoded PNG and 32M pixels per embedded image. V1 remains
  tolerant of malformed historical lines but cannot skip a v2
  page boundary without a v2 header.
- Tests: real v2 save/reopen with label, raster image pixel values,
  map CRS-relevant extent/layer keys, empty page and active-page
  preservation; single page v1 compatibility; deterministic
  simulated image-encoder IOException with byte-unchanged previous
  file and backup/no staging; truncated v2 and invalid PNG fail
  closed; unordered pages and unknown types fail closed.

## Remaining scope — do not claim entire product finished

- Current `LayoutModel.addElement/removeElement/undo/redo` and some
  CATMAP UI operations still primarily modify page 0, even with
  `currentPage` pointing elsewhere. This tranche guarantees disk
  roundtrip of the *existing* pages, not complete multi-page editing UX.
  Editor operations and per-page undo must get separate acceptance.
- `PAGE_SIZE=A4` and `PAGE_ORIENTATION=LANDSCAPE` are legacy fixed
  metadata, not a model of page-specific dimensions/orientation.
  Do not claim support for variable-sized pages.
- Group state/advanced styling beyond the existing v1 element fields
  is not persisted by this change. Real native CATMAP editing and
  printing acceptance remain open.
- Other PDF/PNG/SVG export integrity #99, external concurrency and
  power-loss durability #95, and native GUI remain open.
- The v1 loader intentionally remains tolerant of corrupt historic
  embedded images and will warn; do not misreport corrupt v1 recovery
  as byte-exact.

## GitHub gates

Exact-head CI 4/4 and all 15 platform/natives Certification jobs,
authenticated predecessor main push CI/Certification for the PR #100
merge SHA, merge normally only with expected_head_sha, then fresh
event=push CI/Certification and independent attestation on the
resulting main SHA.

Until all gates: `CATMAP_MULTIPAGE_IMAGE=NOT_CERTIFIED`,
A0.7 OPEN; no Windows local test or destructive operation.
