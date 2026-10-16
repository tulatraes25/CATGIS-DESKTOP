# SPEC/Kiro #132 - Windows cleanup and test-handshake follow-up

Parent candidate: b99b03f6d1fb6f52fd89242697fbe5c24118e756.
Base main: 4454dc033c2392797046b7246c7cd1722a91716b.
Status: NEW CANDIDATE / NOT_CERTIFIED. No failed job is relabeled PASS.

## What the first real matrix found

CI38895807122 completed4/4 and metadata38895807005 passed. The actual
Linux CI clean check/coverage log116955971473 was read. Those results did
not predict all Windows behavior. Runtime38895807124 Windows21 job116955871842
and Windows17 job116955871246 FAILED with1195 cases and the same15 existing
external skips. Both full logs were inspected; they execute synthetic merge
91b4d8e346ba0dfa0ad96d835cc4a6c8d7f647c8 of the original candidate.

Windows21: parallel execution failed with an explicit temporary-cleanup error;
timeout left one owned staging directory; interrupted-child PID parsing found
an empty readiness file. Windows17: timeout and interrupt after-each cleanup
checks found a leftover owned directory. No new test was skipped or disabled.
These are NEW candidate findings, separate from the old main9348 syntax-error
anomaly preserved in issue132. Do not substitute either diagnosis for the other.

## Narrow corrections and limits of causal attribution

The test fixture previously used Files.writeString(ready,pid); the parent waited
only for existence. Existence can precede the completed write, so interrupting
then can expose an empty PID. The fixture now writes/closes a sibling staging
file and atomically moves it to ready. Original test methods, PID assertion,
timeout, worker join and strict after-each cleanup assertions remain unchanged.

The production helper previously attempted each deletion exactly once. It now
retries only its three owned paths under one2-second cleanup budget, at25ms
intervals. It never restarts the script or recursively deletes unknown entries.
Persistent I/O failure remains a failed outcome with a typed diagnostic, not a
success after ignoring leftover files. All paths are attempted even when another
fails. Missing files are already clean; runtime/security failures stop retries;
interrupts are preserved. No global filesystem permission changes or source-file
removal policy were added.

Java documents that open/in-use files may be undeletable on some OSes. The
first candidate collapsed delete exceptions to boolean, so the particular
Windows holder/exception is NOT established by these logs: do not claim antivirus,
a JDK bug, inherited grandchildren or infrastructure as proven cause. The
bounded cleanup is a defensive correction whose effectiveness on actual Windows
must be demonstrated by the NEW candidate matrix. Added diagnostics retain only
exception class names, never script data, private paths or exception messages.

## Added regression evidence

All20 initial JUnit methods are retained; five additional methods check temporary
AccessDenied recovery with actual deletion, permanent failure with exact prior
bytes, refusal to recursively delete an unknown directory entry, interrupt
restoration during retry, and attempting other files despite one failure.
The @AfterEach zero-leftovers requirement is unchanged. Controlled exceptions
are deterministic fault injection, not a claim to reproduce native Windows locks.

The updated helper was compiled with javac --release17 on JVM21.0.12.1 in the
isolated container. The original real-child31-assertion support harness still
passes. A separate filesystem/fault-injection harness passes16 checks. Neither
is the25-method JUnit suite, full CATGIS, or Windows/Mac certification.
Authored bytes match helper blobcca478bdc57f875ef965be90b98d24fd5aabae11 and
test blob848fd971c941409d9ad8756c56a9a1f556a30030 before branch publication.

## Gate and remaining scope

Require fresh CI4/4, Certification15/15, metadata, RuntimeIdentity8/8 and exact
base-main authenticated evidence. No original failing-test rerun is used instead
of this corrective commit. Do not inherit the old candidate's Linux success.
Direct-child-only supervision, file-backed-output overshoot, filesystem/startup
latency, legacy Python discovery/source deleteOnExit, and the original Windows
syntax-error incident remain separate limits under issue132. A0.7 OPEN.
The first SPEC remains as history; this follow-up supersedes its one-shot I/O
cleanup and20-test count. ScriptEngine code and all previous application tests,
dependencies/workflows/installers are unchanged by this follow-up. No user PC,
C:\catgis, OpenCode, credential, dataset or release modification.

Primary reference: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html#deleteIfExists(java.nio.file.Path)
