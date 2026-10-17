# SPEC/Kiro P0 — FlatGeobuf direct-open CRS provenance

BASE_SHA: `2d2c1c608ef82e75da22524486d0f9d368a2ddce`
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Evidence and root cause

PR #187 certifies real FlatGeobuf header CRS parsing for the primary reader,
but `OpenFileAction.openSelectedFile` still imported a FGB file with
`sourceCRS=""` even when the decoded schema had EPSG:32719. The downstream
vector attach/projection path can then mistake source coordinates for project
coordinates. The dialog to load FGB did not block unreferenced imports.

## Requirements and design

- Set the layer's source CRS from the validated schema returned by the FGB
  loader, never from the project target CRS or a blanket EPSG:4326 fallback.
- If the FGB header is unknown, reject noninteractive/headless imports before
  adding the layer. Do not mutate live project state on this negative path.
- On an interactive import of a file lacking CRS, demand manual source CRS
  assignment or cancel. No implicit attach on cancel.
- Preserve existing bounded FGB validation, memory limits, typed attributes,
  and the previously certified header CRS decoding.
- Do not modify CATGIS user data, disk exports, releases or OpenCode.

## Acceptance and verification

`OpenFileFlatGeobufCrsContractTest` covers preservation of EPSG:32719,
explicit EPSG:4326, and unknown/null CRS without a silent fallback.
`FlatGeobufCrsSupportTest` covers actual FGB header CRS decoding.
Candidate requires exact-HEAD CI, Certification, Runtime Identity, GeoParquet,
and authenticated predecessor-main push attestation. After normal merge,
fresh push certification is mandatory.

Residual: full UI manual CRS selection/gesture, close/reopen and native
real-world file roundtrips remain F3/runtime acceptance; no A0.7 closure.
