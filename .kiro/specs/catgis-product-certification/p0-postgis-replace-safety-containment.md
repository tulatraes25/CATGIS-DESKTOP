# SPEC/Kiro #156-A — P0 containment for destructive PostGIS replace

BASE `228e30c60c330eedf8f918d3a4ffae9e660c953d` — DRAFT/NOT_CERTIFIED; #156 remains OPEN.

## Confirmed root cause
Before this candidate, PostgisWriteService.writeLayer(REPLACE_TABLE) called
GeoTools DataStore.removeSchema on the canonical table before creating a new
schema and only later began a feature-data transaction. A failure during new
schema, feature write or verification could irreversibly delete prior data.

## Containment requirements
- Fail closed for REPLACE_TABLE at the public service boundary BEFORE opening a
  database connection, irrespective of validity of the remaining request.
- Remove the destructive removeSchema branch entirely from the writer.
- Hide REPLACE_TABLE from the export dialog and enum visibility metadata.
- Keep CREATE_NEW, APPEND_RECORDS and transactional OVERWRITE_CONTENT enabled
  unchanged. Do not silently convert REPLACE_TABLE to OVERWRITE_CONTENT.
- Show an explicit readable reason and safe alternatives.
- Unit contract proves no DB connection on a REPLACE_TABLE request, even with
  deliberately invalid connection fields; existing isolated PostGIS real
  integration CREATE_NEW must continue to pass.

## Limitations
This is containment, NOT implementation of transactional replacement.
#156 remains P0 OPEN until staging-table write, schema/SRID validation,
PostgreSQL advisory lock, transactional rename/swap, rollback injections,
dependency safety, post-commit verification and CREATE_NEW cleanup are proven
against isolated CI PostgreSQL/PostGIS. OVERWRITE_CONTENT rollback tests also
remain needed. A0.7 stays OPEN and PC/OpenCode/release remains blocked.
