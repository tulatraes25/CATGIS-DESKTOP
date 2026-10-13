# CATGIS — A0.4 decoded XYZ tile byte-budget hardening

Status: CANDIDATE / NOT_CERTIFIED
Exact base main: `44f9555a84044cc378c5943cc4904df900fe89c7`
Branch: `quality/a0-xyz-byte-budget-backing-and-concurrency`
Related: issue #50; integrated cache safety PR #47; A0.7 still OPEN.

## Audit and root cause

PR #47 legitimately integrated the old #44/#45 source-identity, bounded-input,
decoded-image dimensions, and LRU byte-budget fixes. The main tree now limits
the tile cache to 128 MiB of estimated decoded backing pixels and 512 entries.
Issue #50, created before that merge, is not a reason to duplicate work.

However, `imageDataBytes` originally computed only
`DataBuffer.getSize() * getNumBanks() * elementBytes`. That is a **logical**
sample count, not necessarily the capacity of the actual primitive array that
Java retains. For example, a `DataBufferByte` backed by a 4096-byte array
can report only 16 logical samples while the image holds all 4096 bytes.
Under-accounting lets multiple such images defeat the intended byte budget.

## Requirements

- For standard BufferedImage types, use logical DataBuffer size and pixel
  geometry without exposing backing arrays: DataBuffer.getBankData() can make
  Java2D mark an image as untrackable and disable acceleration optimizations.
- For TYPE_CUSTOM images only, account actual primitive bank array lengths
  (byte/short/ushort/int/float/double); retain a conservative offset-aware
  fallback for third-party DataBuffer types. This trades acceleration of
  custom rasters for correct physical backing-byte accounting.
- Never report less than logical sample storage or pixel-format geometry.
- Preserve the existing 128 MiB cache budget, 512-entry LRU count ceiling,
  all provider fingerprints, HTTP limits, EDT worker and repaint boundaries.
- Keep budget admission/eviction within the existing synchronized cache lock;
  no I/O or network operations under that lock. Avoid a second divergent cache.
- Document honestly that this is **backing-image-byte accounting**, not a
  mathematically complete JVM heap cap (object headers, transient decoder
  allocations and other caches are excluded).

## Deterministic acceptance

1. Backing array size 4096 versus logical size 16 must count as >=4096 bytes.
2. A single image larger than the budget must never remain retained.
3. Duplicate-key replacement with larger raster and explicit removal followed
   by insert must not carry stale byte debt into the next admission.
4. Access-order LRU must evict the least-recently-used image.
5. Six concurrent admission/removal threads cannot exceed a small aggregate
   pixel budget. Test without GUI/remote providers.
6. Exact candidate GitHub CI/Certification must pass on Linux, Windows,
   PostGIS and architecture/safety workflows.
7. Close issue #50 only after the candidate is merged and the exact new main
   commit has push-triggered CI/Certification evidence (the #42 protocol).
   Full A0.7 remains open until its other independent gates close.

## Explicit limitations / later verification

- Six simultaneous worker decodes can allocate transient raster memory
  outside the retained-cache limit; application-wide heap benchmark is R1.
- Standard BufferedImage constructors can theoretically wrap nonstandard
  physical banks even when reporting a standard type; the standard fast path
  assumes canonical JDK image allocation. The remaining application-wide heap
  and acceleration risk needs an instrumented runtime profiling gate.
- ImageIO plugin implementations might retain native or secondary allocations
  beyond standard DataBuffer arrays. Instrumented runtime profiling belongs
  to the later certified Windows stage, not a premature desktop release.
