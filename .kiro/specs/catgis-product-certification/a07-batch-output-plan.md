# SPEC/Kiro - immutable batch output planning

BASE: 48c46e957552d28613c367632fafe7b0a5cd7433
ISSUE: #114; professional-capability direction #109
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Objective and root cause

Prevent known whole-batch output collisions before executing the first operation. BatchProcessor already exists; this is a safety foundation for stronger processing, not a newly implemented graphical modeler. Previously each target was derived independently from input.getName() inside the execution loop. Duplicate basenames or a destination aliasing a later source could cause an earlier callback to destroy that source.

## Contract and minimal design

- Snapshot caller inputs. Resolve all source identities, then validate every planned destination before calling an operation. Preserve the caller's basename/extension in the actual source argument; canonical paths are used for identity checking, not silently to rename aliases.
- Reject duplicate output basenames using conservative NFC/case normalization, output symlinks/directories, invalid output directories and destinations aliasing any source or another existing destination. Use indexed filesystem keys and exact isSameFile fallback for providers without keys.
- Return zero successful and total failed with an actionable preflight diagnostic, and invoke zero operation/progress callbacks on a rejected nonempty batch. Do not create an output directory or staging in the core preflight.
- Retain existing valid-copy overwrite behavior for unrelated previous results, per-operation failures, ordered progress and empty-list compatibility. A null input list continues to fail rather than invent input.
- Keep one core helper independent of Swing, HTTP and GIS codecs. BatchProcessorDialog already calls processing from SwingWorker; that integration is unchanged.

## Tests and evidence

Twelve new BatchOutputPlanContractTest tests use actual small files, inode aliases and byte snapshots. They are filesystem tests, not claims of genuine raster validation. Cover real copy output/order/progress, duplicate names, direct source overwrite, cross-input hard links, hard-linked outputs, case/Unicode spelling, caller-list mutation, absent/null output directory, directory target, missing late source and immutable read-only planning. Existing BatchProcessorTest remains unchanged.

Supplementary isolated javac --release 17/JVM21 smoke: 18 checks passed on the exact BatchOutputPlan blob 33def151c93ee590cf4640ecbff9a370b5f2ce80. It additionally exercised a Linux output symlink and a differently named source symlink. This is NOT the JDK17 full suite or Windows/Mac runtime evidence. GitHub's new exact candidate CI/Certification is mandatory; no new skips/disabled tests are introduced.

## Explicit limits and risk

This plan is not a filesystem lock, a guarantee against hostile TOCTOU changes, an all-files rollback, or a sandbox for arbitrary callbacks. It does not validate raster results or prevent callbacks writing undeclared paths. Shapefile sidecars need dataset-level publication. Native filesystems without stable file keys use an exact but potentially slower fallback; that provider path needs separate benchmark/evidence. NFC/lowercase checks are conservative, not proof of all exotic filesystem name equivalences.

The UI's existing mkdirs and done/progress presentation are unchanged; the no-directory-write guarantee applies to the core preflight, not the whole dialog. Cancellation, typed operation descriptors, scientific raster precision/NoData/CRS, atomic publication, durable history and the graphical DAG modeler remain separate required increments. Do not label this entire processing track complete.

## Certification and next decisions

Require CI 4/4, Certification 15/15, independent predecessor-main push attestation, normal expected-head merge, then fresh merged-main push evidence. Main 48c46e... contains the prior PDF correction; its post-main evidence cannot be substituted by PR #111's candidate checks. No local user-PC changes or feature-freeze bypass.
