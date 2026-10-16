# SPEC/Kiro #151A - truthful source CRS for FlatGeobuf and LAS/LAZ

BASE: 48ef1490ae87ab356a0210e6efa238eae31ab8be
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed failures

FlatGeobuf data loaded without a source CRS and the generic attachment path treated blank CRS as the project CRS. Project reload also explicitly wrote EPSG:4326. LAS/LAZ reload explicitly wrote EPSG:4326 despite LasReader not parsing CRS VLR/EVLR metadata.

## Candidate

- Parse FlatGeobuf header CRS from the actual generated Header/Crs object.
- Resolve authority+numeric/string code first; missing organization defaults to EPSG per FlatGeobuf spec. WKT is a fallback. Invalid/unresolvable metadata stays UNKNOWN.
- Build the FGB SimpleFeatureType with the resolved CRS and derive layer sourceCRS from the schema.
- Direct interactive FGB import with unknown CRS requires an explicit CRS selection before adding/projecting the layer. Noninteractive/headless import fails closed.
- Project reload of FGB with no saved and no header CRS throws an actionable error rather than treating coordinates as project CRS.
- LAS/LAZ with no saved source CRS throws an actionable error rather than assigning 4326. Existing explicitly assigned CRS remains usable.

## Tests

FlatGeobufCrsSupportTest proves:
- FlatGeobuf default organization semantics: code 3857 + no org => EPSG:3857;
- absent metadata remains blank/UNKNOWN, never 4326;
- a real generated FlatBuffer Header/Crs with EPSG:32719 is read as EPSG:32719;
- a schema without CRS returns blank rather than a global default.

Existing FGB real tests remain conditional on their GDAL fixture environment. Full independent geographic/projected/CRS-absent file fixtures remain required before #151 closes.

## Residual

LAS CRS VLR/EVLR parsing is NOT implemented in this tranche. Unknown LAS is blocked from false CRS use until a dedicated parser/explicit import assignment is available. This tranche does not alter GPX/KML/GeoJSON format-specific CRS semantics.

Require exact-head CI/Certification/Runtime Identity/GeoParquet and authenticated predecessor-main evidence, normal merge with expected head, then fresh-main proof.
