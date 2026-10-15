# CATGIS SPEC/Kiro — A0.7 P1 WCS fail-closed atomic publication (tranche A)

Status: DRAFT / NOT_CERTIFIED
Issue: #87
Parent protected main: `c94bd4c30e432e09a8bd5358bf5a3c18a11c0f51`
Context: #85 sampled GeoTIFF storage validation and #88 shared download byte budget merged.

## Source-confirmed defect

After validating and staging an HTTP WCS GetCoverage raster, WcsClient caught
`AtomicMoveNotSupportedException` and fell back to non-atomic
`Files.move(staging, destination, REPLACE_EXISTING)`. The application advertised
staged atomic rollback, but the fallback has no portable byte-preservation
guarantee if the replacement fails. Untrusted symlinks and nonregular files at
the destination were not explicitly rejected. This is a source-level integrity
risk, not a claim of observed production data loss.

## Tranche A requirements / design

1. Preserve same-parent temporary staging and existing TIFF, CRS, BBOX, WCS
   v2->v1 fallback, encoded-byte budget, error handling, and successful rename.
2. Publish only with `ATOMIC_MOVE, REPLACE_EXISTING`; if unsupported, fail
   closed with actionable IOException, and never attempt a non-atomic overwrite.
3. Before download and again immediately before publish, reject existing
   symlink destination, directory or any nonregular target using
   `NOFOLLOW_LINKS`. Preserve existing file contents and delete only the
   operation's own stage during caught failure.
4. Provide a package-private injectable atomic mover to test unsupported
   filesystem behavior deterministically through the **actual public WCS
   downloading workflow** without modifying global Java filesystem providers.
5. Test with small actual GeoTiffWriter rasters: ordinary atomic success
   (existing regression); mover throws AtomicMoveNotSupportedException after
   a valid staged raster, original raster bytes retained, no temp leakage;
   directory target blocked before network; real symlink target blocked on
   hosts supporting symlink creation, with explicit nonregular fallback test
   on hardened Windows.
6. Exact candidate-head CI + all mandatory Windows/Linux/macOS Intel/macOS
   Apple Silicon Certification jobs, prior main authenticated push attestation,
   normal expected-head merge, then exact new main push CI+Cert+attestation.
   No local workstation modifications or binary geodata uploads.

## Not in scope; issue #87 remains OPEN after this tranche

- Multiple simultaneous WCS writers to the same target across processes,
  stable cross-process filesystem locks, identity changes between checks,
  and malicious ancestor/symlink/reparse-point races.
- Real power loss and portable parent-directory fsync guarantees.
- Filesystem providers violating their advertised atomic move semantics.
- Total WCS wall-clock timeout and cancellation (#86 tranche B), decoder heap,
  complete band/NoData/pixel integrity (#43), native Windows GUI and A0.7.

## Decision rule

`TRANCHE_A_FAILCLOSED=NOT_CERTIFIED` until post-merge main push CI,
Certification and authenticated evidence are complete. A green PR alone is
insufficient. Keep #87/A0.7 OPEN for remaining requirements.
