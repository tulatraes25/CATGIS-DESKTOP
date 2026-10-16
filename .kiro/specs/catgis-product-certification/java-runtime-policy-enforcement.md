# SPEC/Kiro #130 - enforce measured runtime policy

Combined base: c40f4d43561a7f3cbb91dc2184f0cf8f96fa6bc0 (normal merge PR127).
Previous identity head: a5e9203e4095f98c8a5bcf545099305e760415cd.
Previous identity tree: 193fb0aca015579ca343fb5f8959dfc2b28a3e7e.
STATUS: CANDIDATE / NOT_CERTIFIED. A0.7 OPEN.

## Objective and observed evidence

Close the gap between the actual production target21 and misleading17 labels,
without weakening compatibility testing or downgrading dependencies. The old
five-lane identity workflow38839176334 is now completely successful, including
MacARM17 log116775224035:32 Python regressions and1145 JUnit cases,0failures,
0errors,15reviewed external skips, actual17 compiler/test/package on aarch64.
Certification38839176450 also completed15/15. This is source-build evidence,
not proof that a21 artifact can run on17, nor evidence for new source changes.

## Atomic policy implementation

1. Preserve existing gradle.properties target21 and every application/dependency
   implementation. Update active README policy and ordinary workflow bootstrap
   labels/versions to21. Remove the false hardcoded Mac summary JDK17 claim.
2. Retain all five existing diagnostic lanes and add Windows21, MacIntel21 and
   MacARM21. Do not remove or skip any identity or application test.
3. Remove path filtering from Runtime Identity so every main commit can satisfy
   exact-main attestation, including documentation-only commits.
4. Extend the actual inline attestation script: protected-main workflow presence
   introduces all eight named identity requirements only after the first merge.
   Require same-SHA main push success and each job success. Reject duplicate
   names and incomplete job-page inventory, preserving all old mandatory jobs.
5. New test_runtime_attestation.py executes the shipped inline Python script
   under synthetic API responses. It does not call GitHub or use live tokens.
   Runtime lanes execute it before compilation. Existing32 Python verifier tests
   and3 Java identity methods are unchanged.

## Fifteen new test methods

Complete8-lane proof; pre-introduction compatibility; each missing lane;
failure/cancel/skip/in-progress; old5-lane matrix refusal; duplicate jobs;
incomplete page; exact page count; wrong SHA/event/branch/status/conclusion;
missing workflow run; old CI/native/metadata requirements; malformed SHA with
zero HTTP; read-only/no-checkout permission boundary; exact8-lane unfiltered
workflow; and consistency of21 source property with all ordinary bootstraps.
Nested cases exercise every platform/version and failure state.

Supporting isolated execution passed14 selected methods (including nested
cases), using the exact shipped script/YAML and synthetic API. The15th method
requires the complete repository's real property/workflow files and is left
for actual GitHub execution; it is NOT disabled, filtered or skipped in CI.
All three locally exercised files were hash-matched to their published blobs.
No full local CATGIS, runtime matrix or GUI execution is claimed.

## Integration and risk

A normal two-parent integration brought in only PR127's four unchanged authored
files. New policy files build on that combined tree. PR129 sidecar source is
not silently imported. Revalidate all candidate checks and prior-main proof;
then normal expected-head merge, fresh main CI/Certification/metadata/8-lane
identity and authenticated post-main attestation. Do not reuse the old5-lane
success for the extended8-lane requirement.

The attestation has read-only contents/actions permissions, no checkout and no
PR-provided code execution. It reads protected-main source via authenticated
API. This is not a change to administrative branch protection or a guarantee
against malicious runners. The cost of8 full platform/version lanes is explicit;
no binary artifacts or new persistent caches are stored. Failure evidence stays
small and short-lived. Windows package metadata/PE verification is distinct from
execution of that packaged JVM, GUI or JNI. Historical skill/task-help wording
still needs careful contextual interpretation, not retroactive report rewriting.

## Percentage discipline

Completion of8/8 jobs measures this exact candidate matrix, not product
completion. No A0.7, UX, professional raster semantics or release milestone is
closed by this policy alone. User PC, C:\\catgis, OpenCode and releases untouched.
