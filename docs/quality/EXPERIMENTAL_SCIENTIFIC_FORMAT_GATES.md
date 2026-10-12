# CATGIS Experimental Scientific Format Gates

Status: ACTIVE / NOT_RUNTIME_CERTIFIED

## GRIB and GRIB2 — DISABLED_UNSUPPORTED

The former reflective GRIB loader could manufacture a grayscale checkerboard
even when scientific data extraction failed. That path was not a valid
GRIB raster and must never be presented as meteorological observations.

The GRIB loader is intentionally fail-closed: it does not create a raster
layer and informs the user that direct GRIB import remains unavailable.
The existing NetCDF flow remains a separate capability and must be assessed
on its own evidence.

**Unlock requirements** (all mandatory):

1. Reproducible small, authentic GRIB/GRIB2 test files with known cell values,
   dimensions, CRS, NoData and time axes.
2. A real decoder that preserves those values and never substitutes demo data.
3. Deterministic Linux and Windows validation on the exact tested SHA.
4. Error cases for unsupported templates, corrupt files and missing dependencies.
5. Explicit product matrix/status update and full architecture gate.

The `GribDataAuthenticityContractTest` rejects accidental reinstatement of
the fabricated-data path. Such a guard must only be updated alongside a
properly reviewed authentic-data contract.

## LAS/LiDAR — LIMITED SUPPORT (F2.7, IN CERTIFICATION)

Legacy uncompressed LAS 1.x point formats 0-3 require binary-level fixtures,
coordinate scale/offset, classification, intensity, optional GPS time and
16-bit RGB validation. LAZ/compressed LAS, waveforms and formats 4-10 remain
explicitly unsupported without an appropriate decoder and real-file evidence.

Passing automated binary contracts is not desktop runtime certification;
A0.7 and F3 remain blocking.
