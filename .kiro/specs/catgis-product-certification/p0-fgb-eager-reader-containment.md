# SPEC/Kiro #175 — FlatGeobuf fail-closed eager importer containment

BASE_SHA: `3147cb9db8852f01726e4661ae351ce9242524e9` | STATUS: CANDIDATE / NOT_CERTIFIED

## Objective / evidenced root cause
Current loader allocated unbounded declared header size, allocated the entire
feature payload as a single direct ByteBuffer (up to 2GiB), preallocated
ArrayList by untrusted feature count, ignored short reads, skipped corrupt frames,
reported partial success, inserted fictitious geometry Point(0,0), and used a
shared unsynchronized static feature-ID counter.

## Scope and acceptance
- Bound eagerly loaded header to 4 MiB, entity payload to 128 MiB and
  reported feature count to 100,000; reject bigger professional files explicitly
  until a lazy indexed adapter is available. Do not report them as imported.
- Ensure full framed byte reads and reject short body reads, wrong frame lengths,
  trailing bytes, corrupt features and declared count mismatches with IO error;
  never return a partial ShapefileData as successful import.
- Do not manufacture Point(0,0) for missing geometry. Feature IDs are scoped to
  each load, independent of other threads/imports.
- Add tests using real FileChannel and sparse fake-file oversized-header fixture.

## Non-goals / remaining risks
This is safety **containment**, not a true multi-GiB FGB reader. The current
loader still allocates a <=128MiB buffer instead of streaming features.
Typed property bytes are not independently decoded and validated; see issue #175.
FGB format 3.13 compatibility and full cross-platform genuine geometry and
attribute fixtures remain required. Native/GUI scientific release claim barred.
No user data, local PC, OpenCode or production GIS data touched.
