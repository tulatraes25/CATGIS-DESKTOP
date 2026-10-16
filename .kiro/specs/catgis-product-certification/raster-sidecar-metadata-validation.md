# SPEC/Kiro #128-A - bounded truthful sidecar metadata

BASE: 65cf75a5796d6d1996851162452f8795fa43c8c5 (merged PR125).
STATUS: CANDIDATE / NOT_CERTIFIED. Full issue128 and A0.7 OPEN.

## Cause and bounded tranche

RasterSidecarSupport read missing coordinates as NaN and checked only JTS
Envelope.isNull. It could silently reorder reversed serialized bounds, accept
zero/nonfinite extents, read unbounded Properties input, and turn a missing CRS
into EPSG:4326 through the normalization API's documented-in-code default.
Writes could truncate an existing sidecar before rejecting invalid metadata.
No personal raster, credential or historical incident was inspected.

This tranche makes that optional metadata boundary honest. It does not claim
to solve the separate higher-level precedence between sidecars and GeoTIFF
embedded metadata. The latter is tracked by issue128-B and must remain open.

## Minimal design

- Validate each serialized min/max BEFORE constructing a JTS Envelope: finite,
  increasing axes and finite differences. Do not constrain generic raster
  coordinates to longitude/latitude bounds: projected values remain valid.
- Require a nonblank declared source CRS in read AND write. Normalization is
  retained but never called for unknown values. It is NOT semantic decoding,
  authority verification or evidence that the pixel grid matches that CRS.
- Read at most64KiB+1 into bytes and reject oversize before Properties parsing.
  Keep the standard existing Properties encoding/escaping and optional-read
  null result for absent/malformed/unsupported metadata. No in-place repair.
- Snapshot valid write coordinates and validate before mkdirs or opening output.
  Bound the source CRS character count and actual encoded Properties byte size;
  escaped non-ASCII content cannot produce an unreadable successful write.
- Preserve version1, path naming, positive geographic/projected coordinates,
  extra legacy Properties fields on reads and null/empty optional-write no-ops.
- Keep the same public API/record. No new engine, runtime dependency, network,
  global CRS normalization change, existing test or workflow modification.

## Fourteen genuine filesystem contracts

RasterSidecarValidationContractTest covers geographic/projected roundtrips,
real TIFF byte/pixel preservation and handle release, every missing/nonfinite
axis, reversed/zero/overflowed serialized bounds, unknown CRS, versions/bad
numbers/malformed Properties, exact65536 versus65537 byte boundary, rejected
write preserving old raster+sidecar and not creating new directories, explicit
write CRS, encoded-versus-character size, optional no-ops, absent/empty/directory
sidecars and Unicode paths/escaped legacy fields. No skips or disabled tests.

The small TIFF is produced and reopened by ImageIO; this verifies bytes and
pixels, not embedded GeoTIFF metadata. All older tests stay intact. No full
JDK17/application execution has occurred locally; require actual GitHub results
before reporting any integration PASS.

## Remaining scientific and storage requirements

The reader still follows existing ordinary file-path semantics; it is not a
hostile symlink/TOCTOU sandbox. Byte bounds do not guarantee filesystem read
latency. Properties duplicates retain its standard last-value behavior. The
public Envelope record is not deeply immutable. Valid writes retain the old
non-atomic output operation; an actual I/O failure may still partially replace
a sidecar. Raster+sidecar publication needs its own dataset transaction design.

Most importantly, RasterCoverageSupport still can replace embedded georeference
with sidecar values. Request bounds and actual returned-grid bounds are not
interchangeable. Before closing issue128, introduce an explicit provenance and
precedence policy, preserve legitimate plain-image/user-georeference workflows,
and test conflicting real GeoTIFF CRS/envelopes and unchanged scientific samples.
PR127's immutable request fix is orthogonal and is not reimplemented here.

## Gates

Exact candidate CI4/4, Certification15/15, metadata interoperability and
predecessor-main push attestation; normal expected-head merge only, then fresh
actual-main CI/Certification/interop and authenticated post-main evidence.
No user PC, OpenCode, GUI, credential, release or local installation is changed.
