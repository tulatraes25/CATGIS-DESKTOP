# SPEC/Kiro #144A — bounded project parsing before destructive open

BASE: `228e30c60c330eedf8f918d3a4ffae9e660c953d` — DRAFT / NOT_CERTIFIED
Scope: P0 project-open data safety prerequisite (#144); no full staged commit claim.

## Evidence and hazard
Legacy LoadProjectAction used an unbounded BufferedReader loop and parsed
records until EOF without a project byte limit, record/line limit, or control
character screening. After parsing, it destroys current live raster and vector
session before realizing a required source cannot be restored. This tranche
eliminates excessive or malformed project text before any state mutation.

## Requirements
- Reject non-regular/linked .catgis inputs and files >64 MiB before opening.
- While parsing, cap a physical line to 1 MiB, total records 250000,
  layers 25000, groups 10000, combined CATMAP item records 50000.
- Reject NUL and structural control characters; allow TAB, valid Unicode.
- Throw to the existing outer error handler; do not replace AppContext,
  clear maps, mark clean, or alter recent-file state on rejected input.
- Preserve existing accepted file format and per-layer parsing semantics.
- Do not add logic to the legacy LoadProjectAction architecture hotspot;
  move budget enforcement into a standalone class.

## Tests and evidence
ProjectOpenReadBudgetContractTest exercises independent limits and real
early failure through LoadProjectAction with an existing project identity.
Must also keep ProjectFilePublisherContractTest and project roundtrips green.

## Residual
#144 remains OPEN: detached staging for all mandatory vector/raster sources,
explicit DEFERRED_AUTH/DEFERRED_REMOTE, rollback-free commit boundary, corrupt
required local layer rejection, view/CATMAP malformed record handling and
false success/clean state are NOT addressed. Optional remote data is not
silently declared fully loaded. #149 recovery must reuse the future stage API.
No OpenCode/PC/release authorization; A0.7 OPEN.

## Subgate B — local source existence before live-session mutation

The existing loader parsed into a detached Project but cleared the previous
map/layers BEFORE it attempted to load local files. A missing local Shapefile,
GeoPackage, GPX, SpatiaLite or raster path therefore allowed partial success
and could discard the open in-memory map.

ProjectLocalSourcePreflight now checks required local files are regular,
non-symlink, nonempty sources after parsing and before clearAllLayers or
AppContext.setCurrentProject. Legacy spatialite://file#table is mapped to a
physical source path for this check. Online WMS/WFS/XYZ and PostGIS are
explicitly deferred remote sources and derived rasters may be regenerated.
Tests prove missing local vector rejects the whole open and preserves project
identity, modified flag, CRS and prior layer state before any destructive call.

Residual #144 still OPEN: this verifies existence, NOT full parse/readability,
CRS integrity, database table presence or successful detached reconstruction.
A file may disappear after preflight (TOCTOU). Remote states must eventually
report DEFERRED rather than loaded. No transaction guarantee or merge claim.

## Subgate C — reject silently dropped structural project records

Legacy LoadProjectAction previously treated malformed CATMAP layout/legend
records as null and ignored them, skipped incomplete declared layers and
accepted non-finite VIEW values, sometimes marking a partial project clean.
ProjectOpenRecordIntegrity now rejects those cases during the parse stage,
before the current workspace is disposed. It also rejects incomplete
PROJECT_META/PROJECT_CRS and nameless layer groups.

Contracts exercise NaN/Infinity/negative/zero zoom, malformed layer/CATMAP
and missing metadata, proving old dirty Project reference and CRS stay intact.
Legacy valid records and UI activation remain unchanged. This is fail-closed
text integrity, NOT full detached local raster/vector reconstruction.
Full acceptance of #144 is still blocked.
