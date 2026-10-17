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
