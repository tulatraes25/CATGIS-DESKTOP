# CATGIS SPEC/Kiro — A0.7 P1 — Transactional XYZ tile output

Status: CANDIDATE / NOT_CERTIFIED
Issue: #70
Source base: `9d371fb7d2de51ec592287e3be4068b2c6af5a3a` (post-#71 skill)
Branch: `quality/a0-gdal-xyz-atomic-publish-post71`

## Root cause and scope

Before this change, `Gdal2TilesService.generateTiles()` gave GDAL the final
user-visible output directory. Nonzero exit or timeout could leave incomplete
`z/x/y.png` trees or intermix stale/new rasters. A successful subprocess
exit alone is not a valid cartographic or filesystem publication gate.

Keep the already-proven native modern/Python capability detection and XYZ
north-origin conventions from #69, and route the command's destination to a
**nonexistent** `tiles` path inside a new, sibling, CATGIS-owned staging
directory. The public final output path is never passed to GDAL.

## Acceptance and negative cases

1. Reject Swing EDT, null input/output, missing/nonregular raster, invalid
   zooms, root target, nonexisting output parent, symlink target/direct
   symlinked parent, source raster inside the destination, and unowned
   existing destination; never delete user files.
2. Preserve source and existing output on timeout, process failure, empty
   output, invalid/non-PNG tile, invalid XYZ z/x/y coordinates/zoom, unknown
   root metadata, staging symlinks and other malformed trees.
3. Validate generated PNG 256x256 readability (one file), nonzero tile count,
   numeric 0..30 zoom and in-range x/y paths; all paths must be within
   staging and no links are followed during cleanup. Respect fixed zoom when
   explicitly supplied. Bound traversal and root metadata sizes.
4. Only after validation, stamp `.catgis-xyz-owner` and publish via same-
   filesystem `ATOMIC_MOVE`. Previously owned, structurally valid output
   is atomically renamed into a CATGIS-owned private backup and restored if
   the new-stage rename fails. Reject filesystems without an atomic rename;
   no unsafe copy fallback.
5. A per-process lock on a canonical target prevents simultaneous CATGIS
   writers. A rejected second writer must not release the first lock.
6. Remove owned staging on all normal success/failure paths. If restoration
   itself fails, never erase the surviving backup: log its recovery location.
7. Deterministic JUnit tests exercise new/replace, partial tool failure or
   timeout, ownership, empty/corrupt/wrong-zoom output, symlinks,
   concurrency, rename failure, unsupported atomic move and EDT.
8. Existing real native `Gdal2TilesRealXyzIntegrationTest` must still pass
   using **public** `generateTiles` in Linux, macOS Intel, macOS ARM.
   Windows full suite and image packaging remain required, while actual
   native OSGeo4W/JNI/GUI are separately pending.
9. Exact-head GitHub CI/Certification, preceding main push attestation,
   normal expected-SHA merge and post-main push certification. No binary
   fixture/artifact uploads and no local Windows/OpenCode changes.

## Causal control: why a staging directory and not direct output

On a synthetic runner, generate a legitimate 256x256 PNG then simulate
nonzero exit. The original target must remain byte-exact; no `.catgis-xyz-
stage-*` remnants. Use injected atomic-move failures for deterministic
rollback evidence independent of the runner's filesystem policy.

## Explicit unresolved release limitations

- **Crash window during replacement**: portable Java 17 does not provide an
  atomic directory *exchange*. After moving the old directory into a backup,
  the final destination can briefly be absent before the staged rename.
  Process-level rollback covers caught I/O failures, not abrupt power loss.
  A separate durable journal/startup-recovery or platform-specific atomic
  swap is required before calling crash-safe replacement certified.
- **External multi-process collisions**: same-process writes are serialized;
  a second independent CATGIS process and arbitrary external filesystem
  writers are not yet guarded by a cross-process lock.
- **Scientific completeness**: all tile path names and one real PNG are
  validated before publication; exhaustive CRC/color/NoData verification
  for every potentially enormous tile set and native Windows GIS/GUI remain
  independent acceptance gates.

These limitations must stay visible in issue #70/A0.7 rather than turning
`PASS` of deterministic tests into a claim of total product readiness.
