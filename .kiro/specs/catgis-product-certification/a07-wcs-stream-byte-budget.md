# CATGIS SPEC/Kiro — A0.7 P1 — WCS encoded response byte budget (tranche A)

Status: DRAFT / NOT_CERTIFIED
Issue: #86
Parent main: `a72bc99b907e0fbed1530ed3938793d1c6c8c06c` (post PR #85)
Scope: encoded on-disk streaming budget, not a full network/decode resource certification.

## Objective and verified root cause

`WcsClient.streamCoverageVersion` previously looped over the HTTP response body with no maximum
wire-byte count. A server that omits Content-Length, uses chunked transfer encoding or sends
an enormous body can exhaust workstation storage before TIFF validation is reached.
Socket read timeout alone is not a total-operation time budget.

## Requirements / acceptance

1. Set a conservative default **512 MiB** combined response budget for a WCS download operation.
   An explicit local Java system property `catgis.wcs.maxDownloadBytes` may configure a
   **whole-number bytes** ceiling between **1 MiB and 16 GiB**, inclusive. Reject invalid
   configuration early. These are encoded-byte limits, not pixel-grid or disk-free guarantees.
2. Share one remaining-byte budget across WCS v2 and v1 fallback; a failed/invalid v2 payload
   does not reset the allowance. Reject declared Content-Length above remaining allowance
   before consuming the body, and enforce the cap **before writing** each received block,
   even for unknown/chunked Content-Length. Use overflow-safe subtraction.
3. Reject violation via IOException. Preserve the previous destination byte-for-byte,
   close response streams, disconnect, and remove the operation's own staging file.
   Keep existing TIFF storage/CRS/grid validation, v2->v1 fallback, HTTP error handling,
   and successful atomic path unchanged.
4. Add tiny deterministic HTTP and genuine GeoTIFF fixtures proving: no-length chunked
   over-budget response, oversized declared response, exact-budget valid GeoTIFF,
   aggregate consumed bytes across a failed v2 + valid v1. Verify a path with spaces,
   original destination preservation, no staging remnants, and fallback request count.
5. Exact SHA CI, four-platform Certification with native GDAL/PostGIS, authenticated
   predecessor main push run, normal expected-head merge, then new main push CI/Certification
   and authentication. Keep feature freeze and don't touch user desktop.

## Explicit residuals

- **Tranche B still OPEN in #86:** a monotonic total-operation time budget / cancellation,
  slow trickling server, thread interruption, and cross-download concurrent disk quota.
- Disk free-space limits and global cache quota are not fully managed by this byte budget.
- Decoder compression bombs/heap bounds, TIFF NoData/whole-image scientific verification
  remain OPEN in #43; atomic unsupported filesystem behavior and cross-process destination
  locking remain OPEN in #87.
- This tranche does not certify GUI, Windows native OSGeo4W or scientific raster semantics;
  `A0.7 = OPEN` regardless of green CI.

## Decision

Merge only after all exact candidate and authenticated predecessor-main checks PASS.
Document `TRANCHE_A_BYTE_BUDGET=CLOSED_CERTIFIED` only after post-main evidence,
not from a draft PR or a green unit test alone.
