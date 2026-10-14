# SPEC/Kiro — A0.7 P1 — XYZ recoverable publication after JVM interruption

Status: CANDIDATE / NOT_CERTIFIED
Issue: #73 tranche B
Baseline main: `15f4b9a67014457680830e666cf2884e4ac38ecd`
Branch: `quality/a0-xyz-crash-recovery-journal-post75`

## Evidence and root cause
PR #72 stages and atomically renames folders; PR #75 adds a real kernel
file lock across JVMs. Portable Java 17 does **not** atomically exchange
two directories. Terminating after old->backup but before new->target
can strand the previous map in a private staging folder.

## Algorithm
- Always acquire exact canonical-target kernel lock before reading the
  versioned journal; do not run on the Swing EDT.
- After real PNG/XYZ validation, write a private workspace ownership stamp
  bound to the stable SHA-256-derived lock identity. Force journal content
  into a same-parent temporary file then publish intent by ATOMIC_MOVE
  **before** renaming old output. This is not a portable directory fsync.
- On next attempt for that target, validate journal format and size <=512
  bytes; require a single safe stage basename and correct lock identity.
  Check stage marker, original target and any backup for full CATGIS ownership
  and structurally complete tiles. Never traverse arbitrary journal paths.
- Target missing + owned backup valid: atomically restore previous output.
  Target present and complete + owned staged/backup: preserve target, clear
  authenticated ephemeral stage only when state unambiguous.
  Target missing without valid backup, non-owned output, malformed/symlink
  marker, or several ambiguous survivors: leave all files and fail closed.
- Normal completion and caught errors clean only owned stage; clear journal
  after confirmed stage deletion. Failed rollback retains backup + journal.
- No new binaries, shell execution, secrets, or unbounded raster decode.

## Must verify in CI
- Interrupted after journal creation, before first directory rename.
- Crash after old->backup with target absent: old map/pixels restored.
- Crash after staged->target while owned backup remains: retain new map.
- Journal tamper, symlinks, traversal, malformed stage, missing backup,
  invalid target and hostile file: refuse and retain all candidate data.
- At least one **real child JVM Runtime.halt** after old->backup and
  recovery by the next JVM under a fresh kernel lock.
- Existing 13 tile transaction tests, all three native GDAL real tests,
  Java 17 Windows/Linux/macOS Intel/ARM, exact HEAD CI/Certification,
  authenticated previous main and post-merge push checks.

## Explicit residual limitations
Java 17 lacks a portable atomic directory exchange or directory fsync.
A real sudden power outage or unsupported network filesystem may reorder
metadata persistence even if journal file contents were forced; only JVM
process-crash recovery (not full power-loss durability) can be claimed.
Stale unjournaled staging from a crash during GDAL generation remains
untouched, never deleted without ownership evidence. No native Windows
OSGeo4W/JNI GUI is certified. Issue #73 and A0.7 stay OPEN for these.

## Transaction identity hardening

The journal and the **actual private workspace stamp** now both carry a
fresh 128-bit UUIDv4 transaction ID, validated in a fixed ASCII grammar.
A different or copied workspace marker for the same destination must be
rejected without deleting any raster. The random ID distinguishes accidental
crossed journal/staging state, but is not an authorization credential against
an attacker who can modify both files. The file lock and filesystem ownership
remain separate controls. Added a mismatched-transaction negative test.
