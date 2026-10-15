# CATGIS SPEC/Kiro — A0.7 P1 WCS interprocess target lease

Status: CANDIDATE / NOT_CERTIFIED
Primary issue: #87
Exact protected-main parent: `9bd195ddfbdac4b12e0b8a63311b8921febf3b0e` (post PR #89)
Feature freeze: preserve WCS user workflows; no UI redesign or local execution.

## Objective and confirmed source-level risk

Concurrent independent WCS downloads could each stage and atomically publish a valid
file to the same final pathname; successful atomic rename alone does not serialize
operations. The last-finishing request may silently replace a more recent coverage.
Single-process synchronization is insufficient because users may run multiple CATGIS
JVMs. Issue #87 also tracks separate path-identity/TOCTOU and power-loss risks.

## Minimal design

- Reuse the **reviewed pattern**, not shared lock ownership, from the existing
  `GdalTileTargetLock` implementation. New `WcsTargetLock` uses its own
  `.catgis-lock-wcs-<sha256>.lck` namespace and WCS-specific marker.
  Lock files are deliberately **never removed**, preventing lock-inode races.
- Before network/staging, resolve the existing output parent with `toRealPath()`
  so aliases to the same physical parent share one canonical target key.
  **Mandatory source correction after first CI:** `WcsTargetLock.lockPath()`
  must independently resolve the parent with `toRealPath()`; otherwise
  a direct lease caller using `/var` versus the WCS caller's `/private/var`
  may hash different targets on macOS (and Windows folder aliases similarly).
  A multi-platform negative test originally exposed this defect: 979 tests,
  one failed on Windows and both macOS runners despite Linux CI passing.
  Repair the identity in the lock implementation, not by relaxing assertions.
  Use the normalized output filename within that parent, consistent on
  Windows/default macOS case-insensitive filesystems.
- Require safe regular lock path with NOFOLLOW_LINKS, enforce marker integrity,
  acquire real OS kernel `FileChannel.tryLock()` with immediate collision
  rejection in the **same JVM and independent processes**.
- Hold the lease through both WCS protocol attempts, staged output validation,
  atomic publication and private staging cleanup; close reliably in try-with-resources.
  Do not use shell, wait for locks, mutate the original target on contention,
  or delete a lease file during cleanup.
- Keep existing byte budget, GeoTIFF strip/tile/CRS/pixel sampling, WCS fallback,
  atomic-move-only behavior, and unsafe destination checks.

## Required executable evidence

1. Real same-JVM duplicate lock rejection, release/reacquire and permanent marker.
2. Independent child-JVM OS lock conflict and release, with bounded test timeout.
3. Equal lock identity on case-insensitive Windows/macOS path-case variants.
4. Corrupt/non-owner lock marker and symlink lease path rejected without overwrite.
5. Real GeoTiffWriter HTTP integration: while lock held, refuse WCS download
   **before any network request**, preserve prior raster byte-for-byte, no
   `.catgis-wcs-*.part` staging; after release, succeed and publish valid raster.
6. Exact-head CI all required jobs and all 15 Certification jobs on
   Windows, Linux, macOS Intel, macOS Apple Silicon, native GDAL and PostGIS;
   authenticated event=push CI+Certification for predecessor main. Controlled
   expected-SHA normal merge followed by fresh main push gates and attestation.

## Explicit residuals (issue #87 remains OPEN)

- Symlink/reparse alias swaps during the narrow check→publish window; secure
  directory-handle resolution, hardlinks, replace-on-rename races from other
  *non-CATGIS* applications.
- Network filesystems whose locking semantics are not reliably cross-host;
  hostile processes able to unlink/replace a CATGIS-owned lock file.
- No power-cut directory fsync or guarantee of atomic behavior on unsupported
  filesystems (previous tranche fails closed on unsupported atomic rename).
- A global wall-clock WCS timeout/cancellation and aggregate byte quota (#86);
  full scientific pixel/NoData/decoder budget (#43); physical GUI/native Windows.
- `A0.7` stays OPEN and user `C:\catgis` must not be touched.

## Certification decision

`WCS_INTERPROCESS_LEASE=CANDIDATE` until exact candidate head CI + Certification,
authenticated preceding-main push, controlled normal merge, **and**
post-main CI + Certification + independent authenticated attestation all PASS.
Even then this is only the named lease tranche, not a full A0.7 closure.
