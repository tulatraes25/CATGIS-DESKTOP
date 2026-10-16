# SPEC/Kiro #130 - actual JVM identity and honest certification

BASE: 65cf75a5796d6d1996851162452f8795fa43c8c5.
STATUS: CANDIDATE / NOT_CERTIFIED. A0.7 OPEN.

## Source/log mismatch

README and CI summaries say Java17. gradle.properties explicitly sets
catgisJavaVersion=21 and build.gradle resolves compiler/test/packaging through
that property. PR127 MacIntel job116764921505 produced1158 tests with0 failures,
0 errors and15 reviewed skips, but the packaged java executable reports21.
Thus a setup-java17 step does not prove actual Java17 tests or distribution.
The test results are real; the prior Java17 interpretation was not justified.

## Bounded design

Leave all production source, dependencies, gradle.properties and existing
workflows unchanged. Add an independent diagnostic workflow with explicit17
on Windows/Linux/MacIntel/MacARM and a Linux21 comparison. No auto-provisioning
of an alternative JVM in this lane. Ordinary checks retain the declared21
baseline until an evidence-based version decision is made.

An opt-in Gradle init script checks the resolved compiler and test launcher
against the lane's explicit expected feature. Three new ordinary JUnit methods
inspect actual Runtime.version and actual production/test class-file headers.
A separate Python verifier checks the real reports, exact method identities,
full-suite counters versus testcase nodes, the existing700-test minimum and15
reviewed-skip budget, compiler/launcher records, package release version and
architecture, and tracked Git HEAD/tree/index/worktree integrity before/after.
Linux/Mac also execute packaged java without launching the application GUI.
Windows stripped runtimes are checked through release metadata and real PE
jvm.dll signature; that is NOT a launched Windows packaged JVM or GUI proof.

No existing tests are removed, reclassified or retried. Only dependency
resolution has bounded retries. No runtime binaries or full packages are
uploaded; failure evidence consists of small JSON and one JUnit report.

## Supporting tests

22 new Python unittest methods passed in the isolated analysis environment.
They use deliberately synthetic evidence and an actual disposable Git repo to
reject wrong JVM/architecture, malformed/duplicate/oversized JSON and release
files, preview/truncated classes, wrong/missing/duplicated/substituted JUnit
identities, entity/non-UTF8 XML, inconsistent counters, excessive skips, and
staged/unstaged source changes. These are evidence-validator tests, not actual
CATGIS Java17 compatibility. Full compile/test/package lanes must pass in CI.

## Decision gate and limits

If17 fails, inspect exact compiler/dependency/runtime cause and keep the
production default unchanged. Do not change expectations or claim a successful
Java21 lane proves17. This PR alone does not reconcile all historical text,
prove every JVM21 platform, execute the GUI or solve native JNI compatibility.
It does not defend against a malicious runner fabricating all matching records.
A1MiB report bound and process timeouts bound ordinary evidence handling; an
unexpected packaged executable is not sandboxed by this test.

Require all existing CI/Certification/metadata gates, the new complete matrix,
prior-main push attestation, expected-head merge and fresh post-main evidence.
Before any eventual merge, make this workflow an explicit attestation
requirement as appropriate; merely defining a workflow is not branch protection.
No PC/OpenCode/releases touched. No percentage of bug-freedom is claimed.

Primary references:
https://docs.gradle.org/current/userguide/toolchains.html
https://docs.oracle.com/en/java/javase/17/jpackage/image-and-runtime-modifications.html
https://docs.geotools.org/maintenance/userguide/build/install/jdk.html
