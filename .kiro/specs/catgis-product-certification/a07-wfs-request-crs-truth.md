# SPEC/Kiro #159C — WFS unspecified request CRS truth

BASE_SHA: f5c0223be7f3f9bb4a3b8100850253026f45f059
STATUS: REPORTED / CANDIDATE / NOT_CERTIFIED
Historical implementation: PR #169; ported selectively to current baseline.
A0.7: OPEN
Issue: #159 / #163

## Objective and root cause
OnlineWfsLayer.setRequestCrs used the generic CRSDefinitions.normalizeCode even
for blank input; that global helper deliberately defaults blank to EPSG:4326.
WfsFeatureLoader repeated the default before constructing GeoTools Query, making
UNSPECIFIED WFS request metadata appear as an explicit geographic reprojection.
Schema CRS resolution also normalized CRS.toSRS(null) to fabricated WGS84.

## Bounded requirements
- Preserve null/blank/whitespace WFS request as UNSPECIFIED (empty string).
- Build Query without coordinateSystemReproject for UNSPECIFIED.
- Explicit EPSG requests continue to use GeoTools decode and reject invalid codes.
- Derive layer source CRS from actual feature collection schema when request is
  unspecified, preserving UNKNOWN if schema offers no decodable authority.
- No credentials, network connections or UI changes in this tranche.

## Executable contracts
WfsRequestCrsTruthContractTest: blank/null model and Query policy; a genuine
GeoTools EPSG:32719 schema not relabelled 4326; schema UNKNOWN preserved;
explicit EPSG:32719 and EPSG:4326 behavior; invalid authority rejected.

## Residual and non-claims
A full live WFS GetFeature with server axis-order, request/schema mismatch,
mixed-version pagination and project serialization requires follow-up
independent fixture/integration coverage. Do not claim all #159/#163 closed.
Project reopen subtype payloads are tracked by #147/#146.
The global normalization fallback is unchanged outside explicit WFS request
boundaries. Candidate and post-main exact SHA gates are mandatory.

Consolidated and independently retested with #189/#191/#192 source and tests on current main f5c0223be7f3f9bb4a3b8100850253026f45f059; prior green runs on old SHA are not substituted for this candidate's native gates.
