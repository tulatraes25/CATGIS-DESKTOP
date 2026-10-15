# CATGIS / CATMAP SPEC-Kiro — fail-closed layout persistence tranche A

Status: IMPLEMENTED_CANDIDATE / NOT_CERTIFIED
Primary issue: #95
Canonical base main: `548e4d2208ddfb14bb3408fe076de5341a33e8f9` (post PR #93 merge)
Related atlas content-integrity bug: #96
P0/P1 architecture gates remain open. No interactive local tests or PC cleanup.

## Source-confirmed defect

`CatmapSerializer.save()` formerly wrote a **fixed** `<name>.tmp`,
renamed the existing `<name>.catmap` to `<name>.catmap.bak`,
then attempted to rename the stage to the target. This is *not*
an atomic replacement: if the final rename failed or the process stopped,
the user's primary .catmap could be missing from its saved location.
A second instance could also overwrite the same fixed staging file.
`.bak.delete()` was not checked. This was verified in source, NOT in
production user data; historical reports incorrectly called it atomic.

## Requirements implemented

1. New per-call unique staging path in the **same physical parent directory**.
   Never clobber an unrelated stale `<name>.tmp` and delete only our
   owned staging files on failure.
2. Fail closed on existing destination or backup symlinks, directories
   or nonregular filesystem entries; use `NOFOLLOW_LINKS`.
3. Write the entire layout into the private stage, close and force
   file contents. On an existing target, first copy its bytes to a
   separate private backup stage and atomically publish the backup;
   never move/remove the existing target before writing the new layout.
4. Publish **only** through `Files.move(stage,target,ATOMIC_MOVE,REPLACE_EXISTING)`.
   An unsupported atomic move is a visible IOException preserving the
   previous primary layout. No non-atomic fallback.
5. Preserve existing v1 CATMAP serialization and load semantics: page
   headers, element order, map assigned layer keys, embedded image
   payload, labels, properties, old .bak convention.
6. Provide package-private `AtomicLayoutPublisher` injection to
   deterministically simulate both a failed backup rename and a failed
   final layout rename with real filesystem stage/copy/cleanup.
7. New 5-case JUnit contract covers bytes-unchanged rollback, backup
   intact when backup atomic move fails, successful first+second saves
   and stale .tmp preservation, directory destination fail-close,
   symlink target/backup rejection (only privileged platforms can run
   symlink subcase). All platforms must still exercise rollback.

## Explicit residuals / no overclaiming

- Cross-process destination lease (especially when a second CATMAP is
  saving the same layout) is not yet enforced. Atomic individual
  replacement prevents torn **single** writes, not lost updates.
- Filesystem identity swaps/parent reparse races, hardlinks, malicious
  external writers, network filesystem atomicity lies, parent-directory
  fsync and real power-loss durability are not proven.
- Broken/corrupt `IMAGE_DATA` encoding can still silently serialize
  as empty data in the existing serializer. A separate test-gated fix
  must not change compatibility without evaluating legacy projects.
- PDF/PNG/SVG exports, CATMAP Atlas state mutation (#96), printer,
  real map frame and GUI E2E are **not** certified by this change.
- `LayoutModel` in-memory editing and undo/redo are preserved but
  simultaneous modification during asynchronous saves is untested;
  current standalone save remains synchronous and UI EDT/performance
  is a separate gate.

## CI and certification decision

Exact candidate HEAD 4/4 CI and all 15 Certification jobs required,
including Windows/Linux/macOS Intel/macOS Apple Silicon and real native
GDAL/PostGIS where applicable. The predecessor `main` exact push CI
and Certification must pass an authenticated Main Push Attestation.
Normal SHA-locked merge only, then **new** main push CI/Certification
and independent attestation. Until all this completes:
`CATMAP_LAYOUT_ATOMIC_SAVE=NOT_CERTIFIED`; A0.7 and #95 remain OPEN.
