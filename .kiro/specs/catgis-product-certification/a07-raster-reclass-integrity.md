# SPEC/Kiro #140 - analytical raster reclassification integrity

BASE: f7e0a43155a92f9d2a0062ffe6e8c6749a1a1237
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## User/scientific defects

The current reclassifier forced every result through TYPE_BYTE_GRAY. Negative,
fractional and >255 classes therefore could not be trusted. The batch rule
parser split on "-" and could not parse signed intervals such as -5--1=7.
NoData handling differed between classification/statistics/fill, all-NoData
statistics could expose Double.MAX_VALUE-like sentinels, and TYPE_CUSTOM
FLOAT/DOUBLE fill paths were unsafe.

## Design

- Analytical reclassification outputs one-band DOUBLE samples. Display styling
  remains a separate concern.
- ReclassRule validates finite ordered ranges.
- RasterReclassRuleParser accepts signed decimal/scientific numbers, is bounded,
  rejects malformed/blank/nonfinite/reversed rules, and preserves authored order.
- Range endpoints remain inclusive and first matching rule wins, preserving the
  previous overlap behavior but making it explicit and testable.
- NoData uses exact declared finite sentinel identity; NaN/Infinity samples are
  always invalid analytical samples. NaN can be the declared output sentinel.
- Statistics use one-pass Welford accumulation and return NaNs with count=0 when
  no valid samples exist.
- fillNodata returns the original image if no gap exists; if interpolation is
  needed it uses DOUBLE so integer inputs cannot truncate fractional means.
- Batch parsing delegates to the strict parser. An end-to-end TIFF test requires
  -5.5, 300.25 and 1000 to survive write/reopen.

## Explicit residuals

This tranche does NOT claim geospatial TIFF publication: direct ImageIO output
still does not certify CRS/grid/GeoKeys/NoData metadata or atomic replacement.
Those remain #128/#114 follow-up gates. fillNodata still materializes an
in-memory snapshot per pass and is not a multi-gigabyte streaming algorithm.
Spectral calibration remains #139.

Require exact-head CI, Certification, Runtime Identity, GeoParquet metadata and
authenticated predecessor-main evidence before merge; then fresh merged-main
evidence. No user PC/OpenCode/release changes.


## Main synchronization

This candidate was re-synchronized after PR #138 advanced protected main to `f7e0a43155a92f9d2a0062ffe6e8c6749a1a1237`. All earlier green runs on `62285be0a2fb7ffa50b174c8cfcbda91731165ba` are historical evidence only and cannot authorize merge of the new synchronized head. Fresh exact-head gates are mandatory.

