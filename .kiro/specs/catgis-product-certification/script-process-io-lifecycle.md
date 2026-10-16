# SPEC/Kiro #132 - reproducible script I/O lifecycle defects

Base: 4454dc033c2392797046b7246c7cd1722a91716b (normal runtime-policy PR131 merge).
Status: CANDIDATE / NOT_CERTIFIED. A0.7 and full issue132 OPEN.

## Evidence, not an invented root cause

The prior main9348 push Certification38886987740 Windows job116927181340
failed ScriptEngineTest.executeCodeWithSyntaxError line66 after30.245 seconds
because output was empty. Actual artifact12041120692 (421809 bytes), SHA256
6963910a52d3eb9272b8bccf495268ba5d0e133d62fa086c317bd3a27fb1e821, was downloaded
and its XML inspected before one diagnostic repeat was requested. Original
XML:1172 total cases,1failure,0errors,15existing skips; the scripting suite has
4cases/1failure and its remaining three methods passed. The old assertion did
not retain ScriptResult.error, so the precise cause of that individual failure
is NOT proved by its thirty-second duration. It is not labeled infrastructure.
The subsequent same-source Windows job116950839629 passes unchanged tests;
that demonstrates intermittent manifestation, NOT a code repair. The original
failure remains in issue132. This tranche does not falsely claim to resolve it.

Two DIFFERENT defects are independently reproducible in exact ScriptEngine
blob a2c24bf2b664361a67fa260bfc777572149612ab: null/empty input never closes
child stdin; and synchronous large stdin writes occur before output draining
or deadline monitoring. A Python child waiting for EOF times out despite having
no work left. A child writing256KiB before reading768KiB fills both pipes and
blocks its caller beyond the configured timeout. The old unsynchronized,
unbounded StringBuilder plus a fixed reader.join(2000) also lacks a reliable
bounded output completion contract. These defects must not be confused with
proof of what happened in the separate syntax-error incident.

## Design and compatibility

Introduce one package-private ScriptProcessIO boundary with no UI/GIS/global
state or new dependencies. Each call creates its own random temporary directory
and file-backed stdin/stdout. Null and empty input become an actual empty file,
which supplies EOF. Input UTF-8 size is checked before staging/starting, output
and error are merged into the owned output file, and the parent monitors the
direct process with a monotonic timeout and output byte budget. This removes
both pipe deadlock and the background reader/data race rather than adding more
reader threads. Python standard I/O explicitly uses UTF-8 on every platform.

Preserve public ScriptResult/method signatures, working directory, argv without
a shell, CATGIS environment context, real nonzero-exit stderr, previous line
normalization and a final newline for nonempty output. Invalid timeout, launch,
I/O, over-limit output, cancellation and cleanup failures cannot become success.
A timeout message belongs in error; never fabricate stdout to satisfy a test.

Input and captured-output budgets are1MiB each. Input checks encoded bytes, not
only Java characters; output reads at most budget+1 and retains a bounded prefix
only on failure. On timeout/interruption, request forced termination of the
direct child and wait up to2 seconds, clean owned I/O files, restore the caller's
interrupt flag. A still-running process or failed cleanup is reported explicitly.

## Supporting causal experiment and its limits

An isolated javac --release17/JVM21.0.12.1 experiment compiled the exact old
ScriptEngine source with minimal AppContext/logger stand-ins, then the candidate
with the same stand-ins. It used actual Python, not a simulated Process:
- Old no-input read: failed at2.091s under a2-second script deadline.
- Old full-duplex: still blocked at6.012s despite a2-second deadline; the owned
  experiment process group was terminated by a separate safety harness.
- Candidate same EOF case: success,6output characters,0.908s.
- Candidate same duplex: success,262157output characters,0.857s.
These are causal reproductions, not performance claims or a full CATGIS build.
A separate real-JVM-child harness passed31 supporting assertions for EOF,
UTF-8, simultaneous input/output, actual stderr/nonzero exit, limits, interrupt,
deadline termination, isolation and I/O cleanup. The authored production/test
bytes were hash-matched to their GitHub blobs before tree publication. This does
NOT claim the new JUnit suite or Windows/Mac integration has already passed.

## Twenty new ordinary regression methods

ScriptProcessIOContractTest uses actual JVM children from its compiled fixture
class, absolute java.home argv, actual Python via the public API, real temp files,
PID checks and old-byte comparisons. Tests cover null/empty EOF, UTF-8,256KiB
stdout before768KiB stdin, exit7/stderr, CRLF/CR/final-line compatibility,
oversized output failure, silent success, deadline/direct PID, mid-run interrupt
and restoration, pre-interrupt no start, nonpositive timeouts, oversized input,
encoded-size limit, exact1MiB input, absent executable,8operations across4workers,
and three Python public-API EOF/duplex/UTF-8 cases. Every test checks that its owned
I/O directory is empty afterward. Python absence fails rather than skips.
All four original ScriptEngineTest methods and all other old tests are unchanged.

## Explicit residuals / risks

File-backed I/O trades pipe blockage for temporary disk use. Monitoring every
100ms is NOT a hard filesystem quota: the output can overshoot before the parent
observes it. Slow filesystems, process startup and legacy Python resolution can
exceed a wall-clock service deadline; this API is not a hard real-time system.
A finite input is now a regular file, not a pipe, which is observable to scripts
that seek or inspect stdin. This noninteractive engine supplies finite text only.
The change bounds parent capture, not child RAM/disk/CPU or global concurrency.

Only the direct child is managed; grandchildren inheriting files, hostile scripts,
symlink/TOCTOU attacks and cross-process privacy require separate sandbox/policy.
No scientific GIS output validation is implied. Runtime limits and failures are
reported, not proof that arbitrary untrusted scripts are safe to run.
Legacy Python discovery/cache/probing, executeCode's source-temp deleteOnExit,
context snapshotting and GUI job coordination remain unchanged. Do not claim
all script temporary source files are immediately removed. The pre-existing
Windows syntax-error anomaly remains separately open until diagnostic causality
is demonstrated, even if subsequent test runs pass.

## Gates

Four changed files only: existing engine, new I/O helper, new test file and SPEC.
No dependencies, workflows, existing tests, installer or user data changes.
Require exact candidate CI4/4, Certification15/15, metadata interoperability,
newly mandatory RuntimeIdentity8/8 and authenticated previous-main push proof.
Normal expected-head merge only; then new main push proofs and attestation.
Do not retry failing application tests blindly or weaken their assertions.
No user PC, C:\\catgis, OpenCode, real credentials, production data or releases.

Primary API reference: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Process.html
