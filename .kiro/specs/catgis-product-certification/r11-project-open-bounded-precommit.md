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
