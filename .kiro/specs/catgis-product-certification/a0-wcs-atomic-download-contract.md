# CATGIS — A0.4 / F2.3: atomic WCS coverage download

Status: CANDIDATE — NOT_CERTIFIED
Exact certified parent: `22961526117bf2e090450cd837042241f23af64b`
Branch: `quality/a0-wcs-atomic-stream-download`

## Problem / root cause

`WcsClient.downloadCoverageToFile` fetched an entire WCS raster into a
`byte[]` before opening the destination. Real coverage files can be much
larger than available heap. A failed transfer must not erase or partially
overwrite a previously valid coverage, and a WCS XML exception report with
HTTP 200 must not be presented as a raster file.

## Requirements

- Stream the WCS response to a sibling temporary file with bounded buffer
  size; do not materialize the full raster as a `byte[]`.
- Preserve existing WCS 2.0.1 -> 1.0.0 fallback and URL encoding behavior.
- Reject failed HTTP status, XML/JSON/text error documents, empty downloads
  and payload length mismatches where Content-Length is supplied.
- Only after full success, replace the target via atomic move where supported,
  otherwise an ordinary replacement move. Always remove staging files.
- A failed transfer must preserve an existing destination byte-for-byte.
- Keep synchronous downloading off Swing EDT: WcsDialog already calls this
  API from a SwingWorker.
- Do not add external dependencies, large binary fixtures or remote services.

## Acceptance and verification

`WcsAtomicStreamDownloadContractTest` uses a loopback `HttpServer` and a
temporary directory, covering: a generated 3 MiB successful response and
replacement of old data, WCS 2.0.1 HTTP 503 -> WCS 1.0.0 fallback,
dual-version failure preserving existing file and cleaning staging, and an
HTTP 200 XML exception document that must never overwrite a raster.

These are HTTP/persistence contracts, not proof of scientific raster sample
preservation. The in-memory `downloadCoverage` API remains an unresolved
separate allocation-risk surface; A0.4 and A0.7 stay OPEN.

## Release gate

Exact-commit CI + Certification on Linux and Windows, real PostGIS integration,
source inventory, architectural controls, protected-main merge, and post-main
CI + Certification SUCCESS. No OpenCode or local Windows GUI before A0.7.
