# SPEC/Kiro #159 - fail-closed vector CRS reprojection

BASE: 5ce112493427257093391cbb101423531dbe9f04
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Confirmed corruption

TopographyWorkflowSupport used a bounded manual transformer. If source and project CRS differed but the manual helper returned null, CATGIS copied source coordinates unchanged into a target-CRS schema and then relabeled the layer as the project CRS.

CRSDefinitions.normalizeCode defaults blank input to EPSG:4326. Layer.setSourceCRS previously delegated blank/null directly to that helper, so the model itself converted UNKNOWN into WGS84 before any workflow could inspect it. This caused both blank CAD and schema-fallback vector paths to fabricate source CRS.

## Design

Layer.setSourceCRS now preserves null/blank as UNKNOWN and normalizes only explicit CRS text. Data-mutating vector projection resolves source CRS from explicit layer metadata first and the feature schema second. Blank source or target is terminal. VectorCrsTransformSupport resolves authority codes through GeoTools and WKT through CRS.parseWKT, transforms with CRS.findMathTransform/JTS, rejects empty/nonfinite results, and never invents an authority code.

TopographyWorkflowSupport mutates layer.sourceCRS only after every feature has transformed successfully. No copy-original fallback exists for source != target. Target schemas use the same strict CRS resolver.

MapPanel display fallback is explicitly outside this tranche and remains a separate degraded-visualization concern.

## Contracts

- arbitrary EPSG:3035 -> EPSG:4326 matches direct GeoTools transform numerically;
- WKT source is parsed and transformed;
- model boundary preserves null/blank source CRS as UNKNOWN until explicit assignment;\n- blank layer CRS uses real schema EPSG:3035 rather than defaulting to 4326;
- invalid authority fails and leaves layer CRS and source geometry unchanged.

Require exact-head CI/Certification/RuntimeIdentity/GeoParquet and authenticated predecessor-main evidence, expected-head merge, then fresh-main proof.
