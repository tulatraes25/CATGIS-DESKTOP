# SPEC/Kiro - row probe reconciliation after Java LF repair

STATUS: CANDIDATE / RESEARCH_ONLY / NOT_PRODUCT_CERTIFIED.
Protected base: 11209e37a6bc95375dbe0033abbe7c7b276d95d5 (PR #123).
Prior probe head: c48a42c57327c5a9bdca8a3af234f808677a5a48.
A0.7 and full GeoParquet issue #112: OPEN.

## Verified cause and preservation

The prior row matrix 38829646364 completed successfully on Windows, Mac Intel
and Mac ARM. Linux job116745464821 executed all17 Java tests successfully,
verified all19 source hashes, then failed git diff because twelve existing Java
blobs conflicted with their LF attributes. That was not continuing numeric
corruption: the 0.5 coordinate and signed-zero controls passed in the actual
Linux JUnit artifact. Artifact12016358648 contained17 tests/0 failures/0 errors/
0 skips. Its inventory contains20 JARs/73652924 bytes, not the superseded
bea96a88 inventory29 JARs/94469231 bytes. Neither number is installer growth.

PR #123 already repaired exactly those twelve blobs and added the committed
Java/checkout integrity guard. This branch incorporates that main through a
normal two-parent integration commit, preserving the entire current main tree
and the exact probe Java sources, fixtures, build definitions and original17
Java test methods. No force push, whitespace-ignore, reset or duplicated
normalization is used. The original ROW_READER_PROBE.md remains the historical
experiment specification; this followup supersedes its current checkpoint and
footprint, not its acceptance criteria or documented residuals.

## Evidence guard design

- Snapshot HEAD and tree before dependency resolution. Reject any tracked
  staged or unstaged modification, and reject changed HEAD/tree after tests.
- Require the exact three JUnit suites and17 distinct named contracts, matching
  declared counters and actual failure/error/skip nodes. Counter-only success,
  duplicate/missing/substituted tests, malformed XML, entities and oversized
  reports fail closed. These guards are not proof against an attacker able to
  forge an entire matching report or control the runner.
- Preserve original17 Java tests and19 independent source hashes. Add27 Python
  regression methods for source identity and report validation, with actual
  disposable Git repositories and synthetic malformed report controls.
- The offline delivery had40 checks including13 EOL-generator tests. Do not add
  its now-obsolete repair generator: PR123 already owns the mechanical repair
  and its8 tests. The27 remaining evidence guards passed locally, and also
  accepted the authentic Linux artifact. This is supplemental, not GitHub PASS.
- Add path-scoped main push execution to the four-platform probe workflow for
  eventual post-merge evidence. Current research remains draft pending the
  adapter/resource/distribution decision; no new layer importer is enabled.

## Verification gate and limits

New exact-head CI, all15 Certification jobs, metadata interoperability,
predecessor-main attestation and all4 row-probe jobs must complete successfully.
Each row job requires27 Python guard tests,17 Java tests without omissions,
19 unchanged input hashes, unchanged tracked source identity and dependency
inventory. Java and metadata decoder code, runtime dependencies, installer,
GUI and existing mandatory workflows are not modified relative to main.

The research remains materialized small-file reading, not bounded streaming
into CATGIS layers or proof of hostile-allocation/TOCTOU safety. No full CRS,
3D/M, broad logical-type/codec, physical GUI, cold-start offline or distribution
clearance follows from these checks. No user-PC/OpenCode/release changes.
