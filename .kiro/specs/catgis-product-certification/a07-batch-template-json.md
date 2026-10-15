# SPEC/Kiro #120 - exact reusable batch-template parameters

BASE: 3d60b9fc394015fc678ffbfcdaf0263ffbc01349 (normal merge of #119).
STATUS: IMPLEMENTED_CANDIDATE / NOT_CERTIFIED. A0.7 remains OPEN.
Related: product #109 and batch safety #114; no new modeler is claimed.

## Objective and reproduced root cause

A repeatable professional process must not change its parameters while saving
and reopening its template. BatchTemplate manually parsed JSON by substring,
first-brace scanning and chained replacements. This was a real existing codec,
not a missing feature justified only by competitor comparisons.

The exact full BatchProcessor source was reconstructed from an exact-ref GitHub
connector read and its Git blob hash verified as
672f07b380a75e486173970511ca80f32c119422. The unchanged nested record was then
compiled in isolation with javac --release 17 on JVM21. Four counterexamples
executed: Windows backslash-n/backslash-r path corruption, compact true boolean
becoming false, a closing brace inside a value losing settings, and non-JSON
text being accepted as an empty successful template. This is supplemental
execution of the old codec, not a full CATGIS or Java17/multiOS run.

## Minimal design

Use the existing Jackson dependency, not another homemade JSON parser or a new
library. BatchTemplateCodec handles only encode/decode and explicit validation.
It has no file, project, Swing, network, algorithm execution or global-context
responsibility. Keep BatchTemplate's record/accessors and public toJson/fromJson
signatures. Both delegate to the one codec. All source after the nested record,
including batch output planning, raster operations and template storage, remains
byte-for-byte unchanged. Candidate existing-file blob:
4c231ffb567af7f7b3fdb4e8800acce1094e9c05.

Use a fixed Jackson tree schema, not polymorphic deserialization. Names,
operations, parameters and paths are text, with no automatic type coercion or
normalization. Encoding uses JSON escaping for all controls, quotes, slashes and
Unicode. Field ordering/whitespace do not influence boolean meaning. Duplicate
JSON keys, including escaped spellings, and trailing root values are errors.

## Compatibility and deliberate rejection

- Read every ordinary old six-field template emitted by the previous writer,
  preserving values rather than exact pretty-print whitespace.
- name and operation must be present JSON strings; empty strings remain legal
  for codec compatibility, not proof of a valid executable operation.
- Missing/null description and extensionFilter default to empty text. Missing
  includeSubdirs defaults false; a present value must be a JSON boolean.
- Missing/null settings defaults to a new empty map. All explicit setting
  values must be JSON strings. Programmatic null string values still serialize
  as empty text as before; do not change the caller's map in place.
- Null programmatic parameter keys now fail instead of becoming ambiguous empty
  keys. Ordinary empty string keys remain codec-compatible; parameter semantic
  validation is the algorithm's responsibility.
- Reject malformed/truncated/non-object JSON, wrong field types and unknown
  root fields rather than silently invent/discard recipe parameters.
- Keep the existing record's mutable-map contract in this tranche. Encoding
  takes a map snapshot and decoding owns a fresh map; no guarantee is made
  for a caller mutating a non-thread-safe map during the snapshot itself.

## Bounds and error behavior

At most 256 Ki UTF-16 characters in input/output JSON, 16 Ki characters per text
value, 1024 per setting key, 256 settings, nesting depth 8 and number token
length 32. The writer checks text totals and escaped serialized size. The
reader checks input length before parsing. These are declared resource limits,
not a measured universal heap/time guarantee.

fromJson keeps its unchecked signature, raising IllegalArgumentException with
an actionable message and the original Jackson cause where applicable. Invalid
input never produces a partial BatchTemplate. The existing loadAllTemplates
catch/log behavior is unchanged and skips a rejected file rather than using an
invented recipe; user-facing per-template diagnostics are separate work.

## Fifteen new ordinary JUnit contracts

BatchTemplateCodecContractTest covers exact Windows/UNC/escape-looking paths;
braces/quotes/Unicode keys and values; whitespace/order/text-independent
booleans; independently written Unicode escapes; all JSON control characters;
an unchanged legacy fixture; optional defaults; malformed/missing fields;
duplicate keys/trailing roots; wrong types/unknown fields; accepted and rejected
resource boundaries; oversized escaped output/null-key refusal; map ownership;
a genuine UTF-8 file roundtrip with repeated unchanged-byte checks; and concurrent
independent recipes. No old method, expectation, skip budget or workflow changes.
The old BatchProcessorTest (including template save/load) remains intact.

The original four defects are reproduced, but the NEW tests are not declared
PASS until the exact candidate's GitHub suite executes. No local dependency
substitutes or mock Jackson decoder are used to fabricate execution evidence.

## Mandatory certification and residuals

Require candidate CI 4/4, Certification 15/15, independent GeoParquet interop
and an authenticated prior-main push attestation. Normal expected-head merge
only after all pass; fresh actual-main event=push CI/Certification/Interop and
independent post-main attestation afterward. The previous #119 candidate being
green does not certify its new main or this change.

This codec does not repair template filename collisions, atomic save/replace,
symlink/hard-link storage policy, file leases, unbounded Files.readString in the
old storage method, UI scheduling, large-library indexing, raster numeric
precision or full batch transactions. It does not add an action registry,
persistent execution history or a graphical modeler. Full GeoParquet ingestion
remains #112; this unrelated correctness bug does not stand in for that work.
No new dependency, copied third-party source, binary fixture, user-PC/GUI,
OpenCode or release change. Physical GUI and A0.7 remain NOT_VERIFIED/OPEN.
