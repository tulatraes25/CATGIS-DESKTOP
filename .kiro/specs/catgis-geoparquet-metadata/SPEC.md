# SPEC/Kiro #112 - genuine GeoParquet metadata, tranche M1

Status: IMPLEMENTED_CANDIDATE / NOT_CERTIFIED. A0.7 and #112 remain OPEN.
Base: f59939e2eba6294c3c96a9ac6323d5442a94d55c (normal merge of #116).
Requirements: REQ-PC-2, REQ-PC-4, REQ-PC-5, REQ-PC-7, REQ-PC-8, REQ-PC-11.

## Root cause and decision

The old reader only checked the first PAR1 bytes, ignored footer validity, and
returned constant Point/EPSG:4326/world bounds/geometry-value columns and a
file-size-derived row estimate. Three old tests legitimized synthetic invalid
containers. This is metadata fabrication, not a real format adapter.

Use the official Apache Parquet format-structures 1.17.1 decoder with its
explicit maximum-message-size API, and Jackson 2.21.7 LTS with bounded strict
JSON. Do not implement Thrift/Parquet decoding by hand. Compared with the
GDAL Parquet adapter, this metadata tranche requires no optional Arrow-enabled
GDAL driver or process execution. Compared with parquet-hadoop, it does not
bring a Hadoop/Avro data-page ingestion stack into the application. Neither
choice would itself certify full row/geometry ingestion.

Apache Parquet and Jackson project artifacts are Apache-2.0; retain upstream
notices and inspect the resolved transitive dependency/license inventory before
distribution. No third-party source or native binaries are copied into git.
PyArrow 21.0.0 and pyproj 3.7.2 are CI fixture-writer dependencies only, not
application runtime dependencies. Their older pinned versions are deliberate
interoperability oracles, not assertions about the latest upstream release.

Primary references reviewed:
- https://geoparquet.org/releases/v1.1.0/
- https://parquet.apache.org/docs/file-format/
- https://gdal.org/en/stable/drivers/vector/parquet.html
- https://github.com/apache/parquet-java/blob/apache-parquet-1.17.1/parquet-format-structures/src/main/java/org/apache/parquet/format/Util.java
- https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.21
- https://github.com/apache/arrow/blob/apache-arrow-21.0.0/cpp/src/parquet/column_writer.cc

## Contract

1. Read-only local regular files. Check both PAR1 signatures and unsigned
   little-endian footer length before decoding. Reject encrypted/footer-signed
   or external-column-file inputs, missing required fields and unsupported
   versions/encoding with an error, never an invented successful report.
2. Footer at most 4 MiB; the Apache decoder transport is additionally limited
   to the actual footer length. JSON at most 256 Ki characters, nesting 32,
   numeric token length 64; no duplicate JSON keys or trailing JSON values.
   Bound schema fields, schema nesting and row-group count. Preserve interrupt
   status and close handles on every ordinary success/failure/cancel path.
3. Read schema column names, row count, geo version/primary column and all
   declared geometry-column metadata. Match row-group counts, schema paths and
   physical types and reject data chunk extents outside the file body. Do not
   deserialize any referenced external path. Zero-row/zero-value columns may
   have an absent data page (offset zero) and a bounded empty dictionary page;
   the same zero offset is forbidden for nonzero rows/values. Absent pages
   without a dictionary cannot claim compressed or uncompressed page bytes.
4. Support metadata for GeoParquet 1.0.0 and 1.1.0 WKB root-level, non-repeated,
   unannotated binary geometry columns. Text/decimal/other logical annotations,
   including newer Parquet spatial logical types, require a separate explicit
   compatibility contract; do not silently reinterpret them as WKB. Unknown
   native-GeoArrow encoding is explicitly unsupported. Missing bbox/types stay
   unknown, not world bounds or Point. Attribute-column annotations remain legal.
5. CRS absent means OGC:CRS84 (longitude, latitude); explicit null stays unknown;
   object PROJJSON is retained as a DECLARATION, not silently converted to an
   EPSG label or reprojected. Preserve 2D/3D bbox and geographic antimeridian
   orientation. Optional epoch/covering declarations remain available as JSON.
6. Metadata collections and bounds are defensive snapshots. Preserve the seven
   legacy metadata accessors and constructor while exposing geometry-column
   declarations. No input geometry, feature, file or project is modified.
7. Summary explicitly says METADATA ONLY: no layer loaded, no row-page/WKB
   geometry validation. File row count/extents/types remain declarations rather
   than computed facts. isGeoParquet means supported metadata detection only.

## Verification design

- 18 ordinary JUnit metadata contracts: the initial 14 in
  GeoParquetMetadataContractTest plus 4 in GeoParquetEmptyColumnContractTest.
  Genuine empty Parquet footers/dictionary page headers use the official writer;
  adversarial variants test headers/footers/JSON/schema, row-count consistency,
  CRS distinctions, 2D/3D/antimeridian bounds, immutable values, input integrity,
  interruption, repeated handle cleanup, empty-page rules and logical-type gating.
- Preserve all existing test methods. Strengthen three GeoParquet expectations
  in PmtilesGeoParquetStateTest and HeavyFormatsIntegrationTest. Reject their
  fake-container premise and use genuine empty footers for successful summaries.
  Do not alter unrelated PMTiles/LAS/GDAL assertions.
- Four ADDITIONAL mandatory interop tests use ten tiny files independently
  produced by PyArrow, with real point/polygon WKB pages, projected PROJJSON,
  geographic defaults, explicit-null CRS, multiple geometry columns, Unicode,
  typed/null attributes, multiple row groups, zero rows and malformed examples.
  PyArrow also reopens each generated Parquet file and compares actual rows.
  Java inspects METADATA only; this is not Java row/attribute ingestion evidence.
- geoparquetInteropTest runs only the new geoparquet-interop tag; normal suites
  retain all their previous tests/skips. The dedicated workflow is mandatory,
  verifies exactly 4 tests / 0 failures / 0 errors / 0 skips and hashes every
  source fixture afterward. A missing generator/fixture is FAIL, never a skip.
- Keep CI 4/4 and Certification 15/15 intact, with unchanged coverage/skip floors.
  Add GeoParquet Independent Metadata Interop as a further required gate.
  Post-main attestation requires that workflow once present in protected main,
  but does not demand it retroactively of a predecessor without this code.
- Review full diff and exact synthetic checkout tree, then normal expected-head
  merge only after all candidate gates pass. Fresh actual-main push evidence
  and independent attestation are mandatory after merge.

## Review and independent reproduction before merge

The first workflow draft had two configuration errors: runner context was used
at job-env scope and a plain YAML run scalar contained the colon-space in
--only-binary=:all:. Both were corrected without disabling the new gate.

Independent interop run 38778409487, job 116571946178, executed on synthetic
checkout 3941d7baf34a428261db6786f695116440619258 for candidate
bd5d13b3a7ecc65f3c0cbe0c7e989c82c2e230c1. The Arrow generator created/reopened
9 genuine Parquet files and one deliberately truncated copy. Java compiled
and three interop tests passed, but the real zero-row file FAILED: an absent
data-page offset of zero was incorrectly treated as an address in the header.
The failure is retained as evidence; it is not a green certification run.

Apache Arrow 21.0.0 SerializedPageWriter initializes data_page_offset_ to zero
and may emit only an empty dictionary. The corrected candidate distinguishes
absence of a data page only when both row and value counts are zero, while
retaining dictionary/span bounds and rejecting zero offsets for nonempty data.
Four extra ordinary tests protect this exception and explicit logical-type
compatibility. Existing independent empty-file assertions remain unchanged.
All new-head mandatory gates must execute again; earlier partial results do
not certify this corrected source.

## Explicit non-claims and remaining work

M1 is NOT full GeoParquet ingestion and does not close #112. It reads no row
pages, constructs no Java feature/attribute table, validates no actual WKB
geometry or actual versus declared extent, and performs no CRS transformation.
PROJJSON is shape-checked and retained, not validated against its complete
schema or reconciled with every CRS authority/axis/unit definition. Arbitrary
malicious Thrift fuzzing and measured worst-case heap/time remain separate
security/performance work; byte/work caps are not an absolute heap guarantee.
Same-size concurrent hostile file modification is not a snapshot guarantee.

OpenFileAction still has its existing synchronous double metadata read and
returns false because no layer was loaded. The new summary is truthful, but
this tranche does NOT certify that UI path as responsive; a bounded async
coordinator, progress/cancel and clean no-dialog semantics are still needed.
No UI action, semantic feature count, project persistence, native GDAL package,
user-PC file, release or historical branch is modified here.

## Task state

- [x] Inspect exact source and replace fabricated metadata in candidate code.
- [x] Author ordinary and independent real-file tests, without stored binaries.
- [x] Add bounded isolated interop workflow and predecessor-aware attestation.
- [x] Reproduce and correct the independent zero-row interoperability defect.
- [ ] Execute and review corrected exact-candidate CI/Certification/Interop results.
- [ ] Normal merge and fresh main push certification/attestation.
- [ ] Async metadata UI and genuinely typed geometry/attribute ingestion.
- [ ] Full #112 acceptance, architecture A0.7 and later physical GUI validation.
