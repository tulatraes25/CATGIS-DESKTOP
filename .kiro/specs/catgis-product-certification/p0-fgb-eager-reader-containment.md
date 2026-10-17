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

## B — official 8-byte magic, no filesize heuristics

Independent FlatGeobuf spec confirms `fgb[major]fgb[patch]` is exactly 8
signature bytes, not a version-specific 4-byte GDAL 3.13 addition. Previous
code guessed whether to consume four extra bytes from file-size plausibility,
so large official files could be misread as an enormous header. A strict
HeaderPrefix reader now consumes the two halves for FGB major version 3,
and accepts only the existing legacy CATGIS 4-byte magic separately. It never
guesses from file size. Tests include a 32 MiB sparse official-signature
file and malformed trailing magic to prevent silent ambiguity.

## D — exact packed R-tree size and unknown header count

The previous ad-hoc formula skipped the leaf level and could position
the feature reader at incorrect bytes. Compute all packed levels, including
leaf entries and parents, each exactly 40 bytes, with checked arithmetic
and node size 2..65535. Verify count=1/16/17/100 with branching 16.

For files with declared feature count zero (unknown), enforce the same
100,000 decoded-feature maximum inside the parser, not only at the header.

Still no lazy/indexed multigigabyte adapter and no certified typed-property
codec. Keep #175 OPEN.

## E — lossless typed FlatGeobuf feature attributes (candidate)

The previous `readColumnValue` treated `Feature.properties(i)` as the
i-th numeric field; this is a byte vector of indexed, typed entries.
The new bounded decoder understands per-column u16 keys + little-endian
values across all 15 official ColumnType variants, including signed vs
unsigned widths, 64-bit unsigned integers as BigInteger, strict UTF-8,
date-time/JSON text and exact binary arrays. Missing keys stay null.
Truncated/duplicate/unsupported fields or type conversion errors abort
the WHOLE import instead of setting null and reporting success.
Per-feature independent schema overrides are explicitly unsupported
(fail closed); raw attribute payload limited to 8 MiB per feature.

Unit contracts cover missing/null, UTF-8, JSON/DateTime, UInt/ULong,
signed values, binary, malformed lengths, invalid bool/UTF-8 and repeated
keys. True end-to-end GDAL-generated fixture and indexed multigigabyte
streaming remain separate acceptance gates; #175 is still OPEN.
References: https://flatgeobuf.org/ and
https://github.com/flatgeobuf/flatgeobuf/blob/master/src/fbs/feature.fbs.

## F — independent GDAL real-file acceptance gate

Native GDAL on Linux, macOS Intel and macOS arm64 now must execute a
THIRD JUnit tagged `native-gdal` test (no skip permitted).
`FlatGeobufNativeGdalPropertiesIntegrationTest` creates a tiny GeoJSON
fixture with UTF-8 names, signed decimal and optional null attributes,
writes actual indexed AND non-indexed .fgb with OS-installed ogr2ogr,
then verifies the CATGIS loader's feature count, typed numeric/string
values and original point coordinates. Installed GDAL executable
must be available from trusted `CATGIS_GDAL_BIN` and each command
has a 90-second hard timeout. No user data/files/secrets involved.

Certification matrix strictly changes expected native-GDAL JUnit
accounting from 2 to 3 total, zero skip/failure/error across all three
supported host OS architectures. If this reveals parser divergence,
reject the PR and fix the real format—not waive/skip the test.
Still no indexed gigabyte-scale reader or Windows native GUI claim.
