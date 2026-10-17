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
It also builds real framed FlatGeobuf header files, checks bounded loader
acceptance, asserts the projected EPSG:32719 survives the full file-to-data
path, and asserts a headless import of CRS-free FGB fails closed.
`FlatGeobufCrsSupportTest` independently covers header CRS decoding.
Candidate requires exact-HEAD CI, Certification, Runtime Identity, GeoParquet,
and authenticated predecessor-main push attestation. After normal merge,
fresh push certification is mandatory.

Residual: full UI manual CRS selection/gesture, close/reopen and native
real-world file roundtrips remain F3/runtime acceptance; no A0.7 closure.


## Follow-up architecture finding: projected display CRS is not file-source CRS

Source review confirmed that both `OpenFileAction.addVectorLayer` and
`LayerVectorDataSupport.projectAndAttach` call
`TopographyWorkflowSupport.projectVectorDataToCurrentProject`, which
sets the mutable in-memory layer `sourceCRS` to the *project target CRS*
after reprojection. `SaveProjectAction.buildLayerLine` previously wrote
that projected CRS into the persisted file-source CRS field. On reopen,
`LayerVectorDataSupport` correctly compares the saved CRS with the FGB
header, thus rejecting valid EPSG:32719 files in an EPSG:4326 project.
Unconditionally relaxing the header validation would instead hide tampered
source metadata and destroy the fail-closed guarantee.

Minimal isolated fix:
- `FlatGeobufSourceCrsProvenance` records the *original* FGB file CRS
  before projection in the layer's existing transient user metadata.
- Rendering/projected layer `sourceCRS` retains the established
  in-memory-target semantics, avoiding double transformations.
- `SaveProjectAction` uses original FGB source CRS for the existing
  project-file field. Other vector/raster files preserve original behavior.
- During project reopen, capture the restored original CRS again before
  reprojecting so repeated save/reopen cycles are idempotent.
- No header I/O on the UI thread is added to save. No project file format
  migration or serialized extra field is introduced.
- `FlatGeobufProjectCrsRoundTripContractTest` exercises the actual
  serializer and parser with projected EPSG:32719/4326, repeated
  save/reopen, manual source CRS for headerless FGB, and non-FGB control.

Residual: legacy projects previously persisted with an erroneous target
sourceCRS can still fail closed; a separate explicit reconciliation UI/gate
is required instead of silently overriding historical user assignments.


## WKT-only CRS acceptance without invalid fallback

Another verified source-code defect was in
`FlatGeobufCrsSupport.requireConsistentWithAssignedCrs`: the saved
FlatGeobuf `sourceCRS` can legitimately contain a WKT-only definition,
but `CRSDefinitions.decode(...)` only supports authority identifiers.
The consistency gate now uses `VectorCrsTransformSupport.resolve` for
both an explicit authority and a WKT definition. This still rejects a
semantically contradictory assignment; it does not use a WGS84 fallback.
`FlatGeobufCrsSupportTest.wktOnlyHeaderRemainsReopenableAndContradictionsFailClosed`
is the positive/negative executable contract.
