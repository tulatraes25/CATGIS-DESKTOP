# SPEC/Kiro — LAS header integrity subgate for #176

BASE_MAIN: `303b27b4f5ba18a4f89bb90ea1cb3333c15ab617`
STATUS: DEVELOPMENT_BRANCH / NOT_CERTIFIED

## Root causes
- LAS v1.3 and v1.4 headers were accepted with only 227 bytes. The
  v1.4 extended point count is read at offset 247, requiring 375 bytes.
- Point count was not verified against point-data offset, declared
  record length and actual EOF before a prefix of points was loaded.
- Non-finite/zero scales and invalid min/max extents were accepted as
  projected GIS coordinates.
- Files named .laz could enter the uncompressed LAS parser.

## Containment
Reject malformed/truncated version-specific headers, point-data
count * recordLength overflow/EOF, invalid affine scale/offset/bbox,
and compressed LAZ extension without a certified codec. No user
datasets or production DB. Unit tests use synthetic LAS 1.2/1.3/1.4.

## Explicit residual
This is header integrity ONLY. The LayerVectorDataSupport first-50k
sample still masquerades as full feature count; #176 stays OPEN until
typed PREVIEW/COMPLETE provenance and full streamed LiDAR are built.
Likewise unknown CRS must be explicit (#151). No release claim.

## Gate
Open draft PR only when earlier safety candidates finish competing
native matrices; require exact-head CI/Certification/Runtime+GeoParquet,
authentic predecessor-main atest, merge guard and post-main checks.

## Tranche B — no silent first-50,000-point false completeness

The generic layer loader previously called `LasReader.readPoints(file,
50000)` then published those point features as a successful layer
while the header could claim millions. `ShapefileData` had no typed
PARTIAL_PREVIEW provenance, and GIS selections/exports could silently
misrepresent the full source. Until a truthful streamed/preview-backed
design exists, the generic loader now requires declared point count
<=50,000 BEFORE reading any points, rejects larger files with an explicit
localized message, verifies all declared records were decoded, and
reports N/N complete only for genuine complete imports. The low-level
`readPoints(maxPoints)` preview API remains available to future
explicitly scoped preview screens, but it must not attach itself as a
complete project vector layer.

This intentionally trades large-LAS viewing availability for data
integrity until the dedicated streaming/typed-PARTIAL_PREVIEW subgate
is built. That full capability remains OPEN in #176.
