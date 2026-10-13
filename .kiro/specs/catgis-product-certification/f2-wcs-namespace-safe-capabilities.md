# CATGIS — F2 WCS GetCapabilities interoperable, bounded and safe

Status: CANDIDATE / NOT_CERTIFIED
Parent main: `845c04451d65e7e536d0d61e979bb5c7045b57d5`
Branch: `quality/f2-wcs-namespace-secure-capabilities`
Architecture: A0.7 OPEN, no local GUI/OpenCode release.

## Root-cause evidence

The production `WcsClient.getCoveragesVersion` performs case-sensitive string
searches such as `<CoverageSummary` and `<CoverageOffering`. Valid OGC
responses use qualified names (`wcs:CoverageSummary`, `ows:Title`,
`wcs:CoverageId`) and can return zero inventory entries with no parse error.
The caller then tries an older version, potentially hiding a fully operational
WCS 2.0.1 server. `fetchUrl` also grows an unrestricted StringBuilder on
untrusted GetCapabilities HTTP input.

## Objectives and non-goals

1. Use the preexisting hardened OGC DOM parser
   `OgcCapabilitiesXml.parse(input, false)` and match local tag names
   independent of namespace prefix; reject all DOCTYPE/entity payloads.
2. Preserve WCS 2.0.1 first and 1.0.0 fallback, plus coverage name/title,
   description, supported formats and native resolution where provided.
3. Limit the capabilities response to 4 MiB before XML parsing: reject an
   oversized Content-Length immediately and stop chunked/unknown-length bodies
   after a bounded 4 MiB + 1 read. Keep existing connect/read timeouts and
   enforce HTTP 200 for capabilities.
4. Never extract credentials, read local files from XML or issue external XML
   entity fetches. Keep all tests offline and deterministic via loopback HTTP.
5. This tranche does not alter raster GetCoverage bytes, TIFF validity, URL
   subset/axis-order semantics or GeoTIFF scientific certification (#43).
6. No new library dependencies, downloads, binary fixtures, interface controls
   or runtime launch.

## Acceptance scenarios

- WCS 2.0.1 `wcs:CoverageSummary`, `wcs:CoverageId`,
  `ows:Title`, `ows:Abstract` and `wcs:SupportedFormat` yield correct
  values and **one** HTTP request; no unjustified WCS 1.0.0 fallback.
- Namespaced WCS 1.0.0 offering is recognized after the 2.0.1 endpoint fails.
- A 200 response containing malformed XML or a DOCTYPE/entity is rejected;
  no untrusted service URL or local URI is dereferenced.
- An oversized declared body and an oversized streamed body with unknown
  Content-Length cannot be parsed or retained unboundedly in memory.
- HTTP failure or empty body cannot be silently treated as a valid empty
  coverage catalogue.
- Exact candidate CI and Certification: Linux, Windows, real PostGIS and
  architecture guardrails, plus exact-base main push attestation on
  `845c04451d65e7e536d0d61e979bb5c7045b57d5`.
- A protected-main merge requires SHA lease and post-main CI/Certification
  evidence; passing this tranche does not close A0.7 or #43.

## Failure/recovery

Invalid data is rejected with a bounded exception and version fallback remains
available. The existing return type and public API are unchanged. Limits may
exclude unusually large legitimate WCS capabilities documents; that is an
explicit fail-closed tradeoff pending a future streaming parser tranche.
