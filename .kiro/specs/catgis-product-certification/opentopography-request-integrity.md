# SPEC/Kiro #126 - immutable OpenTopography request extent

BASE: 65cf75a5796d6d1996851162452f8795fa43c8c5 (normal merge #125).
STATUS: CANDIDATE / NOT_CERTIFIED. A0.7 OPEN.

## Source-confirmed cause

OpenTopographyDemService used a shared unsynchronized DecimalFormat, silently
clamped coordinates to world edges, validated before six-decimal formatting,
and created destination directories before validating the request. NaN could
survive comparisons; infinities could become finite edges. A positive extent
could round to equal boundaries. With omitted project CRS, the caller's mutable
Envelope was read again after HTTP, allowing a different extent to be recorded.
Even without mutation, sidecar/result used unrounded values unlike the wire.
These are source-level defects and deterministic numerical counterexamples;
no corrupted user DEM or historical credential exposure is asserted.

## Minimal design and compatibility

- One pure Java helper owns final, already-formatted coordinate strings. Each
  request has its own US DecimalFormat with the existing six-decimal HALF_EVEN
  behavior. There is no shared mutable formatter, dependency or new GIS engine.
- Reject absent/nonfinite/zero-area source values and overflowed spans before
  projection. Copy source values, then use the existing projection service.
- Validate WGS84 longitude[-180,180]/latitude[-90,90] with no silent clipping.
  Quantize once; reject an extent collapsed at wire precision. Preserve valid
  dataset/query order, authentication encoding and six-decimal output.
- Complete planning and check cancellation before mkdirs or HTTP. Omitted/blank
  project CRS retains documented WGS84 longitude/latitude API compatibility.
- The existing result and sidecar workflow derive extent from the immutable
  effective request, not the caller's subsequently modified Envelope. The
  result record's credential-safe string representation from #125 is untouched.
- Do not modify #125's diagnostic/error/close contracts or older tests.

A JTS Envelope already orders its endpoints. This API cannot recover an earlier
reversed coordinate intent or infer a dateline-crossing request; neither is
claimed. Snapshotting stabilizes later caller mutations, not unsynchronized
concurrent mutation during the instant values are copied.

## Regression contracts

Sixteen new ordinary JUnit methods cover canonical URI parameters/key encoding,
all nonfinite axes, out-of-range/no-clipping, null/empty/zero extents, quantization
collapse, signed zero, original-geometry preservation, exact world edges,
1600 simultaneous independent URI builds, invalid input/key/dataset and projected
span-overflow with zero HTTP/mkdir, previous raster+sidecar byte preservation,
actual EPSG:3857->4326 one-degree numerical controls, invalid CRS, omitted CRS,
late caller mutation and wire/result/sidecar extent equality. Success uses a
real JDK TIFF encoder and reopening/sample checks. No public service, account,
real API credential, optional test, new dependency or workflow is introduced.

Supplementary isolated helper validation: 37 checks including 2000 concurrent
requests passed with javac --release17 on JVM21.0.12.1. Exact helper Git blob
3f0bf71aa504cedb278029506182f4b34b227056 matches those authored bytes.
This is NOT full application/GeoTools/JDK17/OS certification. New exact-head
GitHub CI and platform suites remain authoritative for all integration claims.

## Residuals, deliberately not hidden

This repairs REQUEST integrity, not the delivered raster's scientific validity.
The existing sidecar records request bounds but can still override different
embedded raster metadata; independent GeoTIFF-envelope validation and sidecar
precedence need another gate. Provider-specific coverage/size limits, dateline
splitting, strict-versus-lenient transformation policy, HTTP total/byte/decode
limits, redirect policy and atomic raster+sidecar publication remain open.

A valid world-edge request is not a claim that a provider serves that entire
area. TIFF samples in these contracts are real image bytes, not proof of remote
embedded CRS/extent. No physical GUI, user-PC, OpenCode or release is touched.

## Certification and decision

Require exact-head CI4/4, Certification15/15, metadata interop, predecessor-main
push attestation, then normal expected-head merge and fresh actual-main push
CI/Certification/interop and authenticated post-main attestation. The base's
post-main gates must close separately; do not substitute #125 candidate green.
Keep #126 open until this bounded scope is verified after merge; A0.7 stays open.

Primary references: Java17 DecimalFormat (Synchronization and rounding),
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/DecimalFormat.html
and OpenTopography Global DEM API: https://opentopography.org/developers .
