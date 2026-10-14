# CATGIS SPEC/Kiro A0.7 — WCS bounded decoded-sample gate

Status: CANDIDATE / NOT_CERTIFIED
Issue: #43
Base main: `fe7e90d5b96aa684c224caf2efaabb752c0194ec`

## Causal defect
WcsClient already validates TIFF/BigTIFF magic, ImageIO dimensions,
GeoTiffReader CRS and requested geographic bounds. But headers and TIFF
image directories can be readable even when compressed strips/tiles are
truncated. Publishing without any actual sample read defers corruption
discovery until users render scientific data.

## Minimum change
Following metadata validation and before replacing the destination,
ImageReadParam decodes a 1x1 source region from each of four corners and
the center, refusing an unreadable image, missing band or unexpected
returned dimensions. Reuse the same TIFF ImageReader and close it before
filesystem publication. No unbounded whole-image BufferedImage allocation
is permitted in this gate; note that a compressed TIFF tile may still
require reading its full compression block internally.

## Negative/positive evidence
- Positive: genuine small EPSG:4326 GeoTiffWriter raster can be read
  through production validation without modifying bytes.
- Negative: shortened GeoTIFF response must never replace existing output
  even after WCS 2.0.1 -> 1.0.0 fallback; temporary staging is cleaned.
- Existing cases retain real multi-megabyte streamed GeoTIFF, axis/CRS,
  dimension, envelope and HTTP rollback tests.
- All four OS CI test suites, PostGIS and genuine GDAL native integrations
  must pass against exact PR head.

## Residual scientifically important limits
Sampling five pixels does **not** guarantee every strip, tile, band or
NoData cell is valid. TIFF reader implementations may buffer one whole
compression block. Large raster compression bomb budgets, full
pixel-integrity scanning, exact bands/sample types and scientific
NoData/coverage semantics need their own future validation.
No actual workstation file, private geodata or installation is touched.

## Merge gate
Draft PR; exact-head CI+Certification and predecessor-main authenticated
push attestation, controlled expected-head normal merge, and post-main
push CI+Certification with attestation. Issue #43 and A0.7 remain open
until full scientific and Windows GUI/native runtime acceptance.
