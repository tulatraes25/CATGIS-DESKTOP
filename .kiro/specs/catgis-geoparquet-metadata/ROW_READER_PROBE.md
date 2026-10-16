# SPEC/Kiro #112 - executable local GeoParquet row-reader decision

BASE: d10ad77247e47755b7da35bf6460604a396e8188 (normal merge #121).
STATUS: RESEARCH_IMPLEMENTED / EXECUTION_PENDING / NOT_PRODUCT_CERTIFIED.
A0.7: OPEN. No user PC/OpenCode/GUI/release actions.

## Goal and reason

M1 inspects real metadata; M2 keeps its existing open flow off EDT. Neither reads
row-page geometries/attributes into a layer. Stop treating further metadata UI
work as a substitute for actual ingestion. This bounded experiment tests a
candidate maintained decoder against independently generated Arrow files BEFORE
adding libraries to the desktop distribution or claiming usable layer support.

## Isolation and candidate

A standalone Gradle project under tools/qa/geoparquet-reader-probe compiles its
two probe classes and the ACTUAL unchanged current GeoParquetReader. It does not
include the desktop settings/build, Main class, UI or project state. No existing
production source, dependency, workflow or test is changed.

Apache parquet-hadoop/column/format-structures 1.17.1 and Hadoop client API/runtime
3.4.3 are pinned comparison baselines, NOT claims of latest release or minimum
footprint. Jackson 2.21.7 matches the application metadata line; JTS 1.20.0 decodes
WKB. A Hadoop cluster is not used. Record the complete resolved JAR inventory,
byte sizes, SHA256 and available LICENSE/NOTICE entry names. This inventory is
not a vulnerability assessment or completed legal/distribution clearance.

A plain configuration does not magically remove Hadoop client class references.
This experiment measures whether the controlled local-file path actually works
and how much the transitive client costs. GeoTools/DuckDB and a verified GDAL
Parquet driver remain alternatives if this footprint or behavior is unsuitable.
No native extension download, external server or Python runtime is added to the
installed application: Python/Arrow/PROJ here are only independent fixture tools.

Primary source references reviewed: Apache parquet-java tag apache-parquet-1.17.1,
ParquetReadOptions, HadoopCodecs, PageReadStore and DelegatingSeekableInputStream;
https://geoparquet.org/releases/v1.1.0/ ;
https://gdal.org/en/stable/drivers/vector/parquet.html .
Third-party implementation is consumed as a dependency, not copied into CATGIS.

## Explicit small-file contract

Read a regular local no-follow file (12 bytes to 4 MiB), first validating M1
metadata. Use official ParquetFileReader with row-group/page decoding and page
checksum verification. No hand-written Thrift, compression or Parquet decoder.
Use bounded original local I/O with Apache's DelegatingSeekableInputStream for
correct direct, sliced and partial ByteBuffer behavior.

The tested schema is flat optional/required unannotated BOOLEAN/INT32/INT64/
FLOAT/DOUBLE, UTF8 text and unannotated binary WKB geometry. Preserve exact Java
numeric types, int64 beyond double precision, negative zero, Unicode and null
attributes/geometries. Decode every declared geometry column; retain primary
selection and declared CRS state without inventing EPSG or transforming values.

Reject nested/repeated/other logical types and codecs outside the current test
subset (uncompressed, Snappy and GZIP). Reject incomplete rows, invalid/trailing
WKB and corrupt checksum-bearing data pages, without returning partial success.
Bound file bytes, row count, per-row-group declarations and individual decoded
value sizes. These limits are an experimental workload envelope, NOT proof of
a hard bound on malicious decoder allocations, total native memory or process RSS.
A separate 384 MiB JVM heap and job deadline make failures visible in CI.

Rows are materialized for this small experiment; full streaming layer integration
and a dataset abstraction are still needed. Returned maps permit genuine nulls
and are read-only; JTS geometry object mutability is not a deep-immutability claim.
Cancellation is cooperative at local reads and scan boundaries, not an instant
kill inside a codec. Size checks are not a hostile same-size snapshot guarantee.
JTS validity and trailing-byte checks do not prove every malformed WKB is rejected
without any decoder repair; 3D/M, spherical edges and full PROJJSON semantics need
explicit production contracts. Row values and polygon areas in these tests are
not a claim of CRS-transformed or geodesic calculations.

## Independent data and sixteen tests

Keep the existing ten-file Arrow fixture set and its manifest unchanged. Extend
it with nine checksum-bearing typed/codec/malformed-page/WKB/nested fixtures;
Arrow reopens every genuine container and independently refuses the deliberately
corrupted page. Malformed WKB fixtures are valid Parquet containers, not fake
success files. All nineteen source hashes must survive the Java run unchanged.

Thirteen actual row tests cover points/coordinates, projected polygon areas and
int/null attributes, unknown/default CRS distinction, two geometry columns,
empty datasets, three codecs and exact numeric types, genuine metadata failures,
unsupported schemas/codecs, invalid WKB/pages, cancellation/row budget, repeated
read/rename/delete and a Java17 socket-denial diagnostic. The network diagnostic
is not a native-code security sandbox or a complete cold-start offline audit.
A final test proves the desktop Main is not present on the probe classpath.
Three I/O tests exercise direct/sliced buffers, independent positions/EOF and
cancelled-read handle closure. None is silently optional or skipped.

## Workflow, observed evidence and decision gate

New path-scoped PR workflow executes the probe on actual Java17 Linux, Windows,
Mac Intel and Apple Silicon. Record actual checkout/tree and runner architecture.
Resolve its isolated dependencies first; then execute the complete probe with
Gradle --offline. Never retry a failed test as though it were an infrastructure
failure. All sixteen tests, zero failures/errors/skips, nineteen unchanged hashes
and dependency inventory are required per platform.

At authoring time no new test PASS is claimed. Existing main/pull-request CI,
Certification and M1 independent interop remain required and unchanged. Prior-main
attestation from this research PR may certify d10ad.../#121 only; it cannot certify
unmerged row-reader code. Leave the probe PR draft/non-merged until its evidence
and dependency decision are reviewed. A successful probe is not an installed
feature and does not close #112 or A0.7.

Next after measured success: choose the production adapter (or reject this one),
resolve resource/CRS/schema and dependency-distribution obligations, then integrate
real geometries/attributes with CATGIS's existing layer abstractions and background
job contract. No feature advertisement or new menu before that full workflow.
