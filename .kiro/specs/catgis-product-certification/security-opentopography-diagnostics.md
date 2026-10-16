# SPEC/Kiro #124 - OpenTopography credential-safe diagnostics

BASE: 11209e37a6bc95375dbe0033abbe7c7b276d95d5.
STATUS: CANDIDATE / NOT_CERTIFIED. A0.7 OPEN.

## Confirmed cause, not a claim of historical compromise

The service sends an API_Key query parameter legitimately, but previously logged
its entire authenticated URI, response Content-Type and remote error-body text.
The returned record also printed its sensitive URI by default. Callers can log
exception causes and suppressed close failures, so filtering only the message
would not close those paths. No actual user credential, log or service account
was accessed in this work.

## Requirements and minimal design

- Preserve the outgoing authenticated request, public method and record accessors.
  Document buildDownloadUri/requestUri as sensitive and replace record toString
  with a constant-safe representation. Do not print local file paths there.
- Log only fixed local event text plus numeric status/duration. Never include
  request query, remote headers, remote bodies or transport exception objects.
- Never read rejected HTTP/error-document bodies, even a bounded prefix: close
  the owned response stream. A remote server can echo a credential anywhere.
- Use stable local failure-phase codes. Strip untrusted exception messages,
  causes and suppressed close exceptions before exposing errors to callers.
  This deliberately trades remote stack detail for credential safety; it is not
  a generic application-wide redactor.
- Preserve cooperative interruption and distinguish HTTP/socket timeout from
  user cancellation. Close streams when response checks or reads fail. A failed
  body close cannot be reported as a successful download.
- Inject only the HTTP send boundary through a package-private functional
  interface. The normal path retains the existing JDK HttpClient configuration.
  No UI, runtime dependency, global logging config or build/workflow changes.

## Tests

Sixteen new ordinary JUnit methods use synthetic markers and injected in-process
HTTP responses, not any account or public-service request. Capture ACTUAL Log4j
messages through an in-memory WriterAppender with a positive canary. Core is
already runtimeOnly, so reflection is confined to the test capture helper rather
than adding a production/test build dependency. The isolated test restores the
logger level/appender and the caller's interrupt state after each relevant case.

Verify the outgoing encoded credential is preserved, yet absent from actual log
messages, complete exposed stack traces and record formatting. Include HTTP
401/403/503, successful-status text responses, unchecked/checked transport errors,
read plus suppressed close errors, close-only errors, header-access errors,
missing body, two timeout classes and cancellation before/after send and in read.
Rejected bodies assert ZERO reads and exactly one close. Before-copy failures
preserve existing output bytes and do not create sidecar files. The success case
uses an actual TIFF encoder, reopens its two sample cells and checks output bytes
and the existing sidecar. This is NOT embedded GeoTIFF scientific certification.

No older test methods, skips or dependencies are changed. Exact source/test blobs
were checked against the authored files. Full Java17/Log4j/GeoTools execution
must be established by GitHub CI; no local full-suite PASS is claimed.

## Explicit residual risks / separate work

This diagnostic fix does NOT make download publication atomic: successful-status
body copying can still partially replace a file, and raster plus sidecar are not
a transaction. TIFF/georeference validation, response byte/deadline budgets,
redirect policy, request BBOX validation and shared DecimalFormat thread safety
need separate tranches. The sensitive URI accessor is retained for compatibility;
a caller deliberately logging it remains out of scope. Third-party/native debug
output and the rest of CATGIS services are not globally audited by these tests.

No retrospective user-log deletion, credential rotation, user-PC/OpenCode change,
release action or desktop GUI validation is performed. Unknown past exposure is
not proof that a real credential was used or compromised.

## Certification

Require exact candidate CI4/4, Certification15/15, metadata interop and a successful
attestation of protected predecessor-main push evidence. Normal expected-head
merge only, then fresh actual-main CI/Certification/interop and authenticated
post-main attestation. Keep #124 open until this bounded automated scope is
closed with observed evidence; full A0.7 remains open independently.

Primary guidance: https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
(Data to exclude); Java17 HttpClient.send interruption contract at
https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpClient.html .
