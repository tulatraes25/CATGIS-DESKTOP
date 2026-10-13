# CATGIS — A0.4 / F2.7: bounded WCS in-memory compatibility API

State: CANDIDATE; A0.7 NOT_CERTIFIED.
Exact baseline: protected main after WCS #53, SHA
`9408d52d49b430b4fe19d6d86a103cc2ecfa8fc7`.
Branch: `quality/a0-wcs-bounded-inmemory-post53`.

## Root cause

`WcsClient.downloadCoverage(...): byte[]` remains public and internally uses
`fetchUrlBytes`, which formerly read unlimited server bytes into a growing
`ByteArrayOutputStream`. No Content-Length preflight, actual-byte limit or
empty/truncation check applied. An endless chunked response or large malicious
WCS provider could exhaust desktop heap. Non-200 HTTP responses and exceptions
also previously bypassed `HttpURLConnection.disconnect()`.

The regular GUI `WcsDialog` calls the *separate* streaming
`downloadCoverageToFile`, which must remain untouched; the byte-array API is
used by integration tests and may be used by plugins or other callers.

## Requirements

1. Bound encoded body retained by the byte-array API to **32 MiB**; reject
   larger declared HTTP Content-Length **before reading the response body**.
2. Bound actual streamed body independently when transfer length is unknown
   or a server lies. Do not buffer more than the hard limit plus a single
   8 KiB read.
3. Reject empty responses and a mismatch between declared and received byte
   lengths. Retain WCS 2.0.1 -> 1.0.0 fallback, including oversize failures.
4. Disconnect HTTP connections on every success and exception path.
5. Preserve byte-exact compatibility for normal small payloads and the public
   return type; direct large callers should use
   `WcsClient.downloadCoverageToFile` for streaming to disk.
6. No large test fixtures committed, no new dependency, no GUI or local
   Windows execution while A0.7 remains open.

## Deterministic acceptance

- Declared size 32 MiB + 1 rejects without calling InputStream.read.
- Unknown-length generated in-memory stream stops when crossing the limit,
  without preparing a giant fake fixture first.
- Empty and Content-Length truncated bodies fail closed.
- Valid small body round-trips byte-for-byte.
- Local HTTP v2 declares oversized coverage and v1 returns small body:
  fallback returns the exact valid v1 bytes without reading v2 body.
- Local HTTP successful small v2 response uses exactly one HTTP attempt.
- Exact head CI, Windows/Linux Certification, PostGIS, architecture and
  protected-main push attestation pass before merging.

## Honest limitations

- This is a **resource limit**, not a TIFF/GeoTIFF scientific validator.
  The in-memory API preserves raw encoded response semantics (including
  non-TIFF formats). The staged on-disk GeoTIFF path added in PR #53 is the
  user-facing structural protection.
- `ByteArrayOutputStream.toByteArray()` creates a copy, so actual transient
  heap use is greater than 32 MiB. This is a hard input-body limit, not a
  complete JVM heap consumption bound.
- The separate streaming-to-file WCS API is intentionally capable of
  handling larger outputs; disk quota/time limits remain separate A0.4 debt.
- Complete scientific GeoTIFF validation (CRS, GeoKeys, pixels, NoData) stays
  in issue #43. Do not declare A0.7 certified from this change.
