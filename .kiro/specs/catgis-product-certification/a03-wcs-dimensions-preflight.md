# SPEC/Kiro A0.3 P2 - WCS dimensions form integrity and preflight (#94)

STATUS: SOURCE_CANDIDATE / NOT_CERTIFIED
Base main: f16f42d7482ed4d57de4dccd8d3dda92673136b2 (merged #105).
A0.7: OPEN. User machine C:\catgis untouched.

## Source root cause

WcsDialog.downloadCoverage parsed width/height using Integer.parseInt alone.
It accepted 0 and negative integers. WcsClient.buildCoverageRequestUrl
silently replaced nonpositive dimensions with 512. A user could enter
0 by -1 and see a network request for 512 x 512 with no warning.
Sizes above Integer.MAX were already invalid, but UI gave a generic error.

## Requirements / no-goals

1. Before opening JFileChooser and before download HTTP, require width/height
   that parse as positive Java ints, trim surrounding spaces, refuse malformed
   and out-of-range values with field-specific Spanish feedback and focus.
2. Keep 512 only as displayed default/legacy internal API fallback, not a
   silent correction of user-supplied form values.
3. Carry the exact validated dimensions through WcsClient request and
   raster grid metadata verification; do not implicitly round or clamp.
4. Risk preflight computes long pixel count and BigInteger bytes, preventing
   arithmetic overflow at Integer.MAX x Integer.MAX.
5. If illustrative uncompressed single-band 32-bit raster estimate exceeds
   the actual configured WCS network byte budget, ask explicit confirmation
   BEFORE the file chooser; cancel must create no files or HTTP traffic.
6. Treat estimate as guidance only: actual bands/compression/metadata and
   network-byte budget differ. Do not impose arbitrary pixel cap.
7. Invalid WCS byte budget configuration must fail with actionable error.
8. Keep original valid WCS v2/v1 fallback and streaming safety untouched.

## Tests

- Positive exact width/height values, trimming and tiny raster dimensions.
- Zero, negative, null, empty, fraction, scientific notation, non-numeric,
  huge integer overflow and signs rejected for BOTH width and height.
- 65536 x 70000, Integer.MAX x Integer.MAX overflow-safe estimates.
- Warning threshold relative to 256/512MiB actual configured wire budget.
- A rejected estimate confirmation is UI interaction pending native GUI;
  pure headless validator tests prove only core semantics.

## GitHub gates

- Exact candidate HEAD CI 4/4 + Certification 15/15 mandatory.
- Authenticate prior main SHA f16f42d7482ed4d57de4dccd8d3dda92673136b2
  event=push CI and Certification only AFTER these complete.
- No normal merge until all gates pass; expected HEAD SHA mandatory;
  then new post-main push CI, Certification and independent attestation.

## Explicit residuals

- BBOX, EPSG, numeric geographic extent, axis order require independent gate.
- WCS can still request large positive dimensions if user consents; real
  network byte quota/timeouts and native raster memory protection are
  independent requirements; warning is not a proof of server feasibility.
- Physical Windows/Linux/macOS Swing UI chooser/focus/confirmation workflow
  and large real native GeoTIFF integration remain NOT_VERIFIED.
- No source-only assertion closes all of issue #94 or architecture A0.7.
