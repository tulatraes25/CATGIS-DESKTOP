# CATGIS SPEC/Kiro — A0.7 P1 Tranche A — cross-process XYZ lease

Status: CANDIDATE / NOT_CERTIFIED
Issue: #73 (partial gate — not full recovery)
Main baseline: `364d58944a10f261df844fb78affda2487e96898` (PR #72 merged)
Branch: `quality/a0-xyz-cross-process-lease-post72`

## Root cause

PR #72 protects against two threads in **one** CATGIS process via an
in-memory `ACTIVE_TARGETS` set, but two independently launched CATGIS JVMs
cannot see each other's memory. Both may select the same canonical tile output
path and race while moving it to or from backups.

## Minimum implementation

- Resolve one stable SHA-256-derived sibling lease pathname from the canonical
  output target (never from arbitrary untrusted metadata). Only regular files
  are valid; refuse symlinks. Keep the lease file permanently (do not unlink
  while locks can be acquired on different inodes).
- Acquire kernel FileChannel.tryLock() before any staging or mutation, still
  retaining the existing in-JVM target set. Release after cleanup, including
  on an exception or timeout; process death releases the kernel lock.
- Require a fixed, versioned marker in the lease file. Unknown contents,
  denied locks and unsupporting filesystems fail closed, leaving destination
  data untouched.
- Revalidate any preexisting target after acquiring the file lock, before
  generating output. No script shells, no Swing EDT, no third-party binaries.
- Native workflow remains unchanged: the real 256x256 north-origin XYZ test
  runs against Linux GDAL3.8 and both macOS GDAL3.13.

## Deterministic acceptance tests

- Two leases from same JVM: second unavailable; release then reacquire.
- Child JVM obtains a real OS lock, parent JVM cannot obtain it; after child
  release the parent can acquire. Must run on Windows, Linux, Mac Intel & ARM.
- Corrupted lease marker and symlink lock refuse without deleting user data.
- Existing PR #72 transaction and real native GDAL tests all pass unchanged.
- Exact PR CI+Certification 15 jobs, authenticated base-main push CI/Cert,
  controlled merge, new main event=push CI/Cert and attestation.

## Residual risks / STOP

**This PR does not implement a journal.** Abrupt process death between atomic
old-to-backup and staged-to-target moves can still leave the visible output
path absent with the old dataset in a private backup. A separate follow-up
under #73 must implement durable record, recovery of same-target data, startup
reconciliation and process-abort tests. Network shares may have unreliable
POSIX/Windows lock behavior; do not promise universal filesystem guarantees
or 100% safety. Windows native OSGeo4W/JNI and interactive GUI remain OPEN.

No local user computer or geodata is modified.
