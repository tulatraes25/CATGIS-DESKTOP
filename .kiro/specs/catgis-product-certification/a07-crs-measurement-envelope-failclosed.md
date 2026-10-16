# SPEC/Kiro #159B - measurement and envelope CRS fail-closed

BASE: a2ff71e0060cf0145ffc7024532023ffeebecb3b
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed failures

After PR #160, UNKNOWN is preserved at the Layer model/data-mutation boundary, but measurement/display helpers still fail open:
- MapMeasurementUtils substitutes EPSG:4326 for blank and returns raw geometry on transform exception;
- VectorMeasurementSupport inherits project CRS for an UNKNOWN layer and falls back to raw length/area while callers label values m/m2;
- MapUtilities envelope reprojection returns the original BBOX on blank/failed transform;
- OnlineLayerRenderer treats blank CRS as identity and accepts partially transformed corner sets;
- MapPanel and DrawingTools measurement flows default missing project CRS to 4326;
- field calculator metric tokens can turn nonfinite metrics into numeric literals.

## Design

- UNKNOWN/invalid measurement CRS => unavailable: null for map geometry, NaN for low-level vector metrics.
- No layer-source CRS inheritance from project CRS.
- DrawingTools delegates to the same MapMeasurementUtils implementation as MapPanel.
- user measurement requires an explicit project CRS and reports a warning if absent.
- envelope identity is allowed only for two declared equal CRS; blank/invalid/failed transform => null.
- online BBOX requires all corners to transform to finite coordinates.
- field calculator metric tokens reject nonfinite measurement with an actionable CRS message instead of converting it to 0.

## Contracts

CrsMeasurementEnvelopeFailClosedContractTest verifies blank/invalid measurement rejection, valid EPSG:4326 metric result, UNKNOWN layer not inheriting EPSG:32719 project CRS, exact known UTM metric length, blank/invalid envelope rejection, declared identity copy and valid online 4326->3857 envelope.

Existing ReleaseFieldCalculatorTest keeps valid geographic metric behavior.

## Residual

This tranche does not close #159. Snap/edit source-coordinate conversion, every display geometry path, export/PostGIS source-CRS policy, WFS request CRS, topographic profile, raster UNKNOWN (#128), and interactive CRS assignment remain separate. Physical GUI messaging remains A0.7/F3.

Require exact-head CI/Certification/Runtime/GeoParquet plus predecessor-main attestation before merge, then fresh-main proof.
