# CATGIS Java runtime policy

Decision: SPEC/Kiro #130 / PR #131, 2026-10-16.
Production default: **Java 21**, unchanged in `catgis-desktop/gradle.properties`.
Additional source-build compatibility target: **Java 17**, selected explicitly.
STATUS: policy implementation candidate; exact-head and post-main evidence required.
A0.7: OPEN. This document is not a release certificate.

## Why this policy exists

The former README/workflow labels called the baseline Java17 while the actual
Gradle property selected21. A Java17 bootstrap process may launch a Java21
compiler/test process and package a Java21 runtime. Therefore old setup-java
labels or task descriptions alone are not compatibility evidence. Existing
passing tests remain genuine for the code and actual environment they exercised;
their old Java17 interpretation must not be repeated without runtime proof.

Keep the existing default21 rather than downgrade the product to match stale
text. An explicit `-PcatgisJavaVersion=17` build creates a separate artifact;
a default Java21 class-file artifact is NOT certified for execution on17.
No dependency version or GIS implementation is changed by this decision.

## Required identity proof

For each platform (Linux x86_64, Windows x86_64, Mac Intel x86_64, Mac Apple
Silicon aarch64), independently compile/test/package both17 and21. Eight lanes
must observe compiler toolchain, test launcher, actual test Runtime.version,
production/test class-file headers and packaged runtime version/architecture.
The complete tests and coverage run in each lane; existing15 dependency skips
remain explicit and are not counted as executed successes. There is no retry
of failed tests or silent replacement of the requested Java target.

Linux/Mac execute the packaged Java to read its actual properties, without
opening Swing. Windows jpackage strips its Java command, so the current proof
uses release version plus the actual PE32+ JVM DLL machine field. This is not
an executed packaged-Windows JVM or a clean-machine/GUI certification.
The checker rejects mismatched or absent evidence, wrong/ambiguous architecture,
preview/truncated class files, inconsistent test reports and modified tracked
Git HEAD/tree/index/worktree. It cannot defeat a compromised runner fabricating
all internally consistent evidence.

## Version and gate enforcement

The existing default CI, Certification and GeoParquet-metadata workflows now
bootstrap21, matching the unchanged declared default. They retain their jobs,
tests, dependency versions and skip budgets. Runtime Identity separately uses
explicit17/21 overrides and observes the result, not just the setup step.

Runtime Identity runs on every PR and every main push, including documentation
changes. Once its file exists in the exact protected-main workflow inventory,
Main Push Attestation requires all eight named jobs from a successful **push**
on that same main SHA. No PR or sibling-branch badge substitutes for it.
The introduction PR still attests the preceding main's own requirements, so the
new obligation is not applied retroactively. Duplicate or incomplete job lists
are rejected; all previous CI/native/PostGIS/metadata obligations remain.

This amends executable attestation, not GitHub administrative branch-protection
settings. No administration permission or ruleset change is claimed. Readiness
and normal expected-head merge still require explicit review of every relevant
candidate workflow, followed by fresh actual-main evidence and attestation.

## Already observed versus newly required

Earlier head a5e9203e4095f98c8a5bcf545099305e760415cd completed Runtime Identity
38839176334 (five lanes), CI38839176455, Certification38839176450 and metadata
38839176418. All five runtime lanes passed with1145 counted cases,0failures,
0errors,15reviewed skips and32 verifier regressions per lane. The previously
pending MacARM17 log116775224035 explicitly reports17/aarch64 and unchanged tree
193fb0aca015579ca343fb5f8959dfc2b28a3e7e. This measured17 on all four platforms
and21 on Linux only. Those runs do NOT prove the three new21-platform lanes or
the newly combined source tree; new candidate runs are mandatory.

Residuals: JNI/native-library ABI, actual GUI/HiDPI/focus, clean installation,
signing/notarization, all-feature professional acceptance and deployment. Old
archived reports/task-help descriptions are historical context, not authority
for the actual compiler or shipped runtime. The source property, explicit lane,
observed artifacts and exact-SHA evidence govern current claims.

Primary technical references:
- https://docs.gradle.org/current/userguide/toolchains.html
- https://docs.oracle.com/en/java/javase/17/jpackage/image-and-runtime-modifications.html
- https://docs.geotools.org/maintenance/userguide/build/install/jdk.html
