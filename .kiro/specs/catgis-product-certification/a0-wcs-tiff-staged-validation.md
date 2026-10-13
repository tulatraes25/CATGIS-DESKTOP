# CATGIS — A0.4 / F2.7 WCS staged TIFF structural validation

Status: CANDIDATE / NOT_CERTIFIED
Base main: 44f9555a84044cc378c5943cc4904df900fe89c7
Branch: quality/a0-wcs-tiff-structural-publication-guard
A0.7 OPEN; user-facing desktop runtime remains frozen.

## Confirmed security and scientific-integrity gap

The merged PR #41 transport code rejects empty, truncated, XML/JSON-like
HTTP responses but does not inspect raster bytes before publishing them.
The existing WcsAtomicStreamDownloadContractTest accepts a random 3 MiB
byte pattern and a 5-byte payload as successful image/tiff downloads.
Thus CATGIS could publish a bogus .tif that cannot be opened and lose a
previously valid file. The WCS desktop dialog specifically requests
image/tiff and writes to .tif, so this affects the normal user path.

## Required behavior and constraints

1. For the default/explicit TIFF-family request, inspect the staged sibling
   file before replacing the destination. Validate TIFF/BigTIFF magic and
   require an available Java ImageIO reader to report positive image
   dimensions; reject inconsistent requested dimensions.
2. Validation is metadata-only, no full pixel-buffer decode of an
   unbounded remote raster. ImageIO reader and stream must be closed/disposed.
3. Any invalid TIFF attempt must trigger the existing WCS 2.0.1 to 1.0.0
   fallback. If both fail, preserve the old destination byte-for-byte and
   remove all .catgis-wcs-*.part files.
4. Preserve streaming in 64 KiB chunks, HTTP status/type/prefix protections,
   Content-Length checks, read/connect timeouts, same-directory staging and
   atomic rename preference. No synchronous Swing EDT work.
5. Non-TIFF output formats remain outside this tranche. Do not label
   NetCDF, JPEG2000, JPEG or PNG bytes as TIFF and do not block explicitly
   requested alternative formats with a TIFF-specific signature guard.
6. Convert synthetic successful binary fixtures to generated real
   GeoTIFFs using preexisting GeoTools writers. No large binary files checked
   into Git. Keep negative fake data fixtures as deliberate attacks.

## Acceptance criteria

- Real multi-megabyte GeoTIFF generated during the test is streamed and
  replaces the destination exactly, with correct reported dimensions.
- Real tiny 16x16 GeoTIFF downloads successfully after WCS 2.0.1 failure
  and fallback to WCS 1.0.0.
- A random nonempty body and a TIFF-header-only body advertised as
  image/tiff cannot replace an existing destination.
- A real TIFF with dimensions differing from a requested 16x16 grid is
  rejected before publication; no temp files remain after both attempts.
- Validation streams and readers must be closed deterministically on
  Windows; generated GeoTIFF test readers must not retain handles on JUnit
  temporary output. The fallback test validates with the metadata-only
  production helper, not a lazy file-backed GeoTools coverage.
- All existing XML/JSON disguised-body, Content-Length, rollback and cleanup
  tests remain green. Linux and Windows must both pass on exact candidate
  head, including PostGIS/architecture CI and PR-base push attestation.
- After merge, obtain new main CI and Certification push evidence. Keep A0.7
  and the broader scientific validation issue #43 OPEN.

## Explicit residual risks

TIFF header + ImageIO structural dimensions do not prove that every
pixel/strip decodes correctly, that the CRS is valid, or that its world
envelope, NoData or bands match the requested WCS coverage. Full scientific
validation needs bounded GeoTIFF metadata/GeoKey inspection and genuine
geospatial interop tests (issue #43). Do not claim scientific 100% from these checks.
