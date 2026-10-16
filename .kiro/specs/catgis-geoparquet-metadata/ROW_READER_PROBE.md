# SPEC/Kiro #112 - executable local GeoParquet row-reader decision

BASE: d10ad77247e47755b7da35bf6460604a396e8188 (normal, post-main-verified merge #121).
STATUS: RESEARCH_IMPLEMENTED / CORRECTION_AWAITING_EXACT_HEAD_EVIDENCE / NOT_PRODUCT_CERTIFIED.
A0.7: OPEN. No user PC/OpenCode/GUI/release actions.

## Goal and isolation

M1 inspects real metadata; M2 keeps its existing open flow off EDT. Neither imports
rows as a usable CATGIS layer. This experiment must read genuine geometry and typed
attributes before an adapter or its dependencies are accepted in the product.

A standalone Gradle project under tools/qa/geoparquet-reader-probe compiles its
probe classes plus the ACTUAL unchanged GeoParquetReader. It does not include the
desktop Main, application settings/build, UI or project state. Existing production
source, dependencies, tests and workflows remain unchanged. Only an independent
path-scoped PR workflow and research/SPEC files are added.

Pinned Apache Parquet 1.17.1, Hadoop client API/runtime 3.4.3, Jackson 2.21.7 and
JTS 1.20.0 are comparison baselines, not latest-release/security/distribution
clearance claims. No cluster, external data service, native extension installation
or Python runtime is added to CATGIS. Python/Arrow/PROJ are independent test-only
fixture generators. Apache decodes Parquet pages/codecs and JTS decodes WKB; no
third-party source or new homemade binary parser is copied into the project.

## Failure-driven evidence and corrected input boundary

Initial workflow YAML, Arrow compliant-LIST fixture schema and Gradle java-name
shadowing defects were corrected without suppressing any row assertion. Failed
runs and exact candidate history remain in PR #122.

On bea96a88, row workflow 38828289797 / Linux job116740823091 executed16 tests:
2 failed. The custom local input path returned center X=0 instead of0.5 and
positive zero instead of the declared negative-zero bit pattern. Other tests
passing did not make this a usable reader.

Independent Arrow oracles were strengthened to compare exact point WKB and raw
float/double bits, because semantic equality alone treats signed zeros as equal.
The original fixture generator already writes point(0.5,0.5); its expected value
was correct and unchanged. All original16 Java test methods/expectations remain.

A seventeenth diagnostic compares scalar construction, direct WKB decoding, the
official Hadoop local adapter and the custom adapter under both default and probe
read options. Diagnostic values must be copied INSIDE the live page, rather than
returning a borrowed Group after its page was closed. The initial diagnostic's
lifetime mistake was corrected; it is not evidence of an upstream decoder defect.

Exact acaa3d908b9b1213e1b9f036dbf27d427cee1b9b / workflow38829278735 /
Windows job116744015659 full log:
- Arrow confirms original center WKB for(0.5,0.5) and negative-zero bits.
- Direct JTS and primitive Group scalar controls preserve both values.
- Official HadoopInputFile preserves both values BEFORE page close with BOTH
  option sets; byte-array and stream JTS decoding agree.
- The custom channel/stream adapter returns zeros BEFORE page close with BOTH
  option sets. All17 tests ran;3 failed, including8 assertions in the new control.

This isolates the failing boundary to that custom I/O path. It does NOT prove a
specific JDK channel bug or an upstream Parquet/JTS defect. The correction in
f4b651b41bf7cb3be93aed19f13e6c13cc9aaac3 replaces it with explicit delegation of
all read/readFully/ByteBuffer/seek/close methods to the already verified official
HadoopInputFile stream. LocalProbeInput retains only validation, workload limits
and cooperative cancellation. No expected value or test method was removed.
The unchanged seventeen tests must validate the corrected wrapper on every OS.

The wrapper checks regular/no-follow metadata and size before/after open, but the
official path open can follow a concurrently replaced path. These checks are NOT
a hostile TOCTOU or immutable-file snapshot guarantee. Closing always delegates,
even after cancellation. Filesystem-client caches and native resource behavior
need separate production investigation; this is not an application-wide sandbox.

## Explicit small-file contract

Supported workload: local regular files12bytes..4MiB, <=10000 rows, <=8MiB declared
row-group bytes, <=32MiB aggregate declared bytes, <=1MiB decoded binary/text value.
A384MiB test JVM heap and workflow timeout expose failures; they do not prove a
universal native-memory/RSS, malicious allocation or decompression bound.

Flat optional/required primitive BOOLEAN/INT32/INT64/FLOAT/DOUBLE, UTF8 strings
and unannotated binary WKB geometry are tested. Preserve exact numeric Java types,
int64 beyond2^53, signed zero, Unicode and nulls. Decode every geometry column,
retain primary selection and metadata CRS states; never invent SRID or transform
coordinates. Codecs tested: uncompressed, Snappy, GZIP. Nested/repeated/other
logical types/codecs are explicitly unsupported, not silently converted.

Reject malformed metadata, row-count mismatch, invalid/trailing WKB and corrupt
checksum-bearing pages without returning a partial successful scan. No semantic
claim about full3D/M, spherical edges or complete PROJJSON authority handling.
JTS validity is not proof that every malformed geometry is rejected without
possible decoder repair. Polygon-area checks are planar source-coordinate
oracles, not geodesic or transformed area claims.

Rows are materialized for the small experiment, NOT streaming into CATGIS layers.
Maps own null-preserving read-only containers; geometry deep immutability is not
claimed. Cancellation is cooperative at delegated reads and row boundaries, not
a forced interruption inside a native codec. Other formats are untouched.

## Independent fixtures and mandatory seventeen-test gate

Keep the original10 Arrow fixture files/manifest unchanged. Nine extra typed,
codec, malformed-WKB, nested and checksum-fault files produce19 source hashes.
Arrow independently reopens every positive container with metadata/data equality,
compares floating-point bits, confirms the literal center WKB and independently
rejects the deliberately corrupted checksum page. Malformed-WKB cases are real
Parquet containers, not fake valid geometries.

Original13 row tests cover actual coordinates/areas, typed/null attributes, CRS
state, multiple geometries, empty data, three codecs, malformed/unsupported input,
cancellation, file-handle closure, Java socket-denial and absent desktop Main.
Three actual I/O tests cover direct/sliced buffers, independent seeks/EOF and
cancelled-read closure. The added diagnostic is the seventeenth test. Java17's
socket-denial check is a diagnostic, not a native sandbox or full cold-start
offline proof. No skipped or optional probe tests are permitted.

The four-platform workflow checks actual Java17 Linux/Windows/Mac Intel/Mac ARM
and prints checkout SHA/tree/architecture. Resolve dependencies separately, then
execute clean tests with Gradle --offline. Never retry a failing test as an
infrastructure failure. Require exactly17 tests, zero failures/errors/skips,
19 unchanged AFTER-JAVA hashes, clean tracked files and dependency inventory on
each platform. Generating/hash-checking inputs BEFORE a failed test is not the
same evidence as completing the AFTER-JAVA integrity gate.

## Measured footprint and adoption decision

The failed bea96a88 Linux run successfully measured29 runtime JARs totaling
94,469,231bytes. This is the full isolated classpath, NOT incremental installer
growth: some dependencies already overlap the application. Hadoop-client API and
runtime are19,730,067 and30,269,075bytes; fastutil is23,730,770bytes. Complete JAR
coordinates/SHA256/notice-entry inventory is emitted by each successful probe.
Notice filenames are not completed license/security clearance. Heap-pool peak
sums are diagnostics, not simultaneous maximum heap usage or process RSS.

Even after a successful matrix, choose or reject this adapter based on measured
behavior, dependency footprint, distribution/security review and resource/CRS
requirements. Alternatives remain a verified existing GDAL Parquet driver or a
separately assessed GeoTools/DuckDB route. Do not install multiple redundant
engines or automatic extensions merely to make a feature appear supported.

## Certification boundary and next gate

Keep PR #122 draft and non-merged as a research experiment. Existing exact-head
application CI/Certification/M1 interop and predecessor-main attestation remain
unchanged. d10ad/#121 is independently post-main verified; its proof does NOT
certify unmerged probe source. Current corrected-source execution must be observed,
not inferred from the successful official-adapter diagnostic.

A successful probe does not close #112 or A0.7. Next after evidence/adoption:
produce the actual bounded/streaming adapter with precise schema/CRS contracts,
integrate geometries and attributes into existing CATGIS layer abstractions and
background jobs, test real layer/project workflows and then physical installed
GUI behavior. No new capability advertisement before that end-to-end acceptance.

Primary references reviewed: Apache parquet-java tag apache-parquet-1.17.1
(ParquetReadOptions, HadoopInputFile, PageReadStore, DelegatingSeekableInputStream,
LocalInputFile); https://geoparquet.org/releases/v1.1.0/ ;
https://gdal.org/en/stable/drivers/vector/parquet.html .
