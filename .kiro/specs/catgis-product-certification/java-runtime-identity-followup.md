# SPEC/Kiro #130 - package architecture proof correction

BASE: 65cf75a5796d6d1996851162452f8795fa43c8c5.
INITIAL_HEAD: 9328e1deec5b83733511aa1e10213acdc0605791.
STATUS: CANDIDATE / NOT_CERTIFIED. A0.7 OPEN.
This supplements, rather than erases, java-runtime-identity.md.

## Observed first-run result and cause

Initial workflow38838398430 Linux17 job116772575336 completed the full compile,
test, package and coverage command successfully. Logs explicitly show compiler17,
test launcher17 and packaged Java17.0.20.1. The separate checker reached package
release validation after accepting compiler/launcher/actual-test records,
production/test class headers, exact three identity tests and full-suite counters.
It then failed at the OS_ARCH lookup. Linux21 and MacARM17 also completed their
build/test/package command but failed the later checker; those overall jobs are
FAIL, not PASS. No failed application test is bypassed or weakened.

The first verifier wrongly required source-JDK OS_ARCH in a jlink runtime's
release metadata. A separate actual jlink experiment on isolated Debian
JDK21.0.12.1 produced a release containing only JAVA_VERSION and MODULES; executing
that generated runtime reported version21 and os.arch=amd64. Four checks confirmed
this format, executed architecture, rejection without independent architecture
proof, and rejection of the wrong target. This is supporting JDK behavior evidence,
NOT the missing original GitHub release file or a CATGIS runtime test.

## Stronger minimal correction

- Require the declared JAVA_VERSION as before. If OS_ARCH is present, it must
  match and may never be overridden by another value.
- If OS_ARCH is absent, require an independent observed architecture: execute
  packaged Java and parse unique version/architecture properties on Linux/Mac;
  on Windows decode the actual JVM DLL's PE/COFF machine with bounded offsets,
  valid DOS/PE signatures and a complete bounded PE32+ optional header.
- The Windows evidence remains header+release, not execution of its packaged
  JVM or GUI. This is architecture extraction, not a full PE security validator.
- Reject missing independent proof, conflicting architectures, ambiguous runtime
  properties, 32-bit/unknown/truncated PE headers and mismatched Java versions.
- Preserve every original22 Python test unchanged; add10 regression methods.
  All32 tests pass in the isolated analysis environment. Three Java identity
  tests, all old CATGIS tests and all five matrix targets remain unchanged.
- Use the documented -Dorg.gradle.java.installations.auto-download=false flag;
  the actual compiler/test identity assertions remain authoritative.
- Failure artifacts additionally retain the tiny package release text. Do not
  upload binaries, entire packages, credentials or user data.

Published corrected Python bytes must match blobs
`e558d3c0d4c9d871c10a908f466ab45156482518` and
`cb23fd0c6e730390d60e408eaacf6609e2197562`.

## Certification remains pending

No existing production source, dependency, default Java21 property, prior test,
or mandatory workflow is modified. Do not use initial green build steps as a
completed runtime-identity lane. New exact-head full matrix plus normal gates
and eventual post-main attestation are required. Baseline choice17/21, accurate
active documentation and mandatory attestation integration stay open until
that evidence is complete. No user-PC/OpenCode/GUI/release change.
